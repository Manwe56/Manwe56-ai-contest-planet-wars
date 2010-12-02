package game;

public class NeutralTargetEvaluator extends TargetEvaluator {

	public NeutralTargetEvaluator(Planet target) {
		super(target);
	}

	@Override
	public double targetGrowthRate() {
		return target.GrowthRate();
	}
}