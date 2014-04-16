package scbod.managers.Protoss;

import jnibwapi.JNIBWAPI;
import scbod.managers.IntelligenceManager;
import scbod.managers.ScoutManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

/**
 * Stub sub-class for any Protoss Specific Inteligence behaviours
 * 
 * @author Alex Aiton
 */
public class ProtossIntelligenceManager extends IntelligenceManager
{

	public ProtossIntelligenceManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ScoutManager scoutManager)
	{
		super(bwapi, unitManager, workerManager, scoutManager);
	}

}
