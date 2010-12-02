package game;

import time.StateInfo;


public class PlanetInDanger implements Comparable<PlanetInDanger>{
	private Planet planet;
	private StateInfo info;
	public PlanetInDanger(Planet planet, StateInfo info) {
		super();
		this.planet = planet;
		this.info = info;
	}
	public Planet getPlanet() {
		return planet;
	}
	public void setPlanet(Planet planet) {
		this.planet = planet;
	}
	public StateInfo getInfo() {
		return info;
	}
	public void setInfo(StateInfo info) {
		this.info = info;
	}
	@Override
	public int compareTo(PlanetInDanger other) {
		
		int diff = info.getTurn()-other.info.getTurn();
		
		if (diff==0){
			diff = info.getShips()-other.info.getShips();
		}
		return diff;
	}
}
