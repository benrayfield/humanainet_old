package humanainet.blackholecortex.ui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;

import humanainet.blackholecortex.UpDownPaint;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatPYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatYX;
import humanainet.common.CoreUtil;
import humanainet.common.MathUtil;
import humanainet.common.time.InOutTimer;
import humanainet.realtimeschedulerTodoThreadpool.Task;
import humanainet.realtimeschedulerTodoThreadpool.TimedEvent;

/** A screen of 1 color dim which uses colors to display other things
including weights between each node and selected node.
<br><br>
DISPLAYS NOTHING UNLESS...
Every change to RBM, by y and x in FlatPYX, must be followed within a small fraction
of a second by a call to any of the addToChangedAndScheduleRepaint funcs which cause repaint
of only the areas that changed, normally delayed by a small fraction of a second until
paint(Graphics) runs in java swing thread as repaint() schedules.
If it repaints too often, use addToChangedAndScheduleRepaint(Set) instead of per point,
or if you know you're making many changes to RBM in one call, use that. 
<br><br>
If you want mouse painting using P_PAINTVAR to decay, then as an Eventable,
you must call this NodeScreen.event(Object) many times per second so decayPaintingWeights
decays the influence of what main 2 mouse buttons have painted onto those nodes,
so its a way to influence the network temporarily just long enough for it to learn
using mouse wheel to choose learnRate.
<br><br>
TODO rewrite comments because renaming this ResizGraphWeightAndChance to NodeScreen
<br><br>
OLD: A screen of 3 color dims (no feature vectors which creates a new FlatXYP each time
its resized. It connects to a FlatXYP of constant size whose nodes are viewed,
without modifying them, by pointing FROM them in the new FlatXYP. Thats why
nodes are designed to track weights and pointers FROM other nodes instead of to them.
The TO weight is the other node's FROM weight of this node.
*/
public class NodeScreen extends JPanel
		implements MouseListener, MouseMotionListener, MouseWheelListener/*, Task*/, UpDownPaint{
	
	//TODO rename ResizGraphWeightAndChance to NodeScreen
	
	{System.out.println("TODO show weights when mouseover each pixel");}
	
	{System.out.println("TODO show sum of weights when select multiple pixels? Maybe later, using the kind of checkbox that has bright, medium, or dark square around outside");}
	
	public final FlatPYX data;
	
	/** First reserved p for P_* vars named in this class */
	public static final int P_START = P_FROMUP;
	
	/** After P_START, first p which is outside the required range */
	public static final int P_END = P_PAINTVAR+1;
	
	/** constant for FlatPYX.p, which node bit/scalars are drawn on screen *
	public final int P_MAIN_LAYER = 0;
	*/
	
	/** constant for FlatPYX.p, which has the alternating even/odd rbm layers *
	public final int P_FLIP_LAYER = 1;
	*/
	
	protected BufferedImage image;
	
	public final int mousePosition[] = new int[2];
	
	public final short mouseNodePosition[] = new short[2];
	
	public final boolean mouseButton[] = new boolean[3];
	
	public boolean mouseIn;
	
	public double viewWeightAsFractionMultiplyBeforeSigmoid = 10; //TODO
	
	/** Set of neurons, named by long index (16 bits for each of y and x)
	in P_MAIN_LAYER and P_FLIP_LAYER which have nonzero weight
	from the neuron in P_PAINT_LAYER of the same magnified pixel (in FlatPYX).
	These weights are increased or decreased by mouse and decay toward 0.
	*/
	public final Set<Integer> mousePainting = new HashSet();
	
	/** When a paintingWeight decays below this, set it to 0 to disconnect that node pair.
	Its ok for this to be very small since on an exponential decay
	they will get here in linear time however small it is
	and have no significant effect while small. Its best to be continuous like that,
	but must be some cutoff to optimize which P_PAINTVAR nodes have their weights decayed.
	*/
	public double minPaintingWeightBeforeDisconnect = .0001;
	
	/** How much weight per second the main 2 mouse buttons add/subtract
	to selected pixel at P_PAINTVAR pLayer.
	*/
	public double paintSpeed = 120;
	//public double paintSpeed = .2;
	
	/** TODO? Its running much faster since using Graphics.fillRect for magnify x magnify squares,
	so do I really need this complexity?
	Set of 2d grid points which changed since last repaint() which empties this Set<Long> *
	public final Set<Integer> changed = new HashSet();
	*/
	
	/** each grid square is this tall and wide. Default 8. */
	public final int magnify;
	
	/** see mouseWheelScale */
	public static final double DEFAULT_MOUSE_WHEEL_SCALE_FOR_LEARNING = .001;
	
	/** Positive and starts as DEFAULT_MOUSE_WHEEL_SCALE.
	If this number increases, then more learning is done,
	either learning or unlearning depending on mouseWheel direction.
	It may need adjusting because of lack of standardization in mouse wheel numbers
	between different kinds of mouse, like apple computers have more precision,
	but it appears that MouseWheelEvent.getPreciseWheelRotation() (which came in java 1.7)
	is trying to solve that problem, even though on my computer its always -1 or 1
	and does about 5 of those, more or less, depending on how far I push/pull the mouse wheel.
	Thats barely enough precision to fine tune learning, but it will work.
	*/
	public double mouseWheelScale = DEFAULT_MOUSE_WHEEL_SCALE_FOR_LEARNING;
	
	protected double lastUiEvent = CoreUtil.time();
	
	protected double lastTimedEvent = lastUiEvent;
	
	protected double decaySpeedForPaintWeights = .5;
	
	/** TODO change these based on key presses, and hook other key presses into actions */
	public final DisplayWhat displayWhatInEachColor[] = {
		DisplayWhat.Nothing, //red's default
		DisplayWhat.WeightFromSelectedNode, //green's default
		DisplayWhat.ChanceOrInfluenceOfThisNode, //blue's default
	};

	/** Call this with decayFraction proportional to seconds since last call and at most 1.
	TODO since this func sets the weights symmetricly (in 2 different ways, symmetric in both),
	the code which adds to those weights should also.
	*/
	public void decayPaintingWeights(double decayFraction){
		double mult = 1-decayFraction;
		synchronized(mousePainting){
			Iterator<Integer> iter = mousePainting.iterator();
			while(iter.hasNext()){
				int yx = iter.next();
				int y = FlatYX.yxToY(yx);
				int x = FlatYX.yxToX(yx);
				WeightsNode fromUp = data.p[P_FROMUP].yx[y][x];
				WeightsNode fromDown = data.p[P_FROMDOWN].yx[y][x];
				WeightsNode paintVar = data.p[P_PAINTVAR].yx[y][x];
				//weightFrom main and flip should equal, but to handle roundoffError
				double aveWeightFrom = (fromUp.weightFrom(paintVar)+fromDown.weightFrom(paintVar))/2;
				double newWeight = aveWeightFrom*mult;
				if(Math.abs(newWeight) < minPaintingWeightBeforeDisconnect){ //disconnect
					iter.remove();
					newWeight = 0;
				}
				fromUp.setWeightFrom(paintVar, newWeight);
				fromDown.setWeightFrom(paintVar, newWeight);
				paintVar.setWeightFrom(fromUp, newWeight);
				paintVar.setWeightFrom(fromDown, newWeight);
			}
		}
		///System.out.println("mousePainting.size()=="+mousePainting.size());
	}
	
	public void event(Object o){
		if(o instanceof TimedEvent){
			double now = ((TimedEvent)o).time;
			double sinceLastTimedEvent = now-lastTimedEvent;
			double decayFraction = sinceLastTimedEvent*decaySpeedForPaintWeights;
			decayFraction = MathUtil.holdInRange(0, decayFraction, .5);
			decayPaintingWeights(decayFraction);
			lastTimedEvent = now;
		}
	}
	
	public double preferredInterval(){
		return .01;
	}
	
	public void setDisplayModeNodeScalar(){
		displayWhatInEachColor[0] = DisplayWhat.ChanceOrInfluenceOfThisNode;
		displayWhatInEachColor[1] = DisplayWhat.CopyRed; //dont recalculate
		displayWhatInEachColor[2] = DisplayWhat.CopyRed;	
	}
	
	public void setDisplayModeNodeBit(){
		displayWhatInEachColor[0] = DisplayWhat.BitOfThisNode;
		displayWhatInEachColor[1] = DisplayWhat.CopyRed; //dont recalculate
		displayWhatInEachColor[2] = DisplayWhat.CopyRed;
		//displayWhatInEachColor[2] = DisplayWhat.ChanceOrInfluenceOfThisNode;
	}
	
	/*public void setDisplayModeFromDownGreenFromUpBlue(){
		displayWhatInEachColor[0] = DisplayWhat.Nothing;
		displayWhatInEachColor[1] = "TODO this class cant know about up/down so must do in RbmOnScreen, but on further thought it maybe could be done by p in FlatPYX using P_FROMUP and P_FROMDOWN if those constants were (without those labels) copied in through constructor to display colors per layer differently";
		displayWhatInEachColor[2] = TODO;
	}*/
	
	public void setDisplayModeWeightFromSelected(){
		displayWhatInEachColor[0] = DisplayWhat.WeightFromSelectedNode;
		displayWhatInEachColor[1] = DisplayWhat.CopyRed; //dont recalculate
		displayWhatInEachColor[2] = DisplayWhat.CopyRed;
	}
	
	/*public void addToChangedAndScheduleRepaint(Set<Integer> yxPoints){
		changed.addAll(yxPoints);
		repaint();
	}
	
	public void addToChangedAndScheduleRepaint(int yxPoint){
		changed.add(yxPoint);
		repaint();
	}
	
	public void addToChangedAndScheduleRepaint(short y, short x){
		changed.add(FlatYX.yAndXToYx(y,x));
		repaint();
	}*/
	
	/** When things have to be redrawn but you dont know which *
	public void addAllSquaresOnScreenToChangedListAndRepaint(){
		Rectangle r = getVisibleRect();
		int minGridX = r.x/magnify;
		int maxGridX = (r.x+r.width)/magnify; //TODO is this off by 1 or magnify or correct?
		int minGridY = r.y/magnify;
		int maxGridY = (r.y+r.height)/magnify; //TODO is this off by 1 or magnify or correct?
		//TODO? Would be enough with current code (2015-4) to put in the 4 corners,
		//but if optimizations are put in that only paint smaller areas instead of
		//a bigger rectangle containing them all, then 4 corners wouldnt draw it all.
		
		//FIXME verify ranges instead of just casting to short
		Set<Integer> addAllAtOnce = new HashSet();
		for(short y=(short)minGridY; y<=maxGridY; y++){
			for(short x=(short)minGridX; x<=maxGridX; x++){
				addAllAtOnce.add(FlatYX.yAndXToYx(y,x));
			}
		}
		addToChangedAndScheduleRepaint(addAllAtOnce);
	}*/
	
	/** Adds to P_MAIN_LAYER and P_FLIP_LAYER at that pixel.
	No external code is allowed to change weights between any node in P_PAINTVAR
	and any other node, from 0 to nonzero or from nonzero to 0,
	but they are allowed to change it from nonzero to a different nonzero value.
	The problemt hat would cause is not keeping Set mousePainting updated.
	*/ 
	public void addToPaintingWeights(short y, short x, double add){
		WeightsNode fromUp = data.p[P_FROMUP].yx[y][x];
		WeightsNode fromDown = data.p[P_FROMDOWN].yx[y][x];
		WeightsNode paint = data.p[P_PAINTVAR].yx[y][x];
		double aveWeightFrom = (fromUp.weightFrom(paint)+fromDown.weightFrom(paint))/2;
		double newWeight = aveWeightFrom+add;
		if(newWeight != 0){
			synchronized(mousePainting){
				mousePainting.add(FlatYX.yAndXToYx(y,x));
			}
		}
		fromUp.setWeightFrom(paint, newWeight);
		fromDown.setWeightFrom(paint, newWeight);
	}
	
	public NodeScreen(FlatPYX data){
		this(data, 8);
	}
	
	/** FlatPYX with p at least 2, which alternating rbm layers use to only be
	connected up or down but not both at once.
	Neurons may connect into higher p for ui controls etc.
	If There are 3 p layers, its for benfrayfieldResearch.statsysPaintTool.
	*/
	public NodeScreen(FlatPYX data, int magnify){
		this.magnify = magnify;
		if(data.pSize < P_END) throw new IllegalArgumentException(
			"pSize="+data.pSize+" but must be at least "+P_END);
		this.data = data;
		setMinimumSize(new Dimension(data.xSize, data.ySize));
		setPreferredSize(new Dimension(data.xSize*magnify, data.ySize*magnify));
		int maxMagnify = 64;
		setMaximumSize(new Dimension(data.xSize*maxMagnify, data.ySize*maxMagnify));
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		for(int y=0; y<data.ySize; y++){
			for(int x=0; x<data.xSize; x++){
				WeightsNode n = data.p[P_PAINTVAR].yx[y][x];
				//P_PAINT_LAYER stays on and is never refreshed.
				//Only from it to other nodes is refreshed, while the weight from
				//paint to other nodes changes based on mouse clicks and decays to 0.
				n.scalar = 1;
				n.bit = true;
			}
		}
	}
	
	/** Returns smallest Rectangle which contains all points in Set<Long> changed *
	public Rectangle rectContainingAllChangedPoints(){
		if(changed.isEmpty()) return new Rectangle(0, 0);
		short minX = Short.MAX_VALUE;
		short maxX = Short.MIN_VALUE;
		short minY = Short.MAX_VALUE;
		short maxY = Short.MIN_VALUE;
		for(int yx : changed){
			short y = FlatYX.yxToY(yx);
			short x = FlatYX.yxToX(yx);
			if(y < minY) minY = y;
			else if(y > maxY) maxY = y;
			if(x < minX) minX = x;
			else if(x > maxX) maxX = x;
		}
		//+1 so Rectangle includes last row/column
		int dyPlusOne = (int)maxY+1 - minY;
		int dxPlusOne = (int)maxX+1 - minX;
		if(dyPlusOne > Short.MAX_VALUE || dxPlusOne > Short.MAX_VALUE) throw new RuntimeException(
			"Changed area bigger than fits in signed short range: dy="+dyPlusOne+" dx="+dxPlusOne);
		return new Rectangle(minX*magnify, minY*magnify, dxPlusOne*magnify, dyPlusOne*magnify);
	}*/
	
	protected boolean thisVarAlternatesToFlashSelected;
	
	/** draws only the visible part intersect changed part,
	so efficient for scrolling over large areas and changes/repaints to part of screen.
	*/
	public void paint(Graphics g){
		//TODO (verify this is done) Change P_MAIN_LAYER and P_FLIP_LAYER (and comments on this class) to P_FROMDOWN and P_FROMUP, and then decide on a way to display 1 or both in each displayMode
		Rectangle drawInRect = getVisibleRect();
		//FIXME start using changedRect after its working, but for now I'm getting a gray screen when try to display edges
		//Rectangle changedRect = rectContainingAllChangedPoints();
		//drawInRect = drawInRect.intersection(changedRect);
		
		//TODO check ranges
		final int drawW = drawInRect.width, drawH = drawInRect.height;
		if(drawW == 0 || drawH == 0) return;
		final int wholeW = getWidth(), wholeH = getHeight();
		int xPixelStart = drawInRect.x, xPixelEnd = xPixelStart+drawW;
		int yPixelStart = drawInRect.y, yPixelEnd = yPixelStart+drawH;
		if(image == null || image.getWidth() != drawW || image.getHeight() != drawH){
			image = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
		}
		WritableRaster wr = image.getRaster();
		DataBufferInt buffer = (DataBufferInt) wr.getDataBuffer();
		//Neuron selectedNode = data.p[P_MAIN_LAYER].yx[mouseNodePosition[0]][mouseNodePosition[1]];
		WeightsNode selectedNodeFromUp = data.p[P_FROMUP].yx[mouseNodePosition[0]][mouseNodePosition[1]];
		WeightsNode selectedNodeFromDown = data.p[P_FROMDOWN].yx[mouseNodePosition[0]][mouseNodePosition[1]];
		final float rgb[] = new float[3];
		final float maxRgb[] = new float[]{0,0,0}; //for testing why screen is gray
		final float minRgb[] = new float[]{1,1,1}; //for testing why screen is gray
		
		thisVarAlternatesToFlashSelected = !thisVarAlternatesToFlashSelected;
		float brightOfSelected = thisVarAlternatesToFlashSelected ? 1 : 0;
		//int colorOfSelected = CoreUtil.color(brightOfSelected, brightOfSelected, brightOfSelected);
		
		int ySquareStart = yPixelStart/magnify;
		int ySquareEnd = (yPixelEnd+magnify-1)/magnify;
		int xSquareStart = xPixelStart/magnify;
		int xSquareEnd = (xPixelEnd+magnify-1)/magnify;
		//for(int xPixel=xPixelStart; xPixel<xPixelEnd; xPixel++){
		//	for(int yPixel=yPixelStart; yPixel<yPixelEnd; yPixel++){
		for(int ySquare=ySquareStart; ySquare<ySquareEnd; ySquare++){ //TODO does this need to be <=ySquareEnd? 
			for(int xSquare=xSquareStart; xSquare<xSquareEnd; xSquare++){ //TODO does this need to be <=xSquareEnd?
				//TODO IMPORTANT optimize by choosing color then loop over magnify x magnify square, or maybe Graphics.fillRect would be faster.
				
				//TODO could optimize by choosing which node to draw first then drawing it on many pixels in a rectangle
				/*float xFraction = (xPixel+.5f)/wholeW;
				float yFraction = (yPixel+.5f)/wholeH;
				int xNodeIndex = (int)(xFraction*data.xSize);
				int yNodeIndex = (int)(yFraction*data.ySize);
				*/
				//Neuron node = data.p[P_MAIN_LAYER].yx[yNodeIndex][xNodeIndex];
				WeightsNode nodeFromUp = data.p[P_FROMUP].yx[ySquare][xSquare];
				WeightsNode nodeFromDown = data.p[P_FROMDOWN].yx[ySquare][xSquare];
				
				for(int colorDim=0; colorDim<3; colorDim++){
					float bright;
					switch(displayWhatInEachColor[colorDim]){
					case CopyRed:
						if(colorDim == 0) throw new RuntimeException("Cant copy red from itself");
						bright = rgb[0];
					break; case CopyGreen:
						if(colorDim < 2) throw new RuntimeException(
							"Cant copy green from earlier or equal colorDim="+colorDim);
						bright = rgb[1];
					break; case WeightFromSelectedNode:
						//bright = (float) viewWeightAsFraction(node.weightFrom(selectedNode));
						double weightFromUp = nodeFromUp.weightFrom(selectedNodeFromDown);
						if(weightFromUp != 0){
							bright = (float) viewWeightAsFraction(weightFromUp);
						}else{
							double weightFromDown = nodeFromDown.weightFrom(selectedNodeFromUp);
							bright = (float) viewWeightAsFraction(weightFromDown);
						}
						/* Weight can be 0 in both
						double weightFromUp = nodeFromUp.weightFrom(selectedNodeFromDown);
						double weightFromDown = nodeFromDown.weightFrom(selectedNodeFromUp);
						if(weightFromUp != 0){
							bright = (float) viewWeightAsFraction(weightFromUp);
						}else if(weightFromDown != 0){
							bright = (float) viewWeightAsFraction(weightFromDown);
						}else throw new RuntimeException(
							"Both RbmLayer directions cant exist, but weightFromUp="+weightFromUp
							+" and weightFromDown="+weightFromDown);
						*/
					break; case WeightFromSelectedNodeIsNonzero:
						//bright = node.indexOf(selectedNode)==-1 ? 0 : 1;
						int indexFromUp = nodeFromUp.indexOf(selectedNodeFromDown);
						int indexFromDown = nodeFromDown.indexOf(selectedNodeFromUp);
						bright = 0<=indexFromUp && 0<=indexFromDown ? 1 : 0;
					break; case WeightToSelectedNode:
						//bright = (float) viewWeightAsFraction(selectedNode.weightFrom(node));
						double weightToUp = selectedNodeFromDown.weightFrom(nodeFromUp);
						if(weightToUp != 0){
							bright = (float) viewWeightAsFraction(weightToUp);
						}else{
							double weightToDown = selectedNodeFromUp.weightFrom(nodeFromDown);
							bright = (float) viewWeightAsFraction(weightToDown);
						}
						/* Weight can be 0 in both
						double weightToUp = selectedNodeFromDown.weightFrom(nodeFromUp);
						double weightToDown = selectedNodeFromUp.weightFrom(nodeFromDown);
						if(weightToUp != 0){
							bright = (float) viewWeightAsFraction(weightToUp);
						}else if(weightToDown != 0){
							bright = (float) viewWeightAsFraction(weightToDown);
						}else throw new RuntimeException(
							"Both RbmLayer directions cant exist, but weightToUp="+weightToUp
							+" and weightToDown="+weightToDown);
						*/
					break; case WeightToSelectedNodeIsNonzero:
						//bright = selectedNode.indexOf(node)==-1 ? 0 : 1;
						int indexToUp = selectedNodeFromDown.indexOf(nodeFromUp);
						int indexToDown = selectedNodeFromUp.indexOf(nodeFromDown);
						bright = 0<=indexToUp && 0<=indexToDown ? 1 : 0;
					break; case ChanceOrInfluenceOfThisNode:
						//bright = (float) node.influence; //range 0 to 1
						//Nodes bit/scalar value should equal (TODO make sure this is also done at first and last RbmLayers)
						//bright = (float) nodeFromUp.influence; //range 0 to 1
						//Or could display the average... I'll do that until I'm sure its working:
						bright = (float) (nodeFromUp.scalar+nodeFromDown.scalar)/2; //range 0 to 1
					break; case BitOfThisNode:
						//bright = node.bit ? 1 : 0;
						//Nodes bit/scalar value should equal (TODO make sure this is also done at first and last RbmLayers)
						//bright = nodeFromUp.bit ? 1 : 0;
						//Or could display the OR... I'll do that until I'm sure its working:
						bright = nodeFromUp.bit|nodeFromDown.bit ? 1 : 0;
					break; case Nothing:
						bright = 0;
					break; default:
						throw new RuntimeException("Case not recognized: "+displayWhatInEachColor[colorDim]);
					}
					rgb[colorDim] = bright;
					maxRgb[colorDim] = Math.max(maxRgb[colorDim], bright);
					minRgb[colorDim] = Math.min(minRgb[colorDim], bright);
				}
				//int color = CoreUtil.color(rgb[0], rgb[1], rgb[2]);
				
				if(ySquare == mouseNodePosition[0] && xSquare == mouseNodePosition[1]){
					//color = colorOfSelected;
					rgb[0] = rgb[1] = rgb[2] = brightOfSelected;
				}
				
				/* THIS WORKS, BUT when magnify is 8 (default), g.setColor and g.fillRect are much faster.
				int xInImage = xPixel-xPixelStart;
				int yInImage = yPixel-yPixelStart;
				int indexInImage = yInImage*drawW+xInImage;
				buffer.setElem(indexInImage, color);
				*/
				
				//TODO build mutable Color and change it? Or would that break Graphics?
				g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
				g.fillRect(xSquare*magnify, ySquare*magnify, magnify, magnify);
			}
		}
		
		g.drawImage(image, drawInRect.x, drawInRect.y, this);
		g.setColor(new Color(0f,1f,1f));
		//Font.SANS_SERIF changing between java versions. Use string name.
		g.setFont(new Font("SansSerif", Font.PLAIN, 20));
		//String s = "red ranges "+minRgb[0]+" to "+maxRgb[0]+" green "+minRgb[1]+" "+maxRgb[1]+" blue "+minRgb[2]+" "+maxRgb[2];
		//g.drawString(s, 70, 70);
		//changed.clear();
	}
	
	public double viewWeightAsFraction(double weight){
		//return Util.weakRand.nextDouble();
		//return .5+50*weight;
		//return CoreUtil.holdInRange(0, .5+weight, 1);
		return MathUtil.sigmoid(weight*viewWeightAsFractionMultiplyBeforeSigmoid);
	}
	
	/**	This function checks state of mouse buttons and changes display mode.
	<br><br>
	Use setDisplayModeNodeBit() when both mouse buttons are down or middle button is down.
	Use setDisplayModeWeightFromSelected() all other times.
	<br><br>
	After every mouse move, wheel move, or click, addAllSquaresOnScreenToChangedListAndRepaint()
	because it could change between setDisplayModeNodeBit() and setDisplayModeWeightFromSelected()
	or change TODO what other things could it change?
	*/
	public void onMouseMoveOrButtonEvent(){
		if(mouseButton[1] || (mouseButton[0] && mouseButton[2])){
			setDisplayModeWeightFromSelected();
		}else{
			//setDisplayModeNodeBit();
			setDisplayModeNodeScalar();
		}
		//TODO? addAllSquaresOnScreenToChangedListAndRepaint();
		double now = CoreUtil.time();
		
		double secondsSinceLastUiEvent = now-lastUiEvent;
		int main2ButtonsSum = 0;
		if(mouseButton[0]) main2ButtonsSum++;
		if(mouseButton[2]) main2ButtonsSum--;
		if(main2ButtonsSum != 0){
			double maxChangeToWeight = paintSpeed/2; //in case no events for longer or ui gets really slow
			double addToWeight = main2ButtonsSum*secondsSinceLastUiEvent*paintSpeed;
			addToWeight = MathUtil.holdInRange(-maxChangeToWeight, addToWeight, maxChangeToWeight);
			System.out.println("Add to paint weights: "+addToWeight);
			
			/*int y = mouseNodePosition[0], x = mouseNodePosition[1];
			Neuron paintVar = data.p[P_PAINTVAR].yx[y][x];
			Neuron pixUp = data.p[P_FROMUP].yx[y][x];
			Neuron pixDown = data.p[P_FROMDOWN].yx[y][x];
			//pixUp.setWeightFrom(paintVar, addToWeight+pixUp.weightFrom(paintVar));
			//pixDown.setWeightFrom(paintVar, addToWeight+pixDown.weightFrom(paintVar));
			if(!paintVar.bit) throw new RuntimeException(
				"All nodes in P_PAINTVAR pLayer stay on and are never run (since they are used for input only), but this node is off: "+paintVar);
			BoltzUtil.addToBothWeightsBetween(pixUp, addToWeight, paintVar);
			BoltzUtil.addToBothWeightsBetween(pixDown, addToWeight, paintVar);
			*/
			
			//Important comment, dont delete it if the code above isnt needed...
			//Since P_PAINTVAR are never run and always stay on, theres almost no need to
			//make their weights symmetric, but makeSymmetric(RBM) will average them,
			//so I set them symmetricly to avoid the weights jumping to half
			//when thats called after RBM.bidirectionalNormAgainInHowManyMods learns.
			
			addToPaintingWeights(mouseNodePosition[0], mouseNodePosition[1], addToWeight);
		}
		
		lastUiEvent = now;
		repaint();
	}
	
	public void mouseDragged(MouseEvent e){
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e){
		mouseIn = true;
		mousePosition[0] = e.getY();
		mousePosition[1] = e.getX();
		int newNodeY = mousePosition[0]/magnify;
		int newNodeX = mousePosition[1]/magnify;
		if(0 <= newNodeY && newNodeY < data.ySize && 0 <= newNodeX && newNodeX < data.xSize){
			mouseNodePosition[0] = (short) newNodeY;
			mouseNodePosition[1] = (short) newNodeX;
		}
		//TODO this may need to update more often than when mouse moves,
		//but weights tend to change slower than neuralActivation/influence/chance.
		//int selectedColorDim = 2; //TODO choose from red, green, and blue. For now, blue.
		/* do in paint function instead
		NeuralNode selectedNode = data.xyp[mouseNodePosition[0]][mouseNodePosition[1]][0];
		for(int xNode=0; xNode<data.xSize; xNode++){
			for(int yNode=0; yNode<data.ySize; yNode++){
				NeuralNode fromNode = data.xyp[xNode][yNode][0];
				weightToSelectedNode[xNode][yNode] = selectedNode.weightFrom(fromNode);
				weightFromSelectedNode[xNode][yNode] = fromNode.weightFrom(selectedNode);
			}
		}*/
		//TODO runNeurons();
		onMouseMoveOrButtonEvent();
	}

	public void mouseClicked(MouseEvent e){
		//pressed and released events call onMouseMoveOrButtonEvent();
		for(String unit : InOutTimer.units()){
			System.out.println(InOutTimer.forUnit(unit));
		}
	}

	public void mousePressed(MouseEvent e){
		switch(e.getButton()){
		case MouseEvent.BUTTON1:
			mouseButton[0] = true;
		break; case MouseEvent.BUTTON2:
			mouseButton[1] = true;
		break; case MouseEvent.BUTTON3:
			mouseButton[2] = true;
		break; 
		}
		onMouseMoveOrButtonEvent();
	}

	public void mouseReleased(MouseEvent e){
		switch(e.getButton()){
		case MouseEvent.BUTTON1:
			mouseButton[0] = false;
		break; case MouseEvent.BUTTON2:
			mouseButton[1] = false;
		break; case MouseEvent.BUTTON3:
			mouseButton[2] = false;
		break; 
		}
		onMouseMoveOrButtonEvent();
	}
	
	public void mouseEntered(MouseEvent e){
		mouseIn = true;
		//mouse moved event will call onMouseMoveOrButtonEvent();
	}

	public void mouseExited(MouseEvent e){
		mouseIn = false;
		onMouseMoveOrButtonEvent();
	}
	
	/** MOVING THIS TO RbmOnScreen.
	Wheel forward learns positively. Wheel backward learns negatively. *
	public void mouseWheelMoved(MouseWheelEvent e){
		//negative so push forward learns positively
		double learnRate = -mouseWheelScale*e.getPreciseWheelRotation();
		//See if this is fast enough, since each weightedRandomBit costs on average 2 random bits.
		//Or maybe create a medium quality Random that uses multiple low quality pseudorandom
		//which are each reseeded often but not every time, and use them in different orders.
		//I wouldnt want low quality pseudorandomness to create patterns in the rbm learning.
		Random rand = CoreUtil.strongRand;
		BoltzUtil.learnFromCurrentThought(rb, learnRate, rand);
		onMouseMoveOrButtonEvent(); //TODO is this needed?
	}*/
	public void mouseWheelMoved(MouseWheelEvent e){}

}