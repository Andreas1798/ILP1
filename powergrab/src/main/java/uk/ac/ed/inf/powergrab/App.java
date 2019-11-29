package uk.ac.ed.inf.powergrab;

import com.google.common.io.CharStreams;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.LineString;
import com.google.gson.JsonElement;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class App {
	protected static BufferedWriter file;

	protected static int mapLength; // the size of the current map
	protected static double latitudes[] = new double[50]; // array with the latitudes of the stations
	protected static double longitudes[] = new double[50]; // array with the longitudes of the stations
	protected static double powers[] = new double[50]; // array with power held by each station
	protected static double coins[] = new double[50]; // array with coins held by each station
	protected static String symbols[] = new String[50]; // array with the symbol of each station
	protected static Position pos; // current position
	protected static Random randomGenerator; // random variable to choose random direction in stateless
	public static String path; // the path of the drone
	public static ArrayList<Point> flight_coords;

	public static void parseMaps(String mapString) throws IOException { // method for parsing the map

		URL mapUrl = new URL(mapString);
		HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();

		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect(); // starts the query

		InputStream input = conn.getInputStream();
		Reader reader = new InputStreamReader(input);
		String mapSource = CharStreams.toString(reader); // geojson content from the server

		FeatureCollection fc = FeatureCollection.fromJson(mapSource);
		List<Feature> map = fc.features();
		mapLength = map.size();
		for (int i = 0; i < mapLength; i++) {

			Point p = (Point) map.get(i).geometry();
			longitudes[i] = p.coordinates().get(0);
			latitudes[i] = p.coordinates().get(1);
			coins[i] = map.get(i).getProperty("coins").getAsFloat();
			powers[i] = map.get(i).getProperty("power").getAsFloat();
			symbols[i] = map.get(i).getProperty("marker-symbol").getAsString();
		}

	}

	public static String initializeLineString() {
		String init_string_start = "{\"type\": \"Feature\",\"properties\": { },\"geometry\": {\"type\": \"LineString\", \"coordinates\":[ ";
		String init_string_end = "]}}";
		String pos_str = pos.toString();

		return init_string_start + pos_str + init_string_end;

	}

	public static String initializeLineString2() {
		String init = "{\"TYPE\": \"LineString\",\"coordinates\": [";
		String end = "],\"properties\": {}}";
		String pos_str = pos.toString();
		return init + pos_str + end;

	}

	public static void main(String[] args) throws IOException {

		String day = args[0];
		String month = args[1];
		String year = args[2];
		Double Lat = Double.parseDouble(args[3]);
		Double Long = Double.parseDouble(args[4]);
		int seed = Integer.parseInt(args[5]);
		String drone_type = args[6];

		randomGenerator = new Random(seed);
		parseMaps("http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day + "/"
				+ "powergrabmap.geojson");
		pos = new Position(Lat, Long);
		if (drone_type.equals("stateless")) {
			Stateless.playGame();
		}
		if (drone_type.equals("stateful")) {
			Stateful.playGame();
		}
		
		//   FileWriter file = new FileWriter(""); BufferedWriter bw = new BufferedWriter(file); 
	   //   bw.write(Drone.result_txt); 
		 

		System.out.println(path);
		System.out.println(Drone.drone_coins);
		System.out.println(Drone.drone_power);
		System.out.println(Drone.nr_moves);
	}

}
