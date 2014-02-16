package scbod;

import java.awt.Point;
import java.util.List;

import jnibwapi.model.Unit;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType.UnitTypes;

/** Utility functions/methods class */
public class Utility
{

	public final static int	NOT_SET	= -1;

	public static double getDistance(double x1, double y1, double x2, double y2)
	{
		double xDistance = x2 - x1;
		double yDistance = y2 - y1;
		return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
	}

	/** Returns the closest unit in a given list to the given coordinates */
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
	 * 
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

	/** Returns the closest location in a given list to the given coordinates */
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
	
	private static RaceTypes RACE = RaceTypes.Unknown;
	
	public static void setRace( RaceTypes race)
	{
		RACE = race;
	}
	

	public enum CommonUnitType
	{
		Worker,
		Base,
		Extractor
	}
	
	public static int getCommonTypeID( CommonUnitType unit)
	{
		int retVal = UnitTypes.None.ordinal();
		switch(RACE)
		{
			case Zerg:
				switch (unit)
				{
					case Worker:
						retVal = UnitTypes.Zerg_Drone.ordinal();
						break;
					case Base:
						retVal = UnitTypes.Zerg_Hatchery.ordinal();
						break;
					case Extractor:
						retVal = UnitTypes.Zerg_Extractor.ordinal();
						break;
					default:
						System.out.println("Unknown Common Unit Type in getCommonTypeID()!");
						break;
					
				}
				break;
			case Protoss:
				switch (unit)
				{
					case Worker:
						retVal = UnitTypes.Protoss_Probe.ordinal();
						break;
					case Base:
						retVal = UnitTypes.Protoss_Nexus.ordinal();
						break;
					case Extractor:
						retVal = UnitTypes.Protoss_Assimilator.ordinal();
						break;
					default:
						System.out.println("Unknown Common Unit Type in getCommonTypeID()!");
						break;
					
				}
				break;
			case Terran:
				switch (unit)
				{
					case Worker:
						retVal = UnitTypes.Terran_SCV.ordinal();
						break;
					case Base:
						retVal = UnitTypes.Terran_Command_Center.ordinal();
						break;
					case Extractor:
						retVal = UnitTypes.Terran_Refinery.ordinal();
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
		
		
		return retVal;
	}

}
