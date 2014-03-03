package scbod.managers.Protoss;

import java.awt.Point;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import scbod.BaseInfo;
import scbod.Direction;
import scbod.Utility;
import scbod.managers.BuildingManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;

/**
 * Protoss Building Manager * * *
 * 
 * Protoss specific actions related to building stuff
 */
public class ProtossBuildingManager extends BuildingManager
{
	public ProtossBuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		super(bwapi, unitManager, workerManager, resourceManager);
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
								new Point(baseRight	+ 6	, baseTop - 7),
								new Point(baseRight	+ 6	, baseTop), 
								new Point(baseRight	+ 6	, baseBottom + 5),
								new Point(baseLeft		, baseBottom + 5),
								new Point(baseLeft	- 7	, baseBottom + 5)
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
	
	public Unit getLeastBusyGateway()
	{
		// Totally not copied from the terran...
		Unit chosenRax = null;
		int smallestQueue = Utility.NOT_SET;
		
		for(Unit unit : unitManager.getMyUnitsOfType(UnitTypes.Protoss_Gateway.ordinal(), true))
		{
			int queue = unit.getTrainingQueueSize();
			if(chosenRax == null || queue < smallestQueue)
			{
				chosenRax		= unit;
				smallestQueue	= queue;
			}
		}
		
		return chosenRax;
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
	
	/** The number of pylons */
	public int getPylonCount()
	{
		return (unitManager.getMyUnitsOfType(UnitTypes.Protoss_Pylon.ordinal(), false).size());
	}
	
	/** The number of pylons still warping in */
	public int getUnfinishedPylonCount()
	{
		return( unitManager.getMyUnFinishedUnitsOfType(UnitTypes.Protoss_Pylon.ordinal()).size());
	}
	
	/** Get a probe to summon a gateway*/ 
	public boolean buildGateway()
	{
		if (resourceManager.getMineralCount() < 150)
		{
			System.out.println("Need 150 minerals to build a gateway");
			return false;
		}
	
		Point buildLocation = getNextBuildLocation();
	
		if (buildBuilding(UnitTypes.Protoss_Gateway.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	/** The number of summoned gateways */
	public int getGatewayCount()
	{
		return (unitManager.getMyUnitsOfType(UnitTypes.Protoss_Gateway.ordinal(), false).size());
	}
	
	/** The number of gateways ready to build */
	public int getCompletedGatewayCount()
	{
		return( unitManager.getUnitCount(UnitTypes.Protoss_Gateway.ordinal(), true));
	}
	
	/** Get a probe to summon a forge*/ 
	public boolean buildForge()
	{
		if (resourceManager.getMineralCount() < 150)
		{
			System.out.println("Need 150 minerals to build a forge");
			return false;
		}
	
		Point buildLocation = getNextBuildLocation();
	
		if (buildBuilding(UnitTypes.Protoss_Forge.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	/** How many Forges have been summoned */
	public int getForgeCount()
	{
		return( unitManager.getUnitCount(UnitTypes.Protoss_Forge.ordinal(), false));
	}
	
	/** How many Forges are ready to research */
	public int getCompletedForgeCount()
	{
		return( unitManager.getUnitCount(UnitTypes.Protoss_Forge.ordinal(), true)); 
	}
	
	public int getFreeForgeCount()
	{
		int count = 0;
		for( Unit unit : unitManager.getMyUnitsOfType(UnitTypes.Protoss_Forge.ordinal(), true))
		{
			if(!unit.isUpgrading())
			{
				count++;
			}
		}
		
		return count;
	}
	
	public Unit getFreeForge()
	{
		for( Unit unit : unitManager.getMyUnitsOfType(UnitTypes.Protoss_Forge.ordinal(), true))
		{
			if(!unit.isUpgrading())
			{
				return unit;
			}
		}
		
		return null;
	}

	
}
