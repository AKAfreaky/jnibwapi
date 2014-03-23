package scbod.managers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import scbod.AIClient;
import scbod.BuildingInfo;
import scbod.ScoutFinished;
import scbod.Utility;
import scbod.Utility.CommonUnitType;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Player;
import jnibwapi.model.Unit;
import jnibwapi.types.RaceType.RaceTypes;

/**
 * Handles the tasks of scouting and what areas have been scouted, and where the
 * enemy is located.
 * 
 */
public class IntelligenceManager extends Manager implements ScoutFinished
{

	// Start Locations
	// enemyStartLocation starts as unknown
	protected BaseLocation	enemyStartLocation;
	protected BaseLocation	startLocation;

	public BaseLocation getEnemyStartLocation()
	{
		return enemyStartLocation;
	}

	public BaseLocation getPlayerStartLocation()
	{
		return startLocation;
	}

	/* Known building locations */
	private ArrayList<BuildingInfo>	enemyBuildingLocations	= new ArrayList<BuildingInfo>();

	/* * * Drone Scouting * * */
	private boolean					scoutingDrone;

	/** ID of the scouting drone */
	private int						scoutDroneID;

	public int getScoutDroneID()
	{
		return scoutDroneID;
	}

	protected JNIBWAPI			bwapi;
	protected WorkerManager		workerManager;
	protected UnitManager		unitManager;
	protected ScoutManager		scoutManager;

	// Enemy players ID
	private ArrayList<Integer>	enemyPlayersID	= new ArrayList<Integer>();
	private ChokePoint			enemyChokePoint	= null;

	public IntelligenceManager(JNIBWAPI bwapi, UnitManager unitManager, WorkerManager workerManager, ScoutManager scoutManager)
	{
		this.bwapi			= bwapi;
		this.workerManager	= workerManager;
		this.unitManager	= unitManager;
		this.scoutManager	= scoutManager;
	}

	/** Sends a drone to go scout all of the base locations for the enemy */
	public boolean scoutDrone()
	{
		// Find a drone
		Unit worker = null;
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == workerManager.getWorkerTypeID() && !workerManager.isWorkerBusy(unit.getID()))
			{
				worker = unit;
				break;
			}
		}
		// No worker found
		if (worker == null)
		{
			return false;
		}

		workerManager.addBusyWorker(worker.getID());
		scoutDroneID	= worker.getID();
		scoutingDrone	= true;
		
		return scoutManager.scoutBaseLocations(worker.getID(), this);
	}

	/**
	 * Gets the closest choke point to the enemy base. Calculates it the first
	 * time it is called, then saves it for later calls.
	 * 
	 * @return
	 */
	public ChokePoint getEnemyChokePoint()
	{
		if (enemyChokePoint == null && foundEnemyBase())
		{
			List<ChokePoint> chokePoints = bwapi.getMap().getChokePoints();
			ChokePoint closestLocation = null;
			double smallestDistance = Utility.NOT_SET;
			for (ChokePoint location : chokePoints)
			{
				double baseDistance = Utility.getDistance(enemyStartLocation.getX(), enemyStartLocation.getY(),
						location.getCenterX(), location.getCenterY());
				if (closestLocation == null || baseDistance < smallestDistance)
				{
					closestLocation = location;
					smallestDistance = baseDistance;
				}
			}
			enemyChokePoint = closestLocation;
		}
		return enemyChokePoint;
	}

	public void setBaseLocations()
	{
		// Get the base unit 
		Unit theBase = unitManager.getMyUnitOfType(Utility.getCommonTypeID(CommonUnitType.Base));
		
		BaseLocation closestLocation = null;
		double smallestDistance = Utility.NOT_SET;
		
		for (BaseLocation location : bwapi.getMap().getBaseLocations())
		{
			if (location.isStartLocation())
			{
				double baseDistance = Utility.getDistance(theBase.getX(), theBase.getY(), 
														  location.getX(), location.getY());
				if (closestLocation == null || baseDistance < smallestDistance)
				{
					closestLocation = location;
					smallestDistance = baseDistance;
				}
			}
		}
		// Set the known locations of useful things, such as start location
		// and the nearest base location
		startLocation = closestLocation;
	}

	public Point getClosestKnownEnemyLocation(Point location)
	{
		ArrayList<Point> points = new ArrayList<Point>();
		for (BuildingInfo building : enemyBuildingLocations)
		{
			points.add(building.location);
		}
		return Utility.getClosestLocation(points, location);
	}

	public int getEnemyBuildingLocationCount()
	{
		return enemyBuildingLocations.size();
	}

	/**
	 * Returns the nearest non-player start location to the given coordinates.
	 * 
	 * @return The nearest start location from the given coordiantes that is not
	 *         the players spawn.
	 */
	private BaseLocation getNearestBaseLocation(int x, int y)
	{
		BaseLocation closest = null;
		double closestDistance = Utility.NOT_SET;
		
		for (BaseLocation location : bwapi.getMap().getBaseLocations())
		{
			if (location.isStartLocation() && !startLocation.equals(location))
			{
				double distance = Utility.getDistance(x, y, location.getX(), location.getY());
				if (closest == null || distance < closestDistance)
				{
					closest = location;
					closestDistance = distance;
				}
			}
		}
		return closest;
	}

	public void gameStarted()
	{
		scoutingDrone = false;
		enemyStartLocation = null;
		enemyPlayersID.clear();
		// Add all the enemy player IDs to the enemyPlayersID array
		for (Player player : bwapi.getEnemies())
		{
			enemyPlayersID.add(player.getID());
			System.out.println("New enemy :" + player.getID());
		}
		setBaseLocations();
	}

	public void gameUpdate()
	{
		if (AIClient.DEBUG)
		{
			int i = 0;
			for (BaseLocation location : bwapi.getMap().getBaseLocations())
			{
				bwapi.drawDot(location.getX(), location.getY(), 0x04, false);
				bwapi.drawText(location.getX(), location.getY(), "Scout Location : " + i, false);
				i++;
			}
			bwapi.drawText(0, 32, "Base located? " + foundEnemyBase(), true);
			bwapi.drawText(0, 48, "isScouting? " + isScouting(), true);
			for(Player player: bwapi.getEnemies())
			{
				bwapi.drawText(0, 64, "enemyRace: " + bwapi.getRaceType(player.getRaceID()).getName(), true);
			}
			
		}
	}

	/** Returns whether the enemy base location has been foudn */
	public boolean foundEnemyBase()
	{
		if (enemyStartLocation == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/** Returns whether there is a drone/zergling currently scouting */
	public boolean isScouting()
	{
		return scoutingDrone;
	}


	/**
	 * On discovering a building, add it to the knowledge base, and if scouting
	 * return the scout to base.
	 */
	public void unitDiscover(int unitID)
	{
		Unit unit = bwapi.getUnit(unitID);
		// Is this an enemy building?
		if (enemyPlayersID.contains(unit.getPlayerID()) && bwapi.getUnitType(unit.getTypeID()).isBuilding())
		{
			enemyBuildingLocations.add(new BuildingInfo(unit, bwapi));
			if (enemyStartLocation == null)
			{
				enemyStartLocation = getNearestBaseLocation(unit.getX(), unit.getY());

				// Have scout drone go home
				if (scoutingDrone)
				{
					scoutManager.stopScout(scoutDroneID);
				}
			}
		}
	}

	public void unitMorph(int unitID)
	{
		for (BuildingInfo info : enemyBuildingLocations)
		{
			// Remove the building the knowledge base
			if (info.id == unitID)
			{
				System.out.println("Removed enemy building location");
				enemyBuildingLocations.remove(info);
				break;
			}
		}
	}

	public void unitDestroy(int unitID)
	{
		// Check if building is in the knowledge base
		for (BuildingInfo info : enemyBuildingLocations)
		{
			// Remove the building the knowledge base
			if (info.id == unitID)
			{
				System.out.println("Removed enemy building location");
				enemyBuildingLocations.remove(info);
				break;
			}
		}
		if (unitID == scoutDroneID)
		{
			scoutingDrone = false;
			scoutDroneID = Utility.NOT_SET;
		}
	}

	@Override
	public void scoutRouteCompleted(int scoutID)
	{
		if (scoutID == scoutDroneID)
		{
			if (!foundEnemyBase())
			{
				Unit unit = bwapi.getUnit(scoutID);
				enemyStartLocation = getNearestBaseLocation(unit.getX(), unit.getY());
			}
			
			// Return to startlocation
						
			scoutingDrone = false;
			bwapi.move(scoutDroneID, startLocation.getX(), startLocation.getY());
			workerManager.removeBusyWorker(scoutDroneID);
			scoutDroneID = Utility.NOT_SET;
		}
	}
	
	public boolean isEnemyRace( RaceTypes race)
	{
		for(Player player: bwapi.getEnemies())
		{
			if(player.getRaceID() == race.ordinal())
				return true;
		}
		
		return false;
	}

}
