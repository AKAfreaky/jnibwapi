package scbod;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/** Worker manager controls all the workers and has them all mining minerals.
 * Workers can be requisitioned by other managers for use by calling
 * the addBusyWorker method, and then should be released with the removeBusyWorker method.
 *
 */
public class WorkerManager extends Manager {

	/** Holds all of the mineral fields near to a hatchery that should be mined */
	private ArrayList<MineralAllocation> mineralFields = new ArrayList<MineralAllocation>();
	
	private ArrayList<BaseInfo> baseHatcheries = new ArrayList<BaseInfo>();
	
	/** used for identifying all of the workers that are busy with a task and should 
	 * not be called to do another task */
	private ArrayList<Integer> busyWorkers = new ArrayList<Integer>();
	
	/** used for identifying all of the workers that are collecting gas */
	private ArrayList<Integer> globalGasWorkers = new ArrayList<Integer>();
	
	/** All of the workers */
	private ArrayList<Integer> workers = new ArrayList<Integer>();
	
	/** List of gesyers */
	private ArrayList<ExtractorInfo> extractors = new ArrayList<ExtractorInfo>();
	
	JNIBWAPI bwapi;
	
	public WorkerManager(JNIBWAPI bwapi){
		this.bwapi = bwapi;
	}
	
	public int getDroneCount(){
		int count = 0;
		// Count drones in play
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal()){
				count++;
			}
		}
		// Count drones about to be morphed
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == UnitTypes.Zerg_Egg.ordinal()){
				if(unit.getBuildTypeID() == UnitTypes.Zerg_Drone.ordinal()){
					count++;
				}
			}
		}
		return count;
	}
	
	/** returns the number of drones currently collecting gas */
	public int getGasDroneCount(){
		return globalGasWorkers.size();
	}
	
	/** 
	 * Sends 3 workers to each build geyser.
	 */
	public boolean startCollectingGas(){
		for(ExtractorInfo extractor : extractors){
			Unit extractorUnit = bwapi.getUnit(extractor.geyserID);
			Unit worker = null;
			double currentShortestDistance = Utility.NOT_SET;
			
			/* Already have enough workers, don't want anymore */
			if(extractor.gasWorkers.size() >= 3){
				continue;
			}
			
			// Find the 3 nearest drones
			for(int i = extractor.gasWorkers.size(); i < 3; i++){
				worker = null;
				currentShortestDistance = Utility.NOT_SET;
				// Find the nearest free drone
				for (Unit unit : bwapi.getMyUnits()) {
					if (unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal() && !unit.isCarryingMinerals() &&
							!busyWorkers.contains(unit.getID()) && !globalGasWorkers.contains(unit.getID())) {
						double workerDistance = Utility.getDistance(extractorUnit.getX(), unit.getX(), extractorUnit.getY(), unit.getY());
						if (worker == null ||
								workerDistance < currentShortestDistance){
							worker = unit;
							currentShortestDistance = workerDistance;		
						}
					}
				}
				
				if(worker == null){
					return false;
				}
				bwapi.rightClick(worker.getID(), extractorUnit.getID());
				globalGasWorkers.add(worker.getID());
				extractor.gasWorkers.add(worker.getID());
			}
		}
		return true;
	}
	
	public Unit getNearestFreeDrone(int tileX, int tileY) {
		Unit worker = null;
		double currentShortestDistance = Utility.NOT_SET;
		// Find the nearest drone
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal() &&
					!isWorkerBusy(unit.getID()) && !isGasWorker(unit.getID())) {
				double workerDistance = Utility.getDistance(tileX, tileY,
						unit.getTileX(), unit.getTileY());
				if (worker == null ||
						workerDistance < currentShortestDistance){
					worker = unit;
					currentShortestDistance = workerDistance;		
				}
			}
		}
		return worker;
	}
	
	/** Is the given worker ID busy on something and should not
	 * be disturbed?
	 */
	public boolean isWorkerBusy(int unitID){
		if(busyWorkers.contains(unitID)){
			return true;
		}
		else{
			return false;
		}
	}
	/** Is the given worker ID a gas worker?
	 */
	public boolean isGasWorker(int unitID){
		if(globalGasWorkers.contains(unitID)){
			return true;
		}
		else{
			return false;
		}
	}
	
	/** Add the given ID to the list of busy workers */
	public void addBusyWorker(int unitID){
		busyWorkers.add(unitID);
	}
	
	/** Remove the given ID from the list of busy workers */
	public void removeBusyWorker(int unitID){
		busyWorkers.remove(unitID);
	}
	
	@Override
	public void gameStarted(){
		mineralFields.clear();
		busyWorkers.clear();
		baseHatcheries.clear();
		globalGasWorkers.clear();
		workers.clear();
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal()){
				workers.add(unit.getID());
			}
			if(unit.getTypeID() == UnitTypes.Zerg_Hatchery.ordinal()){
				newHatchery(new BaseInfo(unit));
			}
		}
	}

	public void newHatchery(BaseInfo newHatchery){
		baseHatcheries.add(newHatchery);
		Unit hatchery = newHatchery.structure;
		System.out.println("WORKER MANAGER : NEW HATCHERY!");
		if(hatchery.getTypeID() != UnitTypes.Zerg_Hatchery.ordinal()){
			System.out.println("Minerals : Unit type not a hatchery! Ignoring...");
			return;
		}
		// Find all minerals that are nearby
		for(Unit unit : bwapi.getNeutralUnits()){
			if(hasMineralID(unit.getID()))
				continue;
			if(unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()){
				// Are the minerals close?
				if(Utility.getDistance(unit.getX(), unit.getY(), hatchery.getX(), hatchery.getY()) < 500){
					Point location = new Point(unit.getX(), unit.getY());
					mineralFields.add(new MineralAllocation(unit.getID(), location));
					continue;
				}
			}
		}
		recalculateDroneMining();
	}
	
	/** Checks whether the given unitID is a known hatchery, and if so,
	 * remove the mineral patches nearby from the mineral allocation mechanism.
	 * @param unitID
	 */
	private void killedHatchery(int unitID){
		// Find the lost hatchery
		for(BaseInfo base : baseHatcheries){
			if(base.id == unitID){
				for(Unit unit : bwapi.getNeutralUnits()){
					if(unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()){
						// Are the minerals close?
						if(Utility.getDistance(unit.getX(), unit.getY(), base.location.x, base.location.y) < 500){
							removeMineral(unit.getID());
						}
					}
				}
				recalculateDroneMining();
			}
		}
	}
	
	// Remove a mineral field
	private void removeMineral(int id){
		for (MineralAllocation mineral : mineralFields){
			if(mineral.getID() == id){
				mineralFields.remove(mineral);
				break;
			}
		}
	}
	
	public void recalculateDroneMining(){
		for(MineralAllocation mineral : mineralFields){
			mineral.clearDrones();
		}
		for(Unit unit : bwapi.getMyUnits()){
			// don't disturb the busy and the gas workers!
			if(busyWorkers.contains(unit.getID()) || globalGasWorkers.contains(unit.getID()))
				continue;
			if(unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal()){
				calculateMining(unit.getID());
			}
		}
	}
	
	/** Sends a drone to go mine at the best possible mineral patch */
	private void calculateMining(int unitID){
		Unit drone = bwapi.getUnit(unitID);
		MineralAllocation bestMineral = null;
		int lowestAllocationCount = Utility.NOT_SET;
		double lowestDistance = Utility.NOT_SET;
		for(MineralAllocation mineral : mineralFields){
			double distance = Utility.getDistance(drone.getX(), drone.getY(),
					mineral.getLocation().x, mineral.getLocation().y);
			// if nothing been allocated so far, set as this one
			// else get the one with the lowest allocation count
			// else get the closest one with lowest allocation count
			if((lowestAllocationCount == Utility.NOT_SET || lowestDistance == Utility.NOT_SET) ||
					(mineral.getDroneCount() < lowestAllocationCount) ||
					(mineral.getDroneCount() == lowestAllocationCount && distance < lowestDistance)){
				lowestAllocationCount = mineral.getDroneCount();
				lowestDistance = distance;
				bestMineral = mineral;
			}
			else{
				continue;
			}
		}
		
		if(bestMineral == null){
			System.out.println("No minerals found - giving up!");
			return;
		}
		// send drone to mine
		bwapi.rightClick(unitID, bestMineral.getID());
		// add drone to the minerals known drone list
		bestMineral.assignDrone(unitID);
	}
	
	private boolean hasMineralID(int unitID){
		for(MineralAllocation mineral : mineralFields){
			if(mineral.getID() == unitID)
				return true;
		}
		return false;
	}
	
	@Override
	public void unitDestroy(int unitID){
		// If it is a mineral, remove it
		removeMineral(unitID);
		if(workers.contains(unitID)){
			workers.remove(unitID);
			// Check to see whether a drone is currently mining, if so,
			// remove it form mineral allocations.
			for(MineralAllocation mineral : mineralFields){
				mineral.removeDrone(unitID);
			}
		}
		if(busyWorkers.contains(unitID)){
			busyWorkers.remove(unitID);
		}
		if(globalGasWorkers.contains(unitID)){
			globalGasWorkers.remove(unitID);
			for(ExtractorInfo extractor : extractors){
				if(extractor.gasWorkers.contains(unitID)){
					extractor.gasWorkers.remove(unitID);
					break;
				}
			}
		}
		// Check for a killed hatchery?
		killedHatchery(unitID);
	}
	
	@Override
	public void unitMorph(int unitID){
		Unit unit = bwapi.getUnit(unitID);
		if(unit.getPlayerID() == bwapi.getSelf().getID()){
			if(unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal()){
				workers.add(unitID);
			}
			if(unit.getTypeID() == UnitTypes.Zerg_Extractor.ordinal()){
				extractors.add(new ExtractorInfo(unitID));
			}
			if(workers.contains(unitID) && unit.getTypeID() != UnitTypes.Zerg_Drone.ordinal()){
				workers.remove(unitID);
			}
		}
	}
	
	/** If there is an idle drone, have it return to mining */
	public void idleWorker(Unit worker){
		if(!busyWorkers.contains(worker.getID())){
			calculateMining(worker.getID());		
		}
	}
	
	@Override
	public void gameUpdate(){
		if(AIClient.DEBUG){
			bwapi.drawText(new Point(320,32), "Drone Count : " + getDroneCount(), true);
			for(Unit unit : bwapi.getMyUnits()){
				if(unit.getTypeID() == UnitTypes.Zerg_Drone.ordinal()){
					// worker is lying and isn't busy, send them back to work!
					if(isWorkerBusy(unit.getID())){
						bwapi.drawText(unit.getX() + 2, unit.getY() + 4, "Busy", false);
					}
					if(isGasWorker(unit.getID())){
						bwapi.drawText(unit.getX() + 2, unit.getY()+ 4, "Gas", false);
					}
				}
			}
			// Draw minerals
			for(MineralAllocation minerals : mineralFields){
				bwapi.drawText(minerals.getLocation().x, minerals.getLocation().y + 16,
						"Min: " + minerals.getDroneCount(), false);
			}
		}
	}

	
}
