package scbod;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.awt.Point;

import sun.util.calendar.BaseCalendar;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.util.BWColor;

/** Building Manager * * *
 * 
 * Manager for the construction of all of the structures, and their placement.
 */
public class BuildingManager extends Manager {
		
	/** The build map array */
	private boolean mapArray[][];
	
	/** identifies all known buildings */
	private ArrayList<BuildingInfo> buildingsList = new ArrayList<BuildingInfo>();
	
	/** The pre-determined build locations 
	 * These are calculated at the beginning of the game and when new hatcheries
	 * or creep colonies are built, and these determine where buildings should be placed */
	private ArrayList<Point> buildLocations = new ArrayList<Point>();
	
	/** Index into the buildLocations array */
	private int nextBuildLocation;
	private int nextDefenceLocation;
	
	/** Expansion locations, this is a list of expansion locations that should try and go to */
	private ArrayList<BaseLocation> expansionLocations = new ArrayList<BaseLocation>();
	
	/** Index into the next expansion location to go to */
	private int nextExpansionLocation;

	// Info used for generating new building locations
	private ArrayList<BaseInfo> baseHatcheries = new ArrayList<BaseInfo>();
	private ArrayList<BaseInfo> creepColonies = new ArrayList<BaseInfo>();
	
	/* Index for the next expansion */
	private int expansionIndex;
	
	private ArrayList<Integer> expansionIDs = new ArrayList<Integer>();
	
	/** ID for expansion building worker */
	private int expansionWorker = Utility.NOT_SET;
	
	// Map sizes
	private int mapSizeX;
	private int mapSizeY;
	
	private JNIBWAPI bwapi;
	private UnitManager unitManager;
	private WorkerManager workerManager;
	private ResourceManager resourceManager;
	
	public BuildingManager(JNIBWAPI bwapi, UnitManager unitManager
			, WorkerManager workerManager, ResourceManager resourceManager){
		this.bwapi = bwapi;
		this.unitManager = unitManager;
		this.workerManager = workerManager;
		this.resourceManager = resourceManager;
	}
	
	/** Draw all of the buildings squares held in each of the arrays */
	private void drawBuildingSquares(){
		for(BuildingInfo info : buildingsList){
			bwapi.drawText(info.location, info.buildingType.getName(), false);
			bwapi.drawBox(
					info.tileLocation.x * 32,
					info.tileLocation.y * 32,
					(info.tileLocation.x + info.buildingType.getTileWidth()) * 32,
					(info.tileLocation.y + info.buildingType.getTileHeight()) * 32,
					BWColor.GREEN, false, false);
		}
		int i = 1;
		for(Point location : buildLocations){
			bwapi.drawText(location.x * 32, location.y * 32, Integer.toString(i), false);
			bwapi.drawBox(
					location.x * 32,
					location.y * 32,
					(location.x + 2) * 32,
					(location.y + 2) * 32,
					BWColor.PURPLE, false, false);
			i++;
		}
		
		i = 1;
		for(BaseLocation expansion : expansionLocations){
			bwapi.drawText(expansion.getTx() * 32, expansion.getTy() * 32, "Exp " + i, false);
			bwapi.drawBox(
					expansion.getTx() * 32,
					expansion.getTy() * 32,
					(expansion.getTx() + 2) * 32,
					(expansion.getTy() + 2) * 32,
					BWColor.YELLOW, false, false);
			i++;
		}
	}
	
