package humanainet.blackholecortex.boltz;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatPYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.NeuralShape;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.ObservedRect;
import humanainet.blackholecortex.ui.NodeScreen;

/** RBM is renamed to this RbmData. AnnealStrategy and other RbmParams will be
in CustomRBMSameParamsAcrossLayers and similar classes.
RbmData is only the mutable nodes and edges, not the algorithms.
Old text...
<br><br>
After I built RBMWithOneP (and renamed it from RBM to that),
I decided to organize RBM into 2 nodes per pixel on screen,
1 for up/down and the other for down/up in alternating even/odd pairs of RBM layers,
so it naturally flows as nodes only being updated from the layer above or below
but not both. bits andOr scalars must be copied between these pairs of nodes.
<br><br>
Old text, most of it still relevant but may need to rewrite...
<br><br>
Restricted/Layered Boltzmann Machine with an extra layer hanging down from each
layer except the first (visible nodes), parallel to each layer except the last (top layer).
The extra layers are short term memory and are needed for tracking objects
and properties of them in paths through the layers.
<br><br>
This is a pointer into any FlatXYP at rectangles containing many NeuralNode,
but those specific NeuralNode are not reachable from here
since it applies equally to many possible FlatXYP. This is the sizes and
relative locations but not the data past those pointers.
<br><br>
TODO use Node as pointer to group of nodes, instead of List, and create an object that is a network of those, or maybe an acyclicNet is better? Its like my networkgrid but its the part where unitary transforms are between each tree node
BUT NOT ALWAYS UNITARY TRANSFORM BETWEEN branch and parent in the tree,
because some of them are meant to bring data up from multiple branches at once.
The purpose of a unitary tree is to read and write only 1 data at a time,
so by the rules of boltzmann it will read from the branch that best fits.
<br><br>
TODO Should RBM contain ObservedRects or Rectangles?
Since ObservedRect is a List<NeuralNode>, could change them all to List<NeuralNode>,
but then would lose the pointers to Rectangle so dont know where it is on screen
for callers who want to display it, so I'll stick with ObservedRect.
*/
public class RbmData{
	
	//TODO use RbmLayer instead of ObservedRect directly

	
	public RbmLayer mainLayers[];
	//public Rectangle mainRbmLayers[];
	
	//public int mainRbmLayerSize[];
	
	/** This array is 1 smaller than mainRbmLayers because they hang down from all except the first */
	public RbmLayer shortTermMemoryLayers[];
	//public Rectangle shortTermMemoryLayers[];
	
	//public int shortTermMemoryLayerSize[];
	
	/** Indexs aligned to mainRbmLayers (except the last) and shortTermMemoryLayers.
	Each pair of rectangles fits in the larger rectangle here.
	For example, combinedLayers[3] contains mainRbmLayers[3] and shortTermMemoryLayers[3].
	*/
	public RbmLayer combinedLayers[];
	//public Rectangle combinedLayers[];
			
	/** A mod is 1 learning cycle, which may be a learnPositive,
	then updating nodes up/down any number of cycles, followed by a learnNegative,
	or it may be a run of any other learning algorithm.
	Caller adds 1 after each such action.
	*
	public transient long mods;
	*/
	
	/** Last time ran BoltzUtil.makeBidirectional(this), the mods var was this value.
	That function updates this var.
	*
	public transient long lastBidirectionalNormAtHowManyMods;
	*/
	
	public transient int bidirectionalNormAgainInHowManyMods;
	
	//AnnealStrategy is becoming a RbmParam. RBM is only the nodes and edges, a datastruct, not the algorithms.
	//public final AnnealStrategy annealStrategy;
	
	/** Each combinedLayer must be List<Neuron> from mainLayer concat List<Neuron> from shortTermMemoryLayer */
	public RbmData(RbmLayer mainLayers[], RbmLayer shortTermMemoryLayers[], RbmLayer combinedLayers[]){
		this.mainLayers = mainLayers.clone();
		this.shortTermMemoryLayers = shortTermMemoryLayers.clone();
		this.combinedLayers = combinedLayers.clone();
		//this.annealStrategy = annealStrategy;
	}
	
