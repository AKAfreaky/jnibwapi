package scbod;

import java.util.ArrayList;

import scbod.Utility.CommonUnitType;
import scbod.managers.*;
import scbod.managers.Protoss.*;
import scbod.managers.Terran.*;
import scbod.managers.Zerg.*;
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.RaceType.RaceTypes;

/**
 * Java AI Client using JNI-BWAPI.
 * 
 * Contains the interfaces that are eventually used by the BOD / POSH action
 * selection mechanism.
 * 
 * @author Simon Davies
 * @author Alex Aiton
 */
public class AIClient implements BWAPIEventListener, Runnable
{
	/** If true, draws debug info to the screen */
	public static boolean		DEBUG	= false;

	/** reference to JNI-BWAPI */
	private JNIBWAPI			bwapi;

	/** List of all active managers */
	private ArrayList<Manager>	managers;
	/** Keeps track of Minerals, Gas and Supply */
	public ResourceManager		resourceManager;
	/** Controls training more units */
	public ProductionManager	productionManager;
	/** Managers what is known about the enemy */
	public IntelligenceManager	intelligenceManager;
	/** Keeps track of our units */
	public UnitManager			unitManager;
	/** Controls all workers */
	public WorkerManager		workerManager;
	/** Controls building */
	public BuildingManager		buildingManager;
	/** Controls and keeps track of research done */
	public UpgradeManager		upgradeManager;
	/** Controls miliarty units */
	public MilitaryManager		militaryManager;
	/** Controls scout units */
	public ScoutManager			scoutManager;

	/** Is the game currently playing? */
	private boolean				gameStarted;

	/**
	 * Create a Java AI.
	 * 
	 * @author Simon Davies
	 */
	public static void main(String[] args)
	{
		Thread thread = new Thread(new AIClient());
		thread.start();
	}

	/**
	 * Creates a new thread and starts running the bot for the given AI module
	 * 
	 * @author Simon Davies
	 */
	public static void runBot(AIClient AI)
	{
		Thread thread = new Thread(AI);
		thread.start();
	}

	/**
	 * Instantiates the JNI-BWAPI interface and connects to BWAPI.
	 * 
	 * @author Simon Davies
	 */
	public AIClient()
	{
		bwapi = new JNIBWAPI(this);
		managers = new ArrayList<Manager>();
	}

