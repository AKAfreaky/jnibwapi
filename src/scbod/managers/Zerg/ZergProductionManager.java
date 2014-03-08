package scbod.managers.Zerg;

import scbod.managers.BuildingManager;
import scbod.managers.ProductionManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;

/** Handles the spawning and production of new units */
public class ZergProductionManager extends ProductionManager
{
	private ZergBuildingManager buildingManager;
	
	public ZergProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager, UnitManager unitManager)
	{
		super(bwapi, resourceManager,unitManager);
		this.buildingManager = (ZergBuildingManager) buildingManager;
	}

	/**
	 * Builds a drone from any larvae Returns true if successful, else false.
	 */
	public boolean spawnDrone()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.ordinal())
			{
				if (resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 1)
				{
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Drone.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Builds a pair of zerglings from any larvae Returns true if successful,
	 * else false.
	 */
	public boolean spawnZerglings()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.ordinal())
			{
				if (resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 1
						&& buildingManager.hasSpawningPool(true))
				{
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Zergling.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Builds a hydralisk from any larvae Returns true if successful, else
	 * false.
	 */
	public boolean spawnHydralisk()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.ordinal())
			{
				if (resourceManager.getMineralCount() >= 75 && resourceManager.getGasCount() >= 25
						&& resourceManager.getSupplyAvailable() >= 1 && buildingManager.hasHydraliskDen(true))
				{
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Hydralisk.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Builds a mutalisk from any larvae Returns true if successful, else false.
	 */
	public boolean spawnMutalisk()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.ordinal())
			{
				if (resourceManager.getMineralCount() >= 100 && resourceManager.getGasCount() >= 100
						&& resourceManager.getSupplyAvailable() >= 2 && buildingManager.hasSpire(true))
				{
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Mutalisk.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Builds an overlord from any larvae Returns true if successful, else
	 * false.
	 */
	public boolean spawnOverlord()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.ordinal())
			{
				if (resourceManager.getMineralCount() >= 100)
				{
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Overlord.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	/** Returns the number of overlords that are currently in production */
	public int getOverlordsInProduction()
	{
		int count = 0;
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Egg.ordinal())
			{
				if (unit.getBuildTypeID() == UnitTypes.Zerg_Overlord.ordinal())
				{
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Returns the number of Larvae currently available to spawn into something
	 * useful
	 */
	public int getLarvaCount()
	{
		int count = 0;
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.ordinal())
			{
				count++;
			}
		}
		return count;
	}

	public boolean spawnLurker()
	{
		if (!bwapi.getSelf().hasResearched(TechTypes.Lurker_Aspect.ordinal()))
		{
			return false;
		}
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Hydralisk.ordinal())
			{
				if (resourceManager.getMineralCount() >= 50 && resourceManager.getGasCount() >= 100
						&& resourceManager.getSupplyAvailable() >= 1 && buildingManager.hasHydraliskDen(true))
				{
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Lurker.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
		// Update the resource manager with the number of overlords in
		// production
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal() + (8 * getOverlordsInProduction()));
	}

}
