package scbod;

import java.util.ArrayList;

import scbod.Utility.CommonUnitType;
import scbod.managers.*;
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
 */
public class AIClient implements BWAPIEventListener, Runnable
{

	public static boolean		DEBUG	= false;

	/** reference to JNI-BWAPI */
	private JNIBWAPI			bwapi;

	/** Managers */
	private ArrayList<Manager>	managers;
	public ResourceManager		resourceManager;
	public ProductionManager	productionManager;
	public IntelligenceManager	intelligenceManager;
	public UnitManager			unitManager;
	public WorkerManager		workerManager;
	public BuildingManager		buildingManager;
	public UpgradeManager		upgradeManager;
	public MilitaryManager		militaryManager;
	public ScoutManager			scoutManager;

	/** Is the game currently playing? */
	private boolean				gameStarted;

	/**
	 * Create a Java AI.
	 */
	public static void main(String[] args)
	{
		Thread thread = new Thread(new AIClient());
		thread.start();
	}

	/** Creates a new thread and starts running the bot for the given AI module */
	public static void runBot(AIClient AI)
	{
		Thread thread = new Thread(AI);
		thread.start();
	}

	/**
	 * Instantiates the JNI-BWAPI interface and connects to BWAPI.
	 */
	public AIClient()
	{
		bwapi = new JNIBWAPI(this);
		managers = new ArrayList<Manager>();
	}

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
	 */
	public boolean isGameStarted()
	{
		return gameStarted;
	}

	public void test()
	{
		System.out.println("TEST!");
	}

	/**
	 * Connection to BWAPI established.
	 */
	public void connected()
	{
		bwapi.loadTypeData();
	}

	/**
	 * Called at the beginning of a game.
	 */
	public void gameStarted()
	{
		System.out.println("Game Started");
		gameStarted = true;
		Utility.setRace(RaceTypes.values()[bwapi.getSelf().getRaceID()]);

		// Create managers
		resourceManager		= new ResourceManager(bwapi);
		unitManager			= new UnitManager(bwapi);
		workerManager		= new WorkerManager(bwapi);
		scoutManager		= new ScoutManager(bwapi, workerManager);
		
		if (Utility.getRace() == RaceTypes.Zerg)
		{
			System.out.println("ZergZergZergZergZerg..");
			intelligenceManager	= new ZergIntelligenceManager(bwapi, unitManager, workerManager, scoutManager);
			buildingManager		= new ZergBuildingManager(bwapi, unitManager, workerManager, resourceManager);
			productionManager	= new ZergProductionManager(bwapi, resourceManager, buildingManager);
			militaryManager		= new ZergMilitaryManager(bwapi, intelligenceManager, unitManager, workerManager);
			upgradeManager		= new ZergUpgradeManager(bwapi, unitManager, resourceManager);
		}
		else if ( Utility.getRace() == RaceTypes.Terran)
		{
			System.out.println("Terran");
			intelligenceManager	= new IntelligenceManager(bwapi, unitManager, workerManager, scoutManager);
			buildingManager		= new TerranBuildingManager(bwapi, unitManager, workerManager, resourceManager);
			productionManager	= new TerranProductionManager(bwapi, resourceManager, buildingManager);
			militaryManager		= new MilitaryManager(bwapi, intelligenceManager, unitManager, workerManager);
			upgradeManager		= new ZergUpgradeManager(bwapi, unitManager, resourceManager);
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

	/** Game has ended */
	public void gameEnded()
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.gameEnded();
		}
		gameStarted = false;
	}

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

	public void matchEnded(boolean winner)
	{
	}

	public void nukeDetect(int x, int y)
	{
	}

	public void nukeDetect()
	{
	}

	public void playerLeft(int id)
	{
	}

	public void unitCreate(int unitID)
	{		
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitCreate(unitID);
		}
	}

	public void unitDestroy(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitDestroy(unitID);
		}

	}

	public void unitDiscover(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitDiscover(unitID);
		}

	}

	public void unitEvade(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitEvade(unitID);
		}
	}

	public void unitHide(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitHide(unitID);
		}
	}

	public void unitMorph(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitMorph(unitID);
		}
	}

	public void unitShow(int unitID)
	{
		// Call managers
		for (Manager manager : managers)
		{
			manager.unitShow(unitID);
		}
	}

	@Override
	public void sendText(String text)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveText(String text)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void unitRenegade(int unitID)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveGame(String gameName)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void unitComplete(int unitID)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void playerDropped(int playerID)
	{
		// TODO Auto-generated method stub

	}
}
