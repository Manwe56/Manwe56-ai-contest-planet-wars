package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import time.StateInfo;
import distance.Distance;
import game.AvailableShipsEvaluator;
import game.Fleet;
import game.FleetOrder;
import game.Game;
import game.LostPlanetAvailableShipsEvaluator;
import game.Planet;
import game.PossibleAttack;
import game.SafeAvailableShipsEvaluator;
import game.TargetEvaluator;
import game.TargetEvaluatorFactory;
import global.GlobalData;

public abstract class BaseBot implements Bot {

	protected Game game = null;
	
	private List<Planet> getPlanetEnemyTargets(Planet planet){
		List<Planet> potentialTargets = new ArrayList<Planet>();
		int firstEnemyPlanetDistance = game.distanceToFirstEnemyPlanet(planet);
		
		for (Planet enemyPlanet : game.getOtherPlanets()){
			if (game.getFuturOwner(enemyPlanet)==GlobalData.ENEMY
					&& game.distance(planet, enemyPlanet)<=firstEnemyPlanetDistance){
				potentialTargets.add(enemyPlanet);
			}
		}
		
		return potentialTargets;
	}
	
	private List<Planet> getEnemyTargets(){
		List<Planet> potentialTargets = new ArrayList<Planet>();
		
		for (Planet planet : game.getMyPlanets()){
			if (game.isFront(planet)){
				List<Planet> planetPotentialTargets = getPlanetEnemyTargets(planet);
				for (Planet enemyPlanet : planetPotentialTargets){
					if (!potentialTargets.contains(enemyPlanet)){
						potentialTargets.add(enemyPlanet);
					}
				}
			}
		}
		
		return potentialTargets;
	}

