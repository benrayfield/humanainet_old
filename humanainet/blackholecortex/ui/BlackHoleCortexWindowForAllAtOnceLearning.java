package humanainet.blackholecortex.ui;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import humanainet.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.Fast0To16Bits;
import humanainet.wavetree.bit.object.Polydim;
import humanainet.wavetree.bit.object.SimplePolydim;
import humanainet.jselfmodify.PluginLoader;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.anneal.SimpleAnnealStrategy;
import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatPYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatYX;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;
import humanainet.common.ScreenUtil;
import humanainet.datasetsForAI.mnistocrdataset.MnistOcrDataset;

public class BlackHoleCortexWindowForAllAtOnceLearning extends JFrame{
	
	//public final ResizGraphWeightAndChance screen;
	
	public final RbmOnScreen screen;
	
	public BlackHoleCortexWindowForAllAtOnceLearning(int nodesX, int nodesY){
		super("BlackHoleCortex (opensource GNU GPL 2+, unzip this jar file to get source code)");
		Nanotimer t = new Nanotimer();
		//2 layers for RBM, 1 for benfrayfieldResearch.statsysPaintTool
		FlatPYX pixelNodes = new FlatPYX(3, nodesY, nodesX);
		double flatxypConstructorDuration = t.secondsSinceLastCall();
		System.out.println("In BlackHoleCortexWindow constructor, took "+flatxypConstructorDuration
			+" seconds for flatxypConstructorDuration.");
		//screen = new ResizGraphWeightAndChance(pixelNodes)
		//pack();
		//screen = new ResizGraphWithMnist(pixelNodes);
		
		Polydim images = BoltzUtil.getDefaultSmallTrainingData16x16Images();
		int imageHeight = (int) images.dimSize(1);
		int imageWidth = (int) images.dimSize(2);
		int startX = 0, startY = 0;
		AnnealStrategy anneal = new SimpleAnnealStrategy(200, 2, 1);
		screen = new RbmOnScreen(anneal, pixelNodes, 8, startX, startY, imageWidth, imageHeight);
		double startWeightsAve = 0;
		double startWeightsStdDev = .01;
		//double startWeightsStdDev = .2;
		//double stdDev = 8.8;
		screen.rbm.randomizeWeights(startWeightsAve, startWeightsStdDev, MathUtil.strongRand);
		/*for(Neuron n : screen.rbm.nodesInNewMutableList()){
			n.addToWeight = -.005; //TODO leave addToWeight as 0?
		}*/
		for(WeightsNode n : screen.rbm.mainLayers[0].fromUp){
			n.bit = MathUtil.strongRand.nextBoolean();
			n.scalar = n.bit ? 1 : 0;
		}
		for(RbmLayer rLayer : screen.rbm.combinedLayers){
			for(WeightsNode n : rLayer.fromUp){
				n.bit = MathUtil.strongRand.nextBoolean();
				//n.bit = true;
				n.scalar = n.bit ? 1 : 0;
			}
		}
		//TODO this shouldnt be needed since they should copy to eachother, but testing with second loop...
		/*screen.mainRBM.mainLayers[0].copyNodeStates(false); //copy fromUp to fromDown
		for(RbmLayer rLayer : screen.mainRBM.shortTermMemoryLayers){
			for(Neuron n : rLayer.fromUp){
				n.bit = true;
				n.influence = 1;
			}
			rLayer.copyNodeStates(false); //copy fromUp to fromDown
		}
		*/
		double resizGraphWithMnistConstructorDuration = t.secondsSinceLastCall();
		System.out.println("In BlackHoleCortexWindow constructor, took "+resizGraphWithMnistConstructorDuration
			+" seconds for resizGraphWithMnistConstructorDuration.");
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
		JScrollPane scrollScreen = new JScrollPane(screen, v, h);
		add(scrollScreen);
		setSize(new Dimension(600,450));
		ScreenUtil.moveToScreenCenter(this);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
		double flatxypConstructorEnd = t.secondsSinceLastCall();
		System.out.println("In BlackHoleCortexWindow constructor, last part took "+flatxypConstructorEnd+" seconds.");
		System.out.println("RBM maxConnectedPairs = "+screen.rbm.maxConnectedPairs()
			+" nodesWithoutUpDownDuplicates="+screen.rbm.nodesWithoutUpDownDuplicates()
			+" edgesIncludingBidirectionalDuplicates="+screen.rbm.edgesIncludingBidirectionalDuplicates());
		Polydim trainingData = BoltzUtil.getDefaultSmallTrainingData16x16Images();
		//Polydim trainingData = BoltzUtil.getAllTrainingDataForBaseTenDigit((byte)7);
		Bits b = Fast0To16Bits.EMPTY;
		int imageCount = 20;
		for(int i=0; i<imageCount; i++){
			b = b.cat(trainingData.bits(i).data());
		}
		trainingData = new SimplePolydim(b, imageCount, trainingData.dimSize(1), trainingData.dimSize(2));
		double learnRatePerAnnealCycle = .01;
		RealtimeScheduler.start(screen);
		BoltzUtil.learnMany(screen.rbm, anneal, MathUtil.weakRand, trainingData, learnRatePerAnnealCycle);
		screen.refreshOnlyNoPredict = false;
	}
	
	/** Creates random edges between each 2 adjacent rows *
	public BlackHoleCortexWindow(int nodesX, int nodesY, Random rand){
		this(nodesX, nodesY);
		"TODO alternate layers"
		FlatYX pixelNodes = screen.data;
		for(int nodeY=0; nodeY<pixelNodes.ySize-1; nodeY++){
			for(int nodeXa=0; nodeXa<pixelNodes.xSize; nodeXa++){
				for(int nodeXb=0; nodeXb<pixelNodes.xSize; nodeXb++){
					Neuron nodeA = pixelNodes.yx[nodeY][nodeXa];
					Neuron nodeB = pixelNodes.yx[nodeY+1][nodeXb];
					double weight = rand.nextGaussian()*.1;
					nodeA.setWeightFrom(nodeB, weight);
					nodeB.setWeightFrom(nodeA, weight);
				}
			}
		}
	}*/
	
	static volatile boolean started;
	
	public static void main(String args[]) throws Exception{
		if(started) return;
		started = true;
		Nanotimer t = new Nanotimer();
		PluginLoader.loadFirstPlugins(); //call this once when system starts
		double jsmDuration = t.secondsSinceLastCall();
		System.out.println("Took "+jsmDuration+" seconds to load jselfmodify.");
		//new BlackHoleCortexWindow(32, 32, CoreUtil.strongRand);
		//new BlackHoleCortexWindow(1024, 1024); //sparse network size 2^20 with scrolling
		BlackHoleCortexWindowForAllAtOnceLearning window = new BlackHoleCortexWindowForAllAtOnceLearning(256, 256); //sparse network size 2^16 with scrolling
	}

}
