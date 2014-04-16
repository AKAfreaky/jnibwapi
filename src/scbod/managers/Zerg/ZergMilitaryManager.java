package scbod.managers.Zerg;

import java.awt.Point;
import java.util.HashSet;

import scbod.Utility;
import scbod.managers.IntelligenceManager;
import scbod.managers.MilitaryManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Sub-class for any Zerg specific Military behaviours
 * 
 * @author Alex Aiton
 * Originally @author Simon Davies 
 */
public class ZergMilitaryManager extends MilitaryManager
{
	/** A single overlord to take with us  TODO: research why*/
	private int					overlord;
	
	
	public ZergMilitaryManager(	JNIBWAPI bwapi,
								IntelligenceManager intelligenceManager,
								UnitManager unitManager,
								WorkerManager workerManager)
	{
		super(bwapi, intelligenceManager, unitManager, workerManager);
		// Initialise these so they aren't null
		unitGroups.put(UnitTypes.Zerg_Zergling.ordinal(), new HashSet<Integer>());
		unitGroups.put(UnitTypes.Zerg_Hydralisk.ordinal(), new HashSet<Integer>());
		unitGroups.put(UnitTypes.Zerg_Mutalisk.ordinal(), new HashSet<Integer>());
		unitGroups.put(UnitTypes.Zerg_Lurker.ordinal(), new HashSet<Integer>());
		
		specialUnits.add(UnitTypes.Zerg_Lurker.ordinal());
		specialUnits.add(UnitTypes.Zerg_Overlord.ordinal());
		
		// Non-controllable, non-building units
		specialUnits.add(UnitTypes.Zerg_Cocoon.ordinal());
		specialUnits.add(UnitTypes.Zerg_Egg.ordinal());
		specialUnits.add(UnitTypes.Zerg_Lurker_Egg.ordinal());
		specialUnits.add(UnitTypes.Zerg_Larva.ordinal());
		
	}
	
	
	public int getHydraliskCount()
	{
		HashSet<Integer> hydralisks = unitGroups.get(UnitTypes.Zerg_Hydralisk.ordinal());
		if(hydralisks == null)
			return 0;
		
		return hydralisks.size();
	}
	
	@Override
	protected void attackUnits()
	{
		super.attackUnits();
		
		HashSet<Integer> lurkers = lurkers();
		
		for (int unitID : lurkers)
		{
			lurkerAttack(unitID);
		}
		
		if (overlord != Utility.NOT_SET)
		{
			Unit unit = bwapi.getUnit(overlord);
			Unit target = getHighestPriorityUnit((new Point(unit.getX(), unit.getY())));
			bwapi.rightClick(overlord, target.getID());
		}
	}
	
	/**
	 * Moved this out of attackUnits so that method is easier to parse.
	 * 
	 */
	private void lurkerAttack(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		
		if (unit  == null || (unit.getTypeID() != UnitTypes.Zerg_Lurker.ordinal()) )
		{
			return;
		}
		
		Unit target = getHighestPriorityUnit((new Point(unit.getX(), unit.getY())));
		if (!unit.isBurrowed())
		{
			if (Utility.getDistance(unit.getX(), unit.getY(), target.getX(), target.getY()) < 200)
			{
				bwapi.burrow(unitID);
			}
			else
			{
				bwapi.move(unitID, target.getX(), target.getY());
			}
		}
		else
		{
			if (Utility.getDistance(unit.getX(), unit.getY(), target.getX(), target.getY()) > 300)
			{
				bwapi.unburrow(unitID);
			}
			else
			{
				bwapi.attack(unitID, target.getID());
			}
		}
	}
	
	/** Moves all units to the current destination */
	@Override
	protected void moveUnits()
	{
		super.moveUnits();
		
		int i = 0;
		HashSet<Integer> lurkers = lurkers();
		int total = lurkers().size() + ((overlord != Utility.NOT_SET) ? 1 : 0);
		
		for (int unitID : lurkers)
		{
			if (bwapi.getUnit(unitID).isBurrowed())
			{
				bwapi.unburrow(unitID);
				continue;
			}
			moveToDestination(unitID, i, total);
			i++;
		}
		if (overlord != Utility.NOT_SET)
		{
			moveToDestination(overlord, i, total);
			i++;
		}
	}
	
	@Override
	public void gameStarted()
	{
		super.gameStarted();
		overlord = Utility.NOT_SET;
	}
	
	@Override
	public void unitMorph(int unitID)
	{
		super.unitMorph(unitID);
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			if (overlord == Utility.NOT_SET && unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal())
			{
				overlord = unitID;
			}
		}
	}
	@Override
	public void unitCreate(int unitID)
	{
		super.unitCreate(unitID);
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			if (overlord == Utility.NOT_SET && unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal())
			{
				overlord = unitID;
			}
		}
	}
	@Override
	public void unitDestroy(int unitID)
	{
		super.unitDestroy(unitID);
		if (unitID == overlord)
		{
			overlord = Utility.NOT_SET;
			overlord = unitManager.getMyUnitOfType(UnitTypes.Zerg_Overlord.ordinal()).getID();
		}
	}
	
	private HashSet<Integer> lurkers()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Zerg_Lurker.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
}
