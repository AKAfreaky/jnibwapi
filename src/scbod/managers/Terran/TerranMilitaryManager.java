package scbod.managers.Terran;

import java.awt.Point;
import java.util.HashSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.Utility;
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
		unitGroups.put(UnitTypes.Terran_Firebat.ordinal(), new HashSet<Integer>());
		
		specialUnits.add(UnitTypes.Terran_Medic.ordinal());
		specialUnits.add(UnitTypes.Terran_Marine.ordinal());
		specialUnits.add(UnitTypes.Terran_Firebat.ordinal());
	}
	
	/** Moves all units to the current destination */
	@Override
	protected void moveUnits()
	{
		super.moveUnits();
		
		HashSet<Integer> medics		= unitGroups.get(UnitTypes.Terran_Medic.ordinal());
		HashSet<Integer> marines	= unitGroups.get(UnitTypes.Terran_Marine.ordinal());
		HashSet<Integer> firebats	= unitGroups.get(UnitTypes.Terran_Firebat.ordinal());
		
		int i = 0;
		int total = medics.size() + marines.size() + firebats.size();
		
		for (int unitID : medics)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		
		for (int unitID : marines)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		
		for (int unitID : firebats)
		{
			moveToDestination(unitID, i, total);
			i++;
		}
		
	}
	
	@Override
	protected void attackUnits()
	{
		super.attackUnits();
		
		HashSet<Integer> medics		= unitGroups.get(UnitTypes.Terran_Medic.ordinal());
		HashSet<Integer> marines	= unitGroups.get(UnitTypes.Terran_Marine.ordinal());
		HashSet<Integer> firebats	= unitGroups.get(UnitTypes.Terran_Firebat.ordinal());
		HashSet<Integer> ghosts		= unitGroups.get(UnitTypes.Terran_Ghost.ordinal());
		
		HashSet<Integer> bioUnits	= new HashSet<Integer>();
		if(medics != null)		bioUnits.addAll(medics);
		if(marines != null)		bioUnits.addAll(marines); 
		if(firebats != null)	bioUnits.addAll(firebats);
		if(ghosts != null)		bioUnits.addAll(ghosts);
		
		for (Integer medicID : medics)
		{
			Unit medic = bwapi.getUnit(medicID);
			
			Unit closestUnit = null;
			double smallestDistance = Utility.NOT_SET;
			
			for (Integer unitID : bioUnits)
			{
				Unit unit = bwapi.getUnit(unitID);
				if( unit != null)
				{
					int lostHp = bwapi.getUnitType(unit.getTypeID()).getMaxHitPoints() - unit.getHitPoints();
					
					double priority = Utility.getDistance(medic.getX(), medic.getY(), unit.getX(), unit.getY()) - (lostHp*5);
					
					if ((closestUnit == null || priority < smallestDistance))
					{
						closestUnit = unit;
						smallestDistance = priority;
					}
				}
			}
			
			if (closestUnit != null)
			{
				if (!bwapi.useTech(medicID, TechTypes.Healing.ordinal(), closestUnit.getID()))
				{
					bwapi.move(medicID, closestUnit.getX(), closestUnit.getY());
				}
			}
		}
		
		for (int unitID : marines)
		{
			sendToAttackWithStimpack(unitID);
		}
		
		for (int unitID : firebats)
		{
			sendToAttackWithStimpack(unitID);
		}
		
		
	}
	
	
	private void sendToAttackWithStimpack(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (unit == null)
		{
			System.out.println("Trying to attack with an invalid unit! (ID: " + unitID + ")");
			return;
		}
		
		// don't need to stim again if it is active
		if (unit.isStimmed())
		{
			sendToAttackBasic(unitID);
		}
		else
		{
			Unit target = getHighestPriorityUnit((new Point(unit.getX(), unit.getY())));
			if (target != null)
			{
				UnitType unitType = bwapi.getUnitType(unit.getTypeID());
				// This is a very convoluted way to check the max range...
				int maxRange = bwapi.getWeaponType(unitType.getGroundWeaponID()).getMaxRange();
				
				// if the target is close enough and we have enough health, use stim
				if ((Utility.getDistance(unit.getX(), unit.getY(), target.getX(), target.getY()) < maxRange)
					&& (unit.getHitPoints() > (unitType.getMaxHitPoints()/2)))
				{
					bwapi.useTech(unitID, TechTypes.Stim_Packs.ordinal());
				}
				else
				{
					// if we can't attack the target, move towards it.
					if (!bwapi.attack(unitID, target.getID()))
					{
						int x = target.getX();
						int y = target.getY();
						
						bwapi.move(unitID, x, y);				
					}
				}
			}
			else
			{
				moveToDestination(unitID, 0, 1);
			}
		}
	}
}



























