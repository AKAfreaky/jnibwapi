package scbod.managers.Protoss;

import jnibwapi.JNIBWAPI;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.UpgradeManager;

/**
 * Stub sub-class for any Protoss Specific Upgrade behaviours
 * 
 * @author Alex Aiton
 */
public class ProtossUpgradeManager extends UpgradeManager
{
	ProtossBuildingManager buildingManager;
	
	public ProtossUpgradeManager(JNIBWAPI bwapi, UnitManager unitManager, ResourceManager resourceManager)
	{
		super(bwapi, unitManager, resourceManager);
	}
}
