package humanainet.blackholecortex.ui;
import static humanainet.common.CommonFuncs.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import humanainet.realtimeschedulerTodoThreadpool.Task;
import humanainet.realtimeschedulerTodoThreadpool.TimedEvent;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.object.Polydim;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.anneal.SimpleAnnealStrategy;
import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatPYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.ObservedRect;
import humanainet.common.CoreUtil;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;
import humanainet.datasetsForAI.mnistocrdataset.MnistLabeledImage;
import humanainet.datasetsForAI.mnistocrdataset.MnistOcrDataset;

import javax.swing.JPanel;

/** TODO this is generalizing of  ResizGraphWithMnist to any size pictures as Polydim */
public class RbmOnScreen extends NodeScreen implements Task{
	
	//FIXME Possible causes of lack of many edge weights which I tried to put there as random examples (in BlackHoleCortexWindow):
	//Weights really dont exist. OR
	//Reading mouse position wrong OR
	//reading otherNode position wrong.
	
	//TODO now that Task is merged with Eventable, where should the code be for RBM run on an interval? Or can the Set<Long> changed alone do it?
	
	//TODO RbmOnScreen uses a NodeScreen but not extends it, so there can be multiple rbms,
	//but that creates problem for how to get access to mouse wheel events that cause learning on specific rbm
	
	//public final MnistLabeledImage /*trainingImages[],*/ testImages[];
	
	/** Reduce mnistOcrDataset from 28x28 images with scalar brightness and 1 byte label,
	to 16x16 images, where middle 14x14 are half size in each dim of 28x28 and
	the first 10 bitvars are which digit, or maybe it should be 2 rows of 5 bitvars
	and put the 14x14 grid in the lower right corner (yes I'll use that dataFormat).
	All layer sizes will be a multiple of 16 so they fit together on screen.
	Each Bits object in this List is size 256.
	Read it as 1 javaclass://wavetree.bit.ByteArrayUntilSplit per image.
	<br><br>
	I want that mnistOcrDataset converted to that data format, 256 bits per image with label,
	in a simple array of bits, so it will load faster. I cant have it taking 5 extra seconds
	to load each time, even for the smaller testing dataset, when I'm not using its scalars.
	*/
	public final Polydim mainDataset;
	//public final List<boolean[]> mainDataset;
	//TODO public final List<Bits> mainDataset = TODO;
	//TODO? See BhcDatastructUtil public static final List<Bits> mainDataset =
	//What training do I want it to do? I want to explore
	
	//protected long clicks;
	
	protected String textToPaint = "";
	
	public final RbmData rbm;
	
	public final int startGridX, startGridY;
	
	/** imageWidth and imageHeight are displayed as 1 pixel per grid square */
	public final int imageWidth, imageHeight;
	
	protected double runRbmThisManySecondsAfterLastUiEvent = 30;
	
	/** Sum of mouse wheel until next run of rbm, then this is set back to 0 */
	protected volatile double nextLearnRate;
	
	public final AnnealStrategy anneal;
	
	//TODO adjust this.viewWeightAsFractionMultiplyBeforeSigmoid using stdDev of weights in rbm

