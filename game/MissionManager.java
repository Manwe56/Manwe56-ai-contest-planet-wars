package game;

import java.util.ArrayList;
import java.util.List;


public class MissionManager {

	private Game game;
	List<Fleet> sentMissions;
	
	public MissionManager(Game game) {
		this.game = game;
		sentMissions = new ArrayList<Fleet>();
	}
	
	public Fleet sendMission(Planet source, Planet destination, int shipNumbers){
		int distance = game.distance(source, destination);
		if (source.NumShips()>=shipNumbers && shipNumbers>0 && source.PlanetID()!=destination.PlanetID()){
			source.RemoveShips(shipNumbers);
		}
		else{
			shipNumbers = 0;
		}
		Fleet sentMission = new Fleet(1,shipNumbers,source.PlanetID(),destination.PlanetID(), distance, distance);
		sentMissions.add(sentMission);
		return sentMission;
	}
	
	public void cancelMission(Fleet fleet){
		Planet source = game.getPlanet(fleet.SourcePlanet());
		
		source.AddShips(fleet.NumShips());
		
		sentMissions.remove(fleet);
	}
	
	public void executeMissions(){
		PlanetWars pw = game.getPw();
		for (Fleet fleet : sentMissions){
			if (fleet.NumShips()>0){
				pw.IssueOrder(fleet.SourcePlanet(), fleet.DestinationPlanet(), fleet.NumShips());
			}
		}
		sentMissions.clear();
	}
}
