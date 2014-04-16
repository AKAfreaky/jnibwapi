package scbod.managers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.ArrayDeque;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.AIClient;
import scbod.BaseInfo;
import scbod.ExtractorInfo;
import scbod.MineralAllocation;
import scbod.Utility;
import scbod.Utility.CommonUnitType;
import scbod.WorkerOrderData;
import scbod.WorkerOrderData.WorkerOrder;

/**
 * Worker manager controls all the workers and has them all mining minerals.
 * Workers can be requisitioned by other managers for use by calling the
 * addBusyWorker method, and then should be released with the removeBusyWorker
 * method.
 * 
 * @author Simon Davies
 * @author Alex Aiton
 */
public class WorkerManager extends Manager
{
	
	private ArrayDeque<WorkerOrderData> queuedOrders = new ArrayDeque<WorkerOrderData>();
	

	/** Holds all of the mineral fields near to a hatchery that should be mined */
	private ArrayList<MineralAllocation>	mineralFields		= new ArrayList<MineralAllocation>();

	private ArrayList<BaseInfo>				baseBuildings		= new ArrayList<BaseInfo>();

	/**
	 * used for identifying all of the workers that are busy with a task and
	 * should not be called to do another task
	 */
	private ArrayList<Integer>				busyWorkers			= new ArrayList<Integer>();

	/** used for identifying all of the workers that are collecting gas */
	private ArrayList<Integer>				globalGasWorkers	= new ArrayList<Integer>();

	/** All of the workers */
	private ArrayList<Integer>				workers				= new ArrayList<Integer>();

	/** List of geysers */
	private ArrayList<ExtractorInfo>		extractors			= new ArrayList<ExtractorInfo>();

	/** The type ID of the worker unit for the current race */
	private int								workerTypeID		= Utility.NOT_SET;

	/** The type ID of the base building for the current race */
	private int								baseTypeID;

	JNIBWAPI								bwapi;

	public WorkerManager(JNIBWAPI bwapi)
	{
		this.bwapi = bwapi;
	}

