package test;

import java.util.List;

import game.Fleet;
import game.PlanetWars;

import static org.junit.Assert.*;

import org.junit.Test;

import bot.BotRunner;

public class BotRunnerTest {

	private List<Fleet> executeTurn(String world){
		PlanetWars pw = new PlanetWars(world);
		BotRunner.clear();
		BotRunner.DoTurn(pw);
		return pw.getSentFleets();
	}
	
	@Test
	public void testICanAttackNeutral() {
		String world = 
			"P 0 10 0 50 2\n"+
			"P 0 0 1 100 5\n";
		List<Fleet> fleets = executeTurn(world);
		
		assertTrue(fleets.size()>=1);
		int totalShips = 0;
		for (Fleet fleet : fleets){
			totalShips+=fleet.NumShips();
		}
		assertTrue(totalShips>=51);
		
		world = 
			"P 0 10 0 10 2\n"+
			"P 10 0 0 10 2\n"+
			"P 0 0 1 100 5\n";
		fleets = executeTurn(world);
		
		assertTrue(fleets.size()>=2);
		totalShips = 0;
		for (Fleet fleet : fleets){
			totalShips+=fleet.NumShips();
		}
		assertTrue(totalShips>=22);
		
		world = 
			"P 0 0 1 12 5\n"+
			"P 0 5 0 0 5\n"+
			"P 0 11 2 0 5\n"+
			"F 1 1 0 1 5 2\n"+//we capture
			"F 2 7 2 1 4 3\n";//we lost planet
		fleets = executeTurn(world);
		
		assertTrue(fleets.size()==1);
		assertTrue(fleets.get(0).DestinationPlanet()==1);
		assertTrue(fleets.get(0).NumShips()==12);
		
		world = 
			"P 0 0 1 10 5\n"+
			"P 0 5 0 9 5\n"+
			"P 0 11 2 12 5\n";//If the enemy waits, and then fire, I won't be able to counter, and will loose my just taken neutral planet
		fleets = executeTurn(world);
		
		assertTrue(fleets.size()==0);
		
		world = 
			"P 0 0 1 100 5\n"+
			"P 0 4 0 90 5\n"+//If the enemy waits, and then fire, I won't be able to counter, and will loose my just taken neutral planet
			"P 0 7 0 90 5\n"+
			"P 0 12 2 100 5\n";
		fleets = executeTurn(world);
		
		assertTrue(fleets.size()==0);
	}
	
	@Test
	public void testICanMakeSeveralStartMove(){
		String world = 
			"P 11.9319237634 10.6325622242 0 134 2\n"+
		"P 13.5821241085 0.0 1 100 5\n"+
		"P 10.2817234182 21.2651244485 2 100 5\n"+
		"P 1.65129362214 7.1408148547 0 84 1\n"+
		"P 22.2125539046 14.1243095938 0 84 1\n"+
		"P 11.5222143343 15.8804680258 0 2 2\n"+
		"P 12.3416331924 5.3846564227 0 2 2\n"+
		"P 10.9280072588 0.501793027955 0 31 1\n"+
		"P 12.9358402679 20.7633314205 0 31 1\n"+
		"P 8.06192476614 16.9630772207 0 77 1\n"+
		"P 15.8019227606 4.30204722775 0 77 1\n"+
		"P 23.8638475267 2.64977193829 0 35 1\n"+
		"P 0.0 18.6153525102 0 35 1\n"+
		"P 0.82828035712 4.18544947175 0 28 4\n"+
		"P 23.0355671696 17.0796749767 0 28 4\n"+
		"P 3.2623129104 10.9163653525 0 28 1\n"+
		"P 20.6015346163 10.3487590959 0 28 1\n"+
		"P 10.2327574471 12.8752540614 0 25 5\n"+
		"P 13.6310900796 8.38987038704 0 25 5\n"+
		"P 2.57887676881 16.6142256525 0 44 2\n"+
		"P 21.2849707579 4.65089879597 0 44 2\n"+
		"P 18.5878172704 9.29495324904 0 27 4\n"+
		"P 5.27603025636 11.9701711994 0 27 4\n";

		List<Fleet> fleets = executeTurn(world);
		
		assertTrue(fleets.size()>=2);
	}

