package game;

import java.util.Map;

import time.StateInfo;

public abstract class AvailableShipsEvaluator {
	protected Game game;
	
	public AvailableShipsEvaluator(Game game) {
		super();
		this.game = game;
	}
	
		public abstract StateInfo getAvailableShips(Planet planet, Planet targetPlanet, Map<Planet, Integer> reservedShipsForOtherAttacks);
	
	public abstract StateInfo getFuturAvailableShips(Planet planet, Planet targetPlanet, Map<Planet, Integer> reservedShipsForOtherAttacks, int turns);
}
