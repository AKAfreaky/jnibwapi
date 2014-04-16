package scbod.managers.Zerg;

import scbod.managers.BuildingManager;
import scbod.managers.ProductionManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/** 
 * Handles the spawning and production of new units
 * @author Simon Davies
 */
public class ZergProductionManager extends ProductionManager
{
	private ZergBuildingManager buildingManager;
	
	public ZergProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager, UnitManager unitManager)
	{
		super(bwapi, resourceManager,unitManager);
		this.buildingManager = (ZergBuildingManager) buildingManager;
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
	
	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
		// Update the resource manager with the number of overlords in
		// production
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal() + (8 * getOverlordsInProduction()));
	}

}
