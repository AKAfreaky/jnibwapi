package scbod;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class TerranProductionManager extends ProductionManager {

	private TerranBuildingManager buildingManager;
	
	public TerranProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager)
	{
		this.bwapi = bwapi;
		this.resourceManager = resourceManager;
		this.buildingManager = (TerranBuildingManager) buildingManager;
	}
	
	@Override
	public void gameUpdate()
	{
		// TODO: update with the number of supply depots being built
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal());
	}
	
	public boolean trainSCV()
	{
		boolean ccExists = false;
		for(Unit unit : bwapi.getMyUnits())
		{
			if(unit.getTypeID() == UnitTypes.Terran_Command_Center.ordinal())
			{
				if( resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 1)
				{
					return bwapi.train(unit.getID(), UnitTypes.Terran_SCV.ordinal());
				}
				else
				{
					System.out.println("Don't have the supply or minerals to build an SCV");
				}
				
				ccExists = true;
			}
		}
		
		if (!ccExists)
		{
			System.out.println("No command center found (!!!)");
		}
		
		
		return false;
	}
	
	@Override
	public boolean spawn(UnitTypes unitType) {
		// TODO Auto-generated method stub
		return false;
	}

}
