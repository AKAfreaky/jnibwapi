package scbod.managers.Zerg;

import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.UpgradeManager;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;

/** Manages the methods for getting upgrades, such as zergling speed */
public class ZergUpgradeManager extends UpgradeManager
{
	private boolean			researchingLurkerAspect	= false;

	public ZergUpgradeManager(JNIBWAPI bwapi, UnitManager unitManager, ResourceManager resourceManager)
	{
		super(bwapi, unitManager, resourceManager);
	}

	/**
	 * aa425 - There is bug in the JNI version of hasResearched that means it always returns true for Lurker aspect,
	 *  so have to keep this around for now
	 * 
	 * @author Simon Davies
	 */
	public boolean hasLurkerAspect(boolean completed)
	{
		if (completed)
		{
			if (!researchingLurkerAspect)
				return false;
			else
				return bwapi.getSelf().hasResearched(TechTypes.Lurker_Aspect.ordinal())
						&& !bwapi.getSelf().isResearching(TechTypes.Lurker_Aspect.ordinal());
		}
		else
		{
			return researchingLurkerAspect;
		}
	}

	/**
	 * Research Lurker Aspect
	 * 
	 * @author Simon Davies
	 */
	public boolean researchLurkerAspect()
	{
		if (resourceManager.getMineralCount() < 200 || resourceManager.getGasCount() < 200)
		{
			return false;
		}
		Unit lair = null;
		lair = unitManager.getMyUnitOfType(UnitTypes.Zerg_Lair.ordinal());

		Unit den = null;
		den = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hydralisk_Den.ordinal());

		if (lair == null || !lair.isCompleted())
		{
			return false;
		}

		if (den == null || !den.isCompleted())
		{
			return false;
		}

		if (den.isUpgrading())
		{
			return false;
		}

		bwapi.research(den.getID(), TechTypes.Lurker_Aspect.ordinal());
		researchingLurkerAspect = true;
		return true;
	}

}
