package scbod;

/**
 * Java needs structs!
 * 
 * Collates a bunch of data pertaining to worker orders
 * There's a bug with jnibwapi where active workers will 
 * only respond to orders in a gameUpdate tick, so we need
 * to queue orders up.
 * @author Alex Aiton
 */
public class WorkerOrderData
{
	public enum WorkerOrder
	{
		Build,
		Move,
		Attack,
		Gather
	}	
	
	/** What do */
	public final WorkerOrder orderType;
	/** Who do */
	public final int workerID;
	/** Where do */
	public final int x;
	public final int y;
	/** For build, this is building type id
	 *  For attack, this is the target unit id */
	public final int secondID;		
	
	/**
	 * Contructor for Attack/Gather orders
	 * 
	 * @param order	-	The type of order we want
	 * @param workerID	-	Unit ID to send the order to
	 * @param secondID	-	Another ID required for the order
	 */
	public WorkerOrderData( WorkerOrder order, int workerID, int secondID)
	{
		this(order, workerID, Utility.NOT_SET, Utility.NOT_SET, secondID);
	}
	
	/**
	 * Contructor for Attack/Move orders
	 * 
	 * @param order	-	The type of order we want
	 * @param workerID	-	Unit ID to send the order to
	 * @param x	-	x co-ordinate for the order
	 * @param y -	y co-ordinate for the order
	 */
	public WorkerOrderData( WorkerOrder order, int workerID, int x, int y)
	{
		this(order, workerID, x, y, Utility.NOT_SET);
	}
	
	/**
	 * Contructor for build orders
	 * 
	 * @param order	-	The type of order we want
	 * @param workerID	-	Unit ID to send the order to
	 * @param x	-	x co-ordinate for the order
	 * @param y -	y co-ordinate for the order
	 * @param secondID	-	Another ID required for the order
	 */
	public WorkerOrderData( WorkerOrder order, int workerID, int x, int y, int secondID)
	{
		orderType = order;
		this.workerID = workerID;
		this.x = x;
		this.y = y;
		this.secondID = secondID;
	}
}
