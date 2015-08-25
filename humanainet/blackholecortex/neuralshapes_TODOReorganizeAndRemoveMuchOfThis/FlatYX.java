package humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.alloc.RootNeuronAlloc;
import humanainet.common.CoreUtil;
import humanainet.common.MathUtil;

/** Similar to a screen with rows and columns of nodes.
<br><br>
TODO generalize FlatYX to WeightsNode with generics to be more specific like NeuralNode.
This is so things like fourierAodEconbit and complexnum nodes can be in the grid on screen,
and for better integration with the coming complexStack
which is the counterpart to timeSymmetricStack.
*/
public class FlatYX{
	
	public final short ySize, xSize;
	
	public final WeightsNode yx[][];
	
	/** null until first used by nodesCacheable(Rectangle,int) */
	protected WeakHashMap<Rectangle,WeightsNode[]> rectToNodes;
	
	/** This must be updated if Neurons are replaced in yx[][].
	They are not normally replaced but could be in a later version, especially if
	the array becomes sparse, but then I'd probably use a different interface.
	*/
	public final Map<WeightsNode,Integer> neuronToYX = new HashMap();
	
	public FlatYX(short height, short width){
		yx = new WeightsNode[ySize=height][xSize=width];
		WeightsNode n[] = RootNeuronAlloc.newNodes(height*width);
		int nCount = 0;
		for(short x=0; x<width; x++){
			for(short y=0; y<height; y++){
				//xyp[x][y][p] = BHCUtil.newNode();
				yx[y][x] = n[nCount++];
				neuronToYX.put(yx[y][x], yAndXToYx(y,x));
			}
		}
	}
	
	public FlatYX(WeightsNode yx[][]){
		this.yx = yx;
		if(yx.length > Short.MAX_VALUE || yx[0].length > Short.MAX_VALUE) throw new IndexOutOfBoundsException(
			"Sizes must be in short range but are "+yx.length+" and "+yx[0].length);
		this.ySize = (short)yx.length;
		this.xSize = (short)yx[0].length;
		rectToNodes = new WeakHashMap();
	}
	
	public static int yAndXToYx(short y, short x){
		return ((int)y<<16) | x;
	}
	
	public static short yxToY(int yx){
		return (short)(yx>>>16);
	}
	
	public static short yxToX(int yx){
		return (short)yx;
	}
	
	public Rectangle2D.Double pixelRect(int x, int y){
		return new Rectangle2D.Double(x, y, 1, 1);
	}
	
	/** Since nodes have no effect on those their weights are FROM (as they are stored),
	these nodes have no math effect on those viewed and are garbage collectible.
	Use this quickly and often to view rectangles, for zooming in or out views.
	*/
	public WeightsNode viewNewNewNodeAt(Rectangle2D.Double rect){
		WeightsNode view = RootNeuronAlloc.newNode();
		viewNodeAt(view, rect);
		return view;
	}
	public void viewNodeAt(WeightsNode changeMe, Rectangle2D.Double rect){
		int xStart = (int)rect.x;
		int xEndExclusive = (int)Math.ceil(rect.x+rect.width);
		int yStart = (int)rect.y;
		int yEndExclusive = (int)Math.ceil(rect.y+rect.height);
		for(int x=xStart; x<xEndExclusive; x++){
			for(int y=yStart; y<yEndExclusive; y++){
				WeightsNode n = yx[y][x];
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
	*/
	public WeightsNode[] nodes(Rectangle rect){
		int xEnd = Math.min(xSize, rect.x+rect.width);
		int yEnd = Math.min(ySize, rect.y+rect.height);
		int xSize = xEnd-rect.x;
		int ySize = yEnd-rect.y;
		if(xSize <= 0 || ySize <= 0) return new WeightsNode[0];
		WeightsNode nodes[] = new WeightsNode[xSize*ySize];
		int n = 0;
		for(int y=rect.y; y<yEnd; y++){
			for(int x=rect.x; x<xEnd; x++){
				nodes[n++] = yx[y][x];
			}
		}
		return nodes;
	}
	
	public WeightsNode[] nodesCacheable(Rectangle rect){
		if(rectToNodes == null) rectToNodes = new WeakHashMap<Rectangle,WeightsNode[]>();
		WeightsNode n[] = rectToNodes.get(rect);
		if(n == null) rectToNodes.put(rect, n = nodes(rect));
		return n;
	}

}