	/** Image is at topleft corner. Rbm layers hang down and right from startX and startY.
	Caller must set refreshOnlyNoPredict to false after training is done so
	prediction andOr learning (with mouse wheel) happens but not while first training,
	and you get to see it while its being trained.
	*/
	public RbmOnScreen(AnnealStrategy anneal, FlatPYX data, int magnify, int startX, int startY, int imageWidth, int imageHeight){
		super(data, magnify);
		this.anneal = anneal;
		this.startGridX = startX;
		this.startGridY = startY;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		//trainingImages = MnistOcrDataset.readTrainingLabeledImages();
		//testImages = MnistOcrDataset.readTestLabeledImages();
		//TODO use main images to train, not from the test dataset, but I like its smaller size for now
		//mainDataset = MnistOcrDataset.readTestLabeledImages16x16();
		Nanotimer t = new Nanotimer();
		//TODO dataset should be put in by caller, maybe in constructor or after.
		mainDataset = MnistOcrDataset.readTestLabeledImages16x16AsMultiDim();
		double readMnistDataDuration = t.secondsSinceLastCall();
		System.out.println("readTestLabeledImages16x16AsMultiDim took "+readMnistDataDuration+" seconds.");
		displayWhatInEachColor[0] = DisplayWhat.Nothing; //red's default
		displayWhatInEachColor[1] = DisplayWhat.Nothing; //green's default
		displayWhatInEachColor[2] = DisplayWhat.ChanceOrInfluenceOfThisNode; //blue's default
		//TODO? AnnealStrategy anneal = new SimpleAnnealStrategy(50, 5);
		/*rbm = new RBM(
			startX,
			startY,
			data,
			imageHeight,
			new int[]{
				imageHeight*16,
				imageHeight*24,
				imageHeight*32,
				imageHeight*40
			},
			//new int[]{
			//	imageHeight*5, //TODO after rbm is working without extra layers hanging off
			//	imageHeight*5//,
			//	//imageHeight*5
			//},
			new int[]{ 0, 0, 0 }, //TODO after rbm is working without extra layers hanging off
			anneal
		);
		*/
		rbm = new RbmData(
			startX,
			startY,
			data,
			imageHeight,
			new int[]{
				imageHeight*16,
				imageHeight*8
			},
			//new int[]{
			//	imageHeight*5, //TODO after rbm is working without extra layers hanging off
			//	imageHeight*5//,
			//	//imageHeight*5
			//},
			new int[]{ 0 } //TODO after rbm is working without extra layers hanging off
			//anneal
		);
		//viewWeightAsFractionMultiplyBeforeSigmoid = .1;
		//mainRBM.randomize(ave, stdDev, rand);
		/*for(ObservedRect memRect : mainRBM.shortTermMemoryLayers){ //test display
			for(Neuron node : nodesCacheable(memRect.rect)){
				node.influence = (node.bit = true) ? 1 : 0;
			}
		}*/
	}
	
	public double preferredInterval(){
		//return .02;
		//return .001;
		return .01;
		//return .005;
	}
	
	public boolean refreshOnlyNoPredict = true;
	
	public void event(Object o){
		super.event(o); //calls NodeScreen.decayPaintingWeights(decay continuously)
		if(o instanceof TimedEvent){
			double secondsSinceUiEvent = ((TimedEvent)o).time-lastUiEvent;
			if(secondsSinceUiEvent <= runRbmThisManySecondsAfterLastUiEvent){
				if(!refreshOnlyNoPredict){
					//TODO use weakRand to predict and strongRand to learn?
					//BoltzUtil.predictFromCurrentThought(rbm, CoreUtil.strongRand);
					final double learnRate = nextLearnRate;
					nextLearnRate = 0;
					//TODO CoreUtil.strongRand
					//(see if its fast enough, because I dont want low quality pseudorandomness
					//creating patterns in the rbm)
					Random rand = MathUtil.weakRand;
					if(learnRate == 0){
						BoltzUtil.predictFromCurrentThought(rbm, anneal, rand);
					}else{
						System.out.println("learnRate="+learnRate);
						BoltzUtil.learnFromCurrentThought(rbm, anneal, learnRate, true, rand);
					}
				}
				//addAllBoltzNodesToChangedOnScreen();
				repaint();
			}
		}
	}
	
