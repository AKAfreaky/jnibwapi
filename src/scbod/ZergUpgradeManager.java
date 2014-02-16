package scbod;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType.UpgradeTypes;

/** Manages the methods for getting upgrades, such as zergling speed */
public class ZergUpgradeManager extends UpgradeManager
{

	private ResourceManager	resourceManager;
	private UnitManager		unitManager;
	private JNIBWAPI		bwapi;

	private boolean			hasZerglingSpeed		= false;
	private boolean			hasHydraliskSpeed		= false;
	private boolean			hasHydraliskRange		= false;

	private boolean			researchingLurkerAspect	= false;

	private boolean			hasOverlordSpeed		= false;

	public int getZergFlyerAttackLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Flyer_Attacks.ordinal());
	}

	public int getZergFlyerCarapaceLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Flyer_Carapace.ordinal());
	}

	public int getZergMeleeLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Melee_Attacks.ordinal());
	}

	public int getZergRangedLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Missile_Attacks.ordinal());
	}

	public int getZergCarapaceLevel()
	{
		return bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Carapace.ordinal());
	}

	public boolean hasZerglingSpeed()
	{
		return hasZerglingSpeed;
	}

	public boolean hasHydraliskSpeed()
	{
		return hasHydraliskSpeed;
	}

	public boolean hasHydraliskRange()
	{
		return hasHydraliskRange;
	}

	public boolean hasOverlordSpeed()
	{
		return hasOverlordSpeed;
	}

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

	public ZergUpgradeManager(JNIBWAPI bwapi, UnitManager unitManager, ResourceManager resourceManager)
	{
		this.bwapi = bwapi;
		this.unitManager = unitManager;
		this.resourceManager = resourceManager;
	}

	/** Are any of the evo chambers researching melee upgrades? */
	public boolean getUpgradingMelee()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Evolution_Chamber.ordinal())
			{
				if (unit.isCompleted() && unit.isUpgrading())
				{
					if (unit.getUpgradingUpgradeID() == UpgradeTypes.Zerg_Melee_Attacks.ordinal())
						return true;
				}
			}
		}
		return false;
	}

	/** Are any of the evo chambers researching range upgrades? */
	public boolean getUpgradingRanged()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Evolution_Chamber.ordinal())
			{
				if (unit.isCompleted() && unit.isUpgrading())
				{
					if (unit.getUpgradingUpgradeID() == UpgradeTypes.Zerg_Missile_Attacks.ordinal())
						return true;
				}
			}
		}
		return false;
	}

	/** Are any of the evo chambers researching carapace upgrades? */
	public boolean getUpgradingCarapace()
	{
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Evolution_Chamber.ordinal())
			{
				if (unit.isCompleted() && unit.isUpgrading())
				{
					if (unit.getUpgradingUpgradeID() == UpgradeTypes.Zerg_Carapace.ordinal())
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Research Zergling Speed / Metabolic Boost. Returns false if not enough
	 * resources or no spawning pool available.
	 */
	public boolean upgradeZerglingSpeed()
	{
		if (resourceManager.getMineralCount() < 100 || resourceManager.getGasCount() < 100)
		{
			return false;
		}
		Unit pool = null;
		pool = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spawning_Pool.ordinal());

		if (pool == null || !pool.isCompleted())
		{
			return false;
		}
		if (pool.isUpgrading())
		{
			return false;
		}
		bwapi.upgrade(pool.getID(), UpgradeTypes.Metabolic_Boost.ordinal());
		hasZerglingSpeed = true;
		return true;

	}

	/**
	 * Research Hydralisk Speed / Muscular Augments. Returns false if not enough
	 * resources or no hydralisk den available.
	 */
	public boolean upgradeHydraliskSpeed()
	{
		if (resourceManager.getMineralCount() < 150 || resourceManager.getGasCount() < 150)
		{
			return false;
		}
		Unit building = null;
		building = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hydralisk_Den.ordinal());

		if (building == null || !building.isCompleted())
		{
			return false;
		}

		if (building.isUpgrading())
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Muscular_Augments.ordinal());
		hasHydraliskSpeed = true;
		return true;

	}

	/**
	 * Research Hydralisk Speed / Muscular Augments. Returns false if not enough
	 * resources or no hydralisk den available.
	 */
	public boolean upgradeHydraliskRange()
	{
		if (resourceManager.getMineralCount() < 150 || resourceManager.getGasCount() < 150)
		{
			return false;
		}
		Unit building = null;
		building = unitManager.getMyUnitOfType(UnitTypes.Zerg_Hydralisk_Den.ordinal());

		if (building == null || !building.isCompleted())
		{
			return false;
		}

		if (building.isUpgrading())
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Grooved_Spines.ordinal());
		hasHydraliskRange = true;
		return true;
	}

	/**
	 * Research Overlord speed
	 */
	public boolean upgradeOverlordSpeed()
	{
		if (resourceManager.getMineralCount() < 150 || resourceManager.getGasCount() < 150)
		{
			return false;
		}
		Unit building = null;
		building = unitManager.getMyUnitOfType(UnitTypes.Zerg_Lair.ordinal());

		if (building == null || !building.isCompleted())
		{
			return false;
		}

		if (building.isUpgrading())
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Pneumatized_Carapace.ordinal());
		hasOverlordSpeed = true;
		return true;
	}

	/**
	 * Research Lurker Aspect
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

	/**
	 * Upgrade Zerg Flyer attacks Returns false if not enough resources or no
	 * spire available.
	 */
	public boolean upgradeAirFlyerAttack()
	{
		int gasCost = 0;
		int mineralCost = 0;

		// Get the appropriate cost for the upgrade
		switch (bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Flyer_Attacks.ordinal()))
		{
			case 0:
				gasCost = 100;
				mineralCost = 100;
				break;
			case 1:
				gasCost = 175;
				mineralCost = 175;
				break;
			case 2:
				gasCost = 250;
				mineralCost = 250;
				break;
			default:
				gasCost = 100;
				mineralCost = 100;
				break;
		}

		if (resourceManager.getMineralCount() < mineralCost || resourceManager.getGasCount() < gasCost)
		{
			return false;
		}
		Unit building = null;
		building = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spire.ordinal());

		if (building == null || !building.isCompleted())
		{
			return false;
		}

		if (building.isUpgrading())
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Zerg_Flyer_Attacks.ordinal());
		return true;
	}

	/**
	 * Upgrade Zerg Flyer Carapace Returns false if not enough resources or no
	 * spire available.
	 */
	public boolean upgradeAirFlyerCarapace()
	{
		int gasCost = 0;
		int mineralCost = 0;

		// Get the appropriate cost for the upgrade
		switch (bwapi.getSelf().upgradeLevel(UpgradeTypes.Zerg_Flyer_Carapace.ordinal()))
		{
			case 0:
				gasCost = 150;
				mineralCost = 150;
				break;
			case 1:
				gasCost = 225;
				mineralCost = 225;
				break;
			case 2:
				gasCost = 300;
				mineralCost = 300;
				break;
			default:
				gasCost = 150;
				mineralCost = 150;
				break;
		}

		if (resourceManager.getMineralCount() < mineralCost || resourceManager.getGasCount() < gasCost)
		{
			return false;
		}
		Unit building = null;
		building = unitManager.getMyUnitOfType(UnitTypes.Zerg_Spire.ordinal());

		if (building == null || !building.isCompleted())
		{
			return false;
		}

		if (building.isUpgrading())
		{
			return false;
		}

		bwapi.upgrade(building.getID(), UpgradeTypes.Zerg_Flyer_Carapace.ordinal());
		return true;
	}

	public boolean evoChamberUpgrade(UpgradeTypes upgrade)
	{
		int gasCost = 0;
		int mineralCost = 0;

		if (upgrade.ordinal() == UpgradeTypes.Zerg_Melee_Attacks.ordinal()
				|| upgrade.ordinal() == UpgradeTypes.Zerg_Missile_Attacks.ordinal())
		{
			switch (bwapi.getSelf().upgradeLevel(upgrade.ordinal()))
			{
				case 0:
					gasCost = 100;
					mineralCost = 100;
					break;
				case 1:
					gasCost = 150;
					mineralCost = 150;
					break;
				case 2:
					gasCost = 200;
					mineralCost = 200;
					break;
				default:
					gasCost = 100;
					mineralCost = 100;
					break;
			}
		}
		else
		{
			switch (bwapi.getSelf().upgradeLevel(upgrade.ordinal()))
			{
				case 0:
					gasCost = 150;
					mineralCost = 150;
					break;
				case 1:
					gasCost = 225;
					mineralCost = 225;
					break;
				case 2:
					gasCost = 300;
					mineralCost = 300;
					break;
				default:
					gasCost = 150;
					mineralCost = 150;
					break;
			}
		}
		if (resourceManager.getMineralCount() < mineralCost || resourceManager.getGasCount() < gasCost)
		{
			return false;
		}
		for (Unit unit : bwapi.getMyUnits())
		{
			if (unit.getTypeID() == UnitTypes.Zerg_Evolution_Chamber.ordinal())
			{
				if (unit.isCompleted() && !unit.isUpgrading())
				{
					bwapi.upgrade(unit.getID(), upgrade.ordinal());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Upgrade Zerg Melee Attack Returns false if on evo chamber, or not enough
	 * resources
	 */
	public boolean upgradeMelee()
	{
		return evoChamberUpgrade(UpgradeTypes.Zerg_Melee_Attacks);
	}

	/**
	 * Upgrade Zerg Ranged Attack Returns false if on evo chamber, or not enough
	 * resources
	 */
	public boolean upgradeRanged()
	{
		return evoChamberUpgrade(UpgradeTypes.Zerg_Missile_Attacks);
	}

	/**
	 * Upgrade Zerg Ground Carapace Returns false if on evo chamber, or not
	 * enough resources
	 */
	public boolean upgradeCarapace()
	{
		return evoChamberUpgrade(UpgradeTypes.Zerg_Carapace);

	}

	public void gameStarted()
	{
		hasZerglingSpeed = false;
		hasHydraliskSpeed = false;
		hasHydraliskRange = false;
	}

}
