package scbod.managers.Terran;

import jnibwapi.JNIBWAPI;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.UpgradeManager;

/**
 * Stub sub-class for any Terran specific Upgrade behaviours
 * 
 * @author Alex Aiton
 */
public class TerranUpgradeManager extends UpgradeManager
{

	public TerranUpgradeManager(JNIBWAPI bwapi, UnitManager unitManager, ResourceManager resourceManager)
	{
		super(bwapi, unitManager, resourceManager);
	}

}