	@Test
	public void testICanAttackEnemy() {
		String world = 
			"P 0 10 2 10 2\n"+
			"P 0 0 1 100 5\n";
		List<Fleet> fleets = executeTurn(world);
		
		assertTrue(fleets.size()==1);
		assertTrue(fleets.get(0).NumShips()>=31);
		
		world = 
			"P 0 0 1 12 5\n"+
			"P 0 5 2 0 5\n"+
			"P 0 11 2 0 5\n"+
			"F 1 11 0 1 5 2\n"+//we capture
			"F 2 7 2 1 4 3\n";//we lost planet
		fleets = executeTurn(world);
		
		assertTrue(fleets.size()==1);
		assertTrue(fleets.get(0).DestinationPlanet()==1);
		assertTrue(fleets.get(0).NumShips()==12);
		
	}
	
	@Test
	public void testICanDefendPlanetUnderAttack() {
		String world = 
			"P 0 0 1 100 5\n"+
			"P 0 3 1 0 1\n"+
			"P 0 10 2 100 2\n"+
			"F 2 25 2 1 7 4\n";
		List<Fleet> fleets = executeTurn(world);
		
		boolean fleetFound = false;
		
		assertTrue(fleets.size()>=1);
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==1 && fleet.NumShips()>=21){
				fleetFound = true;
			}
		}
		assertTrue(fleetFound);
		world = 
			"P 0 0 1 250 5\n"+
			"P 0 3 1 50 5\n"+
			"P 10 0 2 100 5\n"+
			"P 10 3 2 100 5\n"+
			"F 2 61 2 1 10 2\n"+//lose planet 1
			"F 2 25 2 1 10 3\n"+//31 we should counter this one
			"F 2 25 2 1 10 4\n";//61
		fleets = executeTurn(world);
		
		int totalShipsSentToDefend = 0;
		
		assertTrue(fleets.size()>=1);
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==1){
				totalShipsSentToDefend+=fleet.NumShips();
			}
		}
		assertTrue(totalShipsSentToDefend>=51);
		
		world = 
			"P 11.8039955755 11.2157212798 0 37 3\n"+
			"P 9.31956732508 21.8088737532 1 30 5\n"+
			"P 14.2884238258 0.622568806433 2 5 5\n"+
			"P 11.8654926942 5.2737846552 0 81 3\n"+
			"P 11.7424984567 17.1576579045 0 81 3\n"+
			"P 4.25409258443 0.0 2 1 1\n"+
			"P 19.3538985665 22.4314425597 1 6 1\n"+
			"P 14.7436138612 22.3240014889 0 83 3\n"+
			"P 8.86437728973 0.107441070771 0 83 3\n"+
			"P 19.8543468498 0.711933891201 0 84 1\n"+
			"P 3.75364430115 21.7195086685 0 84 1\n"+
			"P 8.86481414847 9.73662367883 2 14 5\n"+
			"P 14.7431770024 12.6948188808 1 41 5\n"+
			"P 0.0 10.8098889721 0 59 2\n"+
			"P 23.6079911509 11.6215535875 0 59 2\n"+
			"P 20.3967683707 15.5228613809 0 59 3\n"+
			"P 3.21122278016 6.90858117873 0 59 3\n"+
			"P 17.0287479269 6.65976901033 2 78 2\n"+
			"P 6.57924322402 15.7716735493 1 5 2\n"+
			"P 0.782927536597 19.6075053882 0 55 1\n"+
			"P 22.8250636143 2.82393717142 0 55 1\n"+
			"P 2.60103334076 13.172383428 1 25 3\n"+
			"P 21.0069578101 9.25905913169 2 22 3\n"+
			"F 1 2 18 12 9 1\n"+
			"F 1 11 1 12 11 3\n"+
			"F 2 1 5 11 11 4\n"+
			"F 1 2 18 12 9 2\n"+
			"F 1 8 1 12 11 4\n"+
			"F 2 5 2 17 7 1\n"+
			"F 2 1 5 11 11 5\n"+
			"F 1 2 18 12 9 3\n"+
			"F 1 8 1 12 11 5\n"+
			"F 2 5 2 17 7 2\n"+
			"F 2 1 5 11 11 6\n"+
			"F 1 2 18 12 9 4\n"+
			"F 1 8 1 12 11 6\n"+
			"F 2 5 2 17 7 3\n"+
			"F 2 1 5 11 11 7\n"+
			"F 1 2 18 12 9 5\n"+
			"F 1 8 1 12 11 7\n"+
			"F 2 17 11 18 7 4\n"+
			"F 2 5 2 17 7 4\n"+
			"F 2 1 5 11 11 8\n"+
			"F 1 2 18 12 9 6\n"+
			"F 1 8 1 12 11 8\n"+
			"F 2 5 2 17 7 5\n"+
			"F 2 1 5 11 11 9\n"+
			"F 1 3 21 18 5 3\n"+
			"F 2 5 11 18 7 6\n"+
			"F 2 5 2 17 7 6\n"+
			"F 2 1 5 11 11 10\n"+
			"F 1 1 18 12 9 8\n"+
			"F 1 2 1 21 11 10\n"+
			"F 1 1 21 18 5 4\n";
		fleets = executeTurn(world);
		
		for (Fleet fleet : fleets){
			assertTrue(fleet.SourcePlanet()!=18);
		}
		
		world = 
			"P 11.8039955755 11.2157212798 0 37 3\n"+
			"P 9.31956732508 21.8088737532 1 12 5\n"+
			"P 14.2884238258 0.622568806433 2 5 5\n"+
			"P 11.8654926942 5.2737846552 0 81 3\n"+
			"P 11.7424984567 17.1576579045 0 81 3\n"+
			"P 4.25409258443 0.0 2 1 1\n"+
			"P 19.3538985665 22.4314425597 1 16 1\n"+
			"P 14.7436138612 22.3240014889 0 83 3\n"+
			"P 8.86437728973 0.107441070771 0 83 3\n"+
			"P 19.8543468498 0.711933891201 0 84 1\n"+
			"P 3.75364430115 21.7195086685 0 84 1\n"+
			"P 8.86481414847 9.73662367883 2 21 5\n"+
			"P 14.7431770024 12.6948188808 1 340 5\n"+
			"P 0.0 10.8098889721 0 59 2\n"+
			"P 23.6079911509 11.6215535875 0 59 2\n"+
			"P 20.3967683707 15.5228613809 0 59 3\n"+
			"P 3.21122278016 6.90858117873 0 59 3\n"+
			"P 17.0287479269 6.65976901033 2 253 2\n"+
			"P 6.57924322402 15.7716735493 1 2 2\n"+
			"P 0.782927536597 19.6075053882 0 55 1\n"+
			"P 22.8250636143 2.82393717142 0 55 1\n"+
			"P 2.60103334076 13.172383428 1 3 3\n"+
			"P 21.0069578101 9.25905913169 2 97 3\n"+
			"F 2 1 5 11 11 1\n"+
			"F 2 1 5 11 11 2\n"+
			"F 2 1 5 11 11 3\n"+
			"F 2 1 5 11 11 4\n"+
			"F 2 6 11 18 7 1\n"+
			"F 2 5 2 17 7 1\n"+
			"F 2 1 5 11 11 5\n"+
			"F 1 1 1 18 7 1\n"+
			"F 2 6 11 18 7 2\n"+
			"F 2 5 2 17 7 2\n"+
			"F 2 1 5 11 11 6\n"+
			"F 1 1 1 18 7 2\n"+
			"F 2 6 11 18 7 3\n"+
			"F 2 5 2 17 7 3\n"+
			"F 2 1 5 11 11 7\n"+
			"F 1 3 21 18 5 1\n"+
			"F 1 1 1 18 7 3\n"+
			"F 2 6 11 18 7 4\n"+
			"F 2 5 2 17 7 4\n"+
			"F 2 1 5 11 11 8\n"+
			"F 1 1 18 12 9 6\n"+
			"F 1 3 21 18 5 2\n"+
			"F 1 2 1 18 7 4\n"+
			"F 2 6 11 18 7 5\n"+
			"F 2 5 2 17 7 5\n"+
			"F 2 1 5 11 11 9\n"+
			"F 1 6 18 12 9 7\n"+
			"F 1 4 1 21 11 9\n"+
			"F 1 3 21 18 5 3\n"+
			"F 2 5 2 17 7 6\n"+
			"F 2 1 5 11 11 10\n"+
			"F 1 3 21 18 5 4\n"+
			"F 1 31 1 12 11 10\n"+
			"F 1 13 1 21 11 10\n";
		fleets = executeTurn(world);
		
		int totalShipsSent = 0;
		
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==18 && fleet.TurnsRemaining()<=5)
			{
				totalShipsSent+=fleet.NumShips();
			}
			else if (fleet.SourcePlanet()==18){
				totalShipsSent-=fleet.NumShips();
			}
		}
		assertTrue(totalShipsSent>=1);
		
		world = 
			"P 11.8039955755 11.2157212798 0 37 3\n"+
			"P 9.31956732508 21.8088737532 1 5 5\n"+
			"P 14.2884238258 0.622568806433 2 5 5\n"+
			"P 11.8654926942 5.2737846552 0 81 3\n"+
			"P 11.7424984567 17.1576579045 0 81 3\n"+
			"P 4.25409258443 0.0 2 1 1\n"+
			"P 19.3538985665 22.4314425597 1 52 1\n"+
			"P 14.7436138612 22.3240014889 0 83 3\n"+
			"P 8.86437728973 0.107441070771 0 83 3\n"+
			"P 19.8543468498 0.711933891201 0 84 1\n"+
			"P 3.75364430115 21.7195086685 0 84 1\n"+
			"P 8.86481414847 9.73662367883 2 15 5\n"+
			"P 14.7431770024 12.6948188808 1 747 5\n"+
			"P 0.0 10.8098889721 0 59 2\n"+
			"P 23.6079911509 11.6215535875 0 59 2\n"+
			"P 20.3967683707 15.5228613809 0 59 3\n"+
			"P 3.21122278016 6.90858117873 0 59 3\n"+
			"P 17.0287479269 6.65976901033 2 561 2\n"+
			"P 6.57924322402 15.7716735493 1 9 2\n"+
			"P 0.782927536597 19.6075053882 0 55 1\n"+
			"P 22.8250636143 2.82393717142 0 55 1\n"+
			"P 2.60103334076 13.172383428 1 4 3\n"+
			"P 21.0069578101 9.25905913169 2 229 3\n"+
			"F 2 1 5 11 11 1\n"+
			"F 1 5 1 12 11 1\n"+
			"F 2 1 5 11 11 2\n"+
			"F 1 5 1 12 11 2\n"+
			"F 2 1 5 11 11 3\n"+
			"F 1 5 1 12 11 3\n"+
			"F 2 1 5 11 11 4\n"+
			"F 1 5 1 12 11 4\n"+
			"F 2 6 11 18 7 1\n"+
			"F 2 5 2 17 7 1\n"+
			"F 2 1 5 11 11 5\n"+
			"F 1 5 1 12 11 5\n"+
			"F 1 1 1 18 7 1\n"+
			"F 2 6 11 18 7 2\n"+
			"F 2 5 2 17 7 2\n"+
			"F 2 1 5 11 11 6\n"+
			"F 1 5 1 12 11 6\n"+
			"F 1 1 1 18 7 2\n"+
			"F 2 6 11 18 7 3\n"+
			"F 2 5 2 17 7 3\n"+
			"F 2 1 5 11 11 7\n"+
			"F 1 5 1 12 11 7\n"+
			"F 1 3 21 18 5 1\n"+
			"F 1 1 1 18 7 3\n"+
			"F 2 6 11 18 7 4\n"+
			"F 2 5 2 17 7 4\n"+
			"F 2 1 5 11 11 8\n"+
			"F 1 5 1 12 11 8\n"+
			"F 1 3 21 18 5 2\n"+
			"F 1 1 1 18 7 4\n"+
			"F 2 6 11 18 7 5\n"+
			"F 2 5 2 17 7 5\n"+
			"F 2 1 5 11 11 9\n"+
			"F 1 5 1 12 11 9\n"+
			"F 1 3 21 18 5 3\n"+
			"F 1 1 1 18 7 5\n"+
			"F 2 6 11 18 7 6\n"+
			"F 2 5 2 17 7 6\n"+
			"F 2 1 5 11 11 10\n"+
			"F 1 5 1 12 11 10\n"+
			"F 1 2 21 18 5 4\n"+
			"F 1 1 1 18 7 6\n"+
			"F 1 1 6 18 15 14\n";
		fleets = executeTurn(world);
		
		totalShipsSent = 0;
		
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==18)
			{
				totalShipsSent+=fleet.NumShips();
			}
			else if (fleet.SourcePlanet()==18){
				totalShipsSent-=fleet.NumShips();
			}
		}
		assertTrue(totalShipsSent>=4);	
		
		world = "P 14.0 14.0 0 35 5\n"+
		"P 7.29519938179 8.10358689939 1 140 5\n"+//1
		"P 5.55053644449 11.1140678443 2 144 5\n"+//2
		"P 4.87506262764 8.71182833245 0 86 4\n"+
		"P 7.18126819728 10.0483428153 0 68 3\n"+
		"P 8.65805841891 13.0225909439 2 3 3\n"+//5
		"P 10.4960999981 9.85098167083 1 8 3\n"+//6
		"P 17.7957679755 17.8574219741 0 79 5\n"+
		"P 19.2340445917 15.3756220494 0 79 5\n"+
		"P 17.8246820125 18.8736959403 0 84 3\n"+
		"P 20.1301936879 14.8954495875 0 84 3\n"+
		"P 20.1921296053 8.49605883802 0 20 2\n"+
		"P 12.3030191324 22.1090130903 0 20 2\n"+
		"P 5.06977939393 15.6989081726 0 31 2\n"+
		"P 11.0342359001 5.40701609708 0 31 2\n"+
		"P 9.20751809479 9.43458042632 0 35 3\n"+
		"P 7.65612343097 12.1115697597 0 35 3\n"+
		"P 5.72882727109 13.7110432574 0 27 5\n"+
		"P 9.63711554158 6.9671461331 0 27 5\n"+
		"F 1 21 6 11 10 8\n"+
		"F 2 18 5 6 4 3\n"+
		"F 2 4 5 2 4 3";
		
		fleets = executeTurn(world);
		
		totalShipsSent = 0;
		
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==6)
			{
				totalShipsSent+=fleet.NumShips();
			}
		}
		assertTrue(totalShipsSent>=5);	
	}
	
	@Test
	public void testICanStealthNeutralPlanetUnderAttack() {
		String world = 
			"P 0 3 1 100 5\n"+
			"P 0 0 0 24 2\n"+
			"P 0 10 2 100 2\n"+
			"F 2 25 2 1 10 2\n";
		List<Fleet> fleets = executeTurn(world);
		
		assertTrue(fleets.size()==1);
		Fleet fleet = fleets.get(0);
		assertTrue(fleet.NumShips()>=4);
		assertEquals(fleet.DestinationPlanet(), 1);
		
		//real case...
		world = 
			"P 0 0 1 87 5\n"+
			"P 0 4 0 50 5\n"+
			"P 0 8 2 59 5\n"+
			"F 2 51 2 1 4 3\n";
		fleets = executeTurn(world);
		
		assertTrue(fleets.size()>=1);
	}
	
	@Test
	public void testICanMakeReinforcementMoves() {
		String world = 
			"P 0 0 1 50 5\n"+
			"P 0 3 1 50 2\n"+
			"P 0 10 2 100 5\n";
		List<Fleet> fleets = executeTurn(world);
		
		assertTrue(fleets.size()==1);
		Fleet fleet = fleets.get(0);
		assertEquals(fleet.NumShips(), 50);
		assertEquals(fleet.DestinationPlanet(), 1);
	}
	
	@Test
	public void testICanMakeReinforcementMovesOnFuturPositions() {
		String world = 
			"P 0 0 1 50 5\n"+
			"P 0 3 0 10 2\n"+
			"P 0 10 2 0 5\n";
		List<Fleet> fleets = executeTurn(world);
		
		assertTrue(fleets.size()>=1);
		int totalShips = 0;
		for (Fleet fleet : fleets){
			assertEquals(fleet.DestinationPlanet(), 1);
			totalShips+=fleet.NumShips();
		}
		assertEquals(totalShips, 50);
	}
	
	@Test
	public void testICanPlanMultipleSourceAttacks() {
		//Equal distance
		String world = 
			"P 0 0 1 50 5\n"+
			"P 0 5 0 60 2\n"+
			"P 0 10 1 50 5\n";
		List<Fleet> fleets = executeTurn(world);
		int totalShips = 0;
		assertTrue(fleets.size()==2);
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==1){
				totalShips+=fleet.NumShips();
			}
		}
		assertEquals(totalShips, 61);
		//different distances
		world = 
			"P 0 0 1 50 5\n"+
			"P 0 6 0 60 2\n"+
			"P 0 10 1 50 5\n";
		fleets = executeTurn(world);
		totalShips = 0;
		assertTrue(fleets.size()==2);
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==1){
				totalShips+=fleet.NumShips();
			}
		}
		assertEquals(totalShips, 61);
	}
	
	@Test
	public void testICanKeepShipsOfPlanetUnderAttack() {
		String world = 
			"P 0 0 1 50 5\n"+
			"P 0 2 0 10 2\n"+
			"P 0 10 2 50 5\n"+
			"F 2 50 2 0 10 1\n"+
			"F 2 5 2 0 10 2\n";
		List<Fleet> fleets = executeTurn(world);
		assertTrue(fleets.size()==0);
		
	}
	
	@Test
	public void testICanNotPutInDangerAPlanetForNeutralAttack() {
		String world = 
			"P 14.0 14.0 0 35 5\n"+
			"P 7.29519938179 8.10358689939 1 130 5\n"+
			"P 5.55053644449 11.1140678443 2 134 5\n"+
			"P 4.87506262764 8.71182833245 0 86 4\n"+
			"P 7.18126819728 10.0483428153 0 68 3\n"+
			"P 8.65805841891 13.0225909439 2 19 3\n"+
			"P 10.4960999981 9.85098167083 1 23 3\n"+
			"P 17.7957679755 17.8574219741 0 79 5\n"+
			"P 19.2340445917 15.3756220494 0 79 5\n"+
			"P 17.8246820125 18.8736959403 0 84 3\n"+
			"P 20.1301936879 14.8954495875 0 84 3\n"+
			"P 20.1921296053 8.49605883802 0 20 2\n"+
			"P 12.3030191324 22.1090130903 0 20 2\n"+
			"P 5.06977939393 15.6989081726 0 31 2\n"+
			"P 11.0342359001 5.40701609708 0 31 2\n"+
			"P 9.20751809479 9.43458042632 0 35 3\n"+
			"P 7.65612343097 12.1115697597 0 35 3\n"+
			"P 5.72882727109 13.7110432574 0 27 5\n"+
			"P 9.63711554158 6.9671461331 0 27 5";
		List<Fleet> fleets = executeTurn(world);
		int shipsFromPlanet6 = 0;
		
		for (Fleet fleet : fleets){
			if (fleet.SourcePlanet()==6){
				shipsFromPlanet6+=fleet.NumShips();
			}
		}
		
		assertTrue(shipsFromPlanet6<17);
	}
	
	@Test
	public void testICanWaitAFewTurnsForBetterSolutions() {
		String world = 
			"P 0 0 1 50 5\n"+
			"P 0 1 0 30 1\n"+//We could take this one
			"P 1 0 0 50 5\n";//But clearly this one is more interesting if we wait only one turn doing nothing
		List<Fleet> fleets = executeTurn(world);
		assertTrue(fleets.size()==0);
	}
	
	@Test
	public void testAttacksOnEnemyTakeThePlanet() {
		String world =//turn 7 is the key			
			"P 0 0 1 96 5\n"+//fully available+10
			"P 0 5 2 10 5\n"+//target(35)-10
			"P 0 8 2 10 5\n"+//Potential reinforcements(30)
			"P 0 12 2 10 5\n"+//Potential reinforcements(10)
			"F 2 10 3 1 7 5\n"+//Incoming fleet that will count(10)
			"F 2 10 3 1 7 6\n"+//Incoming fleet that will count for future(10)
			"F 2 10 3 2 7 3\n"+//Incoming fleet that will count transfered from middle planet(10)
			"F 2 10 3 2 7 2\n"+//Incoming fleet that will count transfered from middle planet(10)
			"";
		List<Fleet> fleets = executeTurn(world);
		assertTrue(fleets.size()>=1);
		int totalShips = 0;
		for (Fleet fleet : fleets){
			if (fleet.DestinationPlanet()==1){
				totalShips+=fleet.NumShips();
			}
		}
		assertTrue(totalShips>=96);
	}
	
	@Test
	public void testICanEvaluateEnemyStrenght() {
		//If the enemy attacks, we'll can't counter if we took the neutral
		String world = 
			"P 0 0 0 50 5\n"+
			"P 0 5 1 100 5\n"+
			"P 0 10 2 100 5\n";
		List<Fleet> fleets = executeTurn(world);
		assertTrue(fleets.size()==0);
		world = 
			"P 0 0 0 50 5\n"+
			"P 0 10 1 55 5\n"+
			"P 1 10 1 50 5\n"+
			"P 0 20 2 150 5\n";
		fleets = executeTurn(world);
		for (Fleet fleet : fleets){
			assertTrue(fleet.DestinationPlanet()!=0);
		}
		//Inverse previous cases
		//If we attack, and the enemy defends, we won't take the planet
		world = 
			"P 0 0 0 50 5\n"+
			"P 0 10 2 0 5\n"+
			"P 1 10 2 0 5\n"+
			"P 0 20 1 95 5\n";
		fleets = executeTurn(world);
		assertEquals(fleets.size(), 0);
		//If we attacks, with our full ships, enemy won't be able to counter
		world = 
			"P 0 0 0 50 5\n"+
			"P 0 10 2 0 5\n"+
			"P 1 10 2 0 5\n"+
			"P 0 20 1 96 5\n";
		fleets = executeTurn(world);
		assertTrue(fleets.size()>=1);
	}
	
	@Test
	public void testPerformancesAreFine(){
		String world = "P 11.8039955755 11.2157212798 1 8 3\n"+
		"P 9.31956732508 21.8088737532 1 5 5\n"+
		"P 14.2884238258 0.622568806433 2 5 5\n"+
		"P 11.8654926942 5.2737846552 0 81 3\n"+
		"P 11.7424984567 17.1576579045 0 81 3\n"+
		"P 4.25409258443 0.0 2 19 1\n"+
		"P 19.3538985665 22.4314425597 1 1 1\n"+
		"P 14.7436138612 22.3240014889 0 83 3\n"+
		"P 8.86437728973 0.107441070771 0 83 3\n"+
		"P 19.8543468498 0.711933891201 0 84 1\n"+
		"P 3.75364430115 21.7195086685 0 84 1\n"+
		"P 8.86481414847 9.73662367883 1 123 5\n"+
		"P 14.7431770024 12.6948188808 1 51 5\n"+
		"P 0.0 10.8098889721 0 59 2\n"+
		"P 23.6079911509 11.6215535875 0 59 2\n"+
		"P 20.3967683707 15.5228613809 0 59 3\n"+
		"P 3.21122278016 6.90858117873 0 59 3\n"+
		"P 17.0287479269 6.65976901033 2 47 2\n"+
		"P 6.57924322402 15.7716735493 1 2 2\n"+
		"P 0.782927536597 19.6075053882 0 55 1\n"+
		"P 22.8250636143 2.82393717142 0 55 1\n"+
		"P 2.60103334076 13.172383428 1 3 3\n"+
		"P 21.0069578101 9.25905913169 2 6 3\n"+
		"F 1 1 6 17 16 2\n"+
		"F 1 5 1 0 11 1\n"+
		"F 1 1 6 12 11 1\n"+
		"F 1 5 1 0 11 2\n"+
		"F 1 1 6 12 11 2\n"+
		"F 1 5 1 0 11 3\n"+
		"F 1 1 6 12 11 3\n"+
		"F 1 50 11 17 9 2\n"+
		"F 1 5 1 11 13 6\n"+
		"F 1 1 6 17 16 9\n"+
		"F 1 3 21 11 8 1\n"+
		"F 2 59 22 12 8 2\n"+
		"F 2 5 2 5 11 5\n"+
		"F 2 4 17 12 7 1\n"+
		"F 2 17 22 12 8 2\n"+
		"F 1 5 1 0 11 5\n"+
		"F 1 1 6 12 11 5\n"+
		"F 1 2 18 0 7 1\n"+
		"F 1 3 21 11 8 2\n"+
		"F 2 5 2 5 11 6\n"+
		"F 2 3 22 12 8 3\n"+
		"F 1 7 11 12 7 2\n"+
		"F 1 5 1 0 11 6\n"+
		"F 1 1 6 12 11 6\n"+
		"F 1 2 18 0 7 2\n"+
		"F 1 3 21 11 8 3\n"+
		"F 2 5 2 17 7 3\n"+
		"F 1 3 12 17 7 3\n"+
		"F 1 5 1 0 11 7\n"+
		"F 1 2 18 0 7 3\n"+
		"F 1 3 21 11 8 4\n"+
		"F 2 5 2 5 11 8\n"+
		"F 2 6 22 12 8 5\n"+
		"F 1 7 12 17 7 4\n"+
		"F 1 22 11 12 7 4\n"+
		"F 1 5 1 0 11 8\n"+
		"F 1 2 18 0 7 4\n"+
		"F 1 3 21 11 8 5\n"+
		"F 2 1 22 17 5 3\n"+
		"F 2 5 2 5 11 9\n"+
		"F 2 2 22 12 8 6\n"+
		"F 1 10 0 12 4 2\n"+
		"F 1 5 1 11 13 11\n"+
		"F 1 3 6 17 16 14\n"+
		"F 1 2 18 11 7 5\n"+
		"F 1 3 21 11 8 6\n"+
		"F 2 5 2 17 7 6\n"+
		"F 1 5 0 17 7 6\n"+
		"F 1 5 0 11 4 3\n"+
		"F 1 5 1 11 13 12\n"+
		"F 1 1 6 17 16 15\n"+
		"F 1 2 18 11 7 6\n"+
		"F 1 3 21 11 8 7";
		
		long start = System.currentTimeMillis();
		executeTurn(world);
		long elapsedTimeMillis1 = System.currentTimeMillis()-start;
		
		

		world = "P 11.3048151324 10.7906636779 0 128 2\n"+
		"P 0.926306710455 21.4951608387 1 5 5\n"+
		"P 21.6833235543 0.0861665171257 2 5 5\n"+
		"P 3.53503048131 2.05360030237 1 3 3\n"+
		"P 19.0745997835 19.5277270534 0 29 3\n"+
		"P 2.98054245891 5.63993068307 1 2 2\n"+
		"P 19.6290878059 15.9413966727 2 8 2\n"+
		"P 6.14783122991 0.0 1 3 3\n"+
		"P 16.4617990349 21.5813273558 1 404 3\n"+
		"P 1.60219954522 1.15184580925 0 72 2\n"+
		"P 21.0074307196 20.4294815465 0 84 2\n"+
		"P 18.4899711777 5.70675699716 2 233 3\n"+
		"P 4.1196590871 15.8745703586 1 3 3\n"+
		"P 1.66296447686 8.24043662284 1 4 4\n"+
		"P 20.9466657879 13.340890733 2 4 4\n"+
		"P 2.10161565086 3.35617650676 1 1 1\n"+
		"P 20.5080146139 18.225150849 0 26 1\n"+
		"P 0.0 4.82283856102 1 2 2\n"+
		"P 22.6096302648 16.7584887948 2 2 2\n"+
		"P 4.80817080248 8.35581237424 1 8 4\n"+
		"P 17.8014594623 13.2255149816 2 424 4\n"+
		"P 8.30237642052 16.1464692904 1 139 3\n"+
		"P 14.3072538443 5.43485806543 0 61 3\n"+
		"F 1 5 1 21 10 1\n"+
		"F 1 4 19 21 9 1\n"+
		"F 1 5 1 21 10 2\n"+
		"F 1 1 19 21 9 2\n"+
		"F 1 5 1 21 10 3\n"+
		"F 2 5 2 11 7 1\n"+
		"F 1 5 1 21 10 4\n"+
		"F 2 5 2 11 7 2\n"+
		"F 1 8 15 7 6 1\n"+
		"F 1 1 5 7 7 2\n"+
		"F 1 2 17 7 8 3\n"+
		"F 1 28 19 7 9 4\n"+
		"F 1 5 1 21 10 5\n"+
		"F 2 5 2 11 7 3\n"+
		"F 1 3 12 21 5 1\n"+
		"F 1 18 19 21 9 5\n"+
		"F 1 5 1 21 10 6\n"+
		"F 2 5 2 11 7 4\n"+
		"F 2 2 18 6 4 1\n"+
		"F 1 4 13 9 8 5\n"+
		"F 1 2 5 9 5 2\n"+
		"F 1 2 17 9 5 2\n"+
		"F 1 65 19 9 8 5\n"+
		"F 1 5 1 21 10 7\n"+
		"F 1 3 12 21 5 2\n"+
		"F 2 27 6 16 3 1\n"+
		"F 2 5 2 11 7 5\n"+
		"F 2 386 6 16 3 1\n"+
		"F 2 4 14 20 4 2\n"+
		"F 2 2 18 16 3 1\n"+
		"F 1 2 5 19 4 2\n"+
		"F 1 4 13 19 4 2\n"+
		"F 1 1 15 19 6 4\n"+
		"F 1 2 17 19 6 4\n"+
		"F 1 3 3 19 7 5\n"+
		"F 1 1 12 19 8 6\n"+
		"F 1 5 1 21 10 8\n"+
		"F 1 2 12 21 5 3\n"+
		"F 2 5 2 11 7 6\n"+
		"F 2 8 6 16 3 2\n"+
		"F 2 4 14 20 4 3\n"+
		"F 2 2 18 16 3 2\n"+
		"F 1 285 7 11 14 13\n"+
		"F 1 265 19 11 14 13\n"+
		"F 1 5 1 21 10 9\n"+
		"F 1 3 3 19 7 6\n"+
		"F 1 2 5 19 4 3\n"+
		"F 1 3 12 21 5 4\n"+
		"F 1 4 13 19 4 3\n"+
		"F 1 5 15 19 6 5\n"+
		"F 1 2 17 19 6 5\n";
		
		start = System.currentTimeMillis();
		executeTurn(world);
		long elapsedTimeMillis2 = System.currentTimeMillis()-start;
		
		assertTrue(elapsedTimeMillis1<200);
		assertTrue(elapsedTimeMillis2<200);
	}
}