	/** As an optimization, paint(Graphics) only draws what is said to have changed (and whats between) */
	protected void addAllBoltzNodesToChangedOnScreen(){
		//TODO how to know where the Neurons are on screen, if they happen to be normal List<Neuron> instead of ObservedRect? RbmOnScreen (through superclass) has pointer to FlatPXY, but it may be slow for FlatPYX to look them up by Map.
		//Should FlatPYX andOr FlatYX have Map<Neuron,Long> neuronToYX? FlatYX.
		//First layer is P_MAIN_LAYER, then alternates P_FLIP_LAYER.
		
		Set<Integer> addToChangedAllAtOnce = new HashSet();
		for(int layer=0; layer<rbm.combinedLayers.length; layer++){
			RbmLayer rLayer = rbm.combinedLayers[layer];
			//first and last layers have 1 null list
			
			//This could exclude top layer depending on even/odd number of layers:
			//List<Neuron> nodes = (layer&1)==0 ? rLayer.fromUp : rLayer.fromDown;
			
			//TODO Since top layer may only have List<Nodes> in P_MAIN_LAYER or P_FLIP_LAYER depending on even/odd number of layers, need to vary the p instead of only displaying only a single p layer everywhere"
			//If vary p, still have to choose up for some and down for others, since first and last layers dont have the same up/down, while middle layers have both.
			//boolean up = true; //display up vs down
			//int p = 
			
			//FlatYX downYX = data.p[(layer&1)==1 ? P_MAIN_LAYER : P_FLIP_LAYER];
			FlatYX downYX = data.p[P_FROMDOWN];
			if(rLayer.fromDown != null){
				for(WeightsNode n : rLayer.fromDown){
					addToChangedAllAtOnce.add(downYX.neuronToYX.get(n));
				}
			}
			//FlatYX upYX = data.p[(layer&1)==0 ? P_MAIN_LAYER : P_FLIP_LAYER];
			FlatYX upYX = data.p[P_FROMUP];
			if(rLayer.fromUp != null){
				for(WeightsNode n : rLayer.fromUp){
					addToChangedAllAtOnce.add(upYX.neuronToYX.get(n));
				}
			}
			
		}
		//addToChangedAndScheduleRepaint(addToChangedAllAtOnce);
		
		/*for(RbmLayer layer : mainRBM.combinedLayers){
			//first layer is at p=0, then they alternate 1 0 1 0...
			//but at least for now I'll check them all,
			//in case in a later version they dont overlap on screen I still want them to be drawn.
			for(int fromWhere=0; fromWhere<2; fromWhere++){
				List<Neuron> nodes = fromWhere==0 ? layer.fromDown : layer.fromUp;
				if(nodes != null){ //first and last layers have 1 null list
					for(Neuron n : nodes){
						data.
					}
				}
			}
		}*/
	}
	
	/*protected void refreshMnistImage(){
		//observe(testImages[(int)(clicks%testImages.length)]);
		//observe(mainDataset.get((int)(clicks%mainDataset.size())));
		int whichImage = (int)(clicks%mainDataset.dimSize(0));
		observe(mainDataset.bits(whichImage));
	}*/
	
	protected void observe(Polydim image){
		if(image.dims() != 2 || image.dimSize(0) != imageHeight || image.dimSize(1) != imageWidth){
			//TODO generalize this class to any size of image and not just mnistOcrDataset
			throw new RuntimeException("Wrong size image: "+image+" must be h="+imageHeight+" w="+imageWidth
				+" but is h="+image.dimSize(0)+" w="+image.dimSize(1));
		}
		List<WeightsNode> visibleNodes = rbm.mainLayers[0].fromUp;
		Bits imageBits = image.data();
		if(imageBits.siz() != visibleNodes.size()) throw new RuntimeException(
			imageBits.siz()+" == imageBits.siz() != visibleNodes.size() == "+visibleNodes.size());
		/*
		long sizeY = image.dimSize(0);
		long sizeX = image.dimSize(1);
		for(int y=0; y<sizeY; y++){
			for(int x=0; x<sizeX; x++){
				Neuron n = data.p[P_MAIN_LAYER].yx[y][x];
				boolean pixel = image.bit(x, y);
				n.influence = (n.bit = pixel) ? 1 : 0;
			}
		}*/
	}
	
