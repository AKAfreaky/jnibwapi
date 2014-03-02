package scbod.managers.Terran;

import java.util.HashSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.managers.IntelligenceManager;
import scbod.managers.MilitaryManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

public class TerranMilitaryManager extends MilitaryManager
{
	public TerranMilitaryManager(	JNIBWAPI bwapi,
									IntelligenceManager intelligenceManager,
									UnitManager unitManager,
									WorkerManager workerManager)
	{
		super(bwapi, intelligenceManager, unitManager, workerManager);
		// Initialise these so they aren't null
		unitGroups.put(UnitTypes.Terran_Marine.ordinal(), new HashSet<Integer>());
		unitGroups.put(UnitTypes.Terran_Medic.ordinal(), new HashSet<Integer>());
	}
	
	/** Moves all units to the current destination */
	@Override
	protected void moveUnits()
	{
		int i = 0;
		
		HashSet<Integer> marines = marines();
		HashSet<Integer> medics = medics();
		
		int total = marines.size() + medics.size();
		for (int unitID : marines)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		for (int unitID : medics)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
	}
	
	@Override
	protected void attackUnits()
	{
		super.attackUnits();
		
		HashSet<Integer> marines = marines();
		HashSet<Integer> medics = medics();
		
		for (int unitID : marines)
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : medics)
		{
			sendToAttackBasic(unitID);
		}
	}
	
	private HashSet<Integer> marines()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Terran_Marine.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
	private HashSet<Integer> medics()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Terran_Medic.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
}
