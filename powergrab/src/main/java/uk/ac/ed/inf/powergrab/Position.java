package uk.ac.ed.inf.powergrab;

import java.util.Arrays;

public class Position{
	public double latitude;
	public double longitude;
	public int a;
	public Position(double latitude, double longitude) {
		this.latitude=latitude;
		this.longitude=longitude;
		
	}
	
	public Position nextPosition(Direction direction) {
		a = Arrays.asList(Direction.values()).indexOf(direction);
		
		double angle = a*22.5;
		double nextLat = latitude + Math.cos(Math.toRadians(angle))*Drone.radius;
		double nextLon = longitude + Math.sin(Math.toRadians(angle))*Drone.radius;
		
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