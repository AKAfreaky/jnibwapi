package scbod;

/**
 * Provides a method that a scout can call when it completes its assigned route
 * (So it can be given further orders)
 * @author Alex Aiton
 */
public interface ScoutFinished
{
	public void scoutRouteCompleted(int scoutID);
}
