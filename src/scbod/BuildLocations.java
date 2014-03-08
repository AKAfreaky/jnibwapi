package scbod;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Overrides the super-class add() so that new Points don't overlap old points in a 3x3
 * @author Alex Aiton
 */
public class BuildLocations extends ArrayList<Point>
{
	
	/**
	 * Compiler seems to want this
	 */
	private static final long	serialVersionUID	= -675527657118895431L;

	/**
	 * @author Alex Ation
	 * @see java.util.ArrayList#add(Object)
	 */
	public boolean add(Point newPoint)
	{
		int npRight	= newPoint.x + 2;
		int npBot	= newPoint.y + 2;
		
		for(Point point : this)
		{
			int opRight	= point.x + 2;
			int opBot	= point.y + 2;
			
			// If they intersect;
			if (point.x < npRight && opRight > newPoint.x
			&&  point.y < npBot	  && opBot	 > newPoint.y)
			{
				// we already have an equivalant spot, so don't add 
				return false;
			}
		}
		
		// No points intersect so add it
		return super.add(newPoint);
	}
	
}
