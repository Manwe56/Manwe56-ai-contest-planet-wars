package game;

public abstract class TargetEvaluator {
	protected Planet target;
	
	TargetEvaluator(Planet target){
		this.target = target;
	}
	
	public double evaluateTargetInterest()
	{
		return targetGrowthRate()/(target.NumShips()+1);
	}
	
	public abstract double targetGrowthRate();
	
	public double attackPossibilityTurnsReturnOnInvestment(PossibleAttack attack, int additionalTurns){
		double rating = Double.MAX_VALUE;
		
		if (targetGrowthRate()!=0){//we never know...
			rating = (double)(attack.getShipsLostAgainstNeutral()/targetGrowthRate())+attack.maxDistance()+additionalTurns;
		}
		attack.setRating(rating);
		
		return rating;
	}
}