	/** Randomizes all pairs of connections between adjacent layers, on a bellCurve.
	Overwrites all data in this RBM except addToWeight vars and
	weights from Neurons outside this RBM.
	*/
	public void randomizeWeights(double ave, double stdDev, Random rand){
		/**FIXME DO THIS INSTEAD OF MAINLAYERS
		for(int layer=0; layer<combinedLayers.length-1; layer++){
			List<Neuron> low = combinedLayers[layer].fromUp;
			List<Neuron> high = combinedLayers[layer+1].fromDown;
			BoltzUtil.randomizeConnections(low, high, ave, stdDev, rand);
		}*/
		
		for(int layer=0; layer<combinedLayers.length-1; layer++){
			List<WeightsNode> low = combinedLayers[layer].fromUp;
			List<WeightsNode> high = combinedLayers[layer+1].fromDown;
			BoltzUtil.randomizeConnectionsSymmetricly(low, high, ave, stdDev, rand);
		}
	}
	
	/** See comment on this class about why the FlatPYX must have P=2.
	<br><br>
	mainRbmLayerSize.length = 1+shortTermMemoryLayerSize.length.
	A common yBlockSize is 16. Its how many pixels tall the image is at visible layer,
	even if the RBM is not for visual data or is not on screen at all,
	rectangles are 2d indexs and are still a good way to organize AI vars.
	These rectangles are created in a certain shape relative to eachother. 
	*/
	public RbmData(int startX, int startY, FlatPYX data, int yBlockSize, int mainRbmLayerSize[], int shortTermMemoryLayerSize[]){
		//this.startX = startX;
		//this.startY = startY;
		//TODO verify all layer sizes are multiple of yBlockSize
		if(mainRbmLayerSize.length != 1+shortTermMemoryLayerSize.length) throw new RuntimeException(
			"mainRbmLayerSize size "+mainRbmLayerSize.length
			+" is not 1 more than shortTermMemoryLayerSize size "+shortTermMemoryLayerSize.length);
		for(int s : mainRbmLayerSize) if((s%yBlockSize)!=0) throw new RuntimeException(
			"Layer size "+s+" not multiple of yBlockSize "+yBlockSize);
		for(int s : shortTermMemoryLayerSize) if((s%yBlockSize)!=0) throw new RuntimeException(
			"Layer size "+s+" not multiple of yBlockSize "+yBlockSize);
		//this.yBlockSize = yBlockSize;
		mainLayers = new RbmLayer[mainRbmLayerSize.length];
		shortTermMemoryLayers = new RbmLayer[shortTermMemoryLayerSize.length];
		combinedLayers = new RbmLayer[mainRbmLayerSize.length];

		int mainLayerXEnds[] = new int[mainRbmLayerSize.length];
		for(int i=0; i<mainLayers.length; i++){
			int x = startX;
			//int y = yStartOfLayer(i);
			int y = startY+yBlockSize*i;
			int w = mainRbmLayerSize[i]/yBlockSize;
			int h = yBlockSize;
			mainLayerXEnds[i] = x+w;
			//mainRbmLayers[i] = new Rectangle(x,y,w,h);
			Rectangle rect = new Rectangle(x,y,w,h);
			
			//Layer 0 fromUp and layer 1 fromDown are at p=0 in FlatPYX.
			//Layer 1 fromUp and layer 2 fromDown are at p=1 in FlatPYX.
			//Layer 2 fromUp and layer 3 fromDown are at p=0 in FlatPYX.
			//It continues to alternate.
			List<WeightsNode> fromDown, fromUp;
			//if(i == 0){
				//fromDown = null;
				//For display, first and last layers will have 2 RbmLayer the same as any other,
				//but its not used for learning.
			//}else{
				//fromDown = new ObservedRect(data.p[1-(i&1)], rect);
				fromDown = new ObservedRect(data.p[NodeScreen.P_FROMDOWN], rect);
				fromDown = new ArrayList(fromDown);
			//}
			//if(i == mainRbmLayers.length-1){
				//fromUp = null;
				//For display, first and last layers will have 2 RbmLayer the same as any other,
				//but its not used for learning.
			//}else{
				//fromUp = new ObservedRect(data.p[i&1], rect);
				fromUp = new ObservedRect(data.p[NodeScreen.P_FROMUP], rect);
				fromUp = new ArrayList(fromUp);
			//}
			mainLayers[i] = new RbmLayer(fromDown, fromUp);
		}
		
		//shortTermMemoryLayers.length+1 == mainRbmLayers.length
		//Like mainRbmLayers[0], shortTermMemoryLayers only have fromUp.
		for(int i=0; i<shortTermMemoryLayers.length; i++){
			List<WeightsNode> mainLayerFromUp = mainLayers[i].fromUp;
			//int x = mainLayerFromUp.rect.x+mainLayerFromUp.rect.width;
			int x = mainLayerXEnds[i];
			//int y = mainLayerFromUp.rect.y;
			int y = startY+yBlockSize*i;
			int w = shortTermMemoryLayerSize[i]/yBlockSize;
			int h = yBlockSize;
			Rectangle rect = new Rectangle(x,y,w,h);
			//ObservedRect fromDown = null;
			//For display, first and last layers will have 2 RbmLayer the same as any other,
			//but its not used for learning.
			List<WeightsNode> fromDown = new ObservedRect(data.p[NodeScreen.P_FROMDOWN], rect);
			fromDown = new ArrayList(fromDown);
			//ObservedRect fromUp = new ObservedRect(data.p[i&1], rect);
			//shortTermMemoryLayers only go up. fromDown are also there but are empty.
			List<WeightsNode> fromUp = new ObservedRect(data.p[NodeScreen.P_FROMUP], rect);
			fromUp = new ArrayList(fromUp);
			shortTermMemoryLayers[i] = new RbmLayer(fromDown, fromUp);
		}
		
		for(int i=0; i<combinedLayers.length; i++){
			int x = startX;
			//int y = yStartOfLayer(i);
			int y = startY+yBlockSize*i;
			int w = mainRbmLayerSize[i]/yBlockSize;
			if(i < shortTermMemoryLayerSize.length) w += shortTermMemoryLayerSize[i]/yBlockSize;
			int h = yBlockSize;
			Rectangle rect = new Rectangle(x,y,w,h);
			List<WeightsNode> fromDown, fromUp;
			//For display, first and last layers will have 2 RbmLayer the same as any other,
			//but its not used for learning.
			//if(i == 0){
			//	fromDown = null;
			//}else{
				//fromDown = new ObservedRect(data.p[1-(i&1)], rect);
				fromDown = new ObservedRect(data.p[NodeScreen.P_FROMDOWN], rect);
				fromDown = new ArrayList(fromDown);
			//}
			//if(i == mainRbmLayers.length-1){
			//	fromUp = null;
			//}else{
				//fromUp = new ObservedRect(data.p[i&1], rect);
				fromUp = new ObservedRect(data.p[NodeScreen.P_FROMUP], rect);
				fromUp = new ArrayList(fromUp);
			//}
			//overlaps mainRbmLayers and shortTermMemoryLayers
			combinedLayers[i] = new RbmLayer(fromDown, fromUp);
		}
		
		//this.annealStrategy = annealStrategy;
	}
	
