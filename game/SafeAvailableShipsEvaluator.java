package game;

import global.GlobalData;

import java.util.Map;

import time.StateInfo;

public class SafeAvailableShipsEvaluator extends AvailableShipsEvaluator {

	public SafeAvailableShipsEvaluator(Game game) {
		super(game);
	}

	@Override
	public StateInfo getAvailableShips(Planet planet, Planet targetPlanet, Map<Planet, Integer> reservedShipsForOtherAttacks) {
		int turnOfReactions = 1;
		
		if (targetPlanet.Owner()==GlobalData.ENEMY){
			turnOfReactions = 0;
		}
		
		StateInfo availableShips = game.getAvailableShips(planet, targetPlanet, GlobalData.PLAYER, turnOfReactions);
		if (reservedShipsForOtherAttacks.containsKey(planet)){
			availableShips.removeShips(reservedShipsForOtherAttacks.get(planet));
		}
		return availableShips;
	}

	@Override
	public StateInfo getFuturAvailableShips(Planet planet, Planet targetPlanet,
			Map<Planet, Integer> reservedShipsForOtherAttacks, int turns) {
		
		StateInfo availableShips = game.getFuturAvailableShips(planet, targetPlanet, GlobalData.PLAYER, turns);
		if (reservedShipsForOtherAttacks.containsKey(planet)){
			availableShips.removeShips(reservedShipsForOtherAttacks.get(planet));
		}
		return availableShips;
	}
}
