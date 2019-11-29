package uk.ac.ed.inf.powergrab;

import java.util.Arrays;

public class Position {
	public double latitude;
	public double longitude;
	
	public Position(double latitude, double longitude) {
		this.latitude=latitude;
		this.longitude=longitude;
		
	}
	
	public Position nextPosition(Direction direction) {
		double r = 0.0003;
		int a = Arrays.asList(direction.values()).indexOf(direction);
		
		double angle = a*22.5;
		double nextLat = latitude + Math.cos(Math.toRadians(angle))*r;
		double nextLon = longitude + Math.sin(Math.toRadians(angle))*r;
		
		Position newPos = new Position(nextLat, nextLon);
		return newPos;

	}
	

	public boolean inPlayArea() {
		if(this.latitude < 55.946233 && this.latitude > 55.942617 && this.longitude < -3.184319 && 
				this.longitude > -3.192473 ) {
			return true;
		}
		return false;
	}
	public String toString()
	{
		return "[" + longitude + "," + latitude + "]";
	}
	

}