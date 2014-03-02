package scbod.managers;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import scbod.AIClient;
import scbod.BaseInfo;
import scbod.BuildingInfo;
import scbod.Direction;
import scbod.Utility;
import scbod.Utility.CommonUnitType;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.util.BWColor;
import scbod.WorkerOrderData;
import scbod.WorkerOrderData.WorkerOrder;
import scbod.BuildLocations;

/**
 * Building Manager * * *
 * 
 * Manager for the construction of all of the structures, and their placement.
 */
public class BuildingManager extends Manager
{

	/** The build map array */
	private boolean					mapArray[][];

	/** identifies all known buildings */
	private ArrayList<BuildingInfo>	buildingsList		= new ArrayList<BuildingInfo>();

	/**
	 * The pre-determined build locations These are calculated at the beginning
	 * of the game and when new hatcheries or creep colonies are built, and
	 * these determine where buildings should be placed
	 */
	protected BuildLocations		buildLocations		= new BuildLocations();

	/** Index into the buildLocations array */
	private int						nextBuildLocation;
	protected int						nextDefenceLocation;

	/**
	 * Expansion locations, this is a list of expansion locations that should
	 * try and go to
	 */
	private ArrayList<BaseLocation>	expansionLocations	= new ArrayList<BaseLocation>();

	/** Index into the next expansion location to go to */
	private int						nextExpansionLocation;

	// Info used for generating new building locations
	protected ArrayList<BaseInfo>	baseBuildings		= new ArrayList<BaseInfo>();

	/* Index for the next expansion */
	private int						expansionIndex;

	private ArrayList<Integer>		expansionIDs		= new ArrayList<Integer>();

	/** ID for expansion building worker */
	private int						expansionWorker		= Utility.NOT_SET;

	protected int					baseTypeID;
	protected int					extractorTypeID;

	// Map sizes
	private int						mapSizeX;
	private int						mapSizeY;

	protected JNIBWAPI				bwapi;
	protected UnitManager			unitManager;
	private WorkerManager			workerManager;
	protected ResourceManager		resourceManager;

	private ArrayList<Integer>		builders			= new ArrayList<Integer>();

	public boolean build(UnitType.UnitTypes buildType)
	{
		System.out.println("Build doesn't work in the super class!");
		return false;
	}

