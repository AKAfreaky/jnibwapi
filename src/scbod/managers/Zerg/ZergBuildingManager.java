package scbod.managers.Zerg;

import java.awt.Point;
import java.util.ArrayList;

import scbod.BaseInfo;
import scbod.Direction;
import scbod.managers.BuildingManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.WorkerManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Zerg Building Manager * * *
 * 
 * Zerg specific actions related to building stuff
 * 
 * @author Simon Davies
 */
public class ZergBuildingManager extends BuildingManager
{
	private ArrayList<BaseInfo>	creepColonies	= new ArrayList<BaseInfo>();

	public ZergBuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		super(bwapi, unitManager, workerManager, resourceManager);
	}

	private void checkForCompletedColonies()
	{
		for (Unit unit : bwapi.getAllUnits())
		{
			if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
			{

				// Check for colonies
				// if the colony is completed
				// and has not already been added, then add it to the base infos
				if ((unit.getTypeID() == UnitTypes.Zerg_Creep_Colony.ordinal() || unit.getTypeID() == UnitTypes.Zerg_Sunken_Colony
						.ordinal())
						&& unit.getPlayerID() == bwapi.getSelf().getID()
						&& unit.isCompleted()
						&& !colonyInfoExists(unit.getID()))
				{
					BaseInfo newColony = new BaseInfo(unit);
					calculateBuildLocationsColony(newColony, getTownHall());
					creepColonies.add(newColony);
					System.out.println("New colony made!");
				}
			}
		}
	}

	private boolean colonyInfoExists(int unitID)
	{
		for (BaseInfo info : creepColonies)
		{
			if (info.id == unitID)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void gameUpdate()
	{
		super.gameUpdate();
		checkForCompletedColonies();
	}

	@Override
	public void unitDestroy(int unitID)
	{
		for (BaseInfo info : creepColonies)
		{
			if (info.id == unitID)
			{
				System.out.println("TEST: Creep colony destroyed!");
				for (int index : info.buildingIndexes)
				{
					buildLocations.remove(index);
					System.out.println("TEST : Removing colony build locations");
				}
			}
		}
		super.unitDestroy(unitID);

	}

	/** Calculates all of the build locations for a given creep colony */
	protected void calculateBuildLocationsColony(BaseInfo colonyInfo, Unit hatchery)
	{

		Direction hatcheryDirection = getHatcheryDirection(colonyInfo.structure, hatchery);

		int colonyX = colonyInfo.structure.getTileX();
		int colonyY = colonyInfo.structure.getTileY();

		if (hatcheryDirection == null)
		{
			return;
		}
		switch (hatcheryDirection)
		{
			case East:
				buildLocations.add(new Point(colonyX - 3, colonyY + 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX - 3, colonyY - 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
			case South:
				buildLocations.add(new Point(colonyX + 1, colonyY - 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX - 1, colonyY - 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
			case West:
				buildLocations.add(new Point(colonyX + 3, colonyY + 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX + 3, colonyY - 1));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
			case North:
				buildLocations.add(new Point(colonyX + 1, colonyY + 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				buildLocations.add(new Point(colonyX - 1, colonyY + 3));
				colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
				break;
		}
		System.out.println("Colony locations added!");
	}

}