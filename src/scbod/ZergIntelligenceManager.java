package scbod;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ZergIntelligenceManager extends IntelligenceManager
{
	
	public ZergIntelligenceManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager, ScoutManager scoutManager)
	{
		super(bwapi, unitManager, workerManager, scoutManager);
	}
	
	/** Sends an overlord to go scout all of the base locations for the enemy */
	public void scoutOverlord(int unitID)
	{
		scoutManager.scoutBaseLocations(unitID, this);
	}
	
	
	@Override
	public void gameStarted()
	{
		super.gameStarted();
		scoutOverlord(unitManager.getMyUnitOfType(UnitTypes.Zerg_Overlord.ordinal()).getID());
	}
	
	@Override
	public void unitMorph(int unitID)
	{
		super.unitMorph(unitID);
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal())
			{
				scoutOverlord(unitID);
			}
		}
	}
	
	@Override
	public void scoutRouteCompleted(int scoutID)
	{
		super.scoutRouteCompleted(scoutID);
		
		Unit unit = bwapi.getUnit(scoutID);
		if (unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal())
		{			
			// Return to startlocation
			bwapi.move(scoutID, startLocation.getX(), startLocation.getY());
		}
	}
	
}
