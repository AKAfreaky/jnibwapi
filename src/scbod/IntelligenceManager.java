package scbod;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Player;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/** Handles the tasks of scouting and what areas have been scouted,
 * and where the enemy is located. 
 *
 */
public class IntelligenceManager extends Manager {
	
	
	// Start Locations
	// enemyStartLocation starts as unknown
	private BaseLocation enemyStartLocation;
	private BaseLocation startLocation;
	
	private int overlordScoutIndex;
	
	public BaseLocation getEnemyStartLocation(){
		return enemyStartLocation;
	}
	
	public BaseLocation getPlayerStartLocation(){
		return startLocation;
	}
	
	/* Base locations that are not the player start locations, used for scouting */
	private ArrayList<BaseLocation> scoutLocations = new ArrayList<BaseLocation>();
	
	/* Known building locations */
	private ArrayList<BuildingInfo> enemyBuildingLocations = new ArrayList<BuildingInfo>();
	
	
	/* * * Drone Scouting * * */
	private boolean scoutingDrone;
	
	/** ID of the scouting drone */
	private int scoutDroneID;
	
	public int getScoutDroneID() {
		return scoutDroneID;
	}
	
	/** Which location should the scout drone go to next? */
	private int nextScoutLocation;
	
	/* Nearest base. Overlord goes here, scout drone avoids this */
	private BaseLocation nearestBase;
	
	private JNIBWAPI bwapi;
	private WorkerManager workerManager;
	private UnitManager unitManager;
	
	// Enemy players ID
	private ArrayList<Integer> enemyPlayersID = new ArrayList<Integer>();
	private ChokePoint enemyChokePoint = null;
	
