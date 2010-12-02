package bot;
import game.Game;
import global.GlobalData;

public class BotFactory {

	Bot createBotCorrespondingToContext(Game game){
		Bot bot = null;
		
		if (GlobalData.getSingleton().getCurrentTurn()>GlobalData.EXPANSION_TURNS){
			bot = new OffensiveBot();
		}
		else{
			bot = new ExpansiveBot();
		}
		
		return bot;
	}
}
