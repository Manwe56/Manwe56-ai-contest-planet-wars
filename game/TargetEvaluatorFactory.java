package game;

import global.GlobalData;

public class TargetEvaluatorFactory {
	public static TargetEvaluator createTargetEvaluator(Planet planet, int futurOwner){
		if (futurOwner==GlobalData.NEUTRAL){
			return new NeutralTargetEvaluator(planet);
		}
		else{
			return new EnemyTargetEvaluator(planet);
		}
	}
}