	public IntelligenceManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager){
		this.bwapi = bwapi;
		this.workerManager = workerManager;
		this.unitManager = unitManager;
	}
	/** Sends a drone to go scout all of the base locations for the enemy */
	public boolean scoutDrone(){
		// Find a drone
		Unit worker = null;
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal() &&
					!workerManager.isWorkerBusy(unit.getID())) {
				worker = unit;
				break;
			}
		}
		// No worker found
		if(worker == null){
			return false;
		}
		
		workerManager.addBusyWorker(worker.getID());
		scoutDroneID = worker.getID();
		scoutingDrone = true;
		nextScoutLocation = 0;
		
		sendScoutDroneToNextLocation();
		
		return true;
	}
	
	/** Sends an overlord to go scout all of the base locations for the enemy */
	public void scoutOverlord(int unitID){
		BaseLocation scoutLocation = bwapi.getMap().getBaseLocations().get(overlordScoutIndex);
		if(scoutLocation.equals(getPlayerStartLocation())){
			incrementOverlordScout();
			scoutLocation = bwapi.getMap().getBaseLocations().get(overlordScoutIndex);
		}
		bwapi.move(unitID, scoutLocation.getX(), scoutLocation.getY());
		incrementOverlordScout();
	}
	
	private void incrementOverlordScout() {
		overlordScoutIndex = (overlordScoutIndex + 1) % bwapi.getMap().getBaseLocations().size();
	}
	
	
	private void sendScoutDroneToNextLocation(){
		
		if(scoutDroneID == Utility.NOT_SET){
			return;
		}
		bwapi.move(scoutDroneID, scoutLocations.get(nextScoutLocation).getX(),
				scoutLocations.get(nextScoutLocation).getY());
	}
	
	/** Gets the closest choke point to the enemy base.
	 * Calculates it the first time it is called, then saves it for later calls.
	 * @return
	 */
	public ChokePoint getEnemyChokePoint(){
		if(enemyChokePoint  == null && foundEnemyBase()){
			List<ChokePoint> chokePoints = bwapi.getMap().getChokePoints();
			ChokePoint closestLocation = null;
			double smallestDistance = Utility.NOT_SET;
			for(ChokePoint location: chokePoints){
				double baseDistance = Utility.getDistance(enemyStartLocation.getX(), enemyStartLocation.getY(),
						location.getCenterX(), location.getCenterY());
				if(closestLocation == null || baseDistance < smallestDistance){
					closestLocation = location;
					smallestDistance = baseDistance;
				}
			}
			enemyChokePoint = closestLocation;
		}
		return enemyChokePoint;
	}
	
	public void setBaseLocations(){
		// Set enemy ID
		// get hatchery
		Unit hatchery = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal());
		/* Get player locations */
		for(BaseLocation location : bwapi.getMap().getBaseLocations()){
			if(location.isStartLocation()
					//&& TODO:
//					(location.getTx() != hatchery.getTileX() &&
//					location.getTy() != hatchery.getTileY())
				){
				scoutLocations.add(location);
			}
		}
		// TODO: remove this once this is actually proven to be correct
		// Remove the smallest one
		BaseLocation closestLocation = null;
		double smallestDistance = Utility.NOT_SET;
		for(BaseLocation location: scoutLocations){
			double baseDistance = Utility.getDistance(hatchery.getX(), hatchery.getY(),
					location.getX(), location.getY());
			if(closestLocation == null || baseDistance < smallestDistance){
				closestLocation = location;
				smallestDistance = baseDistance;
			}
		}
		// Set the known locations of useful things, such as start location
		// and the nearest base location
		startLocation = closestLocation;
		scoutLocations.remove(closestLocation);
		nearestBase = getNearestBaseLocation(startLocation.getX(), startLocation.getY());
		// Remove the nearest location, then add it again, so it is at the end of the queue,
		// as overlord will scout this.
		scoutLocations.remove(nearestBase);
		scoutLocations.add(nearestBase);
	}
	
	public Point getClosestKnownEnemyLocation(Point location){
		ArrayList<Point> points = new ArrayList<Point>();
		for(BuildingInfo building : enemyBuildingLocations){
			points.add(building.location);
		}
		return Utility.getClosestLocation(points, location);
	}
	
	public int getEnemyBuildingLocationCount(){
		return enemyBuildingLocations.size();
	}
	
	/** Returns the nearest non-player start location to the given coordinates.
	 * 
	 * @return The nearest start location from the given coordiantes that is not the
	 * players spawn.
	 */
	private BaseLocation getNearestBaseLocation(int x, int y){
		if(scoutLocations == null){
			return null;
		}
		else{
			BaseLocation closest = null;
			double closestDistance = Utility.NOT_SET;
			for(BaseLocation location : scoutLocations){
				double distance = Utility.getDistance(x, y, 
						location.getX(), location.getY());
				if(closest == null || distance < closestDistance){
					closest = location;
					closestDistance = distance;
				}
			}
			return closest;
		}
	}
	
	public void gameStarted(){
		scoutingDrone = false;
		overlordScoutIndex = 0;
		enemyStartLocation = null;
		enemyPlayersID.clear();
		// Add all the enemy player IDs to the enemyPlayersID array
		for(Player player : bwapi.getEnemies()){
			enemyPlayersID.add(player.getID());
			System.out.println("New enemy :" + player.getID());
		}
		setBaseLocations();
		scoutOverlord(unitManager.getMyUnitOfType(UnitTypes.Zerg_Overlord.ordinal()).getID());
	}
	
	public void gameUpdate(){
		if(AIClient.DEBUG){
			int i = 0;
			for(BaseLocation location : scoutLocations){
				bwapi.drawDot(location.getX(), location.getY(), 0x04, false);
				bwapi.drawText(location.getX(), location.getY(), "Scout Location : " + i, false);
				i++;
			}
			bwapi.drawText(0,32, "Base located? " + foundEnemyBase(), true);
			bwapi.drawText(0,48, "isScouting? " + isScouting(), true);
		}
		
		// keep the overlords scouting dawg
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal()){
				if(unit.getHitPoints() < 185){
					bwapi.move(unit.getID(), startLocation.getX(), startLocation.getY());
				}
				else if(unit.isIdle()){
					scoutOverlord(unit.getID());
				}
			}
		}
	}
	
	/** Returns whether the enemy base location has been foudn */
	public boolean foundEnemyBase(){
		if(enemyStartLocation == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	/** Returns whether there is a drone/zergling currently scouting */
	public boolean isScouting(){
		return scoutingDrone;
	}
	
	/* Moves the scouting drone to the next location, or returns it to base
	 * if the scouting has been complete.
	 */
	public void moveScoutDroneToNextLocation(Unit unit){
		// Is scouting complete?
		if(++nextScoutLocation < scoutLocations.size() - 1){
			// move to next base
			sendScoutDroneToNextLocation();
		}
		else{
			if(!foundEnemyBase()){
				enemyStartLocation = scoutLocations.get(nextScoutLocation);
			}
			// stop scouting
			scoutingDrone = false;
			// Return to base
			Unit hatchery = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hatchery.ordinal());
			bwapi.move(scoutDroneID, hatchery.getX(), hatchery.getY());
			scoutDroneID = Utility.NOT_SET;
			workerManager.removeBusyWorker(unit.getID());
			
		}
	}
	
	/** On discovering a building, add it to the knowledge base, and if scouting return
	 * the scout to base.
	 */
	public void unitDiscover(int unitID){
		Unit unit = bwapi.getUnit(unitID);
		// Is this an enemy building?
		if(enemyPlayersID.contains(unit.getPlayerID())
				&& bwapi.getUnitType(unit.getTypeID()).isBuilding()){
			enemyBuildingLocations.add(new BuildingInfo(unit, bwapi));
			if(enemyStartLocation == null){
				enemyStartLocation = getNearestBaseLocation(unit.getX(), unit.getY());
				
				// Have scout drone go home
				if(scoutingDrone){
					scoutingDrone = false;
					bwapi.move(scoutDroneID, startLocation.getX(), startLocation.getY());
					workerManager.removeBusyWorker(scoutDroneID);
					scoutDroneID = Utility.NOT_SET;
				}
			}
		}
	}
	
	public void unitMorph(int unitID){
		for(BuildingInfo info : enemyBuildingLocations){
			// Remove the building the knowledge base
			if(info.id == unitID){
				System.out.println("Removed enemy building location");
				enemyBuildingLocations.remove(info);
				break;
			}
		}
		Unit unit = bwapi.getUnit(unitID);
		if(unit.getPlayerID() == bwapi.getSelf().getID()){
			if(unit.getTypeID() == UnitTypes.Zerg_Overlord.ordinal()){
				 scoutOverlord(unitID);
			}
		}
	}
	
	public void unitDestroy(int unitID){
		// Check if building is in the knowledge base
		for(BuildingInfo info : enemyBuildingLocations){
			// Remove the building the knowledge base
			if(info.id == unitID){
				System.out.println("Removed enemy building location");
				enemyBuildingLocations.remove(info);
				break;
			}
		}
		if(unitID == scoutDroneID){
			scoutingDrone = false;
		}
	}

}
