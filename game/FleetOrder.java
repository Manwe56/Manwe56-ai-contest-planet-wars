package game;


public class FleetOrder {
	private Planet sourcePlanet;
	private Planet destinationPlanet;
	private int ships;
	public FleetOrder(Planet sourcePlanet, Planet destinationPlanet, int ships) {
		super();
		this.sourcePlanet = sourcePlanet;
		this.destinationPlanet = destinationPlanet;
		this.ships = ships;
	}
	public Planet getSourcePlanet() {
		return sourcePlanet;
	}
	public void setSourcePlanet(Planet sourcePlanet) {
		this.sourcePlanet = sourcePlanet;
	}
	public Planet getDestinationPlanet() {
		return destinationPlanet;
	}
	public void setDestinationPlanet(Planet destinationPlanet) {
		this.destinationPlanet = destinationPlanet;
	}
	public int getShips() {
		return ships;
	}
	public void setShips(int ships) {
		this.ships = ships;
	}
}
