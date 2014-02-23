package scbod.managers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;

import scbod.AIClient;
import scbod.Utility;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Region;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Zergling manager handles all of the zerglings owned by the player, and deals
 * with sending them to attack, retreat and defending of the base.
 */
public class MilitaryManager extends Manager
{

	private enum State
	{
		// Defending, sit in base near sunken colonies and attack any enemy
		// forces seen
		DEFENDING,
		// Attacking, full on attack. Attack enemy forces and try and take out
		// the opponent
		ATTACKING,
		// Retreat
		RETREAT,
	}

	/**
	 * Tracks whether workers have been used in combat, and have to be sent back
	 * to mine once the combat is over
	 */
	private boolean						workersNeedReset;

	protected Point						destination;

	protected JNIBWAPI					bwapi;
	protected IntelligenceManager		intelligenceManager;
	protected UnitManager				unitManager;
	protected WorkerManager				workerManager;
	protected State						state;

	/** How many times has the attack cycle been run
		Increasing the attack cycle count makes the army move towards the enemy
		base */
	private int							attackStartFrame;

	private double						attackFramesDifference	= 1250;

	/** How many times has the retreat cycle been run
		When this count reaches a set target, defend is initiated instead */
	private int							startRetreatFrame;

	/** Number of frames that has to be passed until the AI will go into retreat mode */
	private final int					retreatLength			= 300;

	/** Maps unit IDs onto their priority (how important the unit is to kill) */
	private HashMap<Integer, Integer>	priorities;
	
	/** All of the workers */
	private HashSet<Integer>	workers			= new HashSet<Integer>();
	
	/** Groups unit IDs together (V) by their type ID (k)*/
	protected HashMap<Integer, HashSet<Integer>> unitGroups = new HashMap<Integer, HashSet<Integer>>();
	
	/** Keys to the unitGroups for groups considered as part of the army*/
	protected HashSet<Integer> attackUnits = new HashSet<Integer>();

