package scbod.managers;

import java.awt.Point;
import java.util.ArrayList;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UpgradeType;


public class UpgradeManager extends Manager
{
	protected JNIBWAPI bwapi;
	protected UnitManager unitManager;
	
	protected ArrayList<Point> upgradeQueue = new ArrayList<Point>();
	
	public boolean upgradeTech( UpgradeType.UpgradeTypes upgradeType )
	{
		int upgradeTypeID = upgradeType.ordinal();
		
		if(bwapi.canResearch(upgradeTypeID))
		{
			int researcherType = bwapi.getUpgradeType(upgradeTypeID).getWhatUpgradesTypeID();
			Unit researcher = unitManager.getLeastBusyUnitofType(researcherType);
			
			if(bwapi.canResearch(researcher.getID(), upgradeTypeID))
			{
				upgradeQueue.add(new Point(researcher.getID(), upgradeTypeID));
				return true;
			}
		}
		
		return false;
	}
	
	public void gameUpdate()
	{
		if(upgradeQueue.size() > 0)
		{
			for(Point point: upgradeQueue)
			{
				bwapi.upgrade(point.x, point.y);
			}
			
			upgradeQueue.clear();
		}
	}
	
}
