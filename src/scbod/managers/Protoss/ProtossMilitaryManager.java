package scbod.managers.Protoss;


import java.util.HashSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.managers.IntelligenceManager;
import scbod.managers.MilitaryManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

/**
 * Sub-class for any Protoss Specific Military behaviours
 * 
 * @author Alex Aiton
 */
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
	
	public int getZealotCount()
	{
		HashSet<Integer> zealots = unitGroups.get(UnitTypes.Protoss_Zealot.ordinal());
		if(zealots == null)
			return 0;
		
		return zealots.size();
	}
}