	/** Set all of the priorities for the units */
	public void setPriorities()
	{
		priorities = new HashMap<Integer, Integer>();
		System.out.println("Set Priorities");
		priorities.put(UnitTypes.Terran_Marine.ordinal(), 13);
		priorities.put(UnitTypes.Terran_Ghost.ordinal(), 20);
		priorities.put(UnitTypes.Terran_Vulture.ordinal(), 5);
		priorities.put(UnitTypes.Terran_Goliath.ordinal(), 10);
		priorities.put(UnitTypes.Terran_Siege_Tank_Tank_Mode.ordinal(), 25);
		priorities.put(UnitTypes.Terran_SCV.ordinal(), 5);
		priorities.put(UnitTypes.Terran_Wraith.ordinal(), 10);
		priorities.put(UnitTypes.Terran_Science_Vessel.ordinal(), 10);
		priorities.put(UnitTypes.Terran_Dropship.ordinal(), 20);
		priorities.put(UnitTypes.Terran_Battlecruiser.ordinal(), 10);
		priorities.put(UnitTypes.Terran_Vulture_Spider_Mine.ordinal(), 0);
		priorities.put(UnitTypes.Terran_Siege_Tank_Siege_Mode.ordinal(), 17);
		priorities.put(UnitTypes.Terran_Firebat.ordinal(), 10);
		priorities.put(UnitTypes.Terran_Medic.ordinal(), 10);
		priorities.put(UnitTypes.Zerg_Larva.ordinal(), -100);
		priorities.put(UnitTypes.Zerg_Egg.ordinal(), -100);
		priorities.put(UnitTypes.Zerg_Zergling.ordinal(), 10);
		priorities.put(UnitTypes.Zerg_Hydralisk.ordinal(), 10);
		priorities.put(UnitTypes.Zerg_Ultralisk.ordinal(), 20);
		priorities.put(UnitTypes.Zerg_Broodling.ordinal(), -10);
		priorities.put(UnitTypes.Zerg_Drone.ordinal(), 5);
		priorities.put(UnitTypes.Zerg_Overlord.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Mutalisk.ordinal(), 10);
		priorities.put(UnitTypes.Zerg_Guardian.ordinal(), 15);
		priorities.put(UnitTypes.Zerg_Queen.ordinal(), 15);
		priorities.put(UnitTypes.Zerg_Defiler.ordinal(), 20);
		priorities.put(UnitTypes.Zerg_Scourge.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Infested_Terran.ordinal(), 20);
		priorities.put(UnitTypes.Terran_Valkyrie.ordinal(), 5);
		priorities.put(UnitTypes.Zerg_Cocoon.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Corsair.ordinal(), 5);
		priorities.put(UnitTypes.Protoss_Dark_Templar.ordinal(), 25);
		priorities.put(UnitTypes.Zerg_Devourer.ordinal(), 10);
		priorities.put(UnitTypes.Protoss_Dark_Archon.ordinal(), 15);
		priorities.put(UnitTypes.Protoss_Probe.ordinal(), 5);
		priorities.put(UnitTypes.Protoss_Zealot.ordinal(), 10);
		priorities.put(UnitTypes.Protoss_Dragoon.ordinal(), 12);
		priorities.put(UnitTypes.Protoss_High_Templar.ordinal(), 20);
		priorities.put(UnitTypes.Protoss_Archon.ordinal(), 15);
		priorities.put(UnitTypes.Protoss_Shuttle.ordinal(), 10);
		priorities.put(UnitTypes.Protoss_Scout.ordinal(), 10);
		priorities.put(UnitTypes.Protoss_Arbiter.ordinal(), 15);
		priorities.put(UnitTypes.Protoss_Carrier.ordinal(), 15);
		priorities.put(UnitTypes.Protoss_Interceptor.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Reaver.ordinal(), 25);
		priorities.put(UnitTypes.Protoss_Observer.ordinal(), 5);
		priorities.put(UnitTypes.Zerg_Lurker_Egg.ordinal(), -20);
		priorities.put(UnitTypes.Zerg_Lurker.ordinal(), 20);
		priorities.put(UnitTypes.Terran_Command_Center.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Comsat_Station.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Nuclear_Silo.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Supply_Depot.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Refinery.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Barracks.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Academy.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Factory.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Starport.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Control_Tower.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Science_Facility.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Covert_Ops.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Physics_Lab.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Machine_Shop.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Engineering_Bay.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Armory.ordinal(), -10);
		priorities.put(UnitTypes.Terran_Missile_Turret.ordinal(), 5);
		priorities.put(UnitTypes.Terran_Bunker.ordinal(), 13);
		priorities.put(UnitTypes.Zerg_Infested_Command_Center.ordinal(), -10);
		priorities.put(UnitTypes.Zerg_Hatchery.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Lair.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Hive.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Nydus_Canal.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Hydralisk_Den.ordinal(), -10);
		priorities.put(UnitTypes.Zerg_Defiler_Mound.ordinal(), -5);
		priorities.put(UnitTypes.Zerg_Greater_Spire.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Queens_Nest.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Evolution_Chamber.ordinal(), -10);
		priorities.put(UnitTypes.Zerg_Ultralisk_Cavern.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Spire.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Spawning_Pool.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Creep_Colony.ordinal(), 0);
		priorities.put(UnitTypes.Zerg_Spore_Colony.ordinal(), 5);
		priorities.put(UnitTypes.Zerg_Sunken_Colony.ordinal(), 10);
		priorities.put(UnitTypes.Zerg_Extractor.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Nexus.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Robotics_Facility.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Pylon.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Assimilator.ordinal(), -10);
		priorities.put(UnitTypes.Protoss_Observatory.ordinal(), -10);
		priorities.put(UnitTypes.Protoss_Gateway.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Photon_Cannon.ordinal(), 13);
		priorities.put(UnitTypes.Protoss_Citadel_of_Adun.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Cybernetics_Core.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Templar_Archives.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Forge.ordinal(), -5);
		priorities.put(UnitTypes.Protoss_Stargate.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Fleet_Beacon.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Arbiter_Tribunal.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Robotics_Support_Bay.ordinal(), 0);
		priorities.put(UnitTypes.Protoss_Shield_Battery.ordinal(), -15);
		priorities.put(UnitTypes.None.ordinal(), 0);
		priorities.put(UnitTypes.Unknown.ordinal(), 0);
	}

	/* Only update units every few frames */
	/*
	 * This is required as units that are being constantly told to attack don't
	 * actually attack.
	 */
	private static final int	UPDATE_TIMER	= 9;
	// Update timer
	private long				previousUpdateTime;

