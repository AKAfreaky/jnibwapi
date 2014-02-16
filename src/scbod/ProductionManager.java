package scbod;

import jnibwapi.types.UnitType;

public abstract class ProductionManager extends Manager
{
	abstract public boolean spawn(UnitType.UnitTypes unitType);
}
