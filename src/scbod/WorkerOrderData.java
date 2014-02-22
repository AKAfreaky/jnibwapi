package scbod;

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
	
	public WorkerOrderData( WorkerOrder order, int workerID, int secondID)
	{
		this(order, workerID, Utility.NOT_SET, Utility.NOT_SET, secondID);
	}
	
	public WorkerOrderData( WorkerOrder order, int workerID, int x, int y)
	{
		this(order, workerID, x, y, Utility.NOT_SET);
	}
	
	public WorkerOrderData( WorkerOrder order, int workerID, int x, int y, int secondID)
	{
		orderType = order;
		this.workerID = workerID;
		this.x = x;
		this.y = y;
		this.secondID = secondID;
	}
}