	public MilitaryManager(JNIBWAPI bwapi, IntelligenceManager intelligenceManager, UnitManager unitManager,
			WorkerManager workerManager)
	{
		this.bwapi = bwapi;
		this.intelligenceManager = intelligenceManager;
		this.state = State.DEFENDING;
		this.unitManager = unitManager;
		this.workerManager = workerManager;
		setPriorities();
	}


	/**
	 * Returns the size of the force currently possesed by the player. This is
	 * the supply cost of the army * 2. In BWAPI, supply is doubled, so a single
	 * zergling is one supply instead of 1/2 as it is in StarCraft itself.
	 * 
	 * @return Supply amount of military forces
	 */
	public int getForceSize()
	{
		int forceSize = 0;
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == workerManager.getWorkerTypeID())
			{
				continue;
			}
			forceSize += bwapi.getUnitType(unit.getTypeID()).getSupplyRequired();
		}
		return forceSize;
	}

	public boolean isAttacking()
	{
		if (state == State.ATTACKING)
		{
			return true;
		}
		else
			return false;
	}

	/** Attack the enemy base if the location is known */
	public boolean attackEnemyBase()
	{
		if (state != State.ATTACKING)
		{
			if (intelligenceManager.getEnemyStartLocation() == null)
			{
				return false;
			}
			else
			{
				state = State.ATTACKING;
				attackStartFrame = bwapi.getFrameCount();
				return true;
			}
		}
		return false;
	}

	/** Attack the enemy base if the location is known */
	public boolean defend()
	{
		attackStartFrame = 0;
		if (state == State.ATTACKING)
		{
			state = State.RETREAT;
			startRetreatFrame = bwapi.getFrameCount();
		}
		else
		{
			state = State.DEFENDING;
		}
		return true;
	}

	private Point getAverageLocation(HashSet<Integer> list)
	{
		int count = 0;
		int totalX = 0;
		int totalY = 0;
		for (int zerglingID : list)
		{
			Unit unit = bwapi.getUnit(zerglingID);
			totalX += unit.getX();
			totalY += unit.getY();
			count++;
		}
		return new Point(totalX / count, totalY / count);
	}

	private void drawIdentifier(String name, HashSet<Integer> list)
	{
		if (list.size() > 0)
		{
			Point averagePoint = getAverageLocation(list);
			bwapi.drawText(averagePoint.x + 2, averagePoint.y, list.size() + " " + name, false);
			bwapi.drawText(averagePoint.x, averagePoint.y + 10, state.toString(), false);
		}
	}

	/** Returns the highest priority unit that a unit should attack */
	public Unit getHighestPriorityUnit(Point location)
	{
		Unit closestUnit = null;
		double smallestDistance = Utility.NOT_SET;
		for (Unit unit : bwapi.getEnemyUnits())
		{
			double priority;
			try
			{
				int type = unit.getTypeID();
				priority = Utility.getDistance(location.x, location.y, unit.getX(), unit.getY()) - priorities.get(type)
						* 20;
				// want to check whether it is visible and not burrowed so as to
				// not try and attack invisible units
				if ((closestUnit == null || priority < smallestDistance) && unit.isVisible()
						&& (!unit.isBurrowed() || unit.isDetected()) && (!unit.isCloaked() || unit.isDetected())
						&& !unit.isInvincible())
				{
					closestUnit = unit;
					smallestDistance = priority;
				}
			}
			catch (Exception e)
			{
				System.out.println("Unit type crash :" + bwapi.getUnitType(unit.getTypeID()).getName());
				e.printStackTrace();
			}

		}
		return closestUnit;
	}

	/** Set the start destination to be the chokepoint of the starting area */
	private void setDestinationToChokePoint()
	{
		Region startRegion = null;
		for (Region region : bwapi.getMap().getRegions())
		{
			if (region.getID() == intelligenceManager.getPlayerStartLocation().getRegionID())
			{
				startRegion = region;
				break;
			}
		}
		if (startRegion != null)
		{
			// TODO:: Does the start region only have a single choke?
			Iterator<ChokePoint> it = startRegion.getChokePoints().iterator();
			if (it.hasNext())
			{
				ChokePoint chokePoint = it.next();
				if (chokePoint.getFirstRegion().getID()  == startRegion.getID())
				{
					destination = new Point(chokePoint.getFirstSideX(), chokePoint.getFirstSideY());
				}
				else
				{
					destination = new Point(chokePoint.getSecondSideX(), chokePoint.getSecondSideY());
				}
			}
			else
			{
				System.out.println("Error setting start choke point / military destination");
				destination = new Point(0, 0);
			}
		}
		else
		{
			System.out.println("Error setting start choke point / military destination");
			destination = new Point(0, 0);
		}
	}

	/** Give all units orders */
	private void giveUnitOrders()
	{
		if (bwapi.getFrameCount() > previousUpdateTime + UPDATE_TIMER)
		{
			previousUpdateTime = bwapi.getFrameCount();
			// Set destination for all units
			switch (state)
			{
				case ATTACKING:
					updateAttackLocation();
					break;
				case DEFENDING:
				case RETREAT:
					setDestinationToChokePoint();
					break;
			}
			// Move units or send them to attack
			if (state == State.ATTACKING && bwapi.getEnemyUnits().size() > 0)
			{
				attackUnits();
			}
			else if (bwapi.getEnemyUnits().size() > 0 && state != State.RETREAT && aggressiveEnemy())
			{
				attackUnits();
			}
			else
			{
				if (workersNeedReset)
				{
					workersNeedReset = false;
					workerManager.recalculateMiningWorkers();
				}
				// Move units
				moveUnits();
			}
		}
		// Do whatever it is they should do depending on state
	}

	/**
	 * Enemy is considered aggressive if the average enemy position is enclosing
	 * on a friendly structure
	 * 
	 * @return
	 */
	private boolean aggressiveEnemy()
	{
		for (Unit enemy : bwapi.getEnemyUnits())
		{
			Unit friendly = getClosestStructureToLocation(new Point(enemy.getX(), enemy.getY()));
			if (Utility.getDistance(enemy.getX(), enemy.getY(), friendly.getX(), friendly.getY()) < 1000)
			{
				return true;
			}
		}
		return false;
	}

	private Unit getClosestStructureToLocation(Point location)
	{
		Unit closestUnit = null;
		double bestDistance = Utility.NOT_SET;

		for (Unit unit : bwapi.getMyUnits())
		{
			if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
			{
				double distance = Utility.getDistance(location.x, location.y, unit.getX(), unit.getY());
				if (closestUnit == null || distance < bestDistance)
				{
					closestUnit = unit;
					bestDistance = distance;
				}
			}
		}
		return closestUnit;
	}

	private Unit getClosestEnemy(Point location)
	{
		Unit closestUnit = null;
		double bestDistance = Utility.NOT_SET;

		for (Unit unit : bwapi.getEnemyUnits())
		{
			if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
			{
				double distance = Utility.getDistance(location.x, location.y, unit.getX(), unit.getY());
				if (closestUnit == null || distance < bestDistance)
				{
					closestUnit = unit;
					bestDistance = distance;
				}
			}
		}
		return closestUnit;
	}

	private double getAverageDistance(ArrayList<Unit> units, int x, int y)
	{
		int totalDistance = 0;
		int count = 0;
		for (Unit unit : units)
		{
			totalDistance += Utility.getDistance(x, y, unit.getX(), unit.getY());
			count++;
		}
		if (count == 0)
		{
			return 0;
		}
		else
		{
			return totalDistance / count;
		}
	}

	private Point getAverageLocation(ArrayList<Unit> units)
	{
		int x = 0;
		int y = 0;
		int count = 0;
		for (Unit unit : units)
		{
			x += unit.getX();
			y += unit.getY();
			count++;
		}
		if (count != 0)
		{
			return new Point(x / count, y / count);
		}
		else
		{
			return null;
		}
	}

	protected void attackUnits()
	{
		if (getForceSize() < 4)
		{
			workersNeedReset = true;
			for (int unitID : workers)
			{
				if (workerManager.isWorkerBusy(unitID) || workerManager.isGasWorker(unitID))
				{
					continue;
				}
				sendToAttackBasic(unitID);
				break;
			}
		}
	}

	protected void sendToAttackBasic(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (unit == null)
		{
			System.out.println("Trying to attack with an invalid unit! (ID: " + unitID);
			return;
		}
		
		Unit target = getHighestPriorityUnit((new Point(unit.getX(), unit.getY())));
		if (target != null)
		{
			bwapi.attack(unitID, target.getID());
		}
		else
		{
			moveToDestination(unitID, 0, 1);
		}
	}

	/** Moves all units to the current destination */
	protected void moveUnits()
	{
		//TODO: Move some of the method back here
	}

	/**
	 * Move a unit to the destination, based on how many units there are
	 * currently in play
	 */
	protected void moveToDestination(int unitID, int place, int unitTotal)
	{
		int x = destination.x;
		int y = destination.y;

		x += (place - unitTotal / 2) * 4;
		y += ((place % 10) - 2) * 16;

		// attack-move
		if (state != State.RETREAT)
		{
			bwapi.attack(unitID, x, y);
		}
		// Move if retreating
		else
		{
			bwapi.move(unitID, x, y);
		}
	}

	private void updateAttackLocation()
	{
		double attackPercentage = (bwapi.getFrameCount() - attackStartFrame) / attackFramesDifference;
		// don't go past the enemy base
		attackPercentage = Math.min(attackPercentage, 1);
		// units move out and meet half way first
		attackPercentage = Math.max(attackPercentage, 0.25);

		Point origin = new Point((bwapi.getMap().getHeight() * 32) / 2, (bwapi.getMap().getHeight() * 32) / 2);

		Point target = new Point(intelligenceManager.getEnemyStartLocation().getX(), intelligenceManager
				.getEnemyStartLocation().getY());

		double distance = Utility.getDistance(target.x, target.y, origin.x, origin.y);

		double unit_x = (target.x - origin.x) / distance;
		double unit_y = (target.y - origin.y) / distance;

		destination = new Point((int) (origin.x + (unit_x * (distance * attackPercentage))),
				(int) (origin.x + (unit_y * (distance * attackPercentage))));
	}

	public void gameUpdate()
	{
		// Draw the Zergling group on the screen
		if (AIClient.DEBUG)
		{
			bwapi.drawText(destination, "Destination", false);
			bwapi.drawText(0, 0, "Army state : " + state.toString(), true);
			bwapi.drawText(0, 16, "Force Size : " + getForceSize(), true);
		}

		// Set to defending if been retreating for a while
		if (state == State.RETREAT)
		{
			if (bwapi.getFrameCount() > startRetreatFrame + retreatLength)
			{
				state = State.DEFENDING;
				startRetreatFrame = 0;
			}
		}
		if (state == State.ATTACKING)
		{
			updateAttackLocation();
		}
		giveUnitOrders();
	}

	public void gameStarted()
	{
		previousUpdateTime = 0;
		/* Set the start destination to be the chokepoint of the starting area */
		setDestinationToChokePoint();
		workersNeedReset = false;
	}

	public void unitMorph(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			int prevGroup = isUnitInGroups(unitID);
			if( prevGroup != Utility.NOT_SET)
			{
				HashSet<Integer> unitGroup = unitGroups.get(prevGroup);
				unitGroup.remove(unitID);
			}
			
			
			HashSet<Integer> unitGroup = unitGroups.get(unit.getTypeID());
			if(unitGroup == null)
			{
				HashSet<Integer> newGroup = new HashSet<Integer>();
				newGroup.add(unitID);
				unitGroups.put(unit.getTypeID(), newGroup);
			}
			else
			{
				unitGroup.add(unitID);
			}
				
			
			if (unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal())
			{
				workers.add(unitID);
			}
		}
	}

	public void unitCreate(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getPlayerID() == bwapi.getSelf().getID())
		{
			
			HashSet<Integer> unitGroup = unitGroups.get(unit.getTypeID());
			if(unitGroup == null)
			{
				HashSet<Integer> newGroup = new HashSet<Integer>();
				newGroup.add(unitID);
				unitGroups.put(unit.getTypeID(), newGroup);
			}
			else
			{
				unitGroup.add(unitID);
			}
			
			
			if (unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal())
			{
				workers.add(unitID);
			}
		}
	}

	public void unitDestroy(int unitID)
	{
		int prevGroup = isUnitInGroups(unitID);
		if( prevGroup != Utility.NOT_SET)
		{
			HashSet<Integer> unitGroup = unitGroups.get(prevGroup);
			unitGroup.remove(unitID);
		}
		
		
		if (workers.contains(unitID))
		{
			workers.remove(unitID);
		}
	}
	
	private int isUnitInGroups( int unitID )
	{
		for(Integer groupKey : unitGroups.keySet())
		{
			HashSet<Integer> group = unitGroups.get(groupKey);
			if(group.contains(unitID))
			{
				return groupKey;
			}
		}
		return Utility.NOT_SET;
	}

}
