package distance;

import game.Planet;
import game.PlanetWars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DistanceUtilities {
	private Map<Integer, List<Distance>> planetDistancesDictionary = new HashMap<Integer, List<Distance>>();
	private static int maxDistance = 0;
	
	public void initialize(PlanetWars pw){
		maxDistance = 0;
		for (Planet planet : pw.Planets()){
			List<Distance> distancesToPlanet = new ArrayList<Distance>();		
			
			for (Planet other : pw.Planets()){
				if (other!=planet){
					int currentDistance = pw.Distance(planet.PlanetID(), other.PlanetID());
					Distance distance = new Distance(planet.PlanetID(), other.PlanetID(), currentDistance);
					if (currentDistance>maxDistance){
						maxDistance = currentDistance;
					}
					distancesToPlanet.add(distance);
				}
			}
			
			Collections.sort(distancesToPlanet);
			planetDistancesDictionary.put(planet.PlanetID(), distancesToPlanet);
		}
	}
	public static int getMaxDistance() {
		return maxDistance;
	}
	public static int getMaxSimulatedTurn(){
		return maxDistance+1;
	}
	public List<Distance> getDistancesToOtherPlanets(Planet planet){
		return planetDistancesDictionary.get(planet.PlanetID());
	}
	
	public static int getDistance(Planet planet1, Planet planet2){
		double dx = planet1.X() - planet2.X();
		double dy = planet1.Y() - planet2.Y();
		return (int)Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}
}
