package distance;

public class Distance implements Comparable<Distance>{
	private int source;
	private int destination;
	private int distance;
	public Distance(int source, int destination, int distance) {
		super();
		this.source = source;
		this.destination = destination;
		this.distance = distance;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getDestination() {
		return destination;
	}
	public void setDestination(int destination) {
		this.destination = destination;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	@Override
	public int compareTo(Distance other) {
		return distance-other.distance;
	}
}
