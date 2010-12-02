package time;
import game.Game;
import game.Planet;
import global.GlobalData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distance.Distance;
import distance.DistanceUtilities;

public class Simulation {
	Game game;
	Map<Planet, TimeEngine> timeEngines;
	
	public Simulation(Game game){
		super();
		this.game = game;
		timeEngines = new HashMap<Planet, TimeEngine>();
	}
	
	public void initialize(){
		timeEngines.clear();
		for (Planet planet : game.getPlanets()){
			TimeEngine engine = new TimeEngine(planet);
			
			computeFuturState(planet, engine);
			
			timeEngines.put(planet, engine);
		}
		for (Planet planet : game.getPlanets()){
			impactPlanetContributionOnDeltas(planet, false);
		}
	}
	
	private void impactPlanetContributionOnDeltas(Planet planet, boolean reverse){
		int turn = 0;
		int maxSimulatedTurn = DistanceUtilities.getMaxSimulatedTurn();
		
		for (turn=0; turn<maxSimulatedTurn; turn++){
			StateInfo turnInfo = getStateAt(planet, turn);
			if (reverse){
				turnInfo = turnInfo.reverseShips();
			}
			//Neutral player does not contribute to fleet delta
			if (turnInfo.getOwner()!=GlobalData.NEUTRAL){
				List<Distance> distances = game.getDistances(planet);
				
				for (Distance distance : distances){
					//compute on which turn this turn will count for the planet
					int impactedTurn = distance.getDistance()+turn;
					if (impactedTurn<maxSimulatedTurn){
						Planet impactedPlanet = game.getPlanet(distance.getDestination());
						TimeEngine impactedEngine = timeEngines.get(impactedPlanet);
						
						impactedEngine.addDeltaContribution(turnInfo, impactedTurn);
					}
				}
			}
		}
	}
	
	private void computeFuturState(Planet planet, TimeEngine engine){
		engine.computeFuturState(game.getIncomingAlliedFleet(planet), game.getIncomingEnemyFleet(planet));
	}
	
	public void refreshPlanetSimulation(Planet planet){
		TimeEngine engine = timeEngines.get(planet);
		impactPlanetContributionOnDeltas(planet, true);
		computeFuturState(planet, engine);
		impactPlanetContributionOnDeltas(planet, false);
	}
	
	public int maxTurnBeforeFinalSideChange(Planet planet){
		TimeEngine engine = timeEngines.get(planet);
		FuturStateInfo infos = engine.getFuturInfo(0);
		return infos.maxTurnBeforeFinalSideChange(planet.Owner());
	}
	
	public int getFuturOwner(Planet planet){
		TimeEngine engine = timeEngines.get(planet);
		FuturStateInfo infos = engine.getFuturInfo(0);
		
		return infos.getFinalOwner();
	}
	
	public StateInfo getStateAt(Planet planet, int turn){
		TimeEngine engine = timeEngines.get(planet);
		
		return engine.getStateAt(turn);
	}
	
	public int requiredShipsToBeOwner(Planet planet, int distance){
		TimeEngine engine = timeEngines.get(planet);
		
		return engine.requiredShipsToBeOwner(distance);
	}
	
	public StateInfo getMinDelta(Planet planet, int owner, int turnMin, int turnMax, int turnsOfReaction){
		TimeEngine engine = timeEngines.get(planet);
		
		return engine.getMinDelta(owner, turnMin, turnMax, turnsOfReaction);
	}

	public StateInfo getDeltaStateAt(Planet planet, int owner, int distance) {
		TimeEngine engine = timeEngines.get(planet);
		
		return engine.getDeltaStateAt(owner, distance);
	}

	public void removePlanetContributionToDelta(Planet planet) {
		impactPlanetContributionOnDeltas(planet, true);
	}

	public void addPlanetContributionToDelta(Planet planet) {
		impactPlanetContributionOnDeltas(planet, false);
	}
	
}
