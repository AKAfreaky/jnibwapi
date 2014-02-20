package scbod;

import java.awt.Point;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType.UnitTypes;

public class TerranBuildingManager extends BuildingManager 
{
	public TerranBuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		super(bwapi, unitManager, workerManager, resourceManager);
	}
	
	public boolean buildSupplyDepot()
	{
		if (resourceManager.getMineralCount() < 100)
		{
			System.out.println("Need 100 minerals to build supply depot");
			return false;
		}
	
		Point buildLocation = getNextBuildLocation();
	
		if (buildBuilding(UnitTypes.Terran_Supply_Depot.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
}