	/**
	 * Connects to the game. This function will never return and should be run
	 * in a seperate thread.
	 * 
	 * @author Simon Davies
	 */
	public void run()
	{
		try
		{
			bwapi.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Has a game of starcraft been started yet?
	 * 
	 * @author Simon Davies
	 * @return gameStarted - Has a game/match started
	 */
	public boolean isGameStarted()
	{
		return gameStarted;
	}

	/**
	 * Connection to BWAPI established.
	 * 
	 * @author Simon Davies
	 */
	public void connected()
	{
		bwapi.loadTypeData();
	}

	/**
	 * Called at the beginning of a game.
	 * 
	 * @author Simon Davies
	 */
	public void gameStarted()
	{
		System.out.println("Game Started");
		gameStarted = true;
		Utility.setRace(RaceTypes.values()[bwapi.getSelf().getRaceID()]);

		// Create managers
		resourceManager = new ResourceManager(bwapi);
		unitManager = new UnitManager(bwapi);
		workerManager = new WorkerManager(bwapi);
		scoutManager = new ScoutManager(bwapi, workerManager);

		// aa425 - This block of if statements was added by me to set up as the
		// correct race
		if (Utility.getRace() == RaceTypes.Zerg)
		{
			System.out.println("ZergZergZergZergZerg..");
			intelligenceManager = new ZergIntelligenceManager(bwapi, unitManager, workerManager, scoutManager);
			buildingManager = new ZergBuildingManager(bwapi, unitManager, workerManager, resourceManager);
			productionManager = new ZergProductionManager(bwapi, resourceManager, buildingManager);
			militaryManager = new ZergMilitaryManager(bwapi, intelligenceManager, unitManager, workerManager);
			upgradeManager = new ZergUpgradeManager(bwapi, unitManager, resourceManager, buildingManager);
		}
		else if (Utility.getRace() == RaceTypes.Terran)
		{
			System.out.println("One ornery son of a bitch");
			intelligenceManager = new IntelligenceManager(bwapi, unitManager, workerManager, scoutManager);
			buildingManager = new TerranBuildingManager(bwapi, unitManager, workerManager, resourceManager);
			productionManager = new TerranProductionManager(bwapi, resourceManager, buildingManager);
			militaryManager = new TerranMilitaryManager(bwapi, intelligenceManager, unitManager, workerManager);
			upgradeManager = new ZergUpgradeManager(bwapi, unitManager, resourceManager, buildingManager);
		}
		else if (Utility.getRace() == RaceTypes.Protoss)
		{
			System.out.println("I can crush you with my mind!");
			intelligenceManager = new IntelligenceManager(bwapi, unitManager, workerManager, scoutManager);
			buildingManager = new ProtossBuildingManager(bwapi, unitManager, workerManager, resourceManager);
			productionManager = new ProtossProductionManager(bwapi, resourceManager, buildingManager);
			militaryManager = new ProtossMilitaryManager(bwapi, intelligenceManager, unitManager, workerManager);
			upgradeManager = new ProtossUpgradeManager(bwapi, unitManager, resourceManager, buildingManager);
		}

		// Add managers to the call list
		// All managers added here will have their associated
		// methods called for the bwapi events
		managers.add(resourceManager);
		managers.add(unitManager);
		managers.add(workerManager);
		managers.add(intelligenceManager);
		managers.add(buildingManager);
		managers.add(productionManager);
		managers.add(upgradeManager);
		managers.add(militaryManager);

		// Cheat flag for user input
		// uncomment this to allow user input
		bwapi.enableUserInput();
		// Load BWTA data
		bwapi.loadMapData(true);
		// Set gamespeed
		bwapi.setGameSpeed(2);

		if (DEBUG)
		{
			// Draw player targets if drawInfo is set to true
			bwapi.drawTargets(true);
			bwapi.drawIDs(true);
		}

		// Call managers
		for (Manager manager : managers)
		{
			manager.gameStarted();
		}

	}

	/**
	 * Called each game cycle.
	 * 
	 * @author Simon Davies
	 */
	public void gameUpdate()
	{

		// Call managers
		for (Manager manager : managers)
		{
			manager.gameUpdate();
		}

		for (Unit unit : bwapi.getMyUnits())
		{
			// aa425 - I modified this slightly to check for idle scouts and
			// idle builders
			if (unit.isIdle())
			{
				if (!scoutManager.idleScout(unit.getID()))
				{
					if (unit.getTypeID() == Utility.getCommonTypeID(CommonUnitType.Worker))
					{
						buildingManager.idleWorker(unit.getID());
						workerManager.idleWorker(unit);
					}
				}
			}
		}
	}

	/**
	 * Game has ended
	 * 
	 * @author Simon Davies
	 */
	public void gameEnded()
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.gameEnded();
		}
		gameStarted = false;
	}

	/**
	 * Called when a key is pressed
	 * 
	 * @author Simon Davies
	 */
	public void keyPressed(int keyCode)
	{
		// On press D
		// Toggle DEBUG
		if (keyCode == 68)
		{
			DEBUG = !DEBUG;
			if (DEBUG)
			{
				bwapi.drawTargets(true);
				bwapi.drawIDs(true);
			}
			else
			{
				bwapi.drawTargets(false);
				bwapi.drawIDs(false);
			}
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void matchEnded(boolean winner)
	{
		System.out.println("matchEnded!");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void nukeDetect(int x, int y)
	{
		System.out.println("nukeDetect! xy");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void nukeDetect()
	{
		System.out.println("nukeDetect!");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void playerLeft(int id)
	{
		System.out.println("playerLeft!");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitCreate(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitCreate(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitDestroy(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitDestroy(unitID);
		}

	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitDiscover(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitDiscover(unitID);
		}

	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitEvade(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitEvade(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitHide(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitHide(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitMorph(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitMorph(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Simon Davies
	 */
	@Override
	public void unitShow(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitShow(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Alex Aiton
	 */
	@Override
	public void sendText(String text)
	{
		System.out.println("sendText!");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Alex Aiton
	 */
	@Override
	public void receiveText(String text)
	{
		System.out.println("receiveText!");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Alex Aiton
	 */
	@Override
	public void unitRenegade(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitRenegade(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Alex Aiton
	 */
	@Override
	public void saveGame(String gameName)
	{
		System.out.println("saveGame!");
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Alex Aiton
	 */
	@Override
	public void unitComplete(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitComplete(unitID);
		}
	}

	/**
	 * BWAPI Callback
	 * 
	 * @author Alex Aiton
	 */
	@Override
	public void playerDropped(int playerID)
	{
		System.out.println("playerDropped!");
	}

	/**
	 * Lets the POSH plan do nothing without quitting its drive Honestly, might
	 * be better to re-work plans so this isn't needed
	 * 
	 * @author Alex Aiton
	 */
	public boolean idle()
	{
		return true;
	}
}
