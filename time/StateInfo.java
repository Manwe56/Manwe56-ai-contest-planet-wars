package time;

import global.GlobalData;

public class StateInfo{
	private int owner;
	private int ships;
	private int turn;
	public StateInfo(int owner, int ships, int turn) {
		super();
		this.owner = owner;
		this.ships = ships;
		this.turn = turn;
	}
	public int getOwner() {
		return owner;
	}
	public int getShips() {
		return ships;
	}
	public int getTurn() {
		return turn;
	}
	public void contribute(StateInfo contribution){
		if (contribution.owner==this.owner){
			this.ships+=contribution.ships;
		}
		else{
			if (contribution.ships>this.ships){
				this.owner = contribution.owner;
				this.ships = contribution.ships - this.ships;
			}
			else{
				this.ships -= contribution.ships;
			}
		}
	}
	public void reset() {
		this.owner = GlobalData.NEUTRAL;
		this.ships = 0;
	}
	public StateInfo reverseShips(){
		return new StateInfo(owner, -1*ships, turn);
	}
	public StateInfo duplicate() {
		return new StateInfo(owner, ships, turn);
	}
	public void removeShips(int number) {
		ships-=number;		
	}
}
