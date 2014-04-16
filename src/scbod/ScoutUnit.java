package scbod;

import java.util.ArrayDeque;
import java.awt.Point;

import jnibwapi.JNIBWAPI;
import scbod.WorkerOrderData.WorkerOrder;
import scbod.managers.WorkerManager;

/**
 * Controls a unit and moves it along a set route
 * @author Alex Aiton
 */
public class ScoutUnit
{
	private int unitID;	
	private ArrayDeque<Point> locations;
	private JNIBWAPI bwapi;
	private ScoutFinished completionHandler;
	private WorkerManager workerManager;
	
	private int previousUpdateFrame = Utility.NOT_SET;
	private final int updatePeriod = 15;
	
	public ScoutUnit( int scoutUnitID, ArrayDeque<Point> path, JNIBWAPI bwapi, ScoutFinished completionHandler)
	{
		unitID		= scoutUnitID;
		locations	= path;
		this.bwapi	= bwapi;
		this.completionHandler	= completionHandler;
		this.workerManager		= null;
	}
	
	public ScoutUnit( int scoutUnitID, ArrayDeque<Point> path, JNIBWAPI bwapi, ScoutFinished completionHandler, WorkerManager workerManager)
	{
		unitID		= scoutUnitID;
		locations	= path;
		this.bwapi	= bwapi;
		this.completionHandler	= completionHandler;
		this.workerManager		= workerManager;
	}
	
	public int getUnitID()
	{
		return unitID;
	}

	
	public boolean goToNextLocation()
	{
		// This can get called several times in a frame, which would lead to skipping path points
		if( (bwapi.getFrameCount() - updatePeriod) < previousUpdateFrame)
		{
			return true;
		}
		
		previousUpdateFrame = bwapi.getFrameCount();
		
		
		Point nextLocation = locations.poll();
		
		if (nextLocation == null)
		{
			System.out.println("Scout " + unitID + " has finished its route.");
			completionHandler.scoutRouteCompleted(unitID);
			return false;
		}
		
		if(workerManager == null)
		{
			if (!bwapi.move(unitID, nextLocation.x, nextLocation.y))
			{
				System.out.println("Can't move Scout " + unitID + " to its next location!");
				completionHandler.scoutRouteCompleted(unitID);
				return false;
			}
		}
		else
		{
			// Workers can be a bit funny with their move orders...
			workerManager.queueOrder(new WorkerOrderData(WorkerOrder.Move, unitID, nextLocation.x, nextLocation.y));
		}
		
		return true;		
	}
	
	
	public void stop()
	{
		System.out.println("Stopping Scout " + unitID);
		completionHandler.scoutRouteCompleted(unitID);
	}
	
}
