package scbod.managers.Protoss;

import java.awt.Point;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.BaseInfo;
import scbod.Direction;
import scbod.managers.BuildingManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

public class ProtossBuildingManager extends BuildingManager
{
	public ProtossBuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		super(bwapi, unitManager, workerManager, resourceManager);
	}
	
	/** Get a probe to summon a pylon*/ 
	public boolean buildPylon()
	{
		if (resourceManager.getMineralCount() < 100)
		{
			System.out.println("Need 100 minerals to build a pylon");
			return false;
		}
	
		Point buildLocation = getNextBuildLocation();
	
		if (buildBuilding(UnitTypes.Protoss_Pylon.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	/** The number of completed pylons */
	public int pylonCount()
	{
		return (unitManager.getMyUnitsOfType(UnitTypes.Protoss_Pylon.ordinal(), true).size());
	}
	
	/** Calculates all of the build locations for a given hatchery / expansion */
	@Override
	protected void calculateBuildLocationsForBase(BaseInfo baseInfo)
	{
		Direction mineralDirection = getMineralDirection(baseInfo.structure);
		Direction geyserDirection = getGeyserDirection(baseInfo.structure);

		if (geyserDirection != null)
		{
			System.out.println("Geyser direction : " + geyserDirection.toString());
		}
		if (mineralDirection != null)
		{
			System.out.println("Mineral direction : " + mineralDirection.toString());
		}
		
		// The tile edges of the base structure
		int baseLeft	= baseInfo.structure.getTileX();
		int baseTop		= baseInfo.structure.getTileY();
		int baseRight	= baseLeft	+	bwapi.getUnitType(baseInfo.structure.getTypeID()).getTileWidth();
		int baseBottom	= baseTop	+	bwapi.getUnitType(baseInfo.structure.getTypeID()).getTileHeight();
		
		// Default locations in all cardinal directions
		Point[] locations = {	
								new Point(baseLeft	- 7	, baseTop),	
								new Point(baseLeft	- 7	, baseTop - 7),
								new Point(baseLeft		, baseTop - 7),
								new Point(baseRight	+ 7	, baseTop - 7),
								new Point(baseRight	+ 7	, baseTop), 
								new Point(baseRight	+ 7	, baseBottom + 7),
								new Point(baseLeft		, baseBottom + 7),
								new Point(baseLeft	- 7	, baseBottom + 7)
							};
		
		
		cullLocationsForDirection(geyserDirection, locations);
		cullLocationsForDirection(mineralDirection, locations);
		
		for(Point loc : locations)
		{
			if (loc != null)
			{
				buildLocations.add(loc);
				baseInfo.buildingIndexes.add(buildLocations.size() - 1);
			}
		}
		
		// Defence location is updated to be the last ones, so that defences are
		// built at expansions first.
		nextDefenceLocation = buildLocations.size() - 1;
	}
	
	/** Add build locations around a pylon */
	private void calculateBuildLocationsForPylon(int unitID)
	{
		int tileX = bwapi.getUnit(unitID).getTileX();
		int tileY = bwapi.getUnit(unitID).getTileY();
		
		bwapi.getUnit(getNearestTownHall(unitID));
		
		buildLocations.add(new Point(tileX - 5, tileY));
		buildLocations.add(new Point(tileX + 3, tileY));
		buildLocations.add(new Point(tileX, tileY - 4));
		buildLocations.add(new Point(tileX, tileY + 3));
		
	}
	
	@Override
	public void unitCreate(int unitID)
	{
		super.unitCreate(unitID);
		
		if(bwapi.getUnit(unitID).getTypeID() == UnitTypes.Protoss_Pylon.ordinal())
		{
			calculateBuildLocationsForPylon(unitID);
		}
		
	}
	
	
}
