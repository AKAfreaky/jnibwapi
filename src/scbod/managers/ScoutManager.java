package scbod.managers;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.awt.Point;

import scbod.ScoutFinished;
import scbod.ScoutUnit;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import scbod.managers.WorkerManager;

public class ScoutManager extends Manager
{
	private ArrayList<ScoutUnit>	scouts	= new ArrayList<ScoutUnit>();

	private JNIBWAPI				bwapi;
	private WorkerManager			workerManager;

	public ScoutManager(JNIBWAPI bwapi, WorkerManager workerManager)
	{
		this.bwapi = bwapi;
		this.workerManager = workerManager;
	}

	private ScoutUnit getScout(int unitID)
	{
		for (ScoutUnit scout : scouts)
		{
			if (scout.getUnitID() == unitID)
			{
				return scout;
			}
		}

		return null;
	}

	public boolean isScout(int unitID)
	{
		return (getScout(unitID) != null);
	}

	public boolean idleScout(int unitID)
	{
		ScoutUnit scout = getScout(unitID);

		if (scout == null)
		{
			return false;
		}
		
		if (!scout.goToNextLocation())
		{
			// goToNextLocation will call ithe scouts completion handler if it fails
			// so we just need to remove our reference 			
			for(Iterator<ScoutUnit> it = scouts.iterator(); it.hasNext(); )
			{
				ScoutUnit itScout = it.next();
				if (itScout.getUnitID() == scout.getUnitID())
				{
					it.remove();
					break;
				}
			}
			
			return false;
		}

		return true;
	}

	public boolean scout(int scoutUnitID, ArrayDeque<Point> path, ScoutFinished completionHandler)
	{
		ScoutUnit scout;
		
		if (bwapi.getUnit(scoutUnitID).getTypeID() == workerManager.getWorkerTypeID())
		{
			scout = new ScoutUnit(scoutUnitID, path, bwapi, completionHandler, workerManager);
		}
		else
		{
			scout = new ScoutUnit(scoutUnitID, path, bwapi, completionHandler);
		}

		scouts.add(scout);

		return scout.goToNextLocation();
	}

	public boolean scoutBaseLocations(int scoutUnitID, ScoutFinished completionHandler)
	{
		ArrayDeque<Point> path = new ArrayDeque<Point>();

		for (BaseLocation location : bwapi.getMap().getBaseLocations())
		{
			path.add(new Point(location.getX(), location.getY()));
		}

		return scout(scoutUnitID, path, completionHandler);
	}

	public boolean scoutStartLocations(int scoutUnitID, ScoutFinished completionHandler)
	{
		ArrayDeque<Point> path = new ArrayDeque<Point>();
		
		for (BaseLocation location : bwapi.getMap().getStartLocations())
		{
			path.add(new Point(location.getX(), location.getY()));
		}

		return scout(scoutUnitID, path, completionHandler);
	}
	
	@Override
	public void unitDestroy(int unitID)
	{
		for(Iterator<ScoutUnit> it = scouts.iterator(); it.hasNext(); )
		{
			if (it.next().getUnitID() == unitID)
			{
				it.remove();
				break;
			}
		}
	}
	
	public void stopScout(int scoutID)
	{
		for(Iterator<ScoutUnit> it = scouts.iterator(); it.hasNext(); )
		{
			ScoutUnit scout = it.next();
			if (scout.getUnitID() == scoutID)
			{
				scout.stop();
				it.remove();
				break;
			}
		}
	}
	
}
