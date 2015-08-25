/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;
import humanainet.smartblob.core.Smartblob;

/** I'm undecided if I'll use this LineData class or if include these vars in CornerData.
<br><br>
Moving. benfrayfieldResearch.qnSmartblobLineObject says:
In context of the datastructs tested by smartblobTestAdjacentTrianglePointLine,
should smartblobLayersOfZigzagAroundRadial have line objects which would hold cached
distance and targetDistance between each pair of connected corner? Maybe they would
all be collected into a set and run once each, then do force calculations at corners
and maybe triangles. Fluid flows between adjacent triangles to hold total volume
constant, but it still has to be calculated at the corners after that.
The corners have to move.
*/
public class MovLine extends Adjacent{
	
	public final Smartblob smartblob;
	
	public final LineName line;
	
	//better to calculate this again each time since thats where dx and dy numbers are:
	//public float distance;
	
	public float targetDistance;
	
	/** Distance in the balanced angles and radius all CornerData of the smartblob started */
	public float startDistance;
	
	/** touching 1 or 2 triangles. Edge is true if both layers are first or last layer. */
	public MovLine(LayeredZigzag smartblob, LineName line, boolean edge){
		super(edge?1:2, 0, 2);
		this.smartblob = smartblob;
		this.line = line;
	}
	
	/** a distanceConstraint not connected to any triangles, directly between 2 Corners/points *
	public LineData(LayeredZigzag smartblob, Line line){
		"TODO how would reverse pointers work back at this LineData since the others know their number of Lines already? Maybe its best not to do this as LineData or not at all, especially if the fewer constraints work on their own."
	}*/
	
	public void connectAdjacent(){
		if(!(smartblob instanceof LayeredZigzag)) throw new RuntimeException(
			"TODO how to efficiently generalize "+Smartblob.class.getName()
			+" interface so we dont have to check if its a "+LayeredZigzag.class.getName());
		final LayeredZigzag smartblob = (LayeredZigzag) this.smartblob;
		
		//TODO pointers to TriData
		
		//TODO pointers to LineData
					
		//TODO pointers to CornerData
		adjacentCorners[0] = smartblob.corners[line.cornerLow.layer][line.cornerLow.point];
		adjacentCorners[1] = smartblob.corners[line.cornerHigh.layer][line.cornerHigh.point];
	}

}