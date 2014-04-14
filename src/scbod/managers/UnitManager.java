package scbod.managers;

import java.util.ArrayList;
import java.util.HashMap;

import scbod.Utility;
import scbod.Utility.CommonUnitType;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;

/** Unit manager, has methods for unit selection */
public class UnitManager extends Manager
{

	JNIBWAPI	bwapi;
	
	/** Only the first unit in a building's training queue actually exists. 
	 *  JNIBWAPI provides no access to the actual training queue yet, 
	 *  so to get an accurate sense of incoming units, we have to track it ourselves.
	 *  TODO: Check if this changes in JNIBWAPI updates, or do it ourselves
	 */
	private HashMap<Integer, Integer>	unitsQueued = new HashMap<Integer, Integer>();

	public UnitManager(JNIBWAPI bwapi)
	{
		this.bwapi = bwapi;
	}

	/**
	 * Finds one of the players units of a given type Returns null if no unit
	 * found.
	 * 
	 * @param typeID
	 * @return unit of given type, null if no unit found
	 */
	public Unit getMyUnitOfType(int typeID, boolean completed)
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == typeID && (!completed || unit.isCompleted()))
			{
				return unit;
			}
		}
		return null;
	}

	public Unit getMyUnitOfType(int typeID)
	{
		return getMyUnitOfType(typeID, false);
	}

	/** Returns the number the player possesses of the given unit type */
	public int getUnitCount(int typeID, boolean completed)
	{
		int count = 0;
		
		for (Unit unit : bwapi.getMyUnits())
		{
			// Count number of units
			if (unit.getTypeID() == typeID && (!completed || unit.isCompleted()))
			{
				count++;
			}
			
			// Count number of type about to be morphed
			if (unit.getTypeID() == UnitTypes.Zerg_Egg.ordinal())
			{
				if (unit.getBuildTypeID() == typeID)
				{
					count++;
				}
			}
		}	
		
		// Count the units that are in the training queue, but not created yet
		Integer numInTraining = unitsQueued.get(typeID);
		count += (numInTraining == null ? 0 : numInTraining.intValue());
		
		
		return count;
	}

	public ArrayList<Unit> getMyUnitsOfType(int typeID, boolean completed)
	{
		ArrayList<Unit> retList = new ArrayList<Unit>();
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == typeID && (!completed || unit.isCompleted()))
			{
				retList.add(unit);
			}
		}
		return retList;
	}
	
	public ArrayList<Unit> getMyUnFinishedUnitsOfType(int typeID)
	{
		ArrayList<Unit> retList = new ArrayList<Unit>();
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == typeID && (!unit.isCompleted()))
			{
				retList.add(unit);
			}
		}
		return retList;
	}
	
	public Unit getLeastBusyUnitofType(int typeID)
	{
		Unit chosenUnit = null;
		
		if (bwapi.getUnitType(typeID).isBuilding())
		{
			ArrayList<Unit> units = getMyUnitsOfType(typeID, true);
			int smallestQueue = Utility.NOT_SET;
			
			for(Unit unit: units)
			{
				int queue = unit.getTrainingQueueSize();
				if(chosenUnit == null || (	queue <= smallestQueue	&&
											!unit.isUpgrading()		&&
											unit.getResearchingTechID() == TechTypes.None.ordinal()))
				{
					chosenUnit		= unit;
					smallestQueue	= queue;
				}
			}
		}
		else
		{
			chosenUnit = getMyUnitOfType(typeID, true);
		}
		
		return chosenUnit;
	}
	
	/**
	 * Do we have the necessary resources/buildings/techs to create the given unit type
	 * 
	 * Direct wrapper on the API
	 * 
	 * @author Alex Aiton
	 * @param unitType - the type of the unit we want to check
	 * @return True if that unitType can be made.
	 */
	public boolean canCreateUnit( UnitTypes unitType )
	{
		return bwapi.canMake(unitType.ordinal());
	}
	
	
	public void addUnitInTraining(int typeID)
	{
		Integer currentQueued = unitsQueued.get(typeID);
		if(currentQueued == null)
		{
			currentQueued = Integer.valueOf(0);
		}
		unitsQueued.put(typeID, ++currentQueued);
	}
	
	@Override
	public void unitCreate(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			int typeID = unit.getTypeID();
			Integer curr = unitsQueued.get(typeID);
			if (curr != null && curr > 0)
			{
				unitsQueued.put(typeID, --curr);
			}
		}
	}
	
	
	public int getWorkerCount()
	{
		return getUnitCount(Utility.getCommonTypeID(CommonUnitType.Worker), false);
	}
}
