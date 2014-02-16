package scbod;

import java.awt.Point;
import java.util.HashSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ZergMilitaryManager extends MilitaryManager
{
	/** used for identifying all of the zerglings */
	private HashSet<Integer>	zerglings		= new HashSet<Integer>();

	/** used for identifying all of the hydralisks */
	private HashSet<Integer>	hydralisks		= new HashSet<Integer>();

	/** used for identifying all of the mutalisks */
	private HashSet<Integer>	mutalisks		= new HashSet<Integer>();

	/** used for identifying all of the lurkers */
	private HashSet<Integer>	lurkers			= new HashSet<Integer>();

	/** A single overlord to take with us  TODO: research why*/
	private int					overlord;
	
	
	public ZergMilitaryManager(	JNIBWAPI bwapi,
								IntelligenceManager intelligenceManager,
								UnitManager unitManager,
								WorkerManager workerManager)
	{
		super(bwapi, intelligenceManager, unitManager, workerManager);
	}
	
	
	public int getHydraliskCount()
	{
		return hydralisks.size();
	}
	
	@Override
	protected void attackUnits()
	{
		super.attackUnits();
		for (int unitID : zerglings)
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : hydralisks)
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : mutalisks)
		{
			sendToAttackBasic(unitID);
		}
		for (int unitID : lurkers)
		{
			Unit unit = bwapi.getUnit(unitID);
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
		int total = zerglings.size() + hydralisks.size();
		for (int unitID : zerglings)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		for (int unitID : hydralisks)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		for (int unitID : mutalisks)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
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
			if (unit.getTypeID() == UnitTypes.Zerg_Zergling.ordinal())
			{
				zerglings.add(unitID);
			}
			else if (unit.getTypeID() == UnitTypes.Zerg_Hydralisk.ordinal())
			{
				hydralisks.add(unitID);
			}
			else if (unit.getTypeID() == UnitTypes.Zerg_Mutalisk.ordinal())
			{
				mutalisks.add(unitID);
			}
			else if (unit.getTypeID() == UnitTypes.Zerg_Lurker.ordinal())
			{
				if (hydralisks.contains(unitID))
				{
					hydralisks.remove(unitID);
				}
				lurkers.add(unitID);
			}
			else if (overlord == Utility.NOT_SET && unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal())
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
			if (unit.getTypeID() == UnitTypes.Zerg_Zergling.ordinal())
			{
				zerglings.add(unitID);
			}
			else if (unit.getTypeID() == UnitTypes.Zerg_Hydralisk.ordinal())
			{
				hydralisks.add(unitID);
			}
			else if (unit.getTypeID() == UnitTypes.Zerg_Mutalisk.ordinal())
			{
				mutalisks.add(unitID);
			}
			else if (unit.getTypeID() == UnitTypes.Zerg_Lurker.ordinal())
			{
				if (hydralisks.contains(unitID))
				{
					hydralisks.remove(unitID);
				}
				lurkers.add(unitID);
			}
			else if (overlord == Utility.NOT_SET && unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal())
			{
				overlord = unitID;
			}
		}
	}
	@Override
	public void unitDestroy(int unitID)
	{
		super.unitDestroy(unitID);
		if (zerglings.contains(unitID))
		{
			zerglings.remove(unitID);
		}
		else if (hydralisks.contains(unitID))
		{
			hydralisks.remove(unitID);
		}
		else if (mutalisks.contains(unitID))
		{
			mutalisks.remove(unitID);
		}
		else if (lurkers.contains(unitID))
		{
			lurkers.remove(unitID);
		}
		else if (unitID == overlord)
		{
			overlord = Utility.NOT_SET;
			overlord = unitManager.getMyUnitOfType(UnitTypes.Zerg_Overlord.ordinal()).getID();
		}
	}
	
	
}