	private int getSourcesAdditionalContributions(Planet targetPlanet,Map<Planet, Fleet> alreadyPlanedAttacks, int currentDistance, AvailableShipsEvaluator evaluator, Map<Planet, Integer> reservedShipsForPostponedAttack){
		int additionalShipsSentNextTurns = 0;
		
		for (Planet source : alreadyPlanedAttacks.keySet()){
			int timeDeltaAvailable = currentDistance-game.distance(targetPlanet, source);
			
			if (timeDeltaAvailable>0){
				StateInfo futurAvailableShips = evaluator.getFuturAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack, timeDeltaAvailable);
				//already sent ships are taken into account since the fleet has not been yet canceled
				StateInfo availableShips = evaluator.getAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack);
				
				additionalShipsSentNextTurns+=futurAvailableShips.getShips()-availableShips.getShips();
			}
		}
		
		return additionalShipsSentNextTurns;
	}
	
	private List<PossibleAttack> evaluateAttackPossibility(Planet targetPlanet, List<Planet> sources, Map<Planet, Integer> reservedShipsForPostponedAttack, Map<Planet, StateInfo> reservedSupport, AvailableShipsEvaluator availableShipsEvaluator){
		List<PossibleAttack> attacks = new ArrayList<PossibleAttack>();
		int sentShips = 0;
		int alreadySentShips = 0;
		int currentRequiredShips = 1;
		Map<Planet, Fleet> fleetToCancel = new HashMap<Planet, Fleet>();
		TargetEvaluator evaluator = TargetEvaluatorFactory.createTargetEvaluator(targetPlanet, game.getFuturOwner(targetPlanet));
		
		if (game.getFuturOwner(targetPlanet)!=GlobalData.PLAYER){
			List<Distance> distances = game.getDistances(targetPlanet);
			int index = 0;
			
			for (index=0; index<distances.size(); index++){
				Distance distance = distances.get(index);
				Planet planet = game.getPlanet(distance.getDestination());
				
				if (planet.Owner() == GlobalData.PLAYER && sources.contains(planet)){
					
					int planetAvailableShips = availableShipsEvaluator.getAvailableShips(planet, targetPlanet, reservedShipsForPostponedAttack).getShips();
					if (reservedShipsForPostponedAttack.containsKey(planet)){
						planetAvailableShips-=reservedShipsForPostponedAttack.get(planet);
					}
					
					currentRequiredShips = game.conquestRequiredShips(planet, targetPlanet);
					StateInfo supportShips = game.supportRequiredShips(planet, targetPlanet, currentRequiredShips);
					sentShips=0;
					
					if (game.distanceToFirstAlliedPlanet(targetPlanet)>=game.distanceToFirstEnemyPlanet(targetPlanet)){
						currentRequiredShips+=supportShips.getShips();
						supportShips = null;
					}
					
					if (planetAvailableShips>0 && currentRequiredShips>0){
						PossibleAttack attack = new PossibleAttack();
						FleetOrder order = null;
						
						//Add all previous already taken into account fleet moves
						for (Planet source : fleetToCancel.keySet()){
							Fleet fleetToAdd = fleetToCancel.get(source);
							
							FleetOrder previousOrder = new FleetOrder(game.getPlanet(fleetToAdd.SourcePlanet()), targetPlanet, fleetToAdd.NumShips());
							
							attack.addFleetOrder(previousOrder);
						}
						attack.setSupport(supportShips);
						if (planetAvailableShips>=currentRequiredShips){
							order = new FleetOrder(planet, targetPlanet, currentRequiredShips);
							index = distances.size();//we have enough ships, break the loop
						}
						else{
							order = new FleetOrder(planet, targetPlanet, planetAvailableShips);
							attack.setPostponed();
						}
						attack.addFleetOrder(order);
						fleetToCancel.put(planet, game.sendMission(planet, targetPlanet, order.getShips()));
						sentShips=order.getShips();
						alreadySentShips+=sentShips;
						attacks.add(attack);
						
						if (thisSolutionBreaksSupportOfOtherAttacks(targetPlanet, reservedSupport, supportShips)){
							attacks.remove(attack);
						}
						else if (thisSolutionPutOnePlanetInDanger(targetPlanet, reservedShipsForPostponedAttack, availableShipsEvaluator, fleetToCancel)){
							tryToRebalanceAttacksOnPlanetNotInDanger( targetPlanet, reservedShipsForPostponedAttack, availableShipsEvaluator, attacks, fleetToCancel, attack);
						}
						
						if (attacks.contains(attack)){
							game.evaluateShipsLostAgainstNeutral(attack);
							
							if (attack.isPostponed()){
								tryToValidateAPossibilityInAFewTurn(targetPlanet, reservedShipsForPostponedAttack, availableShipsEvaluator, attacks, alreadySentShips, currentRequiredShips, fleetToCancel, evaluator, attack);
							}
							else{
								evaluator.attackPossibilityTurnsReturnOnInvestment(attack, 0);
							}
						}
					}
				}
			}
		}
		
		for (Planet source : fleetToCancel.keySet()){
			game.cancelMission(fleetToCancel.get(source));
		}
		return attacks;
	}

	private void tryToValidateAPossibilityInAFewTurn(Planet targetPlanet,
			Map<Planet, Integer> reservedShipsForPostponedAttack,
			AvailableShipsEvaluator availableShipsEvaluator,
			List<PossibleAttack> attacks, int alreadySentShips,
			int currentRequiredShips, Map<Planet, Fleet> fleetToCancel,
			TargetEvaluator evaluator, PossibleAttack attack) {
		boolean solutionFound = false;
		//Perhaps in a few turns...
		//it might not be really useful to wait more than a few turns
		for (int additionalTurn=1; additionalTurn<5 && !solutionFound; additionalTurn++){
			int additionalContributionsFromOtherSources = getSourcesAdditionalContributions(targetPlanet, fleetToCancel, additionalTurn, availableShipsEvaluator, reservedShipsForPostponedAttack);
			
			if (additionalContributionsFromOtherSources+alreadySentShips>=currentRequiredShips){
				solutionFound = true;
				evaluator.attackPossibilityTurnsReturnOnInvestment(attack, additionalTurn);
			}
		}
		
		if (!solutionFound){
			attacks.remove(attack);//We don't have enough available ships to conquer this planet yet
		}
	}

	private void tryToRebalanceAttacksOnPlanetNotInDanger(Planet targetPlanet,
			Map<Planet, Integer> reservedShipsForPostponedAttack,
			AvailableShipsEvaluator availableShipsEvaluator,
			List<PossibleAttack> attacks, Map<Planet, Fleet> fleetToCancel,
			PossibleAttack attack) {
		int totalAvailable = 0;
		for (Planet source : fleetToCancel.keySet()){
			int planetAvailable = availableShipsEvaluator.getAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack).getShips(); 
			totalAvailable+=planetAvailable;
		}
		// TODO change previous actions, so might be isolated and cancel its operations if fails
		if (totalAvailable>=0){
			List<Planet> attackSources = new ArrayList<Planet>();
			List<Planet> sourcesInDanger = new ArrayList<Planet>();
			List<Planet> sourcesNotInDanger = new ArrayList<Planet>();
			attackSources.addAll(fleetToCancel.keySet());
			//change orders to use available ships
			int missingShips = 0;
			//Cancel all the orders and store in danger planets
			for (Planet source : attackSources){
				int availableShips = availableShipsEvaluator.getAvailableShips(source,  targetPlanet, reservedShipsForPostponedAttack).getShips();
				if (availableShips<0){
					sourcesInDanger.add(source);
				}
				else{
					sourcesNotInDanger.add(source);
				}
			}
			for (Planet source : attackSources){
				Fleet toCancel = fleetToCancel.get(source);
				missingShips+=toCancel.NumShips();
				game.cancelMission(toCancel);
				fleetToCancel.remove(source);
			}
			//Reissue orders with maximum ships over not in danger planets
			for (Planet source : sourcesNotInDanger){
				int availableShips = availableShipsEvaluator.getAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack).getShips();
				if (availableShips>0){
					int shipsToSend = 0;
					if (availableShips>=missingShips){
						shipsToSend=missingShips;
					}
					else{
						shipsToSend=availableShips;
					}
					fleetToCancel.put(source, game.sendMission(source, targetPlanet, shipsToSend));
					missingShips-=shipsToSend;
				}
			}
			//Reissue orders on in danger planets
			for (Planet source : sourcesInDanger){
				int availableShips = availableShipsEvaluator.getAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack).getShips();
				if (availableShips>0){
					int shipsToSend = 0;
					if (availableShips>=missingShips){
						shipsToSend=missingShips;
					}
					else{
						shipsToSend=availableShips;
					}
					fleetToCancel.put(source, game.sendMission(source, targetPlanet, shipsToSend));
					missingShips-=shipsToSend;
				}
			}
			//Ensure our trys to make a valid move did not break any equilibrium
			if (missingShips>0){
				attack.setPostponed();
			}
			for (Planet source : attackSources){
				int availableShips = availableShipsEvaluator.getAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack).getShips();
				if (availableShips<0){
					attack.setPostponed();
				}
			}
		}
		else{
			// TODO postponed instead of removed?
			attacks.remove(attack);
		}
	}

	private boolean thisSolutionPutOnePlanetInDanger(Planet targetPlanet,
			Map<Planet, Integer> reservedShipsForPostponedAttack,
			AvailableShipsEvaluator availableShipsEvaluator,
			Map<Planet, Fleet> fleetToCancel) {
		//Ensure we are not moving ships that were protecting one of the source planets
		boolean onePlanetInDanger = false;
		for (Planet source : fleetToCancel.keySet()){
			int planetAvailable = availableShipsEvaluator.getAvailableShips(source, targetPlanet, reservedShipsForPostponedAttack).getShips(); 
			if (planetAvailable<0){
				onePlanetInDanger = true;
			}
		}
		return onePlanetInDanger;
	}

	private boolean thisSolutionBreaksSupportOfOtherAttacks(
			Planet targetPlanet, Map<Planet, StateInfo> reservedSupport,
			StateInfo supportShips) {
		return !game.supportValidated(reservedSupport) || !game.planetSupportValidated(targetPlanet, supportShips);
	}
	
	private List<PossibleAttack> evaluatePossibilities(List<Planet> possibleTargets, List<Planet> sources, AvailableShipsEvaluator shipsAvailableEvaluator, Map<Planet, Integer> planetsWithPostponedAttack, Map<Planet, StateInfo> reservedSupport){
		List<PossibleAttack> attackPossibilities = new ArrayList<PossibleAttack>();
		for (Planet targetPlanet : possibleTargets){
			attackPossibilities.addAll(evaluateAttackPossibility(targetPlanet, sources, planetsWithPostponedAttack, reservedSupport, shipsAvailableEvaluator));
		}
		Collections.sort(attackPossibilities);
		return attackPossibilities;
	}
	
	private void attackPositions(List<Planet> possibleTargets, List<Planet> sources, AvailableShipsEvaluator shipsAvailableEvaluator){
		Map<Planet, Integer> reservedShipsForPostponedAttack = new HashMap<Planet, Integer>();
		Map<Planet, StateInfo> reservedSupport = new HashMap<Planet, StateInfo>();
		boolean uselessAttackFound = false;
		
		List<PossibleAttack> attackPossibilities = evaluatePossibilities(possibleTargets, sources, shipsAvailableEvaluator, reservedShipsForPostponedAttack, reservedSupport);
		
		// TODO optim
		/* it is certainly useless to reevaluate all the possibilities
		 * */
		while (attackPossibilities.size()>0 && !uselessAttackFound)
		{
			PossibleAttack bestPossibility = attackPossibilities.get(0);
			//TODO consider turn of rentability instead of "rating"
			if (bestPossibility.getRating()>GlobalData.TURN_COUNT-GlobalData.getSingleton().getCurrentTurn()){
				uselessAttackFound = true;
			}
			else
			{
				if (GlobalData.SYNCHRONIZED_ATTACKS){
					bestPossibility.executeSynchronized(game, reservedShipsForPostponedAttack);
				}
				else{
					bestPossibility.execute(game, reservedShipsForPostponedAttack);
				}
				if (bestPossibility.getSupport()!=null){
					reservedSupport.put(bestPossibility.getDestination(), bestPossibility.getSupport());
				}
			}
			possibleTargets.remove(bestPossibility.getDestination());
			attackPossibilities = evaluatePossibilities(possibleTargets, sources, shipsAvailableEvaluator, reservedShipsForPostponedAttack, reservedSupport);
		}
	}
	
	protected void attackAllPositions(){
		List<Planet> targets = new ArrayList<Planet>();
		
		targets.addAll(game.getNeutralPlanets());
		targets.addAll(getEnemyTargets());
		targets.addAll(game.getMyPlanets());
		
		attackPositions(targets, game.getMyPlanets(), new SafeAvailableShipsEvaluator(game));
	}
	
	protected void attackEnemyPositionsWithLostPlanetsShips(){
		List<Planet> lostPlanets = new ArrayList<Planet>();
		
		for (Planet myPlanet : game.getMyPlanets()){
			if (game.getFuturOwner(myPlanet)!=GlobalData.PLAYER){
				lostPlanets.add(myPlanet);
			}
		}
		attackPositions(game.getEnemyPlanets(), lostPlanets, new LostPlanetAvailableShipsEvaluator(game));
	}
	
	protected void moveAvailableShipsToFrontPlanets(){
		
		for (Planet myPlanet : game.getMyPlanets()){
			if (!game.isFuturFront(myPlanet)){
				
				int availableShips = game.getAvailableShipsWithoutLoosingPlanet(myPlanet);
				
				if (availableShips>0){
					//Seek nearest front planet
					int index = 0;
					boolean reinforcementSent = false;
					List<Distance> distances = game.getDistances(myPlanet);
					
					for (index=0; index<distances.size() && !reinforcementSent; index++){
						Distance distance = distances.get(index);
						Planet frontPlanet = game.getPlanet(distance.getDestination());
						
						if (game.isFuturFront(frontPlanet)){
							game.sendMission(myPlanet, frontPlanet, availableShips);
							reinforcementSent = true;
						}
					}
				}
			}
		}
	}
	
	@Override
	public abstract void playTurn(Game game);
}
