package bot;

import game.Game;

public class OffensiveBot extends BaseBot {
	
	@Override
	public void playTurn(Game game) {
		this.game = game;
		game.calculateFrontPlanets(false);
		attackAllPositions();
		game.calculateFrontPlanets(false);
		moveAvailableShipsToFrontPlanets();
		attackEnemyPositionsWithLostPlanetsShips();
		game.executeMissions();
	}
}
