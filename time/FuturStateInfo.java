package time;

import global.GlobalData;

import java.util.HashMap;
import java.util.Map;

import distance.DistanceUtilities;


public class FuturStateInfo {
	private Map<Integer,StateInfo> futurState;
	private Map<Integer,StateInfo> futurAlliedDeltas;
	private Map<Integer,StateInfo> futurEnemyDeltas;
	private int finalOwner;
	private int minimumAlliedFleet;
	private int minimumAlliedFleetStartTime;

	FuturStateInfo(){
		super();
		futurState = new HashMap<Integer, StateInfo>();
		futurAlliedDeltas = createDeltasMap(GlobalData.PLAYER);
		futurEnemyDeltas = createDeltasMap(GlobalData.ENEMY);
	}
	
	private Map<Integer, StateInfo> createDeltasMap(int owner){
		Map<Integer, StateInfo> futurDeltas = new HashMap<Integer, StateInfo>();
		for (int turn=0; turn<DistanceUtilities.getMaxSimulatedTurn()+1; turn++){
			futurDeltas.put(turn, new StateInfo(owner, 0, turn));
		}
		
		return futurDeltas;
	}

	public int maxTurnBeforeFinalSideChange(int owner){
		int turn=0;
		int currentTurn = 0;
		
		for (currentTurn=0; currentTurn<DistanceUtilities.getMaxDistance(); currentTurn++){
			StateInfo currentState = futurState.get(currentTurn);
			
			if (currentState!=null){
				if (currentState.getOwner()==owner){
					turn = currentTurn;
				}
			}
		}
		
		return turn+1;
	}
	
	public StateInfo getFuturState(int turn) {
		return futurState.get(turn);
	}
	public void setFuturState(Map<Integer, StateInfo> futurState) {
		this.futurState = futurState;
	}
	public int getMinimumAlliedFleet() {
		return minimumAlliedFleet;
	}
	public void setMinimumAlliedFleet(int minimumAlliedFleet) {
		this.minimumAlliedFleet = minimumAlliedFleet;
	}
	public int getMinimumAlliedFleetStartTime() {
		return minimumAlliedFleetStartTime;
	}
	public void setMinimumAlliedFleetStartTime(int minimumAlliedFleetStartTime) {
		this.minimumAlliedFleetStartTime = minimumAlliedFleetStartTime;
	}
	public Map<Integer, StateInfo> getFuturStates() {
		return futurState;
	}
	public int getFinalOwner() {
		return finalOwner;
	}
	public void setFinalOwner(int finalOwner) {
		this.finalOwner = finalOwner;
	}
	public void putState(StateInfo stateInfo, int turn){
		futurState.put(turn, stateInfo);
	}
	
	public void addDeltaContribution(StateInfo contributionInfo, int impactedTurn) {
		StateInfo impactedDelta = null;
		
		if (contributionInfo.getOwner()==GlobalData.PLAYER){
			impactedDelta = futurAlliedDeltas.get(impactedTurn);
			impactedDelta.contribute(contributionInfo);
		}
		else{
			impactedDelta = futurEnemyDeltas.get(impactedTurn);
			impactedDelta.contribute(contributionInfo);
		}
	}
	
	private StateInfo calculateDeltaState(int turn, int turnsOfReaction){
		StateInfo alliedState = null;
		StateInfo enemyState = futurEnemyDeltas.get(turn);
		StateInfo planetState = futurState.get(turn);
		int alliedTurn = turn-turnsOfReaction;
		
		if (alliedTurn>DistanceUtilities.getMaxSimulatedTurn()){
			alliedState = futurAlliedDeltas.get(DistanceUtilities.getMaxSimulatedTurn());
		}
		else if (alliedTurn>0){
			alliedState = futurAlliedDeltas.get(alliedTurn);
		}
		else{
			alliedState = futurAlliedDeltas.get(0);
		}
		StateInfo result = new StateInfo(planetState.getOwner(), 0, turn);

		if (planetState.getOwner()!=GlobalData.NEUTRAL){
			result.contribute(planetState);
		}
		result.contribute(alliedState);
		result.contribute(enemyState);
		
		return result;
	}

	public StateInfo getMinSimulatedDelta(int owner, int turnMin, int turnMax, int turnsOfReaction) {
		StateInfo minInfo = calculateDeltaState(turnMin, turnsOfReaction);
		
		for (int turn=turnMin+1; turn<turnMax; turn++){
			StateInfo info = calculateDeltaState(turn, turnsOfReaction);
			
			if (info.getOwner()!=GlobalData.NEUTRAL){
				if (info.getOwner()==owner){
					if (minInfo.getOwner()==owner && info.getShips()<minInfo.getShips()){
						minInfo = info;
					}
					//else do nothing
				}
				else{
					if (minInfo.getOwner()==owner){
						minInfo = info;
					}
					else if (info.getShips()>minInfo.getShips()){
						minInfo = info;
					}
					//else do nothing
				}
			}
		}
		
		return minInfo;
	}
	
	public StateInfo getDeltaState(int owner, int distance) {
		if (owner==GlobalData.PLAYER){
			return futurAlliedDeltas.get(distance);
		}
		else{
			return futurEnemyDeltas.get(distance);
		}
	}
}
