package scbod;

import java.awt.Point;
import java.util.List;

import jnibwapi.model.Unit;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType.UnitTypes;

/** 
 * Utility functions/methods class 
 * @author Simon Davies
 * @author Alex Aiton
 */
public class Utility
{

	public final static int	NOT_SET	= -1;
	private static RaceTypes RACE = RaceTypes.Unknown;
	
	/** 
	 * @author Simon Davies
	 */
	public static double getDistance(double x1, double y1, double x2, double y2)
	{
		double xDistance = x2 - x1;
		double yDistance = y2 - y1;
		return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
	}

	/** 
	 * Returns the closest unit in a given list to the given coordinates 
	 * @author Simon Davies
	 */
	public static Unit getClosestUnit(List<Unit> units, Point location)
	{
		Unit closestUnit = null;
		double smallestDistance = Utility.NOT_SET;
		for (Unit unit : units)
		{
			double distance = getDistance(location.x, location.y, unit.getX(), unit.getY());
			if (closestUnit == null || distance < smallestDistance)
			{
				closestUnit = unit;
				smallestDistance = distance;
			}
		}
		return closestUnit;
	}

	/**
	 * Returns the closest unit in a given list to the given coordinates, and of
	 * the given type.
	 * @author Simon Davies
	 */
	public static Unit getClosestUnitOfType(List<Unit> units, Point location, int unitType)
	{
		Unit closestUnit = null;
		double smallestDistance = Utility.NOT_SET;
		for (Unit unit : units)
		{
			if (unit.getTypeID() != unitType)
			{
				continue;
			}
			double distance = getDistance(location.x, location.y, unit.getX(), unit.getY());
			if (closestUnit == null || distance < smallestDistance)
			{
				closestUnit = unit;
				smallestDistance = distance;
			}
		}
		return closestUnit;
	}

	/** 
	 * Returns the closest location in a given list to the given coordinates 
	 * @author Simon Davies
	 */
	public static Point getClosestLocation(List<Point> points, Point target)
	{
		Point closestPoint = null;
		double smallestDistance = Utility.NOT_SET;
		for (Point point : points)
		{
			double distance = getDistance(target.x, target.y, point.x, point.y);
			if (closestPoint == null || distance < smallestDistance)
			{
				closestPoint = point;
				smallestDistance = distance;
			}
		}
		return closestPoint;
	}
	
	/** 
	 * Sets what race we're playing so we can return the correct type ID
	 * @author Alex Aiton
	 */
	public static void setRace( RaceTypes race)
	{
		RACE = race;
	}
	
	/** 
	 * Get the race we're playing
	 * @author Alex Aiton
	 */
	public static RaceTypes getRace()
	{
		return RACE;
	}
	
	/** 
	 * Unit types which are equivalent between races
	 * @author Alex Aiton
	 */
	public enum CommonUnitType
	{
		Worker,
		Base,
		Extractor
	}
	
	/** 
	 * @author Alex Aiton
	 * @param unitType	-	The CommonUnitType you want to get the actual UnitType for
	 * @return typeID	-	The UnitType for the current race's common unit
	 */
	public static int getCommonTypeID( CommonUnitType unitType)
	{
		int typeID = UnitTypes.None.ordinal();
		switch(RACE)
		{
			case Zerg:
				switch (unitType)
				{
					case Worker:
						typeID = UnitTypes.Zerg_Drone.ordinal();
						break;
					case Base:
						typeID = UnitTypes.Zerg_Hatchery.ordinal();
						break;
					case Extractor:
						typeID = UnitTypes.Zerg_Extractor.ordinal();
						break;
					default:
						System.out.println("Unknown Common Unit Type in getCommonTypeID()!");
						break;
					
				}
				break;
			case Protoss:
				switch (unitType)
				{
					case Worker:
						typeID = UnitTypes.Protoss_Probe.ordinal();
						break;
					case Base:
						typeID = UnitTypes.Protoss_Nexus.ordinal();
						break;
					case Extractor:
						typeID = UnitTypes.Protoss_Assimilator.ordinal();
						break;
					default:
						System.out.println("Unknown Common Unit Type in getCommonTypeID()!");
						break;
					
				}
				break;
			case Terran:
				switch (unitType)
				{
					case Worker:
						typeID = UnitTypes.Terran_SCV.ordinal();
						break;
					case Base:
						typeID = UnitTypes.Terran_Command_Center.ordinal();
						break;
					case Extractor:
						typeID = UnitTypes.Terran_Refinery.ordinal();
						break;
					default:
						System.out.println("Unknown Common Unit Type in getCommonTypeID()!");
						break;
					
				}
				break;
			default:
				System.out.println("Unhandled Race in getCommonTypeID()!");
				break;
		}
		
		
		return typeID;
	}

}
