package scbod;

import java.awt.Point;
import java.util.List;

import jnibwapi.model.Unit;

/** Utility functions/methods class */
public class Utility {
	
	public final static int NOT_SET = -1;
	
	public static double getDistance(double x1, double y1, double x2, double y2){
		double xDistance = x2 - x1;
		double yDistance = y2 - y1;
		return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
	}
	
	/** Returns the closest unit in a given list to the given coordinates */
	public static Unit getClosestUnit(List<Unit> units, Point location){
		Unit closestUnit = null;
		double smallestDistance = Utility.NOT_SET;
		for(Unit unit: units){
			double distance = getDistance(location.x, location.y,
					unit.getX(), unit.getY());
			if(closestUnit == null || distance < smallestDistance){
				closestUnit = unit;
				smallestDistance = distance;
			}
		}
		return closestUnit;
	}
	
	/** Returns the closest unit in a given list to the given coordinates,
	 * and of the given type. 
	 * 
	 */
	public static Unit getClosestUnitOfType(List<Unit> units, Point location, int unitType){
		Unit closestUnit = null;
		double smallestDistance = Utility.NOT_SET;
		for(Unit unit: units){
			if(unit.getTypeID() != unitType){
				continue;
			}
			double distance = getDistance(location.x, location.y,
					unit.getX(), unit.getY());
			if(closestUnit == null || distance < smallestDistance){
				closestUnit = unit;
				smallestDistance = distance;
			}
		}
		return closestUnit;
	}
	
	/** Returns the closest location in a given list to the given coordinates */
	public static Point getClosestLocation(List<Point> points, Point target){
		Point closestPoint = null;
		double smallestDistance = Utility.NOT_SET;
		for(Point point: points){
			double distance = getDistance(target.x, target.y,
					point.x, point.y);
			if(closestPoint == null || distance < smallestDistance){
				closestPoint = point;
				smallestDistance = distance;
			}
		}
		return closestPoint;
	}

}
