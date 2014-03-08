package scbod;

import java.awt.Point;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

/** 
 * Data structure class for holding information in buildings
 * Used when the unit data cannot be relied upon, such as for enemy buildings
 * that will not always be visible. 
 * @author Simon Davies
 */
public class BuildingInfo {
	
	public int id;
	public Point location;
	public Point tileLocation;
	public UnitType buildingType;
	
	public BuildingInfo(Unit unit, JNIBWAPI bwapi) {
		id = unit.getID();
		location = new Point(unit.getX(), unit.getY());
		tileLocation = new Point(unit.getTileX(), unit.getTileY());
		buildingType = bwapi.getUnitType(unit.getTypeID());
	}
	
	public BuildingInfo(Point tileLocation, int buildingTypeID, JNIBWAPI bwapi) {
		id = Utility.NOT_SET;
		location = new Point(tileLocation.x * 32, tileLocation.y * 32);
		this.tileLocation = tileLocation;
		this.buildingType = bwapi.getUnitType(buildingTypeID);
	}

}
