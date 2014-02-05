package scbod;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Player;

/** Resource Manager
 * Keeps track of player resources
 *
 */
public class ResourceManager extends Manager{
	
	// Resource tracking
	private int supplyUsed = 0;
	private int supplyTotal = 0;
	private int predictedSupplyTotal = 0;
	private int mineralCount = 0;
	private int gasCount = 0;
	
	private int reservedMinerals = 0;
	
	private JNIBWAPI bwapi;
	
	public ResourceManager(JNIBWAPI bwapi){
		this.bwapi = bwapi;
	}
	
	/** Returns the amount of supply used by the player */
	public int getSupplyUsed(){
		return supplyUsed;
	}
	
	/** Returns the total supply the player can have */
	public int getSupplyTotal(){
		return supplyTotal;
	}
	
	/** Returns the total supply the player can have */
	public int getPredictedSupplyTotal(){
		return predictedSupplyTotal;
	}
	
	/** Returns the mineral count */
	public int getMineralCount(){
		return mineralCount - reservedMinerals;
	}
	
	/** Returns the full mineral amount, should only be used for important things
	 * such as building expansions */
	public int getReservedMineralCount(){
		return mineralCount;
	}
	
	/** Returns the vespene gas amount the player currently has */
	public int getGasCount(){
		return gasCount;
	}
	
	public void setSupplyUsed(int supplyUsed) {
		this.supplyUsed = supplyUsed;
	}

	public void setSupplyTotal(int supplyTotal) {
		this.supplyTotal = supplyTotal;
	}
	
	public void setPredictedSupplyTotal(int precitedTotal){
		this.predictedSupplyTotal = precitedTotal;
	}

	public void setMineralCount(int mineralCount) {
		this.mineralCount = mineralCount;
	}
	
	public void reserveMinerals(int amount){
		reservedMinerals = amount;
	}

	public void setGasCount(int gasCount) {
		this.gasCount = gasCount;
	}
	
	/** Returns the available supply */
	public int getSupplyAvailable(){
		return supplyTotal - supplyUsed;
	}
	
	/** Returns the available predicted supply */
	public int getPredictedSupplyAvailable(){
		return predictedSupplyTotal - supplyUsed;
	}
	
	@Override
	public void gameUpdate(){
		// Update game info
		Player playerInfo = bwapi.getSelf();
		
		setSupplyTotal(playerInfo.getSupplyTotal());
		setSupplyUsed(playerInfo.getSupplyUsed());
		setMineralCount(playerInfo.getMinerals());
		setGasCount(playerInfo.getGas());
	}
	
	@Override
	public void gameStarted(){
		reservedMinerals = 0;
	}
	


}
