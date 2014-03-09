package scbod.managers;

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
	
	protected class Triple
	{
		public static final int TRAIN = 1;
		public static final int ADDON = 2;
		public static final int MORPH = 3;
		
		int type;
		int producerID;
		int productID;
		
		public Triple(int type, int producerID, int productID)
		{
			this.type = type;
			this.producerID = producerID;
			this.productID = productID;
		}
	}
	
	protected ResourceManager		resourceManager;
	protected UnitManager			unitManager;
	protected JNIBWAPI				bwapi;
	protected ArrayList<Triple>		buildQueue = new ArrayList<Triple>();
	
	public boolean produceUnit(UnitType.UnitTypes unitType)
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
					if(bwapi.getUnitType(unitTypeID).isAddon())
					{
						buildQueue.add(new Triple(Triple.ADDON, buildUnitID, unitTypeID));
					}
					else if(bwapi.getUnitType(builderTypeID).isBuilding())
					{
						buildQueue.add(new Triple(Triple.TRAIN, buildUnitID, unitTypeID));
					}
					else
					{
						buildQueue.add(new Triple(Triple.MORPH, buildUnitID, unitTypeID));
					}
					
					return true;
				}
				else
				{
					System.out.println("The builder unit can't build a " + bwapi.getUnitType(unitTypeID).getName());
				}
			}
		}
		else
		{
			System.out.println("Don't have the required tech/resources to produce a " + bwapi.getUnitType(unitTypeID).getName());
		}
	
		return false;
	}
	
	@Override
	public void gameUpdate()
	{
		resourceManager.setPredictedSupplyTotal(resourceManager.getSupplyTotal());
		
		if(buildQueue.size() > 0)
		{
			for(Triple triple: buildQueue)
			{
				switch(triple.type)
				{
					case Triple.TRAIN:
						bwapi.train(triple.producerID, triple.productID);
						break;
					case Triple.ADDON:
						bwapi.buildAddon(triple.producerID, triple.productID);
						break;
					case Triple.MORPH:
						bwapi.morph(triple.producerID, triple.productID);
						break;
					default:
						break;
						
				}
			}
			
			buildQueue.clear();
		}
	}
	
}
