package scbod;

import java.util.ArrayDeque;
import java.awt.Point;
import jnibwapi.JNIBWAPI;


public class ScoutUnit
{
	private int unitID;	
	private ArrayDeque<Point> locations;
	private JNIBWAPI bwapi;
	private ScoutFinished completionHandler;
	
	public ScoutUnit( int scoutUnitID, ArrayDeque<Point> path, JNIBWAPI bwapi, ScoutFinished completionHandler)
	{
		unitID		= scoutUnitID;
		locations	= path;
		this.bwapi	= bwapi;
		this.completionHandler = completionHandler;
	}
	
	public int getUnitID()
	{
		return unitID;
	}

	
	public boolean goToNextLocation()
	{
		Point nextLocation = locations.poll();
		
		if (nextLocation == null)
		{
			System.out.println("Scout " + unitID + "has finished its route.");
			completionHandler.scoutRouteCompleted(unitID);
			return true;
		}
		
		
		if (!bwapi.move(unitID, nextLocation.x, nextLocation.y))
		{
			System.out.println("Can't move Scout " + unitID + " to its next location!");
			locations.addFirst(nextLocation);
			return false;
		}
		
		return true;		
	}
	
	
	public void stop()
	{
		System.out.println("Stopping Scout " + unitID);
		completionHandler.scoutRouteCompleted(unitID);
	}
	
}