	/*protected void observe(boolean image16x16[]){
		if(image16x16.length != 256) throw new IllegalArgumentException(
			"Bit array size is "+image16x16.length+" but must be 256 for 16x16");
		//int xStart = 0;
		//int yStart = (mainRbmLayers.length-1)*16; //below other rbm layers
		//int yStart = yStartOfLayer(0);
		Rectangle layer = mainRBM.mainRbmLayers[0].rect;
		for(int i=0; i<256; i++){
			int x = layer.x+i/16; //TODO swap x and y?
			int y = layer.y+i%16;
			Neuron n = data.yx[y][x];
			n.influence = (n.bit = image16x16[i]) ? 1 : 0;
		}
	}*/
	
	/*protected void observe(MnistLabeledImage image){
		textToPaint = "Digit "+image.label;
		int xEnd = CoreUtil.min(image.pixels[0].length, data.xSize);
		int yEnd = CoreUtil.min(image.pixels.length, data.ySize);
		for(int x=0; x<xEnd; x++){
			for(int y=0; y<yEnd y++){
				byte brightness = image.pixels[x][y];
				double brightnessFraction = brightness/255.;
				Neuron n = data.yx[y][x];
				n.influence = (n.bit=brightnessFraction==0) ? 0 : 1;
				//n.influence = Math.pow(brightnessFraction, 1/32.);
			}
		}
	}*/
	
	public void paint(Graphics g){
		super.paint(g);
		g.setColor(Color.white);
		g.drawString(textToPaint, 25, 25);
	}
	
	/*public Neuron[] nodesCacheable(Rectangle rect){
		return data.nodesCacheable(rect);
	}*/

	

	public void mouseMoved(MouseEvent e){
		//TODO might 
		//BoltzUtil.predictFromCurrentThought(rbm, CoreUtil.weakRand);
		/*for(int i=0; i<30; i++){
			BoltzUtil.predictFromCurrentThought(rbm, new Random());
		}
		*/
		super.mouseMoved(e);
		//addAllSquaresOnScreenToChangedListAndRepaint();
	}


	/*
	public void mouseClicked(MouseEvent e){
		super.mouseClicked(e);
		TODO?
	}

	public void mousePressed(MouseEvent e){
		super.mousePressed(e);
		TODO?
	}

	public void mouseReleased(MouseEvent e){
		super.mouseReleased(e);
		TODO
		addAllSquaresOnScreenToChangedListAndRepaint();
	}
	
	public void mouseEntered(MouseEvent e){
		super.mouseEntered(e);
		TODO?
	}

	public void mouseExited(MouseEvent e){
		super.mouseExited(e)
		TODO?;
	}*/
	
	protected double mouseWheelSum;
	
	/** Wheel forward learns positively. Wheel backward learns negatively. */
	public void mouseWheelMoved(MouseWheelEvent e){
		super.mouseWheelMoved(e);
		//negative so push forward learns positively
		double learnRate = -mouseWheelScale*e.getPreciseWheelRotation();
		//See if this is fast enough, since each weightedRandomBit costs on average 2 random bits.
		//Or maybe create a medium quality Random that uses multiple low quality pseudorandom
		//which are each reseeded often but not every time, and use them in different orders.
		//I wouldnt want low quality pseudorandomness to create patterns in the rbm learning.
		//FIXME TODO Random rand = CoreUtil.strongRand;
		//Random rand = CoreUtil.weakRand; //FIXME trying this instead of CoreUtil.strongRand, to see if it gets faster
		//BoltzUtil.learnFromCurrentThought(rbm, learnRate, rand);
		nextLearnRate += learnRate;
		onMouseMoveOrButtonEvent(); //TODO is this needed?
	}
	
	/*public void mouseWheelMoved(MouseWheelEvent e){
		mouseWheelSum -= e.getPreciseWheelRotation(); //subtract so pushing it forward is positive
		log("mouseWheelSum="+mouseWheelSum+" movedThisTime="+e.getPreciseWheelRotation()+" getWheelRotation="+e.getWheelRotation()+" getScrollAmount="+e.getScrollAmount());
	}*/

}