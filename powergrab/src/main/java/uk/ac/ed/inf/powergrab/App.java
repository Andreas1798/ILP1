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
import java.util.Random;
import java.io.FileWriter;

public class App { 
	protected static int mapLength; // the size of the current map
	protected static double latitudes[] = new double[50]; // array with the latitudes of the stations
	protected static double longitudes[] = new double[50]; // array with the longitudes of the stations
	protected static double powers[] = new double[50]; // array with power held by each station
	protected static double coins[] = new double[50]; // array with coins held by each station
	protected static String symbols[] = new String[50]; // array with the symbol of each station
	protected static Position pos; // current position
	protected static Random randomGenerator; // random variable to choose random direction in stateless
	public static ArrayList<Point> flight_coords;  // the path of the drone

	public static void parseMaps(String mapString) throws IOException {   // method for parsing the map
																		 // implemented by following the lectures
		URL mapUrl = new URL(mapString);
		HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();

		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect(); 

		InputStream input = conn.getInputStream();
		Reader reader = new InputStreamReader(input);
		String mapSource = CharStreams.toString(reader); 

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

	
	public static String AddPathToGeojson(String mapString) throws IOException {
		//this method renders again the map and it appends the LineString feature 
	   //with the drone's flight path to the feature collection of the current map
		URL mapUrl = new URL(mapString);
		HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();

		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect(); 
		InputStream input = conn.getInputStream();
		Reader reader = new InputStreamReader(input);
		String mapSource = CharStreams.toString(reader); 
		
		LineString ls = LineString.fromLngLats(flight_coords);  //we create a LineString from the list of coordinates														   
		Feature f = Feature.fromGeometry(ls);  //we then create a new feature from the LineString above
		FeatureCollection fc = FeatureCollection.fromJson(mapSource);  //we put the feature collection in a local variable
		List<Feature> list = fc.features(); //extract the list of features
		list.add(f); //add the newly created feature to the list
		String res = FeatureCollection.fromFeatures(list).toJson();  //we parse the final feature collection 
	    return res;  //and we return the result that will be written in the .geojson file as a String
	}

	public static void main(String[] args) throws IOException {

		String day = args[0];
		String month = args[1];
		String year = args[2];		//parsing the input arguments into local variables 
		Double Lat = Double.parseDouble(args[3]);
		Double Long = Double.parseDouble(args[4]);
		int seed = Integer.parseInt(args[5]);
		String drone_type = args[6];
		
		long initialTime= System.currentTimeMillis(); //we save the time when the game starts
		randomGenerator = new Random(seed);
		
		parseMaps("http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day + "/"
				+ "powergrabmap.geojson");  //render the map to be used
		
		pos = new Position(Lat, Long);	  //initialise the starting position of the drone
		if (drone_type.equals("stateless")) {
			Stateless.playGame();
		} else if (drone_type.equals("stateful")) {
			Stateful.playGame();
		} else
			System.out.println();
		
	      
		String result = AddPathToGeojson("http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day + "/"
				+ "powergrabmap.geojson"); //populate the local variable res with the final String to be written in the .geojson file
		
		try {
			FileWriter file = new FileWriter(drone_type + "-" + day + "-" +  month + "-" + year + ".geojson");
			file.write(result);		//create the .geojson file
			file.close();
		}catch(Exception Test) {
			System.out.println(Test.toString());	
		}
		
		try {
			FileWriter file = new FileWriter(drone_type + "-" + day + "-" +  month + "-" + year + ".txt");
			file.write(Drone.result_txt);   //create the .txt file
			file.close();
		}catch(Exception Test) {
			System.out.println(Test.toString());	
		}
		
		
		long stopTime=System.currentTimeMillis();  //we store the time when the execution of the game finishes 
		System.out.println(Drone.drone_coins);
		System.out.println(Drone.drone_power);
		System.out.println(Drone.nr_moves);
		System.out.println(stopTime-initialTime);	//we return the difference to check the execution time
	}

}
