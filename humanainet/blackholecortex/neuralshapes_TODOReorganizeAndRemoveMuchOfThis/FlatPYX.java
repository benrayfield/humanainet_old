package humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import humanainet.blackholecortex.alloc.Alloc;
import humanainet.common.CoreUtil;

/** UPDATE: flatpyx contains 1 or more flatyx.
<br><br>
benfrayfieldResearch.endianOfPInFlatXYP says:
I choose pDim to be highestDigit. ... Should pDim come before or after
xDim and yDim in flatxyp? The important question is, compare things that
work on a single pDim and loop over yDim and xDim, compare to things
that work on a single pixel at a time (specific yDim and xDim) and loop
over pDim (or use it as randomAccess), which of those happens more often?
Single pDim is what I've used more of so far, and single pixel is
designed mostly for display purposes, so I choose single pDim.
<br><br>
benfrayfieldResearch.endianOfXYInFlatXYP says:
I choose yDim be higherDigit than xDim, so yDim xDim in bigEndian array
order same as endianOfMultidimAndRowsVsColsOnScreen. ... Should xDim come
before or after yDim in flatxyp? It depends if want each row to be an array
of data that goes together, or each column. Standard way to write it,
people have gotten used to, is xDim then yDim, but standard way to read
on screen is leftToRight then topToBottom (which is yDim first since its
value is constant while loop over values of xDim).
<br><br>  
OLD TEXT:
Same as FlatXY except there is also a pixel dimension
which may be used for red green blue and other dimensions as feature vectors
which represent any node out there which is then thought about more
to generate 2d visuals of where it would be in XY, and then the colors
andOr other featureVectors at andOr around each XY change eachother
or are painted on with mouse.
*/
public class FlatPYX implements NeuralShape{
	
	public final FlatYX p[];
	
	public final short pSize, ySize, xSize;
	
	public FlatPYX(FlatYX... p){
		//TODO verify they are all same size
		this.p = p;
		if(p.length > Short.MAX_VALUE) throw new IndexOutOfBoundsException(
			"pSize (and y and x sizes) must fit in short but is "+p.length);
		pSize = (short)p.length;
		this.ySize = p[0].ySize;
		this.xSize = p[0].xSize;
	}
	
	/** for convenience, casts these sizes to short
	then tests if they're in the right size in case they wrapped
	*/
	public FlatPYX(int pSize, int ySize, int xSize){
		this((short)pSize, (short)ySize, (short)xSize);
		if(pSize > Short.MAX_VALUE || ySize > Short.MAX_VALUE || xSize > Short.MAX_VALUE) throw new IndexOutOfBoundsException(
			"p y and x sizes must fit in short but are pSize="+pSize+" ySize="+ySize+" xSize="+xSize);
	}
	
	public FlatPYX(short pSize, short ySize, short xSize){
		this.pSize = pSize;
		this.ySize = ySize;
		this.xSize = xSize;
		p = new FlatYX[pSize];
		for(int i=0; i<pSize; i++){
			p[i] = new FlatYX(ySize, xSize);
		}
	}
	
	/*public final int xSize, ySize, pSize;
	
	public final NeuralNode xyp[][][];
	
	/** Each of these caches are null until first used by nodesCacheable(Rectangle,int) *
	protected final WeakHashMap<Rectangle,NeuralNode[]> rectToNodes[];
	
	public FlatPYX(int width, int height, int pixelDims){
		xyp = new NeuralNode[xSize=width][ySize=height][pSize=pixelDims];
		NeuralNode n[] = RootNodeAlloc.newNodes(width*height*pixelDims);
		int nCount = 0;
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				for(int p=0; p<pixelDims; p++){
					//xyp[x][y][p] = BHCUtil.newNode();
					xyp[x][y][p] = n[nCount++];
				}
			}
		}
		rectToNodes = new WeakHashMap[pixelDims];
	}
	
	public FlatPYX(NeuralNode xyp[][][]){
		this.xyp = xyp;
		this.xSize = xyp.length;
		this.ySize = xyp[0].length;
		this.pSize = xyp[0][0].length;
		rectToNodes = new WeakHashMap[pSize];
	}
	
	public Rectangle2D.Double pixelRect(int x, int y){
		return new Rectangle2D.Double(x, y, 1, 1);
	}
	
	/** Since nodes have no effect on those their weights are FROM (as they are stored),
	these nodes have no math effect on those viewed and are garbage collectable.
	Use this quickly and often to view rectangles, for zooming in or out views.
	*
	public NeuralNode viewNewNewNodeAt(Rectangle2D.Double rect, int p){
		NeuralNode view = RootNodeAlloc.newNode();
		viewNodeAt(view, rect, p);
		return view;
	}
	public void viewNodeAt(NeuralNode changeMe, Rectangle2D.Double rect, int p){
		int xStart = (int)rect.x;
		int xEndExclusive = (int)Math.ceil(rect.x+rect.width);
		int yStart = (int)rect.y;
		int yEndExclusive = (int)Math.ceil(rect.y+rect.height);
		for(int x=xStart; x<xEndExclusive; x++){
			for(int y=yStart; y<yEndExclusive; y++){
				NeuralNode n = xyp[x][y][p];
				double fractionOfWidthTouched = 1;
				if(x == xStart || x == xEndExclusive-1 || y == yStart || y == yEndExclusive-1){
					Rectangle2D r = rect.createIntersection(pixelRect(x,y));
					fractionOfWidthTouched = r.getWidth()*r.getHeight();
				}
				changeMe.setWeightFrom(n, fractionOfWidthTouched);
			}
		}
	}
	
	/** Returns all nodes (ordered by x and y, TODO which is first?) in the Rectangle.
	Rectangle can extend outside the area where there are NeuralNode.
	Use this with statsys andOr movsys.
	*
	public NeuralNode[] nodes(Rectangle rect, int p){
		int xEnd = CoreUtil.min(xSize, rect.x+rect.width);
		int yEnd = CoreUtil.min(ySize, rect.y+rect.height);
		int xSize = xEnd-rect.x;
		int ySize = yEnd-rect.y;
		if(xSize <= 0 || ySize <= 0) return new NeuralNode[0];
		NeuralNode nodes[] = new NeuralNode[xSize*ySize];
		int n = 0;
		for(int x=rect.x; x<xEnd; x++){
			for(int y=rect.y; y<yEnd; y++){
				nodes[n++] = xyp[x][y][p];
			}
		}
		return nodes;
	}
	
	public NeuralNode[] nodesCacheable(Rectangle rect, int p){
		if(rectToNodes[p] == null) rectToNodes[p] = new WeakHashMap<Rectangle,NeuralNode[]>();
		NeuralNode n[] = rectToNodes[p].get(rect);
		if(n == null){
			n = nodes(rect, p);
			rectToNodes[p].put(rect, n);
		}
		return n;
	}
	*/

}