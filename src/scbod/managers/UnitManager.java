package scbod.managers;

import java.util.ArrayList;

import scbod.Utility;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;

/** Unit manager, has methods for unit selection */
public class UnitManager extends Manager
{

	JNIBWAPI	bwapi;

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
		// Count number of units
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == typeID && (!completed || unit.isCompleted()))
			{
				count++;
			}
		}
		// Count number of type about to be morphed
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Egg.ordinal())
			{
				if (unit.getBuildTypeID() == typeID)
				{
					count++;
				}
			}
		}
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
	
	
}
