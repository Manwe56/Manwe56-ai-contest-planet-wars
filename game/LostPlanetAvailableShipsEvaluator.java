package game;

import global.GlobalData;

import java.util.Map;

import time.StateInfo;

public class LostPlanetAvailableShipsEvaluator extends AvailableShipsEvaluator {

	public LostPlanetAvailableShipsEvaluator(Game game) {
		super(game);
	}

	@Override
	public StateInfo getAvailableShips(Planet planet, Planet targetPlanet, Map<Planet, Integer> reservedShipsForOtherAttacks) {
		int availableShips = planet.NumShips();
		if (reservedShipsForOtherAttacks.containsKey(planet)){
			availableShips-=reservedShipsForOtherAttacks.get(planet);
		}
		return new StateInfo(GlobalData.PLAYER, availableShips, 0);
	}

	@Override
	public StateInfo getFuturAvailableShips(Planet planet, Planet targetPlanet,
			Map<Planet, Integer> reservedShipsForOtherAttacks, int turns) {
		return getAvailableShips(planet, targetPlanet, reservedShipsForOtherAttacks);
	}

}
