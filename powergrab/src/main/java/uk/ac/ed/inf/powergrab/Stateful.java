package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.mapbox.geojson.Point;

public class Stateful extends Drone {
	private static ArrayList<Double> PositiveStations;      //arraylist with distances from drone position to every positive station 
	private static HashMap<Double, Integer> MapPositives;  //hashmap that maps the distances above with the indexes of the stations
	private static ArrayList<Double> DirectionDistance;	  //arraylist with distances from every simulation to the closest positive station
	private static HashMap<Double, Direction> MapDirection;  //hashmap that maps the distances above with its respective direction
	public static HashMap<String, Integer> Visited;			//hashmap with keys=positions as strings, and values=number of times we visited that position 

	protected static void playGame() {

		App.flight_coords = new ArrayList<Point>();
		Visited = new HashMap<String, Integer>();	//initialise the Visited hashmap and add the start position 
		Visited.put(App.pos.toString(), 1);

		while (nr_moves < 250 && drone_power >= 1.25) {  //we are playing until we reach 250 moves or the drone runs out of power

			int ClosestIndex = searchPositives();	//at every step we find the index of the closest positive station
			if (ClosestIndex == -1) {		//if there are no positive stations left:
				RandomToFinish();		   //execute this method until the end of the game
				break;					  //break out of the "while" condition 	
			}

			Direction d = DecideDirection(ClosestIndex);	//find the best direction to go towards the closest positive station
			if (nr_moves != 0) {
				result_txt += "\n";
			}
			drone_power -= 1.25;   //subtract the power needed for the move before 
							 	  //adding the value to the result string(as these are the requirements)

			result_txt = result_txt + App.pos.latitude + "," + App.pos.longitude + "," + d + ","
					+ App.pos.nextPosition(d).latitude + "," + App.pos.nextPosition(d).longitude + "," + drone_coins
					+ "," + drone_power;
			distanceToStations = new ArrayList<Double>();
			mapDistanceToStations = new HashMap<Double, Integer>(); 
			App.pos = App.pos.nextPosition(d);
			Always_Connect();
			if (Visited.containsKey(App.pos.toString())) {    //if the position we moved to was already visited:
				int count = Visited.get(App.pos.toString()); 	
				count++;									//increase its count in the hashmap
				Visited.put(App.pos.toString(), count);		
			} else {
				Visited.put(App.pos.toString(), 1);	//if it's a new position, put it in the hashmap with count 1;
			}
			addToLine(App.pos);
			nr_moves++;
		}

	}

	private static int searchPositives(){	//method that returns the index of the closest positive station
										   //if none, return -1 and so we know there are no positive stations left
		PositiveStations = new ArrayList<Double>();
		MapPositives = new HashMap<Double, Integer>();
		for (int i = 0; i < App.mapLength; i++) {
			if (App.coins[i] > 0) {
				PositiveStations.add((App.pos.latitude - App.latitudes[i]) * (App.pos.latitude - App.latitudes[i])
						+ (App.pos.longitude - App.longitudes[i]) * (App.pos.longitude - App.longitudes[i]));
				
				MapPositives.put((App.pos.latitude - App.latitudes[i]) * (App.pos.latitude - App.latitudes[i])
						+ (App.pos.longitude - App.longitudes[i]) * (App.pos.longitude - App.longitudes[i]), i);
			}
		}
		if (PositiveStations.isEmpty())
			return -1; 
		Collections.sort(PositiveStations);
		return MapPositives.get(PositiveStations.get(0));
	}

	   //this method is used by the DecideDirection method; 
	private static boolean AvoidNegatives(Position nextpos, int ClosestIndex) {
		if (Visited.containsKey(nextpos.toString()))
			if (Visited.get(nextpos.toString()) > 1)
				return false;		//it returns false if we have visited a position already twice, 
								   //and so we don't want to go visit it again(avoid loops)
		for (int i = 0; i < App.mapLength; i++) {
			if (App.coins[i] < 0)   //if there are negative stations on our way to a positive one:
				if ((nextpos.latitude - App.latitudes[i]) * (nextpos.latitude - App.latitudes[i])
						+ (nextpos.longitude - App.longitudes[i]) * (nextpos.longitude - App.longitudes[i]) <= grab
								* grab){
					if ((App.latitudes[ClosestIndex] - App.latitudes[i])
							* (App.latitudes[ClosestIndex] - App.latitudes[i])
							+ (App.longitudes[ClosestIndex] - App.longitudes[i])
									* (App.longitudes[ClosestIndex] - App.longitudes[i]) <= grab * grab)

						return true;    //we set the threshold for avoiding red stations like this:
							//we don't avoid the red station only if its range have common points with 
					       //the green station's range we are going towards
					
					return false;	//otherwise we return false(so we avoid it)

				}
		}

		return true;	//we return true if there are no negative stations in any of the 16 directions (without the condition above) 
	}

	private static Direction DecideDirection(int ClosestIndex) {  //method that returns the best direction to go next
		DirectionDistance = new ArrayList<Double>();
		MapDirection = new HashMap<Double, Direction>();
		for (Direction d : Direction.values()) {
			Position nextpos = App.pos.nextPosition(d);
			if (nextpos.inPlayArea() && AvoidNegatives(nextpos, ClosestIndex)) {  //we add the direction to the arraylist
															//only if it is in play area and it respects the conditions in AvoidNegatives method
				DirectionDistance.add((nextpos.latitude - App.latitudes[ClosestIndex])
						* (nextpos.latitude - App.latitudes[ClosestIndex])
						+ (nextpos.longitude - App.longitudes[ClosestIndex])
								* (nextpos.longitude - App.longitudes[ClosestIndex]));
				MapDirection.put((nextpos.latitude - App.latitudes[ClosestIndex])
						* (nextpos.latitude - App.latitudes[ClosestIndex])
						+ (nextpos.longitude - App.longitudes[ClosestIndex])
								* (nextpos.longitude - App.longitudes[ClosestIndex]),d);
			}
		}
		Collections.sort(DirectionDistance);	//we sort the distances
		return MapDirection.get(DirectionDistance.get(0));  //and we return the direction that gets us closest 
														   //to the positive station we are going towards
	} 
	
    //this method is used when there are no positive stations left and
	//it tells the drone to move randomly by avoiding negative stations until 250 moves are reached.
	//It behaves similar to the random movement of the stateless drone
	private static void RandomToFinish() {  
		while (nr_moves < 250 && drone_power >= 1.25) {
			exclude = new ArrayList<Integer>();
			DirectionsToAvoid(exclude);
			if (!exclude.isEmpty()) {
				int randomInt = RandomWithExclusion(App.randomGenerator, 0, 15, exclude);
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
						+ App.pos.nextPosition(Direction.values()[randomInt]).longitude + "," + drone_coins + ","
						+ drone_power;
				App.pos = App.pos.nextPosition(Direction.values()[randomInt]);  //update the position of the drone
				addToLine(App.pos);
				nr_moves++;

			} else {

				int randomInt = App.randomGenerator.nextInt(16);	//generate a random index between 0 and 15(inclusive)
				Position nextpos = App.pos.nextPosition(Direction.values()[randomInt]);	  //move randomly 
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
							+ App.pos.nextPosition(Direction.values()[randomInt]).longitude + "," + drone_coins + ","
							+ drone_power;
					App.pos = App.pos.nextPosition(Direction.values()[randomInt]);
					addToLine(App.pos);
					nr_moves++;
				}
			}

		}

	}

}
