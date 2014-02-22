package scbod;

import java.awt.Point;
import java.util.ArrayList;

import jnibwapi.model.Unit;

/** Used to keep track of buildings that need to update the building 
 * location information, but only once they have been completed, such as
 * base hatcheries of creep colonies. */
public class BaseInfo {
	public Unit structure;
	public boolean completed;
	public boolean updated;
	
	public Point location;
	public int id;
	public int hatcheryWaitTimer;
	
	public ArrayList<Integer> buildingIndexes;
	
	public BaseInfo(Unit building){
		this.structure = building;
		this.id = building.getID();
		this.location = new Point(building.getX(), building.getY());
		completed = false;
		updated = false;
		buildingIndexes = new ArrayList<Integer>();
	}
	
	public BaseInfo(Unit building, boolean completed){
		this.structure = building;
		this.id = building.getID();
		this.location = new Point(building.getX(), building.getY());
		this.completed = completed;
		this.updated = completed;
		buildingIndexes = new ArrayList<Integer>();
	}
}
