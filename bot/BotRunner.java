package bot;

import game.Game;
import global.GlobalData;
import game.PlanetWars;

public class BotRunner {
	
	public static void clear(){
		GlobalData.reset();
	}
	
	public static void DoTurn(PlanetWars pw) {
		GlobalData globalData = GlobalData.getSingleton(pw);
		BotFactory factory = new BotFactory();
    	Game game = new Game(pw, globalData);
    	Bot bot = factory.createBotCorrespondingToContext(game);
    	
    	bot.playTurn(game);
    	globalData.endTurn();
	}
}
