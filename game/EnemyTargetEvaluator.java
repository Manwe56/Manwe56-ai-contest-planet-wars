package game;

public class EnemyTargetEvaluator extends TargetEvaluator {

	public EnemyTargetEvaluator(Planet target) {
		super(target);
	}

	@Override
	public double targetGrowthRate() {
		return 2*target.GrowthRate();
	}

}
