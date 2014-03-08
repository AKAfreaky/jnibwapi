package scbod.managers;

import java.awt.Point;
import java.util.ArrayList;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

public class ProductionManager extends Manager
{
	public ProductionManager(JNIBWAPI bwapi, ResourceManager resourceManager, UnitManager unitManager)
	{
		this.bwapi = bwapi;
		this.resourceManager = resourceManager;
		this.unitManager = unitManager;
	}
	
	protected ResourceManager		resourceManager;
	protected UnitManager			unitManager;
	protected JNIBWAPI				bwapi;
	protected ArrayList<Point>		buildQueue = new ArrayList<Point>();
	protected ArrayList<Point>		morphQueue = new ArrayList<Point>();
	
	public boolean spawn(UnitType.UnitTypes unitType)
	{		
		
		//TODO: Need a special case for Protoss Archons/Dark Archons which take two of the relevent templars to spawn
		
		int unitTypeID = unitType.ordinal();
		if( bwapi.canMake(unitTypeID) )
		{
			int builderTypeID = bwapi.getUnitType(unitTypeID).getWhatBuildID();
			Unit buildUnit = unitManager.getLeastBusyUnitofType(builderTypeID);
			
			if (buildUnit != null)
			{
				int buildUnitID = buildUnit.getID();
				
				if( bwapi.canMake(buildUnitID, unitTypeID) )
				{
					
					if(bwapi.getUnitType(builderTypeID).isBuilding())
					{
						buildQueue.add(new Point(buildUnitID, unitTypeID));
					}
					else
					{
						morphQueue.add(new Point(buildUnitID, unitTypeID));
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void gameUpdate()
	{
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal());
		
		// Bit of an abuse of the Point class here, but Java don't have a standard pair so fuck 'em
		if(buildQueue.size() > 0)
		{
			for(Point point: buildQueue)
			{
				bwapi.train(point.x, point.y);
			}
			
			buildQueue.clear();
		}		
		
		if(morphQueue.size() > 0)
		{
			for(Point point: morphQueue)
			{
				bwapi.morph(point.x, point.y);
			}
			
			buildQueue.clear();
		}	
		
		
	}
	
}
