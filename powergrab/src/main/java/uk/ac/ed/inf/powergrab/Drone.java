package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import com.mapbox.geojson.Point;

public class Drone {
	protected static double radius = 0.0003; //move distance(degrees)
	protected static double grab = 0.00025; // charging distance
	protected static double drone_power = 250.0;  //the initial amount of power held by the drone
	protected static int nr_moves = 0;
	protected static double drone_coins = 0;	
	protected static String result_txt = ""; // string with all the information to be written in the .txt files
	protected static ArrayList<Double> distanceToStations; // arraylist used to to find the closest station to the drone
															// if more than one are in charging distance
	protected static HashMap<Double, Integer> mapDistanceToStations; // hashmap to map the distances with the indexes of
																		// the stations
	protected static ArrayList<Integer> exclude; // arraylist with indexes of directions to be avoided for random movement

	protected static void playGame() {  //method to be extended by subclasses Stateless and Stateful

	}

	protected static int findClosestStation(Position position){ // method to find the closest station in charging
																	// distance to the position given by parameter

		for (int i = 0; i < App.mapLength; i++) {
			if ((position.latitude - App.latitudes[i]) * (position.latitude - App.latitudes[i])
					+ (position.longitude - App.longitudes[i]) * (position.longitude - App.longitudes[i]) <= grab
							* grab) {
				distanceToStations.add((position.latitude - App.latitudes[i]) * (position.latitude - App.latitudes[i])
						+ (position.longitude - App.longitudes[i]) * (position.longitude - App.longitudes[i]));

				mapDistanceToStations.put((position.latitude - App.latitudes[i]) * (position.latitude - App.latitudes[i])
								+ (position.longitude - App.longitudes[i]) * (position.longitude - App.longitudes[i]), i);
			}
		}
		if (!distanceToStations.isEmpty()) {
			Collections.sort(distanceToStations);	//sort the distances
			return mapDistanceToStations.get(distanceToStations.get(0)); //return the index of the closest station
		}
		return -1; // returns -1 if there are no stations in charging distance
	}


	protected static void Always_Connect() {   // method that searches if it can connect to any 
											  //type of station if in charging distance
		int ok2 = 0; // flag variable that decides if we have to connect to any stations or not
		int i = findClosestStation(App.pos);
		if (i == -1) // if there are no stations in charging distance:
			ok2 = 1; // change the flag
		while (ok2 == 0) {
			if (App.coins[i] > 0) {      //if the station is positive
				drone_coins += App.coins[i];	//take its coins
				drone_power += App.powers[i];	//and its power
				App.coins[i] = 0;		//and empty the station
				App.powers[i] = 0;

			} else if (Math.abs(App.coins[i]) > drone_coins) {  //if the station is negative and it has
															   //more coins to be subtracted than the drone's amount of coins
				App.coins[i] += drone_coins;		//add all drone's coins to the station
				drone_coins = 0;				  //now the drone has 0 coins
						//for the case above there are 2 other sub-cases:
				if (Math.abs(App.powers[i]) > drone_power) {	//if the station has a greater negative amount of power than drone's power
					App.powers[i] += drone_power;
					drone_power = 0;
					break;
				} else {					//or the drone has more power than the amount to be subtracted
					drone_power += App.powers[i];
					App.powers[i] = 0;
				}

			} else if (Math.abs(App.coins[i]) <= drone_coins) { //same for the second case where the amount of negative coins from the station(its module) 
												//is smaller than the drone's coins, and here we also have the 2 sub-cases for power
				drone_coins += App.coins[i];
				App.coins[i] = 0;
				if (Math.abs(App.powers[i]) > drone_power) {
					App.powers[i] += drone_power;
					drone_power = 0;
					break;
				} else {
					drone_power += App.powers[i];
					App.powers[i] = 0;
				}
			}
			ok2 = 1; // after we connected to the closest station, change to flag in order not to
						// connect to any other stations in range before moving to another position
		}

	}

	protected static void DirectionsToAvoid(ArrayList<Integer> exclude) { // method that populates the "exclude" array with directions
																			// that gets the drone either outside the
																			// map or in the range of a negative station
		for (Direction d : Direction.values()) {
			Position nextpos = App.pos.nextPosition(d);
			if (!nextpos.inPlayArea())
				if(!exclude.contains(Arrays.asList(Direction.values()).indexOf(d)))	//make sure we don't have duplicates 
					
				exclude.add(Arrays.asList(Direction.values()).indexOf(d));
			
			for (int i = 0; i < App.mapLength; i++)
				if (App.coins[i] < 0 && (nextpos.latitude - App.latitudes[i]) * (nextpos.latitude - App.latitudes[i])
						+ (nextpos.longitude - App.longitudes[i]) * (nextpos.longitude - App.longitudes[i]) <= grab * grab)
					if(!exclude.contains(Arrays.asList(Direction.values()).indexOf(d)))	//make sure we don't have duplicates 
						
						exclude.add(Arrays.asList(Direction.values()).indexOf(d));
		}
	}

	//method to return a random number between 0 and 15 excluding the indexes in the "exclude" arraylist(if any)
	protected static int RandomWithExclusion(Random rand, int start, int end, ArrayList<Integer> exclusion) {
		int random = start + rand.nextInt(end - start +1 - exclusion.size());
		for (int x : exclusion){
			if (random < x)
				break;
			random++;
		}
		return random;
	}

	static void addToLine(Position new_point) {	   //method that adds to the arraylist of points the position we move to 

		App.flight_coords.add(Point.fromLngLat(new_point.longitude, new_point.latitude));

	}

}
