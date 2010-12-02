package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import time.StateInfo;

import distance.Distance;
import game.Fleet;
import game.Game;
import game.Planet;
import game.PlanetInDanger;
import global.GlobalData;

public class ExpansiveBot extends BaseBot {
	
	private boolean isPlanetNearestThanAlreadyContributing(Planet planet, Planet source, List<Planet> planetsAlreadyContributing){
		boolean isPlanetNearestThanAlreadyContributing = false;
		int distanceToSource = game.distance(planet, source);
		
		for (Planet alreadyContributingPlanet : planetsAlreadyContributing){
			if (distanceToSource<game.distance(source, alreadyContributingPlanet)){
				isPlanetNearestThanAlreadyContributing = true;
			}
		}
		
		return isPlanetNearestThanAlreadyContributing;
	}
	
	private boolean sendCoverReinforcement(Planet planet, Planet sourcePlanet, StateInfo missingShips, List<Planet> planetsAlreadyContributing, List<Fleet> otherOrders){
		int index = 0;
		List<Distance> distances = game.getDistances(planet);
		List<Fleet> localOrders = new ArrayList<Fleet>();
		boolean succeed = false;
		
		planetsAlreadyContributing.add(planet);//avoid useless ping pong and stack overflow
		
		for (index=0; index<distances.size(); index++){
			Distance distance = distances.get(index);
			Planet nextNearestPlanet = game.getPlanet(distance.getDestination());
			
			if (!planetsAlreadyContributing.contains(nextNearestPlanet) && nextNearestPlanet.Owner()==GlobalData.PLAYER && game.getFuturOwner(nextNearestPlanet)==GlobalData.PLAYER && distance.getDistance()<=missingShips.getTurn() && !isPlanetNearestThanAlreadyContributing(planet, sourcePlanet, planetsAlreadyContributing)){
				StateInfo availableShips = game.getAvailableShips(nextNearestPlanet, null, GlobalData.PLAYER, 1);
				
				if (availableShips.getShips()>0){
					
					if (missingShips.getShips()<0)
					{
						if (availableShips.getShips()>-1*missingShips.getShips()){
							localOrders.add(game.sendMission(nextNearestPlanet, planet, -1*missingShips.getShips()));
							missingShips.contribute(new StateInfo(GlobalData.PLAYER, -1*missingShips.getShips(), distance.getDistance()));
						}
						else{
							//We try here to chain covering. So basically, uncover this planet and try to cover it with other planets
							int maxAvailableShips = game.getAvailableShipsWithoutLoosingPlanet(nextNearestPlanet);
							
							if (maxAvailableShips>0){
								Fleet reinforcement = null;
								int shipsToSend = 0;
								List<Fleet> nextNearestPlanetOrders = new ArrayList<Fleet>();
								if (maxAvailableShips>-1*missingShips.getShips()){
									
									shipsToSend = -1*missingShips.getShips();
								}
								else{
									shipsToSend = maxAvailableShips;
								}
								reinforcement = game.sendMission(nextNearestPlanet, planet, shipsToSend);
								
								StateInfo nextNearestPlanetMissingShips = game.getAvailableShips(nextNearestPlanet, null, GlobalData.PLAYER, 1);
								
								if (sendCoverReinforcement(nextNearestPlanet, sourcePlanet, nextNearestPlanetMissingShips, planetsAlreadyContributing, nextNearestPlanetOrders)){
									missingShips.contribute(new StateInfo(GlobalData.PLAYER, shipsToSend, distance.getDistance()));
									localOrders.addAll(nextNearestPlanetOrders);
									localOrders.add(reinforcement);
								}
								else{
									game.cancelMission(reinforcement);
									for (Fleet fleet : nextNearestPlanetOrders){
										game.cancelMission(fleet);
									}
								}
							}
						}
					}
				}
			}
		}
		if (missingShips.getShips()<0){
			for (Fleet fleet : localOrders){
				game.cancelMission(fleet);
			}
			succeed = false;
		}
		else{
			otherOrders.addAll(localOrders);
			succeed = true;
		}
		
		planetsAlreadyContributing.remove(planet);
		
		return succeed;
	}

	private void coverPlanetInDanger() {
		List<PlanetInDanger> list = new ArrayList<PlanetInDanger>();
		List<Planet> enemyPlanetsWeTryToCapture = new ArrayList<Planet>();
		
		for (Planet planet : game.getPlanets()){
			if (planet.Owner()==GlobalData.ENEMY && game.getFuturOwner(planet)==GlobalData.PLAYER){
				enemyPlanetsWeTryToCapture.add(planet);
				game.removePlanetContributionToDelta(planet);
			}
		}
		for (Planet planet : game.getPlanets()){
			if (planet.Owner()==GlobalData.PLAYER || game.getFuturOwner(planet)==GlobalData.PLAYER){
				StateInfo availableShips = game.getAvailableShips(planet, null, GlobalData.PLAYER, 1);
				
				if (availableShips.getShips()<0){
					PlanetInDanger planetToDefend = new PlanetInDanger(planet, availableShips);
					list.add(planetToDefend);
				}
			}
		}
		
		// TODO what should we defend first? planet in most difficulty or planet in less difficulty?
		Collections.sort(list);
		
		for (PlanetInDanger planetToDefend : list){
			sendCoverReinforcement(planetToDefend.getPlanet(), planetToDefend.getPlanet(), planetToDefend.getInfo(), new ArrayList<Planet>(), new ArrayList<Fleet>());
		}
		game.resetSimulation();
	}
	
	@Override
	public void playTurn(Game game) {
		this.game = game;
		game.calculateFrontPlanets(true);
		coverPlanetInDanger();
		attackAllPositions();
		game.calculateFrontPlanets(true);
		moveAvailableShipsToFrontPlanets();
		attackEnemyPositionsWithLostPlanetsShips();
		game.executeMissions();
	}
}
