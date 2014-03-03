package scbod.managers.Protoss;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UpgradeType.UpgradeTypes;
import scbod.managers.BuildingManager;
import scbod.managers.ResourceManager;
import scbod.managers.UnitManager;
import scbod.managers.UpgradeManager;

public class ProtossUpgradeManager extends UpgradeManager
{
	JNIBWAPI bwapi;
	UnitManager unitManager;
	ResourceManager resourceManager;
	ProtossBuildingManager buildingManager;
	
	public ProtossUpgradeManager(JNIBWAPI bwapi, UnitManager unitManager, ResourceManager resourceManager, BuildingManager buildingManager)
	{
		this.bwapi = bwapi;
		this.unitManager = unitManager;
		this.resourceManager = resourceManager;
		this.buildingManager = (ProtossBuildingManager) buildingManager;
	}
	
	public boolean upgradeGroundWeapons()
	{
		int currLevel = getGroundWeaponsLevel();
		
		if(currLevel > 2)
		{
			System.out.println("No more upgrades available for ground weapons");
			return false;
		}
		
		if (resourceManager.getMineralCount() < (100 + (currLevel * 50)) || resourceManager.getGasCount() < (100 + (currLevel * 50)))
		{
			return false;
		}
		
		Unit building = null;
		building = buildingManager.getFreeForge();

		if (building == null)
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Protoss_Ground_Weapons.ordinal());
		return true;
	}
	
	public int getGroundWeaponsLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Protoss_Ground_Weapons.ordinal());
	}
	
	public boolean upgradeGroundArmor()
	{
		int currLevel = getGroundArmorLevel();
		
		if(currLevel > 2)
		{
			System.out.println("No more upgrades available for ground armor");
			return false;
		}
		
		if (resourceManager.getMineralCount() < (100 + (currLevel * 75)) || resourceManager.getGasCount() < (100 + (currLevel * 75)))
		{
			return false;
		}
		
		Unit building = null;
		building = buildingManager.getFreeForge();

		if (building == null)
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Protoss_Ground_Armor.ordinal());
		return true;
	}
	
	public int getGroundArmorLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Protoss_Ground_Armor.ordinal());
	}
}
