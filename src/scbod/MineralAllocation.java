package scbod;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Connects mineral units to their active workers
 * @author Simon Davies
 */
public class MineralAllocation {
	
	private int unitID;
	private ArrayList<Integer> assignedDrones;
	private Point location;
	
	public MineralAllocation(int ID, Point location){
		unitID = ID;
		assignedDrones = new ArrayList<Integer>();
		this.location = location;
	}
	
	public Point getLocation(){
		return location;
	}
	
	public int getID(){
		return unitID;
	}
	
	public void assignDrone(int ID){
		if(assignedDrones.contains(ID)){
			return;
		}
		else{
			assignedDrones.add(ID);
		}
	}
	
	public void removeDrone(int ID){
		if(assignedDrones.contains(ID)){
			assignedDrones.remove(Integer.valueOf(ID));
		}
		else{
			return;
		}
	}
	
	public void clearDrones(){
		assignedDrones.clear();
	}
	
	public boolean hasDrone(int ID){
		return assignedDrones.contains(ID);
	}
	
	public int getDroneCount(){
		return assignedDrones.size();
	}

}
