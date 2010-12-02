package game;

import global.GlobalData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import time.Simulation;
import time.StateInfo;

import distance.Distance;
import distance.DistanceUtilities;


public class Game {
	private PlanetWars pw;
	private MissionManager manager;
	private GlobalData globalData;
	private List<Planet> neutralPlanets;
	private List<Fleet> myFleets;
	private Set<Integer> frontPlanets;
	private Set<Integer> futurFrontPlanets;
	private Map<Planet , List<Fleet>> incomingEnemyFleet;
	private Map<Planet, List<Fleet>> incomingAlliedFleet;
	private Simulation simulation;
	
	public Game(PlanetWars pw, GlobalData globalData){
		this.pw = pw;
		this.globalData = globalData;
		manager = new MissionManager(this);
		myFleets = new ArrayList<Fleet>();
		incomingEnemyFleet = new HashMap<Planet, List<Fleet>>();
		incomingAlliedFleet = new HashMap<Planet, List<Fleet>>();
		neutralPlanets = new ArrayList<Planet>();
		frontPlanets = new TreeSet<Integer>();
		futurFrontPlanets = new TreeSet<Integer>();
		simulation = new Simulation(this);
		
		initialize();
	}
	private void initialize(){
		//fill neutral planets
		for (Planet planet : pw.Planets()){
			if (planet.Owner()==GlobalData.NEUTRAL){
				neutralPlanets.add(planet);
			}
		}
		initializeMyFleets();
		initializeIncomingFleets(this.incomingAlliedFleet, pw.MyFleets());
		initializeIncomingFleets(this.incomingEnemyFleet, pw.EnemyFleets());
		simulation.initialize();
	}
	
	public void resetSimulation(){
		simulation.initialize();
	}
	
	public void calculateFrontPlanets(boolean neutralsImpactFront){
		
		calculateCurrentFrontPlanets(neutralsImpactFront);

		calculateFuturFrontPlanets(neutralsImpactFront);
	}
	private void calculateFuturFrontPlanets(boolean neutralsImpactFront) {
		//Calculate futur owner of each planets
		List<Planet> futurAlliedPlanets = new ArrayList<Planet>();
		List<Planet> futurEnemyPlanets = new ArrayList<Planet>();

		for (Planet planet : pw.Planets()){
			
			if (neutralsImpactFront){
				if (simulation.getFuturOwner(planet)!=GlobalData.PLAYER){
					futurEnemyPlanets.add(planet);
				}
				else{
					futurAlliedPlanets.add(planet);
				}
			}
			else{
				int futurOwner = simulation.getFuturOwner(planet);
				if (futurOwner==GlobalData.ENEMY){
					futurEnemyPlanets.add(planet);
				}
				else if (futurOwner==GlobalData.PLAYER){
					futurAlliedPlanets.add(planet);
				}
			}
		}
		initializeFrontStatus(futurEnemyPlanets, futurAlliedPlanets, futurFrontPlanets);
	}
	private void calculateCurrentFrontPlanets(boolean neutralsImpactFront) {
		if (neutralsImpactFront){
			initializeFrontStatus(pw.NotMyPlanets(), pw.MyPlanets(), frontPlanets);
		}
		else{
			initializeFrontStatus(pw.EnemyPlanets(), pw.MyPlanets(), frontPlanets);
		}
	}
	