	/** Calculates all of the build locations for a given hatchery / expansion */
	private void calculateBuildLocationsHatchery(BaseInfo hatchInfo){
		Direction mineralDirection = getMineralDirection(hatchInfo.structure);
		Direction geyserDirection = getGeyserDirection(hatchInfo.structure);
		
		int hatcheryCentreX = hatchInfo.structure.getTileX() + 2;
		int hatcheryCentreY = hatchInfo.structure.getTileY() + 2;
		// The start coordinates, which are generally opposite the geyser.
		int startX = 1;
		int startY = 0;
		
		// The build direction, which is the opposite of the minerals
		int buildX = 0;
		int buildY = -1;
		
		if(geyserDirection != null){
			System.out.println("Geyser direction : " + geyserDirection.toString());
		}
		if(mineralDirection != null){
			System.out.println("Mineral direction : " + mineralDirection.toString());
		}
		
		// TODO: This goes on the assumption that geyser direction is
		// never opposite of minerals. Should work for most maps
		if(geyserDirection != null){
		switch(geyserDirection){
			case East:
				startX = -1;
				startY = 0;
				break;
			case South:
				startX = 0;
				startY = -1;
				break;
			case West:
				startX = 1;
				startY = 0;
				break;
			case North:
				startX = 0;
				startY = 1;
				break;
			}
		}
		if(mineralDirection != null){
			switch(mineralDirection){
			case East:
				buildX = -1;
				buildY = 0;
				break;
			case South:
				buildX = 0;
				buildY = -1;
				break;
			case West:
				buildX = 1;
				buildY = 0;
				break;
			case North:
				buildX = 0;
				buildY = 1;
				break;
			}
		}
		int negativeStartValueX = 0;
		// For negative values, boost by an extra 2 due to the fact that
		// these points are the top left point
		if(startX == -1){
			negativeStartValueX = -2;
		}
		int negativeBuildValueX = 0;
		// For negative values, boost by an extra 2 due to the fact that
		// these points are the top left point
		if(buildX == -1){
			negativeBuildValueX = -2;
		}
		int negativeStartValueY = 0;
		// For negative values, boost by an extra 2 due to the fact that
		// these points are the top left point
		if(startY == -1){
			negativeStartValueY = -2;
		}
		int negativeBuildValueY = 0;
		// For negative values, boost by an extra 2 due to the fact that
		// these points are the top left point
		if(buildY == -1){
			negativeBuildValueY = -2;
		}
		buildLocations.add(new Point(hatcheryCentreX + 
				((startX * 2 + negativeStartValueX) + (buildX * -1 + negativeBuildValueX)) , 
				hatcheryCentreY + 
				((startY * 2 + negativeStartValueY) + (buildY * -1 + negativeBuildValueY))));
		hatchInfo.buildingIndexes.add(buildLocations.size() - 1);
		buildLocations.add(new Point(hatcheryCentreX + 
				((startX * 2 + negativeStartValueX) + (buildX * 3 + negativeBuildValueX)) , 
				hatcheryCentreY + 
				((startY * 2 + negativeStartValueY) + (buildY * 3 + negativeBuildValueY))));
		hatchInfo.buildingIndexes.add(buildLocations.size() - 1);
		buildLocations.add(new Point(hatcheryCentreX + 
				((startX * -3 + negativeStartValueX) + (buildX * 3 + negativeBuildValueX)) , 
				hatcheryCentreY + 
				((startY * -3 + negativeStartValueY) + (buildY * 3 + negativeBuildValueY))));
		hatchInfo.buildingIndexes.add(buildLocations.size() - 1);
		
		// Defence location is updated to be the last ones, so that defences are built at expansions first.
		nextDefenceLocation = buildLocations.size() - 1;
	}
	
	/** Calculates all of the build locations for a given creep colony */
	private void calculateBuildLocationsColony(BaseInfo colonyInfo, Unit hatchery){
		
		Direction hatcheryDirection = getHatcheryDirection(colonyInfo.structure, hatchery);
		
		int colonyX = colonyInfo.structure.getTileX();
		int colonyY = colonyInfo.structure.getTileY();
		
		if(hatcheryDirection == null){
			return;
		}
		switch(hatcheryDirection){
		case East:
			buildLocations.add(new Point(colonyX - 3,
					colonyY + 1));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			buildLocations.add(new Point(colonyX - 3,
								colonyY - 1));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			break;
		case South:
			buildLocations.add(new Point(colonyX + 1,
					colonyY - 3));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			buildLocations.add(new Point(colonyX - 1,
								colonyY - 3));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			break;
		case West:
			buildLocations.add(new Point(colonyX + 3,
					colonyY + 1));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			buildLocations.add(new Point(colonyX + 3,
								colonyY - 1));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			break;
		case North:
			buildLocations.add(new Point(colonyX + 1,
					colonyY + 3));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			buildLocations.add(new Point(colonyX - 1,
								colonyY + 3));
			colonyInfo.buildingIndexes.add(buildLocations.size() - 1);
			break;
		}
		System.out.println("Colony locations added!");
	}

