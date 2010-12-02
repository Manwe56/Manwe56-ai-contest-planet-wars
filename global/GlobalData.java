package global;
import game.PlanetWars;
import distance.DistanceUtilities;


public class GlobalData {
	private int currentTurn=0;
	private DistanceUtilities distanceUtilities = new DistanceUtilities();
	
	private static GlobalData singleton;

	public static final int NEUTRAL = 0;
	public static final int PLAYER = 1;
	public static final int ENEMY = 2;
	
	public static final int TURN_COUNT = 200;
	
	//Behavior flags
	public static final boolean SYNCHRONIZED_ATTACKS = false;
	public static final int EXPANSION_TURNS = 5;
	
	private GlobalData(PlanetWars pw){
		initialize(pw);
	}
	
	public int getCurrentTurn() {
		return currentTurn;
	}
	
	public void endTurn(){
		currentTurn++;
	}
	
	private void initialize(PlanetWars pw){
		distanceUtilities.initialize(pw);
	}

	public DistanceUtilities getDistanceUtilities() {
		return distanceUtilities;
	}

	public static GlobalData getSingleton(PlanetWars pw) {
		if (singleton==null){
			singleton = new GlobalData(pw);
		}
		return singleton;
	}
	
	public static GlobalData getSingleton() {
		return singleton;
	}
	
	public static void reset(){
		singleton = null;
	}
	
	public static int turnsRemaining(){
		return TURN_COUNT-singleton.currentTurn;
	}
}
