package scbod.managers;

import java.awt.Point;
import java.util.ArrayList;

import jnibwapi.JNIBWAPI;
import jnibwapi.types.UnitType;

public abstract class ProductionManager extends Manager
{
	
	protected ResourceManager		resourceManager;
	protected JNIBWAPI				bwapi;
	protected ArrayList<Point>		buildQueue = new ArrayList<Point>();
	
	abstract public boolean spawn(UnitType.UnitTypes unitType);
	
	@Override
	public void gameUpdate()
	{
		// Bit of an abuse of the Point class here, but Java don't have a standard pair so fuck 'em
		if(buildQueue.size() > 0)
		{
			for(Point point: buildQueue)
			{
				bwapi.train(point.x, point.y);
			}
			
			buildQueue.clear();
		}		
	}
	
}