	private void initializeFrontStatus(List<Planet> enemyPlanets, List<Planet> alliedPlanets, Set<Integer> frontPlanetIds){
		frontPlanetIds.clear();
		for (Planet enemyPlanet : enemyPlanets){
			//tag all the planets at the first distance as front planets
			int minimumDistance = Integer.MAX_VALUE;
			boolean distanceGreaterThanMinimum = false;
			int index = 0;
			List<Distance> distances = this.getDistances(enemyPlanet);
			
			for (index=0; index<distances.size() && !distanceGreaterThanMinimum ; index++){
				Distance current = distances.get(index);
				Planet currentPlanet = this.getPlanet(current.getDestination());
				
				if (alliedPlanets.contains(currentPlanet)){
					int currentDistance = current.getDistance();
					if (currentDistance<=minimumDistance){
						minimumDistance = currentDistance;
						setFront(currentPlanet, frontPlanetIds);
					}
					else{
						distanceGreaterThanMinimum = true;
					}
				}
			}
		}
		for (Planet myPlanet : alliedPlanets){
			//Ensure every planet as an allied planet closer than an enemy planet. Else tag it as front
			int minimumDistance = Integer.MAX_VALUE;
			boolean distanceGreaterThanMinimum = false;
			int index = 0;
			List<Distance> distances = this.getDistances(myPlanet);
			
			for (index=0; index<distances.size() && !distanceGreaterThanMinimum ; index++){
				Distance current = distances.get(index);
				Planet currentPlanet = this.getPlanet(current.getDestination());
				int currentDistance = current.getDistance();
				
				if (alliedPlanets.contains(currentPlanet)){
					if (currentDistance<=minimumDistance){
						minimumDistance = currentDistance;
					}
					else{
						distanceGreaterThanMinimum = true;
					}
				}
				else if (enemyPlanets.contains(currentPlanet)){
					if (currentDistance<=minimumDistance){
						setFront(myPlanet, frontPlanetIds);
						distanceGreaterThanMinimum = true;
					}
				}
			}
			if (enemyPlanets.size()==0){
				setFront(myPlanet, frontPlanetIds);
			}
		}
	}
	private void setFront(Planet planet, Set<Integer> frontList){
		frontList.add(planet.PlanetID());
	}
	public boolean isFront(Planet planet){
		return frontPlanets.contains(planet.PlanetID());
	}
	public boolean isFuturFront(Planet planet){
		return futurFrontPlanets.contains(planet.PlanetID());
	}
	private void initializeMyFleets(){
		for (Fleet fleet : pw.MyFleets()){
			myFleets.add(fleet);
		}
	}
	public PlanetWars getPw() {
		return pw;
	}
	public List<Planet> getNeutralPlanets(){
		return neutralPlanets;
	}
	public List<Planet> getPlanets(){
		return pw.Planets();
	}
	
