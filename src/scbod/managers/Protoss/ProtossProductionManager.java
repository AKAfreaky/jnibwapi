package scbod.managers.Protoss;

import java.awt.Point;
import java.util.ArrayList;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.managers.BuildingManager;
import scbod.managers.ProductionManager;
import scbod.managers.ResourceManager;

public class ProtossProductionManager extends ProductionManager
{
	private ProtossBuildingManager buildingManager;
	private ArrayList<Point> buildQueue = new ArrayList<Point>();
	
	
	public ProtossProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, BuildingManager buildingManager)
	{
		this.bwapi = bwapi;
		this.resourceManager = resourceManager;
		this.buildingManager = (ProtossBuildingManager) buildingManager;
	}
	
	@Override
	public void gameUpdate()
	{
		int incomingSupply = buildingManager.getUnfinishedPylonCount() * 8; 
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal() + incomingSupply);
		
		if(buildQueue.size() > 0)
		{
			for(Point point: buildQueue)
			{
				bwapi.train(point.x, point.y);
			}
			
			buildQueue.clear();
		}
		
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
					bwapi.train(unit.getID(), UnitTypes.Protoss_Probe.ordinal());
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
				return bwapi.train(gateway.getID(), UnitTypes.Protoss_Zealot.ordinal());
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
	
	
	

	@Override
	public boolean spawn(UnitTypes unitType)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