	public BuildingManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager,
			ResourceManager resourceManager)
	{
		this.bwapi = bwapi;
		this.unitManager = unitManager;
		this.workerManager = workerManager;
		this.resourceManager = resourceManager;

		baseTypeID = Utility.getCommonTypeID(CommonUnitType.Base);
		extractorTypeID = Utility.getCommonTypeID(CommonUnitType.Extractor);
	}

	/** Draw all of the buildings squares held in each of the arrays */
	private void drawBuildingSquares()
	{
		for (BuildingInfo info : buildingsList)
		{
			bwapi.drawText(info.location, info.buildingType.getName(), false);
			bwapi.drawBox(info.tileLocation.x * 32, info.tileLocation.y * 32,
					(info.tileLocation.x + info.buildingType.getTileWidth()) * 32,
					(info.tileLocation.y + info.buildingType.getTileHeight()) * 32, BWColor.GREEN, false, false);
		}
		int i = 1;
		for (Point location : buildLocations)
		{
			bwapi.drawText(location.x * 32, location.y * 32, Integer.toString(i) + "(" + location.x + ", " + location.y + ")", false);
			bwapi.drawBox(location.x * 32, location.y * 32, (location.x + 2) * 32, (location.y + 2) * 32,
					BWColor.PURPLE, false, false);
			i++;
		}

		i = 1;
		for (BaseLocation expansion : expansionLocations)
		{
			bwapi.drawText(expansion.getTx() * 32, expansion.getTy() * 32, "Exp " + i, false);
			bwapi.drawBox(expansion.getTx() * 32, expansion.getTy() * 32, (expansion.getTx() + 2) * 32,
					(expansion.getTy() + 2) * 32, BWColor.YELLOW, false, false);
			i++;
		}
	}
	
	/** Sets locations in the passed directions to null in the passed location list
	 * Assumes list starts at west and goes clockwise */
	protected void cullLocationsForDirection(Direction dir, Point[] locations)
	{
		if (dir != null)
		{
			switch (dir)
			{
				case East:
					locations[3] = null;
					locations[4] = null;
					locations[5] = null;
					break;
				case North:
					locations[1] = null;
					locations[2] = null;
					locations[3] = null;
					break;
				case South:
					locations[5] = null;
					locations[6] = null;
					locations[7] = null;
					break;
				case West:
					locations[0] = null;
					locations[1] = null;
					locations[7] = null;
					break;
				default:
					break;
			}
		}
	}
	

	/** Calculates all of the build locations for a given hatchery / expansion */
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
								new Point(baseLeft	- 5	, baseTop),	
								new Point(baseLeft	- 5	, baseTop - 3),
								new Point(baseLeft		, baseTop - 4),
								new Point(baseRight	+ 2	, baseTop - 4),
								new Point(baseRight	+ 2	, baseTop), 
								new Point(baseRight	+ 2	, baseBottom + 2),
								new Point(baseLeft		, baseBottom + 2),
								new Point(baseLeft	- 5	, baseBottom + 2)
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

	protected Point getNextBuildLocation()
	{
		System.out.println("Getting building location");
		if (buildLocations.size() == 0)
		{
			System.out.println("Fail!");
			return null;
		}
		try
		{
			int index = nextBuildLocation;
			nextBuildLocation = (nextBuildLocation + 1) % (buildLocations.size());
			return buildLocations.get(index);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	protected Point getNextDefenceLocation()
	{
		System.out.println("Getting defence building location");
		if (buildLocations.size() == 0)
		{
			System.out.println("Fail!");
			return null;
		}
		try
		{
			System.out.println("Index  : " + nextDefenceLocation);
			System.out.println("nextDefenceLocation  : " + (nextDefenceLocation - 1) % (buildLocations.size() - 1));
			System.out.println("buildLocations.size()  : " + buildLocations.size());
			int index = nextDefenceLocation;
			nextDefenceLocation = (nextDefenceLocation - 1);
			if (nextDefenceLocation < 0)
			{
				nextDefenceLocation = buildLocations.size() - 1;
			}
			return buildLocations.get(index);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private void calculateExpansionLocations(Unit hatchery)
	{
		List<BaseLocation> iBaseList = bwapi.getMap().getBaseLocations(); // immutable
																			// base
																			// list
		ArrayList<BaseLocation> mBaseList = new ArrayList<BaseLocation>(iBaseList);
		BaseLocation homeBase = getNextClosestExpansionLocation(mBaseList, hatchery);
		mBaseList.remove(homeBase); // Remove the home base, don't care about it
		for (int i = 0; i < 3; i++)
		{
			BaseLocation expansion = getNextClosestExpansionLocation(mBaseList, hatchery);
			expansionLocations.add(expansion);
			mBaseList.remove(expansion);
			System.out.println("Added expansion location " + expansion.getTx() + " - " + expansion.getTy());
		}

	}

	private BaseLocation getNextClosestExpansionLocation(ArrayList<BaseLocation> expansions, Unit hatchery)
	{
		double closestDistance = Utility.NOT_SET;
		BaseLocation closestLocation = null;
		for (BaseLocation location : expansions)
		{
			double distance = Utility.getDistance(hatchery.getX(), hatchery.getY(), location.getX(), location.getY());
			if (distance < closestDistance || closestDistance == Utility.NOT_SET)
			{
				closestLocation = location;
				closestDistance = distance;
				continue;
			}
		}
		return closestLocation;
	}

	/**
	 * Gets the map build data for this level, which lets us know where the
	 * buildable space is in the game world.
	 */
	public boolean[][] getBuildableMapData()
	{
		mapSizeX = bwapi.getMap().getWidth();
		mapSizeY = bwapi.getMap().getHeight();
		boolean[][] buildableArray = new boolean[mapSizeX][mapSizeY];
		for (int y = 0; y < mapSizeY; y++)
		{
			for (int x = 0; x < mapSizeX; x++)
			{
				buildableArray[x][y] = bwapi.getMap().isBuildable(x, y);
			}
		}
		return buildableArray;
	}

	/**
	 * Updates the map information to tell the AI which tiles are no longer
	 * buildable
	 */
	private void buildingPlaced(int tileX, int tileY, int unitTypeID)
	{
		UnitType buildingType = bwapi.getUnitType(unitTypeID);
		// Not a building, ignore
		if (!buildingType.isBuilding())
		{
			return;
		}
		for (int x = tileX; x < tileX + buildingType.getTileWidth(); x++)
		{
			for (int y = tileY; y < tileY + buildingType.getTileHeight(); y++)
			{
				mapArray[x][y] = false;
			}
		}
		if (AIClient.DEBUG)
			printMap();
	}

	/** Updates the map information to tell the AI which tiles are now buildable */
	private void buildingRemoved(int tileX, int tileY, UnitType unitType)
	{
		// Not a building, ignore
		if (!unitType.isBuilding())
		{
			return;
		}
		for (int x = tileX; x < tileX + unitType.getTileWidth(); x++)
		{
			for (int y = tileY; y < tileY + unitType.getTileHeight(); y++)
			{
				mapArray[x][y] = true;
			}
		}
		if (AIClient.DEBUG)
			printMap();
	}

	/** Prints the known build map to a file, used for debugging purposes */
	private void printMap()
	{
		// TODO: Probably don't need this, but keep it in here for now.
		PrintWriter out;
		try
		{
			out = new PrintWriter(new FileWriter("map2.txt"));
			for (int y = 0; y < mapSizeY; y++)
			{
				for (int x = 0; x < mapSizeX; x++)
				{
					if (mapArray[x][y])
						out.print(".");
					else
						out.print("x");
				}
				out.print("\n");
			}
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public boolean buildBuilding(int buildingType, int tileX, int tileY)
	{
		Unit worker = workerManager.getNearestFreeWorker(tileX, tileY);
		return buildBuilding(buildingType, tileX, tileY, worker);
	}

	/**
	 * Builds the given building at the location. If the given position is not
	 * buildable, then the location is moved slightly based on a random value,
	 * until a buildable place can be found. This should not be relied upon, and
	 * higher level methods should instead predetermine where to build.
	 */
	public boolean buildBuilding(int buildingType, int tileX, int tileY, Unit worker)
	{
		// if not a valid build position, move it!
		Random random = new Random();
		int retries = 0;
		int timeout = 20;
		while (!validBuildPosition(buildingType, tileX, tileY) && retries < timeout)
		{
			System.out.println("Changing build position slightly.");
			tileX += (random.nextInt(4) - 2);
			tileY += (random.nextInt(4) - 2);
			retries++;
		}

		if (worker == null)
		{
			return false;
		}

		System.out.println("Using worker number " + worker.getID() + " to build unit " + buildingType);
		workerManager.queueOrder(new WorkerOrderData(WorkerOrder.Build, worker.getID(), tileX, tileY, buildingType));
		builders.add(worker.getID());
		return true;
	}

	/** Determines whether a location is a valid build position or not */
	private boolean validBuildPosition(int buildingType, int tileX, int tileY)
	{
		UnitType building = bwapi.getUnitType(buildingType);
		for (int x = tileX; x < tileX + building.getTileWidth(); x++)
		{
			for (int y = tileY; y < tileY + building.getTileHeight(); y++)
			{
				if (x >= mapSizeX || y >= mapSizeY || x < 0 || y < 0)
					return false;
				if (!mapArray[x][y])
					return false;
			}
		}
		return true;
	}

	/** Builds a gas extraction building on the nearest free geyser */
	public boolean buildExtractor()
	{
		/* First find the closest drone to the geyser */
		Unit worker = null;
		Unit geyser = null;

		if (resourceManager.getMineralCount() < 25)
		{
			return false;
		}

		Unit base = getTownHall();
		if (base == null)
		{
			return false;
		}
		Point baseLocation = new Point(base.getX(), base.getY());

		// Find the nearest free geyser
		geyser = Utility.getClosestUnitOfType(bwapi.getNeutralUnits(), baseLocation,
				UnitTypes.Resource_Vespene_Geyser.ordinal());

		// No free geyser found, give up.
		if (geyser == null)
		{
			return false;
		}

		worker = workerManager.getNearestFreeWorker(geyser.getTileX(), geyser.getTileY());

		if (worker == null)
		{
			return false;
		}

		workerManager.queueOrder(new WorkerOrderData(	WorkerOrder.Build,
														worker.getID(),
														geyser.getTileX(),
														geyser.getTileY(),
														extractorTypeID));
		builders.add(worker.getID());
		return true;
	}

	/**
	 * Send worker to expansion location - Also reserves the minerals so you can
	 * actually build it.
	 */
	public void sendWorkerToExpansionLocation()
	{
		BaseLocation location = expansionLocations.get(expansionIndex);
		Unit worker = workerManager.getNearestFreeWorker(location.getTx(), location.getTy());
		if (worker == null)
		{
			System.out.println("No free worker!");
			return;
		}

		workerManager.addBusyWorker(worker.getID());
		expansionWorker = worker.getID();
		workerManager.queueOrder(new WorkerOrderData(	WorkerOrder.Move,
														expansionWorker, 
														location.getX(), 
														location.getY()));
		resourceManager.reserveMinerals(300);
	}

	/** Builds an expansion hatchery */
	public boolean buildExpansionHatchery()
	{
		try
		{
			if (resourceManager.getReservedMineralCount() < 350)
			{
				return false;
			}
			BaseLocation expansionLocation = expansionLocations.get(expansionIndex);
			System.out.println("Go build!");
			System.out.println("Base location " + expansionLocation.getTx() + expansionLocation.getTy());
			Unit worker = bwapi.getUnit(expansionWorker);
			if (buildBuilding(baseTypeID, expansionLocation.getTx(), expansionLocation.getTy(), worker))
			{
				System.out.println("New hatchery added to expansions");
				Unit hatchery = worker;
				BaseInfo newExpansion = new BaseInfo(hatchery);
				newExpansion.hatcheryWaitTimer = bwapi.getFrameCount();
				baseBuildings.add(newExpansion);
				expansionIDs.add(hatchery.getID());
				expansionWorker = Utility.NOT_SET;
				resourceManager.reserveMinerals(0);
				expansionIndex++;
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public boolean expansionDroneReady()
	{
		if (bwapi.getUnit(expansionWorker) == null)
		{
			return false;
		}
		if (expansionWorker == Utility.NOT_SET)
		{
			return false;
		}
		if (bwapi.getUnit(expansionWorker).isIdle())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * Called when a unit is destroyed. If the unit is a players hatchery or
	 * colony, then all of the associated build locations should be removed
	 */
	private void checkForBuildLocationRemoval(int unitID)
	{
		for (BaseInfo info : baseBuildings)
		{
			if (info.id == unitID)
			{
				System.out.println("TEST: Expo destroyed");
				for (int index : info.buildingIndexes)
				{
					buildLocations.remove(index);
					System.out.println("TEST: Removing build locations");
				}
			}
		}
		if (nextBuildLocation >= buildLocations.size())
		{
			nextBuildLocation = 0;
		}
		if (nextDefenceLocation >= buildLocations.size())
		{
			nextDefenceLocation = buildLocations.size() - 1;
		}
	}

	private BaseLocation getNextExpansionLocation()
	{
		BaseLocation location = expansionLocations.get(nextExpansionLocation);
		nextExpansionLocation = (nextExpansionLocation + 1) % expansionLocations.size();
		return location;
	}

	public int getExtractorCount()
	{
		return unitManager.getUnitCount(extractorTypeID, false);
	}

	/** Do we have one extractor for each base? */
	public boolean hasExtractorSaturation()
	{
		return unitManager.getUnitCount(extractorTypeID, false) >= (expansionIDs.size() + 1);
	}

	/** have all the extractors been completed? */
	public boolean allExtractorsCompleted()
	{
		return (unitManager.getUnitCount(extractorTypeID, false)) == (unitManager.getUnitCount(extractorTypeID, true));
	}

	/** Determines the direction of a hatchery to another building */
	public Direction getHatcheryDirection(Unit colony, Unit hatchery)
	{
		// No hatchery found, give up?
		if (hatchery == null)
		{
			return null;
		}

		int distanceX = colony.getX() - hatchery.getX();
		int distanceY = colony.getY() - hatchery.getY();

		// Is the X direction more important, or the Y direction
		if (Math.abs(distanceX) > Math.abs(distanceY))
		{
			if (distanceX > 0)
			{
				return Direction.West;
			}
			else
			{
				return Direction.East;
			}
		}
		else
		{
			if (distanceY < 0)
			{
				return Direction.South;
			}
			else
			{
				return Direction.North;
			}
		}
	}

	/** Determines the direction of the minerals */
	public Direction getMineralDirection(Unit townHall)
	{
		// No hatchery found, give up?
		if (townHall == null)
		{
			return null;
		}

		// totals and count for average
		int totalX = 0;
		int totalY = 0;
		int count = 0;

		for (Unit unit : bwapi.getNeutralUnits())
		{
			// TODO: Test this
			if (unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal())
			{
				if (Utility.getDistance(townHall.getX(), townHall.getY(), unit.getX(), unit.getY()) < 512)
				{
					totalX += unit.getX();
					totalY += unit.getY();
					count++;
				}
			}
		}
		// No minerals, what the deuce
		if (count == 0)
		{
			return null;
		}
		// Average the values
		int averageMineralX = totalX / count;
		int averageMineralY = totalY / count;

		int distanceX = townHall.getX() - averageMineralX;
		int distanceY = townHall.getY() - averageMineralY;

		// Is the X direction more important, or the Y direction
		if (Math.abs(distanceX) > Math.abs(distanceY))
		{
			if (distanceX > 0)
			{
				return Direction.West;
			}
			else
			{
				return Direction.East;
			}
		}
		else
		{
			if (distanceY < 0)
			{
				return Direction.South;
			}
			else
			{
				return Direction.North;
			}
		}
	}

	/**
	 * Returns a town hall. This is either a Hatchery, Lair, or Hive. If there
	 * is a Hive, will return that, then Lair, then Hatchery.
	 * 
	 * @return Returns a Hive unit, Lair unit, or Hatchery unit.
	 */
	protected Unit getTownHall()
	{
		Unit hall = null;
		hall = unitManager.getMyUnitOfType(baseTypeID, false);

		// Zerg can upgrade thier bases
		if (hall == null && bwapi.getSelf().getRaceID() == RaceTypes.Zerg.ordinal())
		{
			hall = unitManager.getMyUnitOfType(UnitTypes.Zerg_Lair.ordinal(), false);
		}
		if (hall == null && bwapi.getSelf().getRaceID() == RaceTypes.Zerg.ordinal())
		{
			hall = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal(), false);
		}
		return hall;
	}

	/** Determines the direction of the geyser */
	public Direction getGeyserDirection(Unit townHall)
	{
		// No hatchery found, give up?
		if (townHall == null)
		{
			return null;
		}
		Unit closestGeyser = null;
		double currentClosestDistance = Utility.NOT_SET;
		for (Unit unit : bwapi.getNeutralUnits())
		{
			// TODO: This should only be the geyser that is next to the base
			double distance = Utility.getDistance(townHall.getX(), townHall.getY(), unit.getX(), unit.getY());
			if (unit.getTypeID() == UnitTypes.Resource_Vespene_Geyser.ordinal()
					&& (distance < currentClosestDistance || currentClosestDistance == Utility.NOT_SET))
			{
				if (distance > 1024)
				{
					// gesyer not likely to be part of this base
					continue;
				}
				closestGeyser = unit;
				currentClosestDistance = distance;
			}
		}

		// No geyser, give up
		if (closestGeyser == null)
		{
			return null;
		}
		// Average the values
		int geyserX = closestGeyser.getX();
		int geyserY = closestGeyser.getY();

		int distanceX = townHall.getX() - geyserX;
		int distanceY = townHall.getY() - geyserY;

		// Is the X direction more important, or the Y direction
		if (Math.abs(distanceX) > Math.abs(distanceY))
		{
			if (distanceX > 0)
			{
				return Direction.West;
			}
			else
			{
				return Direction.East;
			}
		}
		else
		{
			if (distanceY < 0)
			{
				return Direction.South;
			}
			else
			{
				return Direction.North;
			}
		}

	}

	/**
	 * Returns whether the AI has built an extractor yet.
	 * 
	 * @param completed
	 *            If true, will return false if the building is still in
	 *            progress
	 * @return
	 */
	public boolean hasExtractor(boolean completed)
	{
		// Find the nearest Zerg Extractor
		Unit extractor = unitManager.getMyUnitOfType(extractorTypeID, completed);
		if (extractor == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Returns the number of hatcheries, lairs or hives currently owned by the
	 * player
	 */
	public int getHatcheryCount()
	{
		int count = 0;
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == baseTypeID)
			{
				count++;
			}
			// Darn zerg and their upgrading town halls
			else if (bwapi.getSelf().getRaceID() == RaceTypes.Zerg.ordinal()
					&& (unit.getTypeID() == UnitTypes.Zerg_Lair.ordinal() || unit.getTypeID() == UnitTypes.Zerg_Hive
							.ordinal()))
			{
				count++;
			}
		}
		return count;
	}

	public int getExpansionCount()
	{
		return expansionIDs.size();
	}

	private int	count	= 0;

	@Override
	public void gameUpdate()
	{
		if (AIClient.DEBUG)
		{
			drawBuildingSquares();

			bwapi.drawText(new Point(150, 0), "Expansion Count : " + getExpansionCount(), true);
			bwapi.drawText(new Point(150, 16), "Expansion Index : " + expansionIndex, true);
			bwapi.drawText(new Point(150, 32), "Drone Ready : " + expansionDroneReady(), true);

			bwapi.drawText(new Point(320, 0), "Next build location: " + nextBuildLocation, true);
			bwapi.drawText(new Point(320, 16), "Next defence location : " + nextDefenceLocation, true);
		}
		checkCompletedHatcheries();
	}

	private void checkCompletedHatcheries()
	{
		for (BaseInfo base : baseBuildings)
		{
			if (base.updated)
			{
				continue;
			}
			// Make sure it is actually a hatchery first
			if (base.structure.getTypeID() == baseTypeID)
			{
				// Update information, has now been completed
				if (base.structure.isCompleted() && !base.completed)
				{
					base.completed = true;
					base.hatcheryWaitTimer = bwapi.getFrameCount();
					calculateBuildLocationsForBase(base);
				}
				if (base.completed && !base.updated && bwapi.getFrameCount() > base.hatcheryWaitTimer + 100)
				{
					System.out.println("BUILDING MANAGER : NEW HATCHERY!");
					workerManager.newBaseBuilding(base);
					base.updated = true;
				}
			}
			else
			{
				// Timeout, has not become a hatchery
				// Drone has failed, reset state
				if (bwapi.getFrameCount() > base.hatcheryWaitTimer + 500)
				{
					System.out.println("Expansion failed to be made, reset.");
					expansionIndex = expansionIDs.indexOf(base.id);
					expansionIDs.remove(Integer.valueOf(base.id));
					expansionIDs.trimToSize();
					// Force expansion to be made again!
					expansionWorker = base.id;
					buildExpansionHatchery();
					baseBuildings.remove(base);
					break;
				}
			}
		}
	}

	private boolean hatcheryInfoExists(int unitID)
	{
		for (BaseInfo info : baseBuildings)
		{
			if (info.id == unitID)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void gameStarted()
	{
		// Reset variables
		buildingsList.clear();
		buildLocations.clear();
		expansionLocations.clear();
		baseBuildings.clear();
		// Start build locations from 0
		nextBuildLocation = 0;
		expansionIndex = 0;
		// Defence locations start from the other end, so defences get build at
		// expansions first
		nextDefenceLocation = buildLocations.size() - 1;
		// TODO: Currently only tested with Azalea
		nextExpansionLocation = 0;
		expansionWorker = Utility.NOT_SET;
		/* Build build map */
		mapArray = getBuildableMapData();
		// Add all of the known buildings / minerals / geysers to the build map
		for (Unit unit : bwapi.getAllUnits())
		{
			if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
			{
				addBuildingToKnowldegeBase(unit.getTypeID(), unit);
			}
		}
		BaseInfo mainBase = new BaseInfo(unitManager.getMyUnitOfType(baseTypeID), true);
		baseBuildings.add(mainBase);
		calculateBuildLocationsForBase(mainBase);
		calculateExpansionLocations(unitManager.getMyUnitOfType(baseTypeID));
	}

	@Override
	public void unitCreate(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
		{
			addBuildingToKnowldegeBase(unitID, unit);
		}
	}

	@Override
	public void unitDestroy(int unitID)
	{
		removeBuildingFromKnowldegeBase(unitID);
		checkForBuildLocationRemoval(unitID);
		// If an expansion hatchery remove from list of expansions
		if (expansionIDs.contains(unitID))
		{
			System.out.println("EXPANSION DESTROYED");
			expansionIndex = expansionIDs.indexOf(unitID);
			expansionIDs.remove(Integer.valueOf(unitID));
			expansionIDs.trimToSize();
		}
		if (unitID == expansionWorker)
		{
			expansionWorker = Utility.NOT_SET;
		}

		if (builders.contains(unitID))
		{
			workerManager.removeBusyWorker(unitID);
			builders.remove(Integer.valueOf(unitID));
		}
	}

	@Override
	public void unitDiscover(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
		{
			addBuildingToKnowldegeBase(unitID, unit);
		}
	}

	private void addBuildingToKnowldegeBase(int unitID, Unit unit)
	{
		buildingPlaced(unit.getTileX(), unit.getTileY(), unit.getTypeID());
		buildingsList.add(new BuildingInfo(unit, bwapi));
	}

	private void removeBuildingFromKnowldegeBase(int unitID)
	{
		// Work around
		// Have to keep track of all the Unit Handles so that we can find out
		// the building
		// information when it is destroyed
		for (BuildingInfo buildingInfo : buildingsList)
		{
			// Found the correct building
			// Remove
			if (buildingInfo.id == unitID)
			{
				System.out.println("Removing existing building from knowledge base. ");
				System.out.println("TileX : " + buildingInfo.location.x + " TileY: " + buildingInfo.location.y
						+ "Type: " + buildingInfo.buildingType.getName());
				buildingRemoved(buildingInfo.tileLocation.x, buildingInfo.tileLocation.y, buildingInfo.buildingType);
				buildingsList.remove(buildingInfo);
				break;
			}
		}
	}

	@Override
	public void unitMorph(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		if (bwapi.getUnitType(unit.getTypeID()).isBuilding())
		{
			addBuildingToKnowldegeBase(unitID, unit);
		}
		// If the ID is used to be a building, and now a worker, assume a
		// cancelled building
		else if (bwapi.getUnitType(unit.getTypeID()).isWorker())
		{
			removeBuildingFromKnowldegeBase(unitID);
		}

		if (builders.contains(unitID))
		{
			workerManager.removeBusyWorker(unitID);
			builders.remove(Integer.valueOf(unitID));
		}
	}

	public void idleWorker(int unitID)
	{
		if (builders.contains(unitID))
		{
			// Idle unit implies it's done what we asked or can't continue
			workerManager.removeBusyWorker(unitID);
			builders.remove(Integer.valueOf(unitID));
		}
	}
	
	public int getNearestTownHall(int unitID)
	{
		Point unitLoc = new Point(bwapi.getUnit(unitID).getX(), bwapi.getUnit(unitID).getY());
		double nearestDistance = Utility.NOT_SET;
		int nearestUnit = Utility.NOT_SET;
		for(BaseInfo base : baseBuildings)
		{
			double distance = Utility.getDistance(unitLoc.x, unitLoc.y, base.location.x, base.location.y);
			
			if( nearestUnit == Utility.NOT_SET || distance < nearestDistance)
			{
				nearestUnit		= base.structure.getID();
				nearestDistance	= distance;
			}
			
		}
		
		return nearestUnit;
	}
}
