/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;
import humanainet.smartblob.core.Smartblob;
//Move all uses of java.awt to pc package: import java.awt.Color;
//Move all uses of java.awt to pc package: import java.awt.Polygon;
//Move all uses of java.awt to pc package: import java.awt.Shape;

/** Mutable data about a specific triangle in a specific polygon mesh
of shape the same as LayeredZigzag.
<br><br>
Moving. The first 2 triangles are in the same trianglelayer
(different layers but same distance outward because of inward/outward boolean).
The other is either 1 layer higher or lower.
This array is either size 2 (if edge) or 3.
All adjacent Tri are opposite of inward/outward.
*/
public class MovTri extends Adjacent{
	
	public final Smartblob smartblob;
	
	public final TriName tri;
	
	public float volume;
	
	/** Volume mostly flows between adjacent triangles so total volume is conserved
	and it helps to flow force through the smartblob.
	*/
	public float targetVolume;
	
	//protected Shape cachedShape;
	//"TODO use int x[] and y[] instead of Shape? In LayeredZigzag outer shape? At least to replace TriData.cachedShape, or actually let that be replaced by each TriData knowing its 3 CornerData and let the display code get it from there."
	
	//protected long cachedInWhatCycle = -1;
	
	/** If null, use the default color depending on tri.inward and the LayeredZigzag.
	This can be used to display pressure, selection by mouse, or other things that happen on screen.
	*/
	//public Color colorOrNull;
	public int colorARGB = 0x7f7f7f7f;
	
	/** If edge, there are 2 adjacent Tri, else 3.
	Those TriData are filled in later by caller, so that array contains nulls until then.
	*/
	public MovTri(LayeredZigzag smartblob, TriName tri, boolean edge){
		super(edge?2:3, 3, 3);
		this.smartblob = smartblob;
		this.tri = tri;
		//adjacentTris = new TriData[edge ? 2 : 3];
	}


	/** First LineData in TriData is at same layer as first 2 corners */
	public void connectAdjacent(){
		if(!(smartblob instanceof LayeredZigzag)) throw new RuntimeException(
			"TODO how to efficiently generalize "+Smartblob.class.getName()
			+" interface so we dont have to check if its a "+LayeredZigzag.class.getName());
		final LayeredZigzag smartblob = (LayeredZigzag) this.smartblob;
			
		//TODO pointers to TriData
		
		//pointers to CornerData works
		adjacentCorners[0] = smartblob.corners[tri.layer][tri.point];
		adjacentCorners[1] = smartblob.corners[tri.layer][(tri.point+1)%smartblob.layerSize];
		boolean layerIsOdd = (tri.layer&1)==1;
		int pInOtherLayer = layerIsOdd ? (tri.point+1)%smartblob.layerSize : tri.point;
		adjacentCorners[2] = tri.inward
			? smartblob.corners[tri.layer-1][pInOtherLayer]
			: smartblob.corners[tri.layer+1][pInOtherLayer];
			
			
		//TODO pointers to LineData
	}
	
	//"TODO qnSmartblobLineObject"

}
