package scbod.managers.Zerg;

import java.awt.Point;
import java.util.ArrayList;

import scbod.BaseInfo;
import scbod.Direction;
import scbod.managers.BuildingManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Zerg Building Manager * * *
 * 
 * Zerg specific actions related to building stuff
 */
public class ZergBuildingManager extends BuildingManager
{
	private ArrayList<BaseInfo>	creepColonies	= new ArrayList<BaseInfo>();

	public ZergBuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		super(bwapi, unitManager, workerManager, resourceManager);
	}

	/** Builds a macro hatchery */
	public boolean buildMacroHatchery()
	{
		Unit hatchery = getTownHall();
		if (resourceManager.getMineralCount() < 300)
		{
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if (hatchery == null)
		{
			System.out.println("No hatchery?");
			return false;
		}
		Point buildLocation = getNextBuildLocation();

		if (buildBuilding(UnitTypes.Zerg_Hatchery.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}

	/** Builds a spire */
	public boolean buildSpire()
	{
		if (resourceManager.getMineralCount() < 200 || resourceManager.getGasCount() < 200)
		{
			return false;
		}
		if (!hasLair(true))
		{
			return false;
		}

		Point buildLocation = getNextBuildLocation();

		if (buildBuilding(UnitTypes.Zerg_Spire.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}

	/** Builds a spawning pool! */
	public boolean buildSpawningPool()
	{

		Unit hatchery = getTownHall();
		if (resourceManager.getMineralCount() < 200)
		{
			System.out.println("Need 200 minerals to build spawning pool");
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if (hatchery == null)
		{
			System.out.println("Need a hatchery to build spawning pool");
			return false;
		}

		Point buildLocation = getNextBuildLocation();

		if (buildBuilding(UnitTypes.Zerg_Spawning_Pool.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}

	/** Builds a hydralisk den */
	public boolean buildHydraliskDen()
	{
		Unit pool = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spawning_Pool.ordinal(), true);
		if (resourceManager.getMineralCount() < 100 || resourceManager.getGasCount() < 50)
		{
			return false;
		}
		// No spawning pool, can't do it anyways, give up
		if (pool == null)
		{
			return false;
		}

		Point buildLocation = getNextBuildLocation();

		if (buildBuilding(UnitTypes.Zerg_Hydralisk_Den.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}

	public boolean buildEvolutionChamber()
	{
		Unit hatchery = getTownHall();
		if (resourceManager.getMineralCount() < 75)
		{
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if (hatchery == null)
		{
			return false;
		}

		Point buildLocation = getNextBuildLocation();

		if (buildBuilding(UnitTypes.Zerg_Evolution_Chamber.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}

	/** Builds a creep colony ! */
	public boolean buildCreepColony()
	{
		Unit hatchery = getTownHall();
		if (resourceManager.getMineralCount() < 75)
		{
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if (hatchery == null)
		{
			return false;
		}

		Point buildLocation = getNextDefenceLocation();

		if (buildBuilding(UnitTypes.Zerg_Creep_Colony.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}

	public int getCompletedColonyCount()
	{
		return unitManager.getUnitCount(UnitTypes.Zerg_Creep_Colony.ordinal(), true) + getSunkenColonyCount();
	}

	public int getCompletedCreepColonyCount()
	{
		return unitManager.getUnitCount(UnitTypes.Zerg_Creep_Colony.ordinal(), true);
	}

	/** Upgrades a random creep colony to sunken colony */
	public boolean upgradeSunkenColony()
	{
		Unit creep = unitManager.getMyUnitOfType(UnitTypes.Zerg_Creep_Colony.ordinal(), true);
		if (creep == null)
		{
			return false;
		}
		if (!hasSpawningPool(true))
		{
			return false;
		}
		if (resourceManager.getMineralCount() < 50)
		{
			return false;
		}
		bwapi.morph(creep.getID(), UnitTypes.Zerg_Sunken_Colony.ordinal());
		return true;
	}

	public int getSunkenColonyCount()
	{
		return unitManager.getUnitCount(UnitTypes.Zerg_Sunken_Colony.ordinal(), false);
	}

	/** Upgrades a hatchery to a lair */
	public boolean upgradeToLair()
	{
		Unit hatchery = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal(), true);
		;
		if (hatchery == null)
		{
			return false;
		}
		if (!hasSpawningPool(true))
		{
			return false;
		}
		if (resourceManager.getMineralCount() < 150 || resourceManager.getGasCount() < 100)
		{
			return false;
		}
		bwapi.morph(hatchery.getID(), UnitTypes.Zerg_Lair.ordinal());
		return true;
	}

	/**
	 * Returns whether the AI has built an extractor yet.
	 * 
	 * @param completed
	 *            If true, will return false if the building is still in
	 *            progress
	 * @return
	 */
	public boolean hasSpawningPool(boolean completed)
	{
		// Find a pool
		Unit pool = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spawning_Pool.ordinal(), completed);
		if (pool == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Checks whether there is a lair in progress, or completed
	 * 
	 * @param completed
	 *            If true, will return false if the building is still in
	 *            progress
	 * @return
	 */
	public boolean hasLair(boolean completed)
	{
		Unit lair = null;
		lair = unitManager.getMyUnitOfType(UnitTypes.Zerg_Lair.ordinal(), completed);
		if (lair == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Checks whether there is a hydralisk den built.
	 * 
	 * @param completed
	 *            If true, will return false if the building is still in
	 *            progress
	 * @return
	 */
	public boolean hasHydraliskDen(boolean completed)
	{
		Unit den = null;
		den = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hydralisk_Den.ordinal(), completed);
		if (den == null)
		{
			return false;
		}
		else
		{
			return true;
		}

	}

	/**
	 * Checks whether there is a spire
	 * 
	 * @param completed
	 *            If true, will return false if the building is still in
	 *            progress
	 * @return
	 */
	public boolean hasSpire(boolean completed)
	{
		Unit spire = null;
		spire = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spire.ordinal(), completed);
		if (spire == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Get the number of evolution chambers
	 * 
	 * @param completed
	 *            If true, will only count completed evolution chambers
	 * @return
	 */
	public int getEvolutionChamberCount(boolean completed)
	{
		return unitManager.getUnitCount(UnitTypes.Zerg_Evolution_Chamber.ordinal(), true);
	}

	private void checkForCompletedColonies()
	{
		for (Unit unit : bwapi.getAllUnits())
		{
			if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
			{

				// Check for colonies
				// if the colony is completed
				// and has not already been added, then add it to the base infos
				if ((unit.getTypeID() == UnitTypes.Zerg_Creep_Colony.ordinal() || unit.getTypeID() == UnitTypes.Zerg_Sunken_Colony
						.ordinal())
						&& unit.getPlayerID() == bwapi.getSelf().getID()
						&& unit.isCompleted()
						&& !colonyInfoExists(unit.getID()))
				{
					BaseInfo newColony = new BaseInfo(unit);
					calculateBuildLocationsColony(newColony, getTownHall());
					creepColonies.add(newColony);
					System.out.println("New colony made!");
				}
			}
		}
	}

	private boolean colonyInfoExists(int unitID)
	{
		for (BaseInfo info : creepColonies)
		{
			if (info.id == unitID)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
		checkForCompletedColonies();
	}

	@Override
	public void unitDestroy(int unitID)
	{
		for (BaseInfo info : creepColonies)
		{
			if (info.id == unitID)
			{
				System.out.println("TEST: Creep colony destroyed!");
				for (int index : info.buildingIndexes)
				{
					buildLocations.remove(index);
					System.out.println("TEST : Removing colony build locations");
				}
			}
		}
		super.unitDestroy(unitID);

	}

	@Override
	public boolean build(UnitType.UnitTypes buildType)
	{
		switch (buildType)
		{
			case Zerg_Creep_Colony:
				return buildCreepColony();
			case Zerg_Evolution_Chamber:
				return buildEvolutionChamber();
			case Zerg_Hydralisk_Den:
				return buildHydraliskDen();
			case Zerg_Hatchery:
				return buildMacroHatchery();
			case Zerg_Spawning_Pool:
				return buildSpawningPool();
			case Zerg_Spire:
				return buildSpire();
			default:
				System.out.println("Can't build that building!");
				break;
		}
		return false;
	}

	/** Calculates all of the build locations for a given creep colony */
	protected void calculateBuildLocationsColony(BaseInfo colonyInfo, Unit hatchery)
	{

		Direction hatcheryDirection = getHatcheryDirection(colonyInfo.structure, hatchery);

		int colonyX = colonyInfo.structure.getTileX();
		int colonyY = colonyInfo.structure.getTileY();

		if (hatcheryDirection == null)
		{
			return;
		}
		switch (hatcheryDirection)
		{
			case East:
				buildLocations.add(new Point(colonyX - 3, colonyY + 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX - 3, colonyY - 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
			case South:
				buildLocations.add(new Point(colonyX + 1, colonyY - 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX - 1, colonyY - 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
			case West:
				buildLocations.add(new Point(colonyX + 3, colonyY + 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX + 3, colonyY - 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
			case North:
				buildLocations.add(new Point(colonyX + 1, colonyY + 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX - 1, colonyY + 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
		}
		System.out.println("Colony locations added!");
	}

}