package scbod.managers.Terran;

import jnibwapi.JNIBWAPI;
import scbod.managers.IntelligenceManager;
import scbod.managers.ScoutManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

/**
 * Stub sub-class for any Terran specific Intelligence behaviours
 * 
 * @author Alex Aiton
 */
public class TerranIntelligenceManager extends IntelligenceManager
{

	public TerranIntelligenceManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ScoutManager scoutManager)
	{
		super(bwapi, unitManager, workerManager, scoutManager);
	}

}