	public Fleet sendMission(Planet source, Planet destination, int shipNumber){
		Fleet fleetSent = manager.sendMission(source, destination, shipNumber);
		myFleets.add(fleetSent);
		pushIncomingFleet(this.incomingAlliedFleet, fleetSent, destination);
		simulation.refreshPlanetSimulation(destination);
		simulation.refreshPlanetSimulation(source);
		return fleetSent;
	}
	public void refreshPlanetSimulation(Planet planet){
		simulation.refreshPlanetSimulation(planet);
	}
	public void cancelMission(Fleet fleet){
		Planet destination = getPlanet(fleet.DestinationPlanet());
		Planet source = getPlanet(fleet.SourcePlanet());
		manager.cancelMission(fleet);
		myFleets.remove(fleet);
		removeIncomingFleet(this.incomingAlliedFleet, fleet, destination);
		simulation.refreshPlanetSimulation(destination);
		simulation.refreshPlanetSimulation(source);
	}
	public void executeMissions(){
		manager.executeMissions();
	}
	public List<Planet> getMyPlanets(){
		return this.pw.MyPlanets();
	}
	public List<Planet>getEnemyPlanets(){
		return pw.EnemyPlanets();
	}
	public List<Fleet> getMyFleets(){
		return myFleets;
	}
	public List<Distance> getDistances(Planet planet){
		return globalData.getDistanceUtilities().getDistancesToOtherPlanets(planet);
	}
	public int getMaxDistance(){
		return DistanceUtilities.getMaxDistance();
	}
	public int getMaxTurnBeforeDefinitiveSideChange(Planet planet){
		return simulation.maxTurnBeforeFinalSideChange(planet);
	}
	public StateInfo getAvailableShips(Planet planet, Planet targetPlanet, int ownerPointOfView, int turnsOfReaction){
		StateInfo minAvailableShips = null;
		int turnMaxWeMustConsider = DistanceUtilities.getMaxDistance()+1;
		
		if (targetPlanet!=null){
			//We need to simulate we will take this position
			simulation.removePlanetContributionToDelta(targetPlanet);
			if (getFuturOwner(targetPlanet)!=GlobalData.NEUTRAL){
				turnMaxWeMustConsider = distance(planet, targetPlanet)+1;
			}
		}
		
		StateInfo minDelta = simulation.getMinDelta(planet, ownerPointOfView, 0, turnMaxWeMustConsider, turnsOfReaction);
		
		if (targetPlanet!=null){
			simulation.addPlanetContributionToDelta(targetPlanet);
		}
		if (minDelta.getOwner()==ownerPointOfView){
			minAvailableShips = minDelta.duplicate();
		}
		else{
			minAvailableShips = new StateInfo(ownerPointOfView, -1*minDelta.getShips(), minDelta.getTurn());
		}
		
		return minAvailableShips;
	}
	public void removePlanetContributionToDelta(Planet planet){
		simulation.removePlanetContributionToDelta(planet);
	}
	public void addPlanetContributionToDelta(Planet planet){
		simulation.addPlanetContributionToDelta(planet);
	}
	public int getAvailableShipsWithoutLoosingPlanet(Planet planet){
		return -1*simulation.requiredShipsToBeOwner(planet, 0);
	}
	public StateInfo getFuturAvailableShips(Planet planet, Planet targetPlanet, int ownerPointOfView, int turns){
		StateInfo currentAvailableShips = getAvailableShips(planet, targetPlanet, ownerPointOfView, 0);
		StateInfo currentShips = simulation.getStateAt(planet, 0);
		StateInfo futurShips = simulation.getStateAt(planet, turns);
		
		if (futurShips.getShips()>currentShips.getShips()){
			currentAvailableShips.contribute(currentShips);
			currentAvailableShips.contribute(futurShips);
		}
		
		return currentAvailableShips;
	}
	private void pushIncomingFleet(Map<Planet, List<Fleet>> map, Fleet fleet, Planet destination){
		List<Fleet> planetFleets = map.get(destination);
		if (planetFleets==null){
			planetFleets = new ArrayList<Fleet>();
			map.put(destination, planetFleets);
		}
		planetFleets.add(fleet);
	}
	private void removeIncomingFleet(Map<Planet, List<Fleet>> map, Fleet fleet, Planet destination){
		List<Fleet> planetFleets = map.get(destination);
		if (planetFleets==null){
			planetFleets = new ArrayList<Fleet>();
			map.put(destination, planetFleets);
		}
		planetFleets.remove(fleet);
	}
	private void initializeIncomingFleets(Map<Planet, List<Fleet>> map, List<Fleet> fleets){
		for (Fleet fleet : fleets){
			Planet destination = pw.GetPlanet(fleet.DestinationPlanet());
			this.pushIncomingFleet(map, fleet, destination);	
		}
	}
	public List<Fleet> getIncomingEnemyFleet(Planet planet){
		List<Fleet> incomingEnemyFleet = this.incomingEnemyFleet.get(planet);  
		
		if (incomingEnemyFleet==null){
			incomingEnemyFleet = new ArrayList<Fleet>();
		}
		return incomingEnemyFleet;
	}
	public List<Fleet> getIncomingAlliedFleet(Planet planet){
		List<Fleet> incomingAlliedFleet = this.incomingAlliedFleet.get(planet);  
		
		if (incomingAlliedFleet==null){
			incomingAlliedFleet = new ArrayList<Fleet>();
		}
		return incomingAlliedFleet;
	}
	public List<Planet>getOtherPlanets(){
		return pw.NotMyPlanets();
	}
	private StateInfo supportRequiredShipsOfDefendedPlanet(Planet source, Planet destination, int maxConsideredTurn, int conquestRequiredShips){
		int distance = distance(source, destination);
		StateInfo shipsRequired = null;
		int turnOfReactions = 0;
		
		//we should consider we took the planet
		// TODO check that if shipsRequired>Numships on planet, this is not an issue
		Fleet orderToCancel = sendMission(source, destination, conquestRequiredShips);
		
		StateInfo minPosition = simulation.getMinDelta(destination, GlobalData.PLAYER, distance, maxConsideredTurn, turnOfReactions);
		StateInfo delta = simulation.getDeltaStateAt(destination, GlobalData.ENEMY, minPosition.getTurn());
		
		shipsRequired=delta;
				
		cancelMission(orderToCancel);
		
		return shipsRequired;
	}

