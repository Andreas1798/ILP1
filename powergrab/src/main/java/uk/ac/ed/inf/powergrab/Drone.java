package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class Drone {

	protected static double grab = 0.00025; // charging distance
	protected static double drone_power = 250.0;
	protected static int nr_moves = 0;
	protected static double drone_coins = 0;
	protected static String result_txt = ""; // string with all the information to be written in the .txt file
	protected static ArrayList<Double> distanceToStations; // arraylist used to to find the closest station to the drone
															// if more than one are in charging distance
	protected static HashMap<Double, Integer> mapDistanceToStations; // hashmap to map the distances with the indexes of
																		// the stations
	protected static ArrayList<Integer> exclude; // arraylist with indexes of directions to be avoided for random movement

	protected static void playGame() { // method to be extended by subclasses Stateless and Stateful

	}

	protected static int findClosestStation(Position position){ // method to find the closest station in charging
																	// distance to the position given by parameter

		for (int i = 0; i < App.mapLength; i++) {
			if ((position.latitude - App.latitudes[i]) * (position.latitude - App.latitudes[i])
					+ (position.longitude - App.longitudes[i]) * (position.longitude - App.longitudes[i]) <= grab
							* grab) {
				distanceToStations.add((position.latitude - App.latitudes[i]) * (position.latitude - App.latitudes[i])
						+ (position.longitude - App.longitudes[i]) * (position.longitude - App.longitudes[i]));

				mapDistanceToStations.put(
						(position.latitude - App.latitudes[i]) * (position.latitude - App.latitudes[i])
								+ (position.longitude - App.longitudes[i]) * (position.longitude - App.longitudes[i]),i);

			}
		}
		if (!distanceToStations.isEmpty()) {
			Collections.sort(distanceToStations);
			return mapDistanceToStations.get(distanceToStations.get(0));
		}
		return -1; // returns -1 if there are no stations in charging distance
	}

	public static void Connect(int i) { // method that connects the drone to a positive station
		drone_coins += App.coins[i];
		drone_power += App.powers[i];
		App.coins[i] = 0;
		App.powers[i] = 0;
		App.symbols[i] = "danger";
	}

	protected static void Always_Connect() { // method that searches if it can connect to any type of station if in
												// charging distance
		int ok2 = 0; // flag variable that decides if we have to connect to any stations or not
		int i = findClosestStation(App.pos);
		if (i == -1) // if there are no stations in charging distance:
			ok2 = 1; // change the flag
		while (ok2 == 0) {
			if (App.coins[i] > 0) {
				drone_coins += App.coins[i];
				drone_power += App.powers[i];
				App.coins[i] = 0;
				App.powers[i] = 0;
				App.symbols[i] = "danger";

			} else if (Math.abs(App.coins[i]) > drone_coins) {
				App.coins[i] += drone_coins;
				drone_coins = 0;
				if (Math.abs(App.powers[i]) > drone_power) {
					App.powers[i] += drone_power;
					drone_power = 0;
					break;
				} else {
					drone_power += App.powers[i];
					App.powers[i] = 0;
				}

			} else if (Math.abs(App.coins[i]) <= drone_coins) {
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
		int random = start + rand.nextInt(end - start +1  - exclusion.size());
		for (int x : exclusion){
			if (random < x)
				break;
			random++;
		}
		return random;
	}

	@SuppressWarnings("static-access")
	static void addToLine(Position new_point) {	   //method that adds to the LineString the position we move to 

		//App.flight_coords.add(Point.fromLngLat(new_point.longitude, new_point.latitude));
		
		
	/*	LineString temp = LineString.fromJson(App.path);
		ArrayList<Point> list = (ArrayList<Point>) temp.coordinates();
		Point new_p = Point.fromLngLat(new_point.longitude, new_point.latitude);
		list.add(new_p);
		LineString s_line = LineString.fromJson(App.path);
		App.path = s_line.fromLngLats(list).toJson();
		*/

	}

}