	/** copies all RbmLayer, including fromUp and fromDown,
	into a mutable list increasing layer order, fromUp before fromDown
	since visibleNodes are first fromUp. Includes duplicates if any are in multiple RbmLayer,
	which is not recommended but is in theory allowed.
	*/
	public List<WeightsNode> nodesInNewMutableList(){
		List<WeightsNode> list = new ArrayList();
		for(RbmLayer rLayer : combinedLayers){
			list.addAll(rLayer.fromUp);
			list.addAll(rLayer.fromDown);
		}
		return list;
	}
	
	public long nodesWithoutUpDownDuplicates(){
		long sum = 0;
		for(RbmLayer rLayer : combinedLayers){
			sum += rLayer.size;
		}
		return sum;
	}
	
	public long maxConnectedPairs(){
		long sum = 0;
		for(int layer=0; layer<combinedLayers.length-1; layer++){
			sum += combinedLayers[layer].size*combinedLayers[layer+1].size;
		}
		return sum;
	}
	
	public long edgesIncludingBidirectionalDuplicates(){
		long sum = 0;
		for(RbmLayer rLayer : combinedLayers){
			for(WeightsNode n : rLayer.fromUp){
				sum += n.size;
			}
			for(WeightsNode n : rLayer.fromDown){
				sum += n.size;
			}
		}
		return sum;
	}
	
	/*protected int yStartOfLayer(int layer){
		//return yBlockSize*(mainRbmLayers.length-1-layer);
		return startY+yBlockSize*layer;
	}*/


	//Since RbmLayer uses List<Neuron> instead of specificly ObservedRect, 2d coordinates shouldnt be here.
	//public final int startX, startY;
	
	/** All layer sizes must be a multiple of this, which happens along y dim on screen.
	Since RbmLayer uses List<Neuron> instead of specificly ObservedRect, 2d size shouldnt be here.
	*
	public int yBlockSize;
	*/
	
}
