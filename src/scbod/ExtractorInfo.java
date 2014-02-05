package scbod;

import java.util.ArrayList;

public class ExtractorInfo {
	public int geyserID;
	public ArrayList<Integer> gasWorkers;
	
	public ExtractorInfo(int geyserID){
		this.geyserID = geyserID;
		gasWorkers = new ArrayList<Integer>();
	}
}
