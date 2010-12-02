package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import time.StateInfo;

import distance.DistanceUtilities;


public class PossibleAttack implements Comparable<PossibleAttack>{
	double rating;
	List<FleetOrder> fleets;
	int shipsLostAgainstNeutral;
	StateInfo support;
	boolean postponed;
	public PossibleAttack(){
		fleets = new ArrayList<FleetOrder>();
	}
	public double getRating() {
		return rating;
	}
	public Planet getDestination(){
		if (fleets.size()>0){
			return fleets.get(0).getDestinationPlanet();
		}
		else{
			return null;
		}
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public StateInfo getSupport() {
		return support;
	}
	public void setSupport(StateInfo support) {
		this.support = support;
	}
	public List<FleetOrder> getFleets() {
		return fleets;
	}
	public boolean isPostponed() {
		return postponed;
	}
	public void setPostponed() {
		postponed = true;
	}
	public int getShipsLostAgainstNeutral() {
		return shipsLostAgainstNeutral;
	}
	public void setShipsLostAgainstNeutral(int shipsLostAgainstNeutral) {
		this.shipsLostAgainstNeutral = shipsLostAgainstNeutral;
	}
	public void addFleetOrder(FleetOrder order) {
		fleets.add(order);
	}
	public void execute(Game game, Map<Planet, Integer> planetsWithPosponedAttack){
		for (FleetOrder order : fleets){
			if (postponed){
				reserveShips(order, planetsWithPosponedAttack);
			}
			else{
				game.sendMission(order.getSourcePlanet(), order.getDestinationPlanet(), order.getShips());
			}
		}
	}
	public void executeSynchronized(Game game, Map<Planet, Integer> planetsWithPosponedAttack) {
		Planet destination = getDestination();
		int turnOfImpact = game.firstIncomingAlliedFleetImpact(destination);
		
		if (turnOfImpact==-1){
			turnOfImpact = maxDistance();
		}
		
		for (FleetOrder order : fleets){
			int distance = game.distance(order.getSourcePlanet(), destination);
			if (postponed){
				reserveShips(order, planetsWithPosponedAttack);
			}
			else if (distance>=turnOfImpact){
				game.sendMission(order.getSourcePlanet(), destination, order.getShips());
			}
			else{
				reserveShips(order, planetsWithPosponedAttack);
			}
			
		}
	}
	public int maxDistance(){
		int maxDistance = 0;
		
		for (FleetOrder fleet : fleets){
			int distance = DistanceUtilities.getDistance(fleet.getSourcePlanet(), fleet.getDestinationPlanet());
			
			if (distance>maxDistance){
				maxDistance = distance;
			}
		}
		
		return maxDistance;
	}
	public List<Planet> sources(){
		List<Planet> sources = new ArrayList<Planet>();
		
		for (FleetOrder order : fleets){
			sources.add(order.getSourcePlanet());
		}
		
		return sources;
	}
	@Override
	public int compareTo(PossibleAttack other) {
		double diff = rating-other.rating;
		
		if (diff<0){
			return -1;
		}
		else if (diff>0){
			return 1;
		}
		else{
			return 0;
		}
	}
	private void reserveShips(FleetOrder order, Map<Planet, Integer> planetsWithPosponedAttack) {
		Planet source = order.getSourcePlanet();
		if (planetsWithPosponedAttack.containsKey(source)){
			int alreadyReservedShips = planetsWithPosponedAttack.get(source);
			planetsWithPosponedAttack.put(order.getSourcePlanet(), alreadyReservedShips+order.getShips());
		}
		else{
			planetsWithPosponedAttack.put(order.getSourcePlanet(), order.getShips());
		}
	}
}
