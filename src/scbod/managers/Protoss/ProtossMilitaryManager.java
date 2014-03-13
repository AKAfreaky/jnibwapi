package scbod.managers.Protoss;


import java.util.HashSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.managers.IntelligenceManager;
import scbod.managers.MilitaryManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

public class ProtossMilitaryManager extends MilitaryManager
{
	public ProtossMilitaryManager(	JNIBWAPI bwapi,
									IntelligenceManager intelligenceManager,
									UnitManager unitManager,
									WorkerManager workerManager)
	{
		super(bwapi, intelligenceManager, unitManager, workerManager);
		// Initialise these so they aren't null
		unitGroups.put(UnitTypes.Protoss_Zealot.ordinal(), new HashSet<Integer>());
		unitGroups.put(UnitTypes.Protoss_Dragoon.ordinal(), new HashSet<Integer>());
		unitGroups.put(UnitTypes.Protoss_Observer.ordinal(), new HashSet<Integer>());
	}
	
	/** Moves all units to the current destination */
	@Override
	protected void moveUnits()
	{
		super.moveUnits();
		int i = 0;
		
		HashSet<Integer> zealots = zealots();
		HashSet<Integer> dragoons = dragoons();
		HashSet<Integer> observers = observers();
		
		int total = zealots.size() + dragoons.size();
		for (int unitID : zealots)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		
		for (int unitID : dragoons)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		
		for (int unitID : observers)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
	}
	

	@Override
	protected void attackUnits()
	{
		super.attackUnits();
		
		HashSet<Integer> zealots = zealots();
		HashSet<Integer> dragoons = dragoons();
		HashSet<Integer> observers = observers();
		
		for (int unitID : zealots)
		{
			sendToAttackBasic(unitID);
		}
		
		for (int unitID : dragoons)
		{
			sendToAttackBasic(unitID);
		}
		
		for (int unitID : observers)
		{
			sendToAttackBasic(unitID);
		}
	}
	
	private HashSet<Integer> zealots()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Protoss_Zealot.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
	private HashSet<Integer> dragoons()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Protoss_Dragoon.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
	private HashSet<Integer> observers()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Protoss_Observer.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
	public int getZealotCount()
	{
		return zealots().size();
	}
}
