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
	}
	
	
	public int getHydraliskCount()
	{
		return hydralisks().size();
	}
	
	@Override
	protected void attackUnits()
	{
		super.attackUnits();
		for (int unitID : zerglings())
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : hydralisks())
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : mutalisks())
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : lurkers())
		{
			Unit unit = bwapi.getUnit(unitID);
			
			if (unit  == null)
			{
				continue;
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
		if (overlord != Utility.NOT_SET)
		{
			Unit unit = bwapi.getUnit(overlord);
			Unit target = getHighestPriorityUnit((new Point(unit.getX(), unit.getY())));
			bwapi.rightClick(overlord, target.getID());
		}
	}
	
	/** Moves all units to the current destination */
	@Override
	protected void moveUnits()
	{
		int i = 0;
		
		int total = zerglings().size() + hydralisks().size() + mutalisks().size() + lurkers().size() + ((overlord != Utility.NOT_SET) ? 1 : 0);
		for (int unitID : zerglings())
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		for (int unitID : hydralisks())
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		for (int unitID : mutalisks())
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		for (int unitID : lurkers())
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
	
	
	private HashSet<Integer> zerglings()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Zerg_Zergling.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
	private HashSet<Integer> hydralisks()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Zerg_Hydralisk.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
	}
	
	private HashSet<Integer> mutalisks()
	{
		HashSet<Integer> retVal = unitGroups.get(UnitTypes.Zerg_Mutalisk.ordinal());
		if (retVal == null)
		{
			retVal = new HashSet<Integer>();
		}
		return retVal;
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