	public int conquestRequiredShips(Planet source, Planet destination){
		int futurOwner = simulation.getFuturOwner(destination);
		if (futurOwner==source.Owner()){
			return 0;
		}
		else {
			return simulation.requiredShipsToBeOwner(destination, distance(source, destination));
		}
	}
	public StateInfo supportRequiredShips(Planet source, Planet destination, int conquestRequiredShips){
		int futurOwner = simulation.getFuturOwner(destination);
		if (futurOwner==GlobalData.NEUTRAL){
			return supportRequiredShipsOfDefendedPlanet(source, destination, DistanceUtilities.getMaxDistance()+1, conquestRequiredShips);
		}
		else{
			return supportRequiredShipsOfDefendedPlanet(source, destination, distance(source, destination)+1, conquestRequiredShips);
		}
	}
	private int distanceToFirstPlanet(Planet planet, int owner){
		int distance = DistanceUtilities.getMaxDistance();
		int index = 0;
		List<Distance> distances = this.getDistances(planet);
		
		if (planet.Owner()==owner){
			return 0;
		}
		
		for (index=0; index<distances.size(); index++){
			Distance current = distances.get(index);
			if (pw.GetPlanet(current.getDestination()).Owner()==owner){
				distance = current.getDistance();
				index = distances.size();
			}
		}
		
		return distance;
	}
	public int distanceToFirstAlliedPlanet(Planet planet){
		return distanceToFirstPlanet(planet, GlobalData.PLAYER);
	}
	public int distanceToFirstEnemyPlanet(Planet planet){
		return distanceToFirstPlanet(planet, GlobalData.ENEMY);
	}
	public int distance(Planet planet1, Planet planet2){
		return pw.Distance(planet1.PlanetID(), planet2.PlanetID());
	}
	public Planet getPlanet(int iD){
		return pw.GetPlanet(iD);
	}
	public int getFuturProduction(int owner){
		int futurProduction = 0;
		
		for (Planet planet : getPlanets()){
			if (getFuturOwner(planet)==owner){
				futurProduction+=planet.GrowthRate();
			}
		}
		
		return futurProduction;
	}
	public boolean iHaveMoreShips(double threshold){
		return pw.NumShips(GlobalData.PLAYER)>pw.NumShips(GlobalData.ENEMY);
	}
	public boolean iProduceMoreShips(double threshold){
		int myTotalGrowth = 0;
		int enemyTotalGrowth = 0;
		
		for (Planet p : pw.MyPlanets()){
			myTotalGrowth+=p.GrowthRate();
		}
		for (Planet p : pw.EnemyPlanets()){
			enemyTotalGrowth+=p.GrowthRate();
		}
		
		return myTotalGrowth>enemyTotalGrowth*threshold;
	}
	public boolean iWillProduceMoreShips(double threshold){
		int playerFuturProduction = getFuturProduction(GlobalData.PLAYER);
		int enemyFuturProduction = getFuturProduction(GlobalData.ENEMY);
		
		return playerFuturProduction>enemyFuturProduction*threshold;
	}
	public boolean iWillHaveMoreShips(double threshold){
		int playerTotalShips = 0;
		int enemyTotalShips = 0;
		
		int turn = DistanceUtilities.getMaxDistance();
		//Fleet will be arrived at maxDistance
		for (Planet planet : getPlanets()){
			StateInfo turnInfo = simulation.getStateAt(planet, turn);
			
			if (turnInfo.getOwner()==GlobalData.PLAYER){
				playerTotalShips+=turnInfo.getShips();
			}
			else if (turnInfo.getOwner()==GlobalData.ENEMY){
				enemyTotalShips+=turnInfo.getShips();
			}
		}
		
		return playerTotalShips>enemyTotalShips*threshold;
	}
	public int getFuturOwner(Planet planet){
		return simulation.getFuturOwner(planet);
	}
	public int requieredShipsToBeOwner(Planet planet, int distance){
		return simulation.requiredShipsToBeOwner(planet, distance);
	}
	public int firstIncomingAlliedFleetImpact(Planet destination) {
		int turn = -1;
		
		if (incomingAlliedFleet.containsKey(destination)){
			List<Fleet> incomingFleets = incomingAlliedFleet.get(destination);
			
			if (incomingFleets.size()>0){
				turn = getMaxDistance();
				
				for (Fleet fleet : incomingFleets){
					if (fleet.TurnsRemaining()<turn){
						turn = fleet.TurnsRemaining();
					}
				}
			}
		}
		
		return turn;
	}
	public void evaluateShipsLostAgainstNeutral(PossibleAttack attack) {
		int neutralCount = 0;
		
		for (FleetOrder order : attack.getFleets()){
			StateInfo impactInfo = simulation.getStateAt(order.getDestinationPlanet(), distance(order.getDestinationPlanet(), order.getSourcePlanet())-1);
			
			if (impactInfo.getOwner()==GlobalData.NEUTRAL){
				if (order.getShips()>impactInfo.getShips()){
					neutralCount+=impactInfo.getShips();
				}
				else{
					neutralCount+=order.getShips();	
				}
			}
		}
		
		attack.setShipsLostAgainstNeutral(neutralCount);
	}
	
	public boolean supportValidated(Map<Planet, StateInfo> supports){
		boolean supportValidated = true;
		
		for (Planet target : supports.keySet()){
			StateInfo info = supports.get(target);
			//retrieve delta at info turn
			if (!planetSupportValidated(target, info)){
				supportValidated = false;
			}
		}
		
		return supportValidated;
	}
	public boolean planetSupportValidated(Planet targetPlanet,
			StateInfo supportShips) {
		if (supportShips==null){
			return true;
		}
		
		StateInfo delta = simulation.getDeltaStateAt(targetPlanet, GlobalData.PLAYER, supportShips.getTurn());
		
		if (delta.getShips()<supportShips.getShips()){
			return false;
		}
		else{
			return true;
		}
	}
}
