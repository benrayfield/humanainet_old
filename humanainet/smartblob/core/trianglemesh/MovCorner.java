/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;
import humanainet.smartblob.core.Smartblob;
//Move all uses of java.awt to pc package: import java.awt.Color;

/** TODO similar to TriData.
Moving. Maybe I'll put distance constraints here between the up to 6 adjacent TriData
or up to 6 adjacent CornerData. */
public class MovCorner extends Adjacent{
	
	public final Smartblob smartblob;
	
	public final CornerName corner;
	
	public float y, x, speedX, speedY;
	
	/** addtoX and addToY are similar to speed vars in that position is updated using them
	at the same time as speed vars (counts as isUpdatingSpeed) but are different in that they
	add directly to position and then are set to 0, as a 1 time thing during bounce calculations.
	*/
	public float addToX, addToY;
	
	/** This may be ignored, since TriData color is the main thing thats drawn.
	This would be drawn as a small circle or single pixel.
	<br><br>
	As int, see definition of color in DrawTri. TODO should each corner be drawn as potentially different color?
	*/
	public int colorARGB;
	//public Color colorOrNull;
	
	public MovCorner(LayeredZigzag smartblob, CornerName corner, boolean edge){
		super(edge?3:6, edge?4:6, edge?4:6);
		this.smartblob = smartblob;
		this.corner = corner;
	}
	
	public void connectAdjacent(){
		final int lay = corner.layer;
		final int pt = corner.point;
		if(!(smartblob instanceof LayeredZigzag)) throw new RuntimeException(
			"TODO how to efficiently generalize "+Smartblob.class.getName()
			+" interface so we dont have to check if its a "+LayeredZigzag.class.getName());
		final LayeredZigzag smartblob = (LayeredZigzag) this.smartblob;
		final int laySiz = smartblob.layerSize;
		
		//TODO pointers to TriData
			
		//TODO pointers to CornerData
		adjacentCorners[0] = smartblob.corners[lay][(pt+1)%laySiz];
		adjacentCorners[1] = smartblob.corners[lay][(pt-1+laySiz)%laySiz];
		boolean layerIsOdd = (corner.layer&1)==1;
		int highPInOtherLayer = layerIsOdd ? (pt+1)%laySiz : pt;
		if(adjacentCorners.length == 6){ //6 adjacentCorners, all other 4
			adjacentCorners[2] = smartblob.corners[lay-1][(highPInOtherLayer)%laySiz];
			adjacentCorners[3] = smartblob.corners[lay-1][(highPInOtherLayer-1+laySiz)%laySiz];
			adjacentCorners[4] = smartblob.corners[lay+1][(highPInOtherLayer)%laySiz];
			adjacentCorners[5] = smartblob.corners[lay+1][(highPInOtherLayer-1+laySiz)%laySiz];
		}else if(corner.layer == 0){ //4 adjacentCorners, other 2 are at higher layer
			adjacentCorners[2] = smartblob.corners[lay+1][(highPInOtherLayer)%laySiz];
			adjacentCorners[3] = smartblob.corners[lay+1][(highPInOtherLayer-1+laySiz)%laySiz];
		}else{ //4 adjacentCorners, other 2 are at lower layer
			adjacentCorners[2] = smartblob.corners[lay-1][(highPInOtherLayer)%laySiz];
			adjacentCorners[3] = smartblob.corners[lay-1][(highPInOtherLayer-1+laySiz)%laySiz];
		}
		
		//TODO pointers to LineData
		for(int i=0; i<adjacentCorners.length; i++){
			//matches as key when either corner does this
			LineName line = new LineName(corner, adjacentCorners[i].corner);
			adjacentLines[i] = smartblob.lineData(line);
		}
	}

}
