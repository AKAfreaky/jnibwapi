package scbod.managers;

import java.util.HashMap;

import scbod.Utility;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Player;

/**
 * Resource Manager Keeps track of player resources
 * 
 * @author Simon Davies - Everything not explicitly stated
 * @author Alex Aiton
 */
public class ResourceManager extends Manager
{

	// Resource tracking
	private int							supplyUsed				= 0;
	private int							supplyTotal				= 0;
	private int							predictedSupplyTotal	= 0;
	private int							mineralCount			= 0;
	private int							gasCount				= 0;
	
	private HashMap<Integer, Integer>	reservedMinerals		= new HashMap<Integer, Integer>();

	private JNIBWAPI					bwapi;

	public ResourceManager(JNIBWAPI bwapi)
	{
		this.bwapi = bwapi;
	}

	/** Returns the amount of supply used by the player */
	public int getSupplyUsed()
	{
		return supplyUsed;
	}

	/** Returns the total supply the player can have */
	public int getSupplyTotal()
	{
		return supplyTotal;
	}

	/** Returns the total supply the player can have */
	public int getPredictedSupplyTotal()
	{
		return predictedSupplyTotal;
	}

	/** Returns the mineral count */
	public int getMineralCount()
	{
		return mineralCount - calculateReservedMinerals();
	}

	/**
	 * Returns the full mineral amount, should only be used for important things
	 * such as building expansions
	 */
	public int getReservedMineralCount()
	{
		return mineralCount;
	}

	/** Returns the vespene gas amount the player currently has */
	public int getGasCount()
	{
		return gasCount;
	}

	public void setSupplyUsed(int supplyUsed)
	{
		this.supplyUsed = supplyUsed;
	}

	public void setSupplyTotal(int supplyTotal)
	{
		this.supplyTotal = supplyTotal;
	}

	public void setPredictedSupplyTotal(int precitedTotal)
	{
		this.predictedSupplyTotal = precitedTotal;
	}

	public void setMineralCount(int mineralCount)
	{
		this.mineralCount = mineralCount;
	}

	/**
	 * @author Alex Aiton
	 */
	public void reserveMinerals(int amount)
	{
		reserveMinerals(amount, Utility.NOT_SET);
	}

	/**
	 * @author Alex Aiton
	 */
	public void reserveMinerals(Integer amount, Integer id)
	{
		reservedMinerals.put(id, amount);
	}

	/**
	 * @author Alex Aiton
	 */
	public int getReservation(Integer id)
	{
		Integer value = reservedMinerals.get(id);
		
		if(value != null)
			return value;
		else
			return 0;
	}
	
	/**
	 * @author Alex Aiton
	 */
	public void clearReservation(Integer id)
	{
		reservedMinerals.remove(id);
	}
	
	/**
	 * @author Alex Aiton
	 */
	private int calculateReservedMinerals()
	{
		int count = 0;
		for (Integer value : reservedMinerals.values())
		{
			count += value;
		}

		return count;
	}

	public void setGasCount(int gasCount)
	{
		this.gasCount = gasCount;
	}

	/** Returns the available supply */
	public int getSupplyAvailable()
	{
		return supplyTotal - supplyUsed;
	}

	/** Returns the available predicted supply */
	public int getPredictedSupplyAvailable()
	{
		return predictedSupplyTotal - supplyUsed;
	}

	@Override
	public void gameUpdate()
	{
		// Update game info
		Player playerInfo = bwapi.getSelf();

		// System.out.println("Supply: (" + playerInfo.getSupplyUsed() + "/" +
		// playerInfo.getSupplyTotal() + ")");

		setSupplyTotal(playerInfo.getSupplyTotal());
		setSupplyUsed(playerInfo.getSupplyUsed());
		setMineralCount(playerInfo.getMinerals());
		setGasCount(playerInfo.getGas());
	}

}
