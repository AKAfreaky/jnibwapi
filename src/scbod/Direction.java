package scbod;

/** 
 * Direction enumeration 
 * In BWAPI, North is equivalent to a negative Y vector,
 * East, positive X vector and vice versa 
 * @author Simon Davies
 */
public enum Direction {
	North, // Negative Y
	East,  // Positive X
	South, // Positive Y
	West;  // Negative X
}
