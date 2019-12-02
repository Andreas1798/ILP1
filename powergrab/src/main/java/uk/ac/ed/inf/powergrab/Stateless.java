package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.HashMap;

import com.mapbox.geojson.Point;

public class Stateless extends Drone {

	protected static void playGame() {

		App.flight_coords = new ArrayList<Point>();   //initialise the path at the beginning of the game
		while (nr_moves < 250 && drone_power >= 1.25) {  //we are playing until we reach 250 moves or the drone runs out of power
			int ok = 0; 		//flag that tells the drone to move randomly if set to 0 or not if set to 1
			for (Direction d : Direction.values()){      
				Position nextpos = App.pos.nextPosition(d);    //simulate all the 16 possible moves 
				if (nextpos.inPlayArea()){
					distanceToStations = new ArrayList<Double>();   
							               //reinitialise both the arraylist and the hashmap for every simulation
					mapDistanceToStations = new HashMap<Double, Integer>();
					int k = findClosestStation(nextpos);   //search for the closest station in charging distance 
					if (k == -1){        //if none:
						continue;       //go to the next possible direction
					}
					if (App.coins[k] > 0) {    //if the closest station is positive, go towards it an connect
						if (nr_moves != 0) {
							result_txt += "\n";
						}
						drone_power -= 1.25;  //subtract the power needed for the move before 
											 //adding the value to the result string(as these are the requirements)
						result_txt = result_txt + App.pos.latitude + "," + App.pos.longitude + "," + d + ","        
								+ App.pos.nextPosition(d).latitude + "," + App.pos.nextPosition(d).longitude + ","
								+ drone_coins + "," + drone_power;
						App.pos = App.pos.nextPosition(d);    //update the position
						addToLine(App.pos);                  //put the new position in the path
						nr_moves++;
						Always_Connect();
						ok = 1;        		//set flag to 1 in order not to move randomly before simulating again
					}
				}				
			}
			while (ok == 0) {                            // go randomly
				exclude = new ArrayList<Integer>();		//reinitialise the array with directions to be excluded
				DirectionsToAvoid(exclude);			   //we populate it
				if (!exclude.isEmpty()) {			  //if it's not empty:
					int randomInt = RandomWithExclusion(App.randomGenerator, 0, 15, exclude);	//decide the random direction by excluding what's in the arraylist			
					distanceToStations = new ArrayList<Double>();
					mapDistanceToStations = new HashMap<Double, Integer>();
					if (nr_moves != 0){
						result_txt += "\n";
					}
					drone_power -= 1.25; //subtract the power needed for the move before 
					 					//adding the value to the result string(as these are the requirements)
					result_txt = result_txt + App.pos.latitude + "," + App.pos.longitude + ","
							+ Direction.values()[randomInt] + ","
							+ App.pos.nextPosition(Direction.values()[randomInt]).latitude + ","
							+ App.pos.nextPosition(Direction.values()[randomInt]).longitude + "," + drone_coins + ","
							+ drone_power;
					App.pos = App.pos.nextPosition(Direction.values()[randomInt]);
					addToLine(App.pos);
					nr_moves++;
					Always_Connect();    //check if it can connect to anything around, just in case
					ok = 1;             //change the flag in oder to get out of the "while" condition

				} else{		//if "exclude" is empty, just go randomly
					int randomInt = App.randomGenerator.nextInt(16);					
					Position nextpos = App.pos.nextPosition(Direction.values()[randomInt]);
					if (nextpos.inPlayArea()) {
						distanceToStations = new ArrayList<Double>();
						mapDistanceToStations = new HashMap<Double, Integer>();
						if (nr_moves != 0) {
							result_txt += "\n";
						}
						drone_power -= 1.25;  //subtract the power needed for the move before 
	 										 //adding the value to the result string(as these are the requirements)
						result_txt = result_txt + App.pos.latitude + "," + App.pos.longitude + ","
								+ Direction.values()[randomInt] + ","
								+ App.pos.nextPosition(Direction.values()[randomInt]).latitude + ","
								+ App.pos.nextPosition(Direction.values()[randomInt]).longitude + "," + drone_coins
								+ "," + drone_power;
						App.pos = App.pos.nextPosition(Direction.values()[randomInt]);
						addToLine(App.pos);
						nr_moves++;
						Always_Connect();	//check if it can connect to anything around, just in case
						ok = 1;
						
					}
				}
			}
		}
	}
}