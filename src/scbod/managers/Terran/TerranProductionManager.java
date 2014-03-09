package scbod.managers.Terran;

import scbod.managers.BuildingManager;
import scbod.managers.ProductionManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class TerranProductionManager extends ProductionManager {

	private TerranBuildingManager buildingManager;
	private UnitManager unitManager;
	
	public TerranProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager, UnitManager unitManager)
	{
		super(bwapi, resourceManager,unitManager);
		this.buildingManager = (TerranBuildingManager) buildingManager;
	}
	
	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
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
				if( resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 2)
				{
					bwapi.train(unit.getID(), UnitTypes.Terran_SCV.ordinal());
					return true;
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
	
	public boolean trainMarine()
	{
		boolean raxExists = false;
		
		Unit rax = buildingManager.getLeastBusyBarracks();
		
		if (rax != null)
		{
			raxExists = true;
			if( resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 2)
			{
				bwapi.train(rax.getID(), UnitTypes.Terran_Marine.ordinal());
				return true;
			}
			else
			{
				System.out.println("Don't have the supply or minerals to build a Marine");
			}
		}
		
		if (!raxExists)
		{
			System.out.println("No Barracks found");
		}
		
		
		return false;
	}
	
	public boolean trainMedic()
	{		
		if(unitManager.getMyUnitOfType(UnitTypes.Terran_Academy.ordinal(), true) != null)
		{
			Unit rax = buildingManager.getLeastBusyBarracks();
			
			if (rax != null)
			{
				if( resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 2)
				{
					bwapi.train(rax.getID(), UnitTypes.Terran_Medic.ordinal());
					return true;
				}
				else
				{
					System.out.println("Don't have the supply or minerals to build a Medic");
				}
			}
			else
			{
				System.out.println("No Barracks found");
			}
		}
		else
		{
			System.out.println("Need an Academy to train Medics!");
		}
		
		return false;
	}

}
