package scbod;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType;

public abstract class ProductionManager extends Manager
{
	
	protected ResourceManager		resourceManager;
	protected JNIBWAPI				bwapi;
	
	abstract public boolean spawn(UnitType.UnitTypes unitType);
}
