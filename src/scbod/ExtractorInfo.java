package scbod;

import java.util.ArrayList;

/**
 * Connects gas processing units to their active workers
 * @author Simon Davies
 */
public class ExtractorInfo {
	public int geyserID;
	public ArrayList<Integer> gasWorkers;
	
	public ExtractorInfo(int geyserID){
		this.geyserID = geyserID;
		gasWorkers = new ArrayList<Integer>();
	}
}
