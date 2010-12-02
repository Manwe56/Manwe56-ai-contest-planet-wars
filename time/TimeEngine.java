package time;
import game.Fleet;
import game.Planet;
import global.GlobalData;

import java.util.ArrayList;
import java.util.List;


import distance.DistanceUtilities;


public class TimeEngine {
	private Planet planet;
	private FuturStateInfo futurInfo;
	private List<Fleet> incomingAlliedFleets;
	private List<Fleet> incomingEnemyFleets;
	
	public TimeEngine(Planet planet) {
		super();
		this.planet = planet;
		this.futurInfo = null;
		this.incomingAlliedFleets = null;
		this.incomingEnemyFleets = null;
	}
	public Planet getPlanet() {
		return planet;
	}
	public void setPlanet(Planet planet) {
		this.planet = planet;
	}
	private int sumIncomingShipsAtTurn(List<Fleet> fleets, int turn){
		int total = 0;
		
		for (Fleet fleet : fleets){
			if (fleet.TurnsRemaining()==turn){
				total+=fleet.NumShips();
			}
		}
		
		return total;
	}
	public int requiredShipsToBeOwner(int distance){
		int shipsRequired = 20000;
		List<Fleet> localIncomingAlliedFleets = new ArrayList<Fleet>();
		Fleet newFleet = new Fleet(GlobalData.PLAYER, shipsRequired, 0, 0, distance, distance);
		StateInfo impactState = futurInfo.getFuturState(distance);
		
		localIncomingAlliedFleets.addAll(incomingAlliedFleets);
		localIncomingAlliedFleets.add(newFleet);
		
		FuturStateInfo infos = runSimulation(localIncomingAlliedFleets, incomingEnemyFleets, distance, null);
		
		shipsRequired -= infos.getMinimumAlliedFleet();
		
		if (impactState.getOwner()!=GlobalData.PLAYER){
			shipsRequired+=1;
		}
		
		return shipsRequired;
	}
	
	public void computeFuturState(List<Fleet> incomingAlliedFleets, List<Fleet> incomingEnemyFleets){
		this.incomingAlliedFleets = incomingAlliedFleets;
		this.incomingEnemyFleets = incomingEnemyFleets;
		futurInfo = runSimulation(incomingAlliedFleets, incomingEnemyFleets, 0, futurInfo);
	}
	
	public FuturStateInfo getFuturInfo(int minimumTurns) {
		return futurInfo;
	}
	public StateInfo getStateAt(int turn){
		return futurInfo.getFuturStates().get(turn);
	}
	private FuturStateInfo runSimulation(List<Fleet> localIncomingAlliedFleets, List<Fleet> localIncomingEnemyFleets, int minimumTurns, FuturStateInfo futurStateInfo){
		FuturStateInfo futurState = futurStateInfo;
		
		int maxTurns = DistanceUtilities.getMaxSimulatedTurn();
		
		int currentTurn = 0;
		int currentShipsOnPlanet = planet.NumShips();
		int owner = planet.Owner();
		
		if (futurState==null){
			futurState = new FuturStateInfo();
		}
		
		futurState.setMinimumAlliedFleetStartTime(minimumTurns);
		futurState.setMinimumAlliedFleet(Integer.MAX_VALUE);
		
		for (currentTurn=0; currentTurn<=maxTurns; currentTurn++){
			if (owner!=GlobalData.NEUTRAL && currentTurn>0){
				currentShipsOnPlanet += planet.GrowthRate();
			}
			
			int incomingAlliedShips = sumIncomingShipsAtTurn(localIncomingAlliedFleets, currentTurn);
			int incomingEnemyShips = sumIncomingShipsAtTurn(localIncomingEnemyFleets, currentTurn);
			
			switch (owner)
			{
				case GlobalData.NEUTRAL:{
					if (currentShipsOnPlanet>=incomingAlliedShips && currentShipsOnPlanet>=incomingEnemyShips){
						if (incomingAlliedShips>incomingEnemyShips){
							currentShipsOnPlanet -= incomingAlliedShips;
						}
						else{
							currentShipsOnPlanet -= incomingEnemyShips;
						}
					}
					else{
						if (incomingAlliedShips==incomingEnemyShips){
							currentShipsOnPlanet = 0;//draw! Planet stay neutral
						}
						else if (incomingAlliedShips>incomingEnemyShips){
							owner = GlobalData.PLAYER;
							if (currentShipsOnPlanet>incomingEnemyShips){
								currentShipsOnPlanet = incomingAlliedShips-currentShipsOnPlanet;
							}
							else
							{
								currentShipsOnPlanet = incomingAlliedShips-incomingEnemyShips;
							}
						}
						else{
							owner = GlobalData.ENEMY;
							if (currentShipsOnPlanet>incomingAlliedShips){
								currentShipsOnPlanet = incomingEnemyShips-currentShipsOnPlanet;
							}
							else
							{
								currentShipsOnPlanet = incomingEnemyShips-incomingAlliedShips;
							}
						}
					}
				}
				break;
				case GlobalData.ENEMY:{
					if (incomingAlliedShips>currentShipsOnPlanet+incomingEnemyShips){
						owner = GlobalData.PLAYER;
						currentShipsOnPlanet = incomingAlliedShips-(currentShipsOnPlanet+incomingEnemyShips);
					}
					else{
						currentShipsOnPlanet += incomingEnemyShips-incomingAlliedShips;
					}
				}
				break;
				case GlobalData.PLAYER:{
					if (incomingEnemyShips>currentShipsOnPlanet+incomingAlliedShips){
						owner = GlobalData.ENEMY;
						currentShipsOnPlanet = incomingEnemyShips-(currentShipsOnPlanet+incomingAlliedShips);
					}
					else{
						currentShipsOnPlanet += incomingAlliedShips-incomingEnemyShips;
					}
				}
				break;
			}
			if (currentTurn>=minimumTurns){
				int alliedShips = 0;
				if (owner == GlobalData.PLAYER){
					alliedShips = currentShipsOnPlanet;
				}
				if (alliedShips<futurState.getMinimumAlliedFleet()){
					futurState.setMinimumAlliedFleet(alliedShips);
				}
			}
			StateInfo turnState = new StateInfo(owner, currentShipsOnPlanet, currentTurn);
			futurState.putState(turnState, currentTurn);
		}
		futurState.setFinalOwner(owner);
		return futurState;
	}
	public void addDeltaContribution(StateInfo contributionInfo, int impactedTurn) {
		futurInfo.addDeltaContribution(contributionInfo, impactedTurn);		
	}
	public StateInfo getMinDelta(int owner, int turnMin, int turnMax, int turnsOfReaction) {
		return futurInfo.getMinSimulatedDelta(owner, turnMin, turnMax, turnsOfReaction);
	}
	public StateInfo getDeltaStateAt(int owner, int distance) {
		return futurInfo.getDeltaState(owner, distance);
	}
}
