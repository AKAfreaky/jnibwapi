package scbod.managers.Protoss;

import jnibwapi.JNIBWAPI;
import scbod.managers.BuildingManager;
import scbod.managers.ProductionManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;

public class ProtossProductionManager extends ProductionManager
{
	private ProtossBuildingManager buildingManager;	
	
	public ProtossProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager, UnitManager unitManager)
	{
		super(bwapi, resourceManager,unitManager);
		this.buildingManager = (ProtossBuildingManager) buildingManager;
	}
	
	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
		int incomingSupply = buildingManager.getUnfinishedPylonCount() * 8; 
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal() + incomingSupply);
	}

}
