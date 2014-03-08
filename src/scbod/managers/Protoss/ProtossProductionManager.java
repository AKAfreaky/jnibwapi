package scbod.managers.Protoss;

import java.awt.Point;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
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
	
	
	public boolean trainProbe()
	{
		boolean ccExists = false;
		
		for(Unit unit : bwapi.getMyUnits())
		{
			if(unit.getTypeID() == UnitTypes.Protoss_Nexus.ordinal())
			{
				if( resourceManager.getMineralCount() >= 50 && resourceManager.getSupplyAvailable() >= 1)
				{
					buildQueue.add(new Point(unit.getID(), UnitTypes.Protoss_Probe.ordinal()));
					return true;
				}
				else
				{
					System.out.println("Don't have the supply or minerals to build a probe");
				}
				
				ccExists = true;
			}
		}
		
		if (!ccExists)
		{
			System.out.println("No nexus found (!!!)");
		}
		
		
		return false;
	}
	
	public boolean trainZealot()
	{
		Unit gateway = buildingManager.getLeastBusyGateway();
		
		if (gateway != null)
		{
			if( resourceManager.getMineralCount() >= 100 && resourceManager.getSupplyAvailable() >= 1)
			{
				buildQueue.add(new Point(gateway.getID(), UnitTypes.Protoss_Zealot.ordinal()));
				return true;
			}
			else
			{
				System.out.println("Don't have the supply or minerals to build a zealot");
			}
		}
		else
		{
			System.out.println("No Gateway?");
		}
		
		
		return false;
	}

}
