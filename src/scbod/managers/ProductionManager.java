package scbod.managers;

import java.util.ArrayList;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import scbod.IntTriple;

/**
 * Production Manager has general methods for the production of units and addons
 * 
 * @author Alex Aiton
 */
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
	protected ArrayList<IntTriple>		buildQueue = new ArrayList<IntTriple>();
	
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
						buildQueue.add(new IntTriple(IntTriple.ADDON, buildUnitID, unitTypeID));
					}
					else if(bwapi.getUnitType(builderTypeID).isBuilding())
					{
						buildQueue.add(new IntTriple(IntTriple.TRAIN, buildUnitID, unitTypeID));
						
						unitManager.addUnitInTraining(unitTypeID);
					}
					else
					{
						buildQueue.add(new IntTriple(IntTriple.MORPH, buildUnitID, unitTypeID));
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
			for(IntTriple triple: buildQueue)
			{
				switch(triple.x)
				{
					case IntTriple.TRAIN:
						bwapi.train(triple.y, triple.z);
						break;
					case IntTriple.ADDON:
						bwapi.buildAddon(triple.y, triple.z);
						break;
					case IntTriple.MORPH:
						bwapi.morph(triple.y, triple.z);
						break;
					default:
						break;
						
				}
			}
			
			buildQueue.clear();
		}
	}
	
}
