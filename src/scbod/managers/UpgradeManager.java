package scbod.managers;

import java.util.ArrayList;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType;
import jnibwapi.types.UpgradeType;
import scbod.IntTriple;


public class UpgradeManager extends Manager
{
	protected JNIBWAPI bwapi;
	protected UnitManager unitManager;
	
	protected ArrayList<IntTriple> upgradeQueue = new ArrayList<IntTriple>();
	
	public UpgradeManager(JNIBWAPI bwapi, UnitManager unitManager)
	{
		this.bwapi = bwapi;
		this.unitManager = unitManager;
	}
	
	
	public boolean upgrade( UpgradeType.UpgradeTypes upgradeType )
	{
		int upgradeTypeID = upgradeType.ordinal();
		
		if(bwapi.canUpgrade(upgradeTypeID))
		{
			int researcherType = bwapi.getUpgradeType(upgradeTypeID).getWhatUpgradesTypeID();
			Unit researcher = unitManager.getLeastBusyUnitofType(researcherType);
			
			if(bwapi.canUpgrade(researcher.getID(), upgradeTypeID))
			{
				upgradeQueue.add(new IntTriple(IntTriple.UPGRADE, researcher.getID(), upgradeTypeID));
				return true;
			}
		}
		
		return false;
	}
	
	public boolean upgrade( TechType.TechTypes techType )
	{
		int techTypeID = techType.ordinal();
		
		if(bwapi.canResearch(techTypeID))
		{
			int researcherType = bwapi.getTechType(techTypeID).getWhatResearchesTypeID();
			Unit researcher = unitManager.getLeastBusyUnitofType(researcherType);
			
			if(bwapi.canResearch(researcher.getID(), techTypeID))
			{
				upgradeQueue.add(new IntTriple(IntTriple.RESEARCH, researcher.getID(), techTypeID));
				return true;
			}
		}
		
		return false;
	}
	
	public void gameUpdate()
	{
		if(upgradeQueue.size() > 0)
		{
			for(IntTriple point: upgradeQueue)
			{
				switch(point.x)
				{
					case IntTriple.UPGRADE:
						bwapi.upgrade(point.y, point.z);
						break;
					case IntTriple.RESEARCH:
						bwapi.research(point.y, point.z);
						break;
					default:
						break;
				}
				
			}
			
			upgradeQueue.clear();
		}
	}
	
}
