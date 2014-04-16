package scbod.managers.Terran;

import java.awt.Point;

import scbod.managers.BuildingManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;

/**
 * Terran Building Manager * * *
 * 
 * Terran specific actions related to building stuff
 * 
 * @author Alex Aiton
 */
public class TerranBuildingManager extends BuildingManager 
{
	public TerranBuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		super(bwapi, unitManager, workerManager, resourceManager);
	}	
	
	@Override
	public void unitCreate(int unitID)
	{
		super.unitCreate(unitID);
		if (bwapi.getUnitType(bwapi.getUnit(unitID).getTypeID()).isBuilding())
		{
			updateBuildLocations(unitID);
		}
	}
	
	private void updateBuildLocations(int unitID)
	{
		Unit building	= bwapi.getUnit(unitID);
		int leftTile	= building.getTileX();
		int topTile		= building.getTileY();
		int rightTile	= leftTile	+ bwapi.getUnitType(building.getTypeID()).getTileWidth();
		int bottomTile	= topTile	+ bwapi.getUnitType(building.getTypeID()).getTileHeight();
		
		System.out.println("Building dimensions were x: " + leftTile + " - " + rightTile + " and y: " + topTile + " - " + bottomTile);
		
		Point loc1;
		Point loc2;
		
		switch(getHatcheryDirection(bwapi.getUnit(unitID), bwapi.getUnit(getNearestTownHall(unitID)) ))
		{
			case East:
				loc1 = new Point(leftTile - 6, bottomTile);
				loc2 = new Point(leftTile - 6, topTile - 2);
				break;
			case North:
				loc1 = new Point(leftTile - 3, bottomTile + 3);
				loc2 = new Point(rightTile, bottomTile + 3);
				break;
			case South:
				loc1 = new Point(leftTile - 3, topTile - 5);
				loc2 = new Point(rightTile, topTile - 5);
				break;
			case West:
				loc1 = new Point(rightTile + 3, bottomTile);
				loc2 = new Point(rightTile + 3, topTile - 2);
				break;
			default:
				loc1 = null;
				loc2 = null;
				break;
			
		}
		
		if (loc1 != null && loc2 != null)
		{
			buildLocations.add(loc1);
			buildLocations.add(loc2);
		}
	}
}