	@Deprecated/** @deprecated Use unitManager instead*/
	public int getWorkerCount()
	{
		int count = 0;
		// Count workers in play
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == workerTypeID)
			{
				count++;
			}
		}

		// Count drones about to be morphed if we're Zerg
		if (workerTypeID == UnitTypes.Zerg_Drone.ordinal())
		{
			for (Unit unit : bwapi.getMyUnits())
			{
				if (unit.getTypeID() == UnitTypes.Zerg_Egg.ordinal())
				{
					if (unit.getBuildTypeID() == UnitTypes.Zerg_Drone.ordinal())
					{
						count++;
					}
				}
			}
		}
		return count;
	}

	/** returns the number of drones currently collecting gas 
	 *	@author Simon Davies 
	 */
	public int getGasWorkerCount()
	{
		return globalGasWorkers.size();
	}

	/**
	 * Sends 3 workers to each build geyser.
	 * @author Simon Davies
	 */
	public boolean startCollectingGas()
	{
		for (ExtractorInfo extractor : extractors)
		{
			Unit extractorUnit = bwapi.getUnit(extractor.geyserID);
			Unit worker = null;

			// Can't assign units to an incomplete extractor
			if( !extractorUnit.isCompleted())
			{
				continue;
			}
			
			/* Already have enough workers, don't want anymore */
			if (extractor.gasWorkers.size() >= 3)
			{
				continue;
			}

			// Find the 3 nearest drones
			for (int i = extractor.gasWorkers.size(); i < 3; i++)
			{
				worker = getNearestFreeWorker(extractorUnit.getX(), extractorUnit.getY());

				if (worker == null)
				{
					return false;
				}
				queueOrder(new WorkerOrderData(WorkerOrder.Gather, worker.getID(), extractorUnit.getID()));
				globalGasWorkers.add(worker.getID());
				extractor.gasWorkers.add(worker.getID());
			}
		}
		return true;
	}

	/**
	 * @author Simon Davies
	 */
	public Unit getNearestFreeWorker(int tileX, int tileY)
	{
		Unit worker = null;
		double currentShortestDistance = Utility.NOT_SET;
		// Find the nearest drone
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == workerTypeID && !isWorkerBusy(unit.getID()) && !isGasWorker(unit.getID()) && unit.isCompleted())
			{
				double workerDistance = Utility.getDistance(tileX, tileY, unit.getTileX(), unit.getTileY());
				if (worker == null || workerDistance < currentShortestDistance)
				{
					worker = unit;
					currentShortestDistance = workerDistance;
				}
			}
		}

		return worker;
	}

	/**
	 * Is the given worker ID busy on something and should not be disturbed?
	 * @author Simon Davies
	 */
	public boolean isWorkerBusy(int unitID)
	{
		if (busyWorkers.contains(unitID))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Is the given worker ID a gas worker?
	 * @author Simon Davies
	 */
	public boolean isGasWorker(int unitID)
	{
		if (globalGasWorkers.contains(unitID))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/** Add the given ID to the list of busy workers 
	 *	@author Simon Davies 
	 */
	public void addBusyWorker(int unitID)
	{
		busyWorkers.add(unitID);
	}

	/** Remove the given ID from the list of busy workers 
	 *  @author Simon Davies
	 */
	public void removeBusyWorker(int unitID)
	{
		busyWorkers.remove(Integer.valueOf(unitID));
	}

	@Override
	/**
	 * @author Simon Davies
	 */
	public void gameStarted()
	{
		mineralFields.clear();
		busyWorkers.clear();
		baseBuildings.clear();
		globalGasWorkers.clear();
		workers.clear();
		workerTypeID = Utility.getCommonTypeID(CommonUnitType.Worker);
		baseTypeID = Utility.getCommonTypeID(CommonUnitType.Base);

		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == workerTypeID)
			{
				workers.add(unit.getID());
			}

			if (unit.getTypeID() == baseTypeID)
			{
				newBaseBuilding(new BaseInfo(unit));
			}
		}
	}

	/**
	 * @author Simon Davies
	 */
	public void newBaseBuilding(BaseInfo newBase)
	{
		baseBuildings.add(newBase);
		Unit hatchery = newBase.structure;
		System.out.println("WORKER MANAGER : NEW HATCHERY!");
		if (hatchery.getTypeID() != baseTypeID)
		{
			System.out.println("Minerals : Unit type not a hatchery! Ignoring...");
			return;
		}
		// Find all minerals that are nearby
		for (Unit unit : bwapi.getNeutralUnits())
		{
			if (hasMineralID(unit.getID())) // If already know about this unit
				continue;
			if (unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal())
			{
				// Are the minerals close?
				if (Utility.getDistance(unit.getX(), unit.getY(), hatchery.getX(), hatchery.getY()) < 500)
				{
					Point location = new Point(unit.getX(), unit.getY());
					mineralFields.add(new MineralAllocation(unit.getID(), location));
					continue;
				}
			}
		}
		recalculateMiningWorkers();
	}

	/**
	 * Checks whether the given unitID is a known hatchery, and if so, remove
	 * the mineral patches nearby from the mineral allocation mechanism.
	 * 
	 * @author Simon Davies
	 * @param unitID
	 */
	private void killedBaseBuilding(int unitID)
	{
		// Find the lost hatchery
		for (BaseInfo base : baseBuildings)
		{
			if (base.id == unitID)
			{
				for (Unit unit : bwapi.getNeutralUnits())
				{
					if (unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal())
					{
						// Are the minerals close?
						if (Utility.getDistance(unit.getX(), unit.getY(), base.location.x, base.location.y) < 500)
						{
							removeMineral(unit.getID());
						}
					}
				}
				recalculateMiningWorkers();
			}
		}
	}
 
	/**
	 * Remove a mineral field
	 * @author Simon Davies
	 */
	private void removeMineral(int id)
	{
		for (MineralAllocation mineral : mineralFields)
		{
			if (mineral.getID() == id)
			{
				mineralFields.remove(mineral);
				break;
			}
		}
	}

	/**
	 * @author Simon Davies
	 */
	public void recalculateMiningWorkers()
	{
		System.out.println("Recalculating mining workers");
		for (MineralAllocation mineral : mineralFields)
		{
			mineral.clearDrones();
		}
		for (Unit unit : bwapi.getMyUnits())
		{
			// don't disturb the busy and the gas workers!
			if (busyWorkers.contains(unit.getID()) || globalGasWorkers.contains(unit.getID()))
			{
				continue;
			}

			if (unit.getTypeID() == workerTypeID)
			{
				calculateMining(unit.getID());
			}
		}
	}

	/** 
	 * Sends a drone to go mine at the best possible mineral patch 
	 * @author Simon Davies 
	 */
	private void calculateMining(int unitID)
	{
		Unit worker = bwapi.getUnit(unitID);
		MineralAllocation bestMineral = null;
		int lowestAllocationCount = Utility.NOT_SET;
		double lowestDistance = Utility.NOT_SET;
		for (MineralAllocation mineral : mineralFields)
		{
			double distance = Utility.getDistance(worker.getX(), worker.getY(), mineral.getLocation().x,
					mineral.getLocation().y);
			// if nothing been allocated so far, set as this one
			// else get the one with the lowest allocation count
			// else get the closest one with lowest allocation count
			if ((lowestAllocationCount == Utility.NOT_SET || lowestDistance == Utility.NOT_SET)
					|| (mineral.getDroneCount() < lowestAllocationCount)
					|| (mineral.getDroneCount() == lowestAllocationCount && distance < lowestDistance))
			{
				lowestAllocationCount = mineral.getDroneCount();
				lowestDistance = distance;
				bestMineral = mineral;
			}
			else
			{
				continue;
			}
		}

		if (bestMineral == null)
		{
			System.out.println("No minerals found - giving up!");
			return;
		}
		// send drone to mine
		bwapi.gather(unitID, bestMineral.getID());
		// add drone to the minerals known drone list
		bestMineral.assignDrone(unitID);
	}

	/**
	 * @author Simon Davies
	 */
	private boolean hasMineralID(int unitID)
	{
		for (MineralAllocation mineral : mineralFields)
		{
			if (mineral.getID() == unitID)
				return true;
		}
		return false;
	}

	@Override
	/**
	 * @author Simon Davies
	 */
	public void unitDestroy(int unitID)
	{
		// If it is a mineral, remove it
		removeMineral(unitID);
		if (workers.contains(unitID))
		{
			workers.remove(Integer.valueOf(unitID));
			// Check to see whether a drone is currently mining, if so,
			// remove it form mineral allocations.
			for (MineralAllocation mineral : mineralFields)
			{
				mineral.removeDrone(unitID);
			}
		}
		if (busyWorkers.contains(unitID))
		{
			busyWorkers.remove(Integer.valueOf(unitID));
		}
		if (globalGasWorkers.contains(unitID))
		{
			globalGasWorkers.remove(Integer.valueOf(unitID));
			for (ExtractorInfo extractor : extractors)
			{
				if (extractor.gasWorkers.contains(unitID))
				{
					extractor.gasWorkers.remove(Integer.valueOf(unitID));
					break;
				}
			}
		}
		// Check for a killed hatchery?
		killedBaseBuilding(unitID);
	}

	@Override
	/**
	 * @author Simon Davies
	 */
	public void unitMorph(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			if (unit.getTypeID() == workerTypeID)
			{
				workers.add(unitID);
			}
			if (unit.getTypeID() == Utility.getCommonTypeID(CommonUnitType.Extractor))
			{
				extractors.add(new ExtractorInfo(unitID));
			}
			if (workers.contains(unitID) && unit.getTypeID() != workerTypeID)
			{
				workers.remove(Integer.valueOf(unitID));
			}
		}
	}

	/** 
	 * If there is an idle drone, have it return to mining 
	 * @author Simon Davies
	 */
	public void idleWorker(Unit worker)
	{
		if (!busyWorkers.contains(worker.getID()))
		{
			//System.out.println("Worker number " + worker.getID() + " was idle. Cracking the whip...");
			calculateMining(worker.getID());
		}
	}

	@Override
	/**
	 * @author Simon Davies
	 */
	public void gameUpdate()
	{
		if (AIClient.DEBUG)
		{
			bwapi.drawText(new Point(320, 32), "Drone Count : " + getWorkerCount(), true);
			for (Unit unit : bwapi.getMyUnits())
			{
				if (unit.getTypeID() == workerTypeID)
				{
					// worker is lying and isn't busy, send them back to work!
					if (isWorkerBusy(unit.getID()))
					{
						bwapi.drawText(unit.getX() + 2, unit.getY() + 4, "Busy", false);
					}
					if (isGasWorker(unit.getID()))
					{
						bwapi.drawText(unit.getX() + 2, unit.getY() + 4, "Gas", false);
					}
				}
			}
			// Draw minerals
			for (MineralAllocation minerals : mineralFields)
			{
				bwapi.drawText(minerals.getLocation().x, minerals.getLocation().y + 16,
						"Min: " + minerals.getDroneCount(), false);
			}
		}
		
		if(globalGasWorkers.size() < extractors.size() * 3)
		{
			startCollectingGas();
		}
		
		while(!queuedOrders.isEmpty())
		{
			WorkerOrderData order = queuedOrders.poll(); 
			switch(order.orderType)
			{
				case Attack:
					if (order.secondID != Utility.NOT_SET)
					{
						bwapi.attack(order.workerID, order.secondID);
					}
					else
					{
						bwapi.attack(order.workerID, order.x, order.y);
					}
					break;
				case Build:
					bwapi.build(order.workerID, order.x, order.y, order.secondID);
					break;
				case Move:
					bwapi.move(order.workerID, order.x, order.y);
					break;
				case Gather:
					bwapi.gather(order.workerID, order.secondID);
					break;
				default:
					System.out.println("Invalid order queued, moving on...");
					break;
			}
		}
		
		
	}

	/**
	 * @author Alex Aiton
	 * @return The type ID for the type of worker unit (drone/scv/probe)
	 */
	public int getWorkerTypeID()
	{
		if (workerTypeID == Utility.NOT_SET)
		{
			workerTypeID = Utility.getCommonTypeID(CommonUnitType.Worker);
		}
		return workerTypeID;
	}
	
	/**
	 * @author Alex Aiton
	 */
	public void queueOrder( WorkerOrderData order)
	{
		
		if( order != null)
		{
			if( !isWorkerBusy(order.workerID) )
			{
				addBusyWorker(order.workerID);
			}
			queuedOrders.add(order);
		}
		else
		{
			System.out.println("Was given a null order in WorkerManager!");
		}
	}
	
}
