package scbod.managers.Terran;

import scbod.managers.BuildingManager;
import scbod.managers.ProductionManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Stub sub-class for any Terran specific Production behaviours
 * 
 * @author Alex Aiton
 */
public class TerranProductionManager extends ProductionManager {

	private TerranBuildingManager buildingManager;
	
	public TerranProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager, UnitManager unitManager)
	{
		super(bwapi, resourceManager,unitManager);
		this.buildingManager = (TerranBuildingManager) buildingManager;
	}
	
	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
		int incomingSupply = unitManager.getMyUnFinishedUnitsOfType( UnitTypes.Terran_Supply_Depot.ordinal()).size() * 8;
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal() + incomingSupply);
	}

}
