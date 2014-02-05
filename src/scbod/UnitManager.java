package scbod;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/** Unit manager, has methods for unit selection */
public class UnitManager extends Manager {
	
	JNIBWAPI bwapi;
	
	public UnitManager(JNIBWAPI bwapi){
		this.bwapi = bwapi;
	}
	
	/** Finds one of the players units of a given type
	 *	Returns null if no unit found. 
	 * @param typeID
	 * @return unit of given type, null if no unit found
	 */
	public Unit getMyUnitOfType(int typeID, boolean completed){
		for (Unit unit : bwapi.getMyUnits()) {
			if(unit.getTypeID() == typeID &&
					(!completed || unit.isCompleted())){
				return unit;
			}
		}
		return null;
	}
	
	public Unit getMyUnitOfType(int typeID){
		return getMyUnitOfType(typeID, false);
	}
	
	/** Returns the number the player possesses of the given unit type */
	public int getUnitCount(int typeID, boolean completed){
		int count = 0;
		// Count number of units
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == typeID &&
					(!completed || unit.isCompleted())){
				count++;
			}
		}
		// Count number of type about to be morphed
		for(Unit unit : bwapi.getMyUnits()){
			if(unit.getTypeID() == UnitTypes.Zerg_Egg.ordinal()){
				if(unit.getBuildTypeID() == typeID){
					count++;
				}
			}
		}
		return count;
	}
}