	private Point getNextBuildLocation(){
		System.out.println("Getting building location");
		if(buildLocations.size() == 0){
			System.out.println("Fail!");
			return null;
		}
		try {
			int index = nextBuildLocation;
			nextBuildLocation = (nextBuildLocation + 1) % (buildLocations.size() - 1);
			return buildLocations.get(index);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Point getNextDefenceLocation(){
		System.out.println("Getting defence building location");
		if(buildLocations.size() == 0){
			System.out.println("Fail!");
			return null;
		}
		try {
			System.out.println("Index  : "+ nextDefenceLocation);
			System.out.println("nextDefenceLocation  : " + (nextDefenceLocation - 1) % (buildLocations.size() - 1));
			System.out.println("buildLocations.size()  : " + buildLocations.size());
			int index = nextDefenceLocation;
			nextDefenceLocation = (nextDefenceLocation - 1);
			if(nextDefenceLocation < 0){
				nextDefenceLocation = buildLocations.size() - 1;
			}
			return buildLocations.get(index);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void calculateExpansionLocations(Unit hatchery){
		ArrayList<BaseLocation> baseList = (ArrayList<BaseLocation>) bwapi.getMap().getBaseLocations();
		BaseLocation homeBase = getNextClosestExpansionLocation(baseList, hatchery);
		baseList.remove(homeBase); // Remove the home base, don't care about it
		for(int i = 0; i < 3; i++){
			BaseLocation expansion = getNextClosestExpansionLocation(baseList, hatchery);
			expansionLocations.add(expansion);
			baseList.remove(expansion); // Add the first location to the expansion list
			System.out.println("Added expansion location " + expansion.getTx() + " - " + expansion.getTy());
		}
	}
	
	private BaseLocation getNextClosestExpansionLocation(ArrayList<BaseLocation> expansions, Unit hatchery){
		double closestDistance = Utility.NOT_SET;
		BaseLocation closestLocation = null;
		for(BaseLocation location : bwapi.getMap().getBaseLocations()){
			double distance = Utility.getDistance(hatchery.getX(), hatchery.getY(),
					location.getX(), location.getY());
			if(distance < closestDistance || closestDistance == Utility.NOT_SET){
				closestLocation = location;
				closestDistance = distance;
				continue;
			}
		}
		return closestLocation;
	}
	
	/** Gets the map build data for this level, which lets us know where the buildable
	 * space is in the game world.
	 */
	public boolean[][] getBuildableMapData(){
		mapSizeX = bwapi.getMap().getWidth();
		mapSizeY = bwapi.getMap().getHeight();
		boolean[][] buildableArray = new boolean[mapSizeX][mapSizeY];
		for(int y = 0; y < mapSizeY; y++){
			for(int x = 0; x < mapSizeX; x++){
				buildableArray[x][y] = bwapi.getMap().isBuildable(x, y);
			}
		}
		return buildableArray;
	}
	
	
	/** Updates the map information to tell the AI which tiles are no longer buildable */
	private void buildingPlaced(int tileX, int tileY, int unitTypeID){
		UnitType buildingType = bwapi.getUnitType(unitTypeID);
		// Not a building, ignore
		if(!buildingType.isBuilding()){
			return;
		}
		for(int x = tileX; x < tileX + buildingType.getTileWidth(); x++){
			for(int y = tileY; y < tileY + buildingType.getTileHeight(); y++){
				mapArray[x][y] = false;
			}
		}
		if(AIClient.DEBUG)
			printMap();
	}

	/** Updates the map information to tell the AI which tiles are now buildable */
	private void buildingRemoved(int tileX, int tileY, UnitType unitType){
		// Not a building, ignore
		if(!unitType.isBuilding()){
			return;
		}
		for(int x = tileX; x < tileX + unitType.getTileWidth(); x++){
			for(int y = tileY; y < tileY + unitType.getTileHeight(); y++){
				mapArray[x][y] = true;
			}
		}
		if(AIClient.DEBUG)
			printMap();
	}
	
	/** Prints the known build map to a file, used for debugging purposes */
	private void printMap() {
		//TODO: Probably don't need this, but keep it in here for now.
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("map2.txt"));
			for(int y = 0; y < mapSizeY; y++){
				for(int x = 0; x < mapSizeX; x++){
					if(mapArray[x][y])
						out.print(".");
					else
						out.print("x");
				}
				out.print("\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean buildBuilding(int buildingType, int tileX, int tileY){
		Unit worker = workerManager.getNearestFreeDrone(tileX, tileY);
		return buildBuilding(buildingType, tileX, tileY, worker);
	}
	
	/** Builds the given building at the location. If the given position 
	 * is not buildable, then the location is moved slightly based on a random value,
	 * until a buildable place can be found. This should not be relied upon, and higher
	 * level methods should instead predetermine where to build.
	 */
	public boolean buildBuilding(int buildingType, int tileX, int tileY, Unit worker){
		System.out.println("Going to build?");
		System.out.println("Finished looking at drones...");
		
		// if not a valid build position, move it!
		Random random = new Random();
		int retries = 0;
		int timeout = 20;
		while(!validBuildPosition(buildingType, tileX, tileY) && retries < timeout){
			System.out.println("Valid build position?");
			tileX += (random.nextInt(4) - 2);
			tileY += (random.nextInt(4) - 2);
			retries++;
		}
		
		if (worker == null){
			return false;
		}
		
		System.out.println("Worker ID : " + worker.getID());
//		workerManager.addBusyWorker(worker.getID());
		System.out.println("Stopping worker" + worker.getID());
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		bwapi.build(worker.getID(), tileX, tileY, buildingType);
		System.out.println("Giving build order" + worker.getID());
		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
//		long deadline = bwapi.getFrameCount() + 175;
//		// Check to make sure that it actually gets constructed in the future
//		while(bwapi.getFrameCount() < deadline){
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("FrameCount : " + bwapi.getFrameCount() + " Deadline : " + deadline);
//			// Worker has magically become a building, means building success
//			if(worker.getTypeID() != UnitTypes.Zerg_Drone.ordinal() && worker.isBeingConstructed()){
//				System.out.println("Building complete! :" + worker.getID());
//				workerManager.removeBusyWorker(worker.getID());
//				return true;
//			}
//			// Worker has gone back to slacking
//			else if(worker.isIdle()){
//				System.out.println("Worker returned to idle, giving up. :" + worker.getID());
//				workerManager.removeBusyWorker(worker.getID());
//				return false;
//			}
//		}
		// Timeout
//		workerManager.removeBusyWorker(worker.getID());
//		System.out.println("Time out on building, worker :" + worker.getID());
		return true;
	}
	
	/** Determines whether a location is a valid build position or not */
	private boolean validBuildPosition(int buildingType, int tileX, int tileY){
		UnitType building = bwapi.getUnitType(buildingType);
		for(int x = tileX; x < tileX + building.getTileWidth(); x++){
			for(int y = tileY; y < tileY + building.getTileHeight(); y++){
				if(x >= mapSizeX || y >= mapSizeY || x < 0 || y < 0)
					return false;
				if(!mapArray[x][y])
					return false;
			}
		}
		return true;
	}
	
	/** Builds a Zerg Extractor on the nearest free geyser */
	public boolean buildExtractor(){
		/* First find the closest drone to the geyser */
		Unit worker = null;
		Unit geyser = null;
		
		if(resourceManager.getMineralCount() < 25){
			return false;
		}
		
		Unit base = getTownHall();
		if(base == null){
			return false;
		}
		Point baseLocation = new Point(base.getX(), base.getY());
		
		// Find the nearest free geyser
		geyser = Utility.getClosestUnitOfType((ArrayList<Unit>) bwapi.getNeutralUnits(),baseLocation,
				UnitTypes.Resource_Vespene_Geyser.ordinal());
		
		// No free geyser found, give up.
		if (geyser == null){
			return false;
		}
		
		worker = workerManager.getNearestFreeDrone(geyser.getTileX(), geyser.getTileY());
		
		if (worker == null){
			return false;
		}
		
		workerManager.addBusyWorker(worker.getID());
		bwapi.build(worker.getID(), geyser.getTileX(), geyser.getTileY(), UnitTypes.Zerg_Extractor.ordinal());
//		long deadline = bwapi.getFrameCount() + 175;
		// Check to make sure that it actually gets constructed in the future
		// Check to make sure that it actually gets constructed in the future
//		while(bwapi.getFrameCount() < deadline){
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("FrameCount : " + bwapi.getFrameCount() + " Deadline : " + deadline);
//			// Worker has magically become a building, means building success
//			if(worker.getTypeID() != UnitTypes.Zerg_Drone.ordinal() && worker.isBeingConstructed()){
//				System.out.println("Building complete! :" + worker.getID());
//				workerManager.removeBusyWorker(worker.getID());
//				return true;
//			}
//			// Worker has gone back to slacking
//			else if(worker.isIdle()){
//				System.out.println("Worker returned to idle, giving up. :" + worker.getID());
//				workerManager.removeBusyWorker(worker.getID());
//				return false;
//			}
//		}
		// Timeout
		return true;
	}
	
	/** Builds a macro hatchery */
	public boolean buildMacroHatchery(){
		Unit hatchery = getTownHall();
		if(resourceManager.getMineralCount() < 300){
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if(hatchery == null){
			System.out.println("No hatchery?");
			return false;
		}
		Point buildLocation = getNextBuildLocation();
		
		if(buildBuilding(UnitTypes.Zerg_Hatchery.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	/** Send drone to expansion location -
	 * Also reserves the minerals so you can actually build it.*/
	public void sendDroneToExpansionLocation(){
		BaseLocation location = expansionLocations.get(expansionIndex);
		Unit worker = workerManager.getNearestFreeDrone(location.getTx(), location.getTy());		
		workerManager.addBusyWorker(worker.getID());
		expansionWorker = worker.getID();
		bwapi.move(worker.getID(), location.getX(), location.getY());
		resourceManager.reserveMinerals(300);
	}
	
	/** Builds an expansion hatchery */
	public boolean buildExpansionHatchery(){
		try {
			if(resourceManager.getReservedMineralCount() < 350){
				return false;
			}
			BaseLocation expansionLocation = expansionLocations.get(expansionIndex);
			System.out.println("Go build!");
			System.out.println("Base location " + expansionLocation.getTx() + expansionLocation.getTy());
			Unit worker = bwapi.getUnit(expansionWorker);
			if(buildBuilding(UnitTypes.Zerg_Hatchery.ordinal(), expansionLocation.getTx(), expansionLocation.getTy(), worker)){
				System.out.println("New hatchery added to expansions");
				Unit hatchery = worker;
				BaseInfo newExpansion = new BaseInfo(hatchery);
				newExpansion.hatcheryWaitTimer = bwapi.getFrameCount();
				baseHatcheries.add(newExpansion);
				expansionIDs.add(hatchery.getID());
				expansionWorker = Utility.NOT_SET;
				resourceManager.reserveMinerals(0);
				expansionIndex++;
				return true;
			}
			else{
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean expansionDroneReady(){
		if(bwapi.getUnit(expansionWorker) == null){
			return false;
		}
		if (expansionWorker == Utility.NOT_SET){
			return false;
		}
		if (bwapi.getUnit(expansionWorker).isIdle()){
			return true;
		}
		else{
			return false;
		}
	}
	
	/* Called when a unit is destroyed.
	 * If the unit is a players hatchery or colony, then all of the
	 * associated build locations should be removed
	 */
	private void checkForBuildLocationRemoval(int unitID){
		for(BaseInfo info : baseHatcheries){
			if(info.id == unitID){
				System.out.println("TEST: Expo destroyed");
				for(int index : info.buildingIndexes){
					buildLocations.remove(index);
					System.out.println("TEST: Removing build locations");
				}
			}
		}
		for(BaseInfo info : creepColonies){
			if(info.id == unitID){
				System.out.println("TEST: Creep colony destroyed!");
				for(int index : info.buildingIndexes){
					buildLocations.remove(index);
					System.out.println("TEST : Removing colony build locations");
				}
			}
		}
		if(nextBuildLocation >= buildLocations.size()){
			nextBuildLocation = 0;
		}
		if(nextDefenceLocation >= buildLocations.size()){
			nextDefenceLocation = buildLocations.size() - 1;
		}
	}
	
	private BaseLocation getNextExpansionLocation(){
		BaseLocation location = expansionLocations.get(nextExpansionLocation);
		nextExpansionLocation = (nextExpansionLocation + 1) % expansionLocations.size();
		return location;
	}
	
	/** Builds a spire */
	public boolean buildSpire(){
		if(resourceManager.getMineralCount() < 200 ||
				resourceManager.getGasCount() < 200){
			return false;
		}
		if(!hasLair(true)){
			return false;
		}
		
		Point buildLocation = getNextBuildLocation();
		
		if(buildBuilding(UnitTypes.Zerg_Spire.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	/** Builds a spawning pool! */
	public boolean buildSpawningPool(){
		
		Unit hatchery = getTownHall();
		if(resourceManager.getMineralCount() < 200){
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if(hatchery == null){
			return false;
		}
		
		Point buildLocation = getNextBuildLocation();
		
		if(buildBuilding(UnitTypes.Zerg_Spawning_Pool.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	/** Builds a hydralisk den */
	public boolean buildHydraliskDen(){
		Unit pool = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spawning_Pool.ordinal(), true);
		if(resourceManager.getMineralCount() < 100 || resourceManager.getGasCount() < 50){
			return false;
		}
		// No spawning pool, can't do it anyways, give up
		if(pool == null){
			return false;
		}
		
		Point buildLocation = getNextBuildLocation();

		if(buildBuilding(UnitTypes.Zerg_Hydralisk_Den.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	public boolean buildEvolutionChamber(){
		Unit hatchery = getTownHall();
		if(resourceManager.getMineralCount() < 75){
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if(hatchery == null){
			return false;
		}
		
		Point buildLocation = getNextBuildLocation();
		
		if(buildBuilding(UnitTypes.Zerg_Evolution_Chamber.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	/** Builds a creep colony ! */
	public boolean buildCreepColony(){
		Unit hatchery = getTownHall();
		if(resourceManager.getMineralCount() < 75){
			return false;
		}
		// No hatchery, can't do it anyways, give up
		if(hatchery == null){
			return false;
		}
		
		Point buildLocation = getNextDefenceLocation();
		
		if(buildBuilding(UnitTypes.Zerg_Creep_Colony.ordinal(), buildLocation.x, buildLocation.y))
			return true;
		else
			return false;
	}
	
	public int getCompletedColonyCount(){
		return unitManager.getUnitCount(UnitTypes.Zerg_Creep_Colony.ordinal(), true) + getSunkenColonyCount();
	}
	
	public int getCompletedCreepColonyCount(){
		return unitManager.getUnitCount(UnitTypes.Zerg_Creep_Colony.ordinal(), true);
	}
	
	/** Upgrades a random creep colony to sunken colony */
	public boolean upgradeSunkenColony(){
		Unit creep = unitManager.getMyUnitOfType(UnitTypes.Zerg_Creep_Colony.ordinal(), true);
		if (creep == null){
			return false;
		}
		if(!hasSpawningPool(true)){
			return false;
		}
		if(resourceManager.getMineralCount() < 50){
			return false;
		}
		bwapi.morph(creep.getID(), UnitTypes.Zerg_Sunken_Colony.ordinal());
		return true;
	}
	
	public int getSunkenColonyCount(){
		return unitManager.getUnitCount(UnitTypes.Zerg_Sunken_Colony.ordinal(), false);
	}
	
	public int getExtractorCount(){
		return unitManager.getUnitCount(UnitTypes.Zerg_Extractor.ordinal(), false);
	}
	
	/** Do we have one extractor for each base? */
	public boolean hasExtractorSaturation(){
		return unitManager.getUnitCount(UnitTypes.Zerg_Extractor.ordinal(), false) >= (expansionIDs.size() + 1);
	}
	
	/** have all the extractors been completed? */
	public boolean allExtractorsCompleted(){
		return (unitManager.getUnitCount(UnitTypes.Zerg_Extractor.ordinal(), false)) == 
				(unitManager.getUnitCount(UnitTypes.Zerg_Extractor.ordinal(), true));
	}
	
	/** Determines the direction of a hatchery to another building */
	public Direction getHatcheryDirection(Unit colony, Unit hatchery){
		// No hatchery found, give up?
		if(hatchery == null){
			return null;
		}
		
		int distanceX = colony.getX() - hatchery.getX();
		int distanceY = colony.getY() - hatchery.getY();
		
		// Is the X direction more important, or the Y direction
		if(Math.abs(distanceX) > Math.abs(distanceY)){
			if(distanceX > 0){
				return Direction.West;
			}
			else{
				return Direction.East;
			}
		}
		else{
			if(distanceY < 0){
				return Direction.South;
			}
			else{
				return Direction.North;
			}
		}
	}
	
	/** Determines the direction of the minerals */
	public Direction getMineralDirection(Unit townHall){
		// No hatchery found, give up?
		if(townHall == null){
			return null;
		}

		// totals and count for average
		int totalX = 0;
		int totalY = 0;
		int count = 0;
		
		for (Unit unit : bwapi.getNeutralUnits()) {
			// TODO: Test this
			if (unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()){
				if(Utility.getDistance(townHall.getX(), townHall.getY(),
						unit.getX(), unit.getY()) < 512){
					totalX += unit.getX();
					totalY += unit.getY();
					count++;
				}
			}
		}
		// No minerals, what the deuce
		if(count == 0){
			return null;
		}
		// Average the values
		int averageMineralX = totalX / count;
		int averageMineralY = totalY / count;
		
		int distanceX = townHall.getX() - averageMineralX;
		int distanceY = townHall.getY() - averageMineralY;
		
		// Is the X direction more important, or the Y direction
		if(Math.abs(distanceX) > Math.abs(distanceY)){
			if(distanceX > 0){
				return Direction.West;
			}
			else{
				return Direction.East;
			}
		}
		else{
			if(distanceY < 0){
				return Direction.South;
			}
			else{
				return Direction.North;
			}
		}
	}

	/** Returns a town hall.
	 * This is either a Hatchery, Lair, or Hive.
	 * If there is a Hive, will return that, then Lair, then Hatchery.
	 * @return Returns a Hive unit, Lair unit, or Hatchery unit.
	 */
	private Unit getTownHall() {
		Unit hall = null;
		hall = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hive.ordinal(), false);
		if(hall == null){
			hall = unitManager.getMyUnitOfType(UnitTypes.Zerg_Lair.ordinal(), false);
		}
		if(hall == null){
			hall = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal(), false);
		}
		return hall;
	}
	
	
	/** Determines the direction of the geyser */
	public Direction getGeyserDirection(Unit townHall){
		// No hatchery found, give up?
		if(townHall == null){
			return null;
		}
		Unit closestGeyser = null;
		double currentClosestDistance = Utility.NOT_SET;
		for (Unit unit : bwapi.getNeutralUnits()) {
			// TODO: This should only be the geyser that is next to the base
			double distance = Utility.getDistance(townHall.getX(), townHall.getY(),
					unit.getX(), unit.getY());
			if (unit.getTypeID() == UnitTypes.Resource_Vespene_Geyser.ordinal() &&
					(distance < currentClosestDistance|| currentClosestDistance == Utility.NOT_SET)){
				if(distance > 1024){
					// gesyer not likely to be part of this base
					continue;
				}
				closestGeyser = unit;
				currentClosestDistance = distance;
			}
		}
		
		// No geyser, give up
		if(closestGeyser == null){
			return null;
		}
		// Average the values
		int geyserX = closestGeyser.getX();
		int geyserY = closestGeyser.getY();
		
		int distanceX = townHall.getX() - geyserX;
		int distanceY = townHall.getY() - geyserY;
		
		// Is the X direction more important, or the Y direction
		if(Math.abs(distanceX) > Math.abs(distanceY)){
			if(distanceX > 0){
				return Direction.West;
			}
			else{
				return Direction.East;
			}
		}
		else{
			if(distanceY < 0){
				return Direction.South;
			}
			else{
				return Direction.North;
			}
		}
		
	}
	
	/** Upgrades a hatchery to a lair */
	public boolean upgradeToLair(){
		Unit hatchery = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal(), true);;
		if( hatchery == null){
			return false;
		}
		if(!hasSpawningPool(true)){
			return false;
		}
		if(resourceManager.getMineralCount() < 150 || resourceManager.getGasCount() < 100){
			return false;
		}
		bwapi.morph(hatchery.getID(), UnitTypes.Zerg_Lair.ordinal());
		return true;
	}
	
	/** Returns whether the AI has built an extractor yet.
	 * 
	 * @param completed If true, will return false if the building is still in progress
	 * @return
	 */
	public boolean hasExtractor(boolean completed){
		// Find the nearest Zerg Extractor
		Unit extractor = unitManager.getMyUnitOfType(UnitTypes.Zerg_Extractor.ordinal(), completed);
		if(extractor == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	/** Returns whether the AI has built an extractor yet. 
	 * @param completed If true, will return false if the building is still in progress
	 * @return
	 */
	public boolean hasSpawningPool(boolean completed){
		// Find a pool
		Unit pool = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spawning_Pool.ordinal(), completed);
		if(pool == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	/** Checks whether there is a lair in progress, or completed
	 * @param completed If true, will return false if the building is still in progress
	 * @return
	 */
	public boolean hasLair(boolean completed){
		Unit lair = null;
		lair = unitManager.getMyUnitOfType(UnitTypes.Zerg_Lair.ordinal(), completed);
		if( lair == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	/** Checks whether there is a hydralisk den built.
	 * @param completed If true, will return false if the building is still in progress
	 * @return
	 */
	public boolean hasHydraliskDen(boolean completed){
		Unit den = null;
		den = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hydralisk_Den.ordinal(), completed);
		if( den == null){
			return false;
		}
		else{
			return true;
		}
		
	}
	/** Checks whether there is a spire
	 * @param completed If true, will return false if the building is still in progress
	 * @return
	 */
	public boolean hasSpire(boolean completed){
		Unit spire = null;
		spire = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spire.ordinal(), completed);
		if( spire == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	/** Returns the number of hatcheries, lairs or hives currently owned by the player */
	public int getHatcheryCount(){
		int count = 0;
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == UnitTypes.Zerg_Hatchery.ordinal() ||
					unit.getTypeID() == UnitTypes.Zerg_Lair.ordinal() ||
					unit.getTypeID() == UnitTypes.Zerg_Hive.ordinal()){
				count++;
			}
		}
		return count;
	}
	
	public int getExpansionCount(){
		return expansionIDs.size();
	}
	
	/** Get the number of evolution chambers
	 * @param completed If true, will only count completed evolution chambers
	 * @return
	 */
	public int getEvolutionChamberCount(boolean completed){
		return unitManager.getUnitCount(UnitTypes.Zerg_Evolution_Chamber.ordinal(), true);
	}
	
	@Override
	public void gameUpdate(){
		if(AIClient.DEBUG){
			drawBuildingSquares();
			
			bwapi.drawText(new Point(150,0), "Expansion Count : " + getExpansionCount(), true);
			bwapi.drawText(new Point(150,16), "Expansion Index : " + expansionIndex, true);
			bwapi.drawText(new Point(150,32), "Drone Ready : " + expansionDroneReady(), true);
			
			bwapi.drawText(new Point(320,0), "Next build location: " + nextBuildLocation, true);
			bwapi.drawText(new Point(320,16), "Next defence location : " + nextDefenceLocation, true);
		}
		checkForCompletedColonies();
		checkCompletedHatcheries();
	}
	
	private void checkForCompletedColonies(){
		for(Unit unit : bwapi.getAllUnits()){
			if(bwapi.getUnitType(unit.getTypeID()).isBuilding()){
				
				// Check for colonies
				// if the colony is completed
				// and has not already been added, then add it to the base infos
				if((unit.getTypeID() == UnitTypes.Zerg_Creep_Colony.ordinal() ||
						unit.getTypeID() == UnitTypes.Zerg_Sunken_Colony.ordinal())
						&& unit.getPlayerID() == bwapi.getSelf().getID() && unit.isCompleted()
						&& !colonyInfoExists(unit.getID())){
					BaseInfo newColony = new BaseInfo(unit);
					calculateBuildLocationsColony(newColony, getTownHall());
					creepColonies.add(newColony);
					System.out.println("New colony made!");
				}
			}
		}
	}
	
	private void checkCompletedHatcheries(){
		for(BaseInfo base : baseHatcheries){
			if(base.updated){
				continue;
			}
			// Make sure it is actually a hatchery first
			if(base.structure.getTypeID() == UnitTypes.Zerg_Hatchery.ordinal()){
				// Update information, has now been completed
				if(base.structure.isCompleted() && !base.completed){
					base.completed = true;
					base.hatcheryWaitTimer = bwapi.getFrameCount();
					calculateBuildLocationsHatchery(base);
				}
				if(base.completed && !base.updated && bwapi.getFrameCount() > base.hatcheryWaitTimer + 100){
					System.out.println("BUILDING MANAGER : NEW HATCHERY!");
					workerManager.newHatchery(base);
					base.updated = true;
				}
			}
			else{
				// Timeout, has not become a hatchery
				// Drone has failed, reset state
				if(bwapi.getFrameCount() > base.hatcheryWaitTimer + 500){
					System.out.println("Expansion failed to be made, reset.");
					expansionIndex = expansionIDs.indexOf(base.id);
					expansionIDs.remove(base.id);
					expansionIDs.trimToSize();
					// Force expansion to be made again!
					expansionWorker = base.id;
					buildExpansionHatchery();
					baseHatcheries.remove(base);
					break;
				}
			}
		}
	}
	
	private boolean colonyInfoExists(int unitID){
		for(BaseInfo info : creepColonies){
			if(info.id == unitID){
				return true;
			}
		}
		return false;
	}
	
	private boolean hatcheryInfoExists(int unitID){
		for(BaseInfo info : baseHatcheries){
			if(info.id == unitID){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void gameStarted(){
		// Reset variables
		buildingsList.clear();
		buildLocations.clear();
		expansionLocations.clear();
		baseHatcheries.clear();
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
		for(Unit unit : bwapi.getAllUnits()){
			if(bwapi.getUnitType(unit.getTypeID()).isBuilding()){
				addBuildingToKnowldegeBase(unit.getTypeID(), unit);
			}
		}
		BaseInfo mainBase = new BaseInfo(unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal()), true);
		baseHatcheries.add(mainBase);
		calculateBuildLocationsHatchery(mainBase);
		calculateExpansionLocations(unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal()));
	}
	
	@Override
	public void unitCreate(int unitID){
		Unit unit = bwapi.getUnit(unitID);
		if(bwapi.getUnitType(unit.getTypeID()).isBuilding()){
			addBuildingToKnowldegeBase(unitID, unit);
		}
	}
	
	@Override
	public void unitDestroy(int unitID){
		removeBuildingFromKnowldegeBase(unitID);
		checkForBuildLocationRemoval(unitID);
		// If an expansion hatchery remove from list of expansions
		if(expansionIDs.contains(unitID)){
			System.out.println("EXPANSION DESTROYED");
			expansionIndex = expansionIDs.indexOf(unitID);
			expansionIDs.remove(unitID);
			expansionIDs.trimToSize();
		}
		if(unitID == expansionWorker){
			expansionWorker = Utility.NOT_SET;
		}
	}
	
	@Override
	public void unitDiscover(int unitID){
		Unit unit = bwapi.getUnit(unitID);
		if(bwapi.getUnitType(unit.getTypeID()).isBuilding()){
			addBuildingToKnowldegeBase(unitID, unit);
		}
	}

	private void addBuildingToKnowldegeBase(int unitID, Unit unit) {
		buildingPlaced(unit.getTileX(), unit.getTileY(), unit.getTypeID());
		buildingsList.add(new BuildingInfo(unit, bwapi));
	}
	
	private void removeBuildingFromKnowldegeBase(int unitID) {
		// Work around
		// Have to keep track of all the Unit Handles so that we can find out the building
		// information when it is destroyed
		for(BuildingInfo buildingInfo : buildingsList){
			// Found the correct building
			// Remove
			if(buildingInfo.id == unitID){
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
	public void unitMorph(int unitID){
		Unit unit = bwapi.getUnit(unitID);
		if(bwapi.getUnitType(unit.getTypeID()).isBuilding()){
			addBuildingToKnowldegeBase(unitID, unit);
		}
		// If the ID is used to be a building, and now a worker, assume a cancelled building
		else if(bwapi.getUnitType(unit.getTypeID()).isWorker()){
			removeBuildingFromKnowldegeBase(unitID);
		}
	}
}
