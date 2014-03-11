package scbod;

/**
 * Often have need to queue up orders to be sent during a game tick (seems to be bug with jnibwapi)
 * Orders are often of the form order([id to order],[id of order])
 * Was using point class to store this, with seperate queue for each order type.
 * A triple is more contained.
 * 
 * @author Alex
 */
public class IntTriple
{
	public static final int TRAIN = 1;
	public static final int ADDON = 2;
	public static final int MORPH = 3;
	public static final int UPGRADE  = 4;
	public static final int RESEARCH = 5;
	
	public int x;
	public int y;
	public int z;
	
	public IntTriple(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
