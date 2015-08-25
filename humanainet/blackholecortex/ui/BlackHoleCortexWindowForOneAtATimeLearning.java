package humanainet.blackholecortex.ui;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.Fast0To16Bits;
import humanainet.wavetree.bit.object.Polydim;
import humanainet.wavetree.bit.object.SimplePolydim;
import humanainet.jselfmodify.PluginLoader;
import humanainet.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.anneal.SimpleAnnealStrategy;
import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatPYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.TriMem;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;
import humanainet.common.ScreenUtil;
import humanainet.datasetsForAI.mnistocrdataset.MnistOcrDataset;
import humanainet.datasetsForAI.rectanglesdataset.WrappedRectanglesDataset16x16;

/** Unlike BlackHoleCortexWindowForAllAtOnceLearning for one big "batch learning",
this BlackHoleCortexWindowForOneAtATimeLearning is for "online learning",
using batch size of 1, and learning each next data point 1 at a time.
This is where I'm exploring annealing strategies and other rbm parameters,
especially ways to get "online learning" to work.
This is about how much of previous learning can be kept without having
to relearn it later after enough new datapoints are learned.
*/
public class BlackHoleCortexWindowForOneAtATimeLearning extends JFrame{
	
	public final RbmOnScreen screen;
	
	protected final boolean learnAllAtOnce = false;
	
	public BlackHoleCortexWindowForOneAtATimeLearning(int nodesX, int nodesY){
		super("BlackHoleCortex TODO_batchSize1 (opensource GNU GPL 2+, unzip this jar file to get source code)");
		Nanotimer t = new Nanotimer();
		//2 layers for RBM, 1 for benfrayfieldResearch.statsysPaintTool
		FlatPYX pixelNodes = new FlatPYX(3, nodesY, nodesX);
		double flatxypConstructorDuration = t.secondsSinceLastCall();
		System.out.println("In BlackHoleCortexWindow constructor, took "
			+flatxypConstructorDuration
			+" seconds for flatxypConstructorDuration.");
		
		Polydim images = BoltzUtil.getDefaultSmallTrainingData16x16Images();
		int imageHeight = (int) images.dimSize(1);
		int imageWidth = (int) images.dimSize(2);
		int startX = 0, startY = 0;
		AnnealStrategy anneal = new SimpleAnnealStrategy(200, 50, 1);
		screen = new RbmOnScreen(anneal, pixelNodes, 8, startX, startY, imageWidth, imageHeight);
		double startWeightsAve = 0;
		double startWeightsStdDev = .01;
		screen.rbm.randomizeWeights(startWeightsAve, startWeightsStdDev, MathUtil.strongRand);
		/*for(Neuron n : screen.rbm.mainLayers[0].fromUp){
			n.bit = CoreUtil.strongRand.nextBoolean();
			n.influence = n.bit ? 1 : 0;
		}*/
		for(RbmLayer rLayer : screen.rbm.combinedLayers){
			for(WeightsNode n : rLayer.fromUp){
				n.bit = MathUtil.strongRand.nextBoolean();
				//n.bit = true;
				n.scalar = n.bit ? 1 : 0;
				
				//n.addToWeight = 1;
				//n.addToWeight = -1;
			}
		}
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
		Polydim trainingData = trainingData();
		
		
		//double learnRatePerAnnealCycle = .01;
		double learnRatePerAnnealCycle = .1;
		
		
		RealtimeScheduler.start(screen);
		
		//TODO turn down incrementModsAndMakeBidirectionalIfNeeded if norming in a symmetric way, since its almost as slow as learning again to do that
		
		//TODO normWeightsToCenteredHypersphere
		//TODO normWeightsByBellCurve

		final List<WeightsNode> rbmNodes = screen.rbm.nodesInNewMutableList();

		System.out.println("learnAllAtOnce="+learnAllAtOnce);
		if(learnAllAtOnce){
			BoltzUtil.learnMany(screen.rbm, anneal, MathUtil.weakRand, trainingData, learnRatePerAnnealCycle);
		}else{
			//learn one at a time instead. Need to adjust AnnealStrategy.cyclesPerLearnOne().
			RbmData rbm = screen.rbm;
			//Random rand = CoreUtil.weakRand;
			Random rand = MathUtil.strongRand;
			for(long whichImage=0; whichImage<trainingData.dimSize(0); whichImage++){
				Polydim image = trainingData.bits(whichImage);
				Bits imageBits = image.data();
				//TriMem mem = new TriMem(screen.rbm, Fast0To16Bits.EMPTY, imageBits);
				//BoltzUtil.think(mem, learnRatePerAnnealCycle, true, rand)
				
				int cycles = anneal.cyclesPerLearnOne(); // 1.5 cycles (up down up) per cycle
				for(int c=0; c<cycles; c++){
					double temperature = anneal.temperature(c, cycles);
					BoltzUtil.setVisibleNodesScalarsAndBits(rbm, imageBits);
					BoltzUtil.learnFromCurrentThoughtManualCyclesAndTemperature(
						rbm, learnRatePerAnnealCycle, true, rand, 2, temperature);
					
					/*for(Neuron n : rbmNodes){
						//n.normWeightsToCenteredHypersphere(1);
						*TODO add Random param to choose random vector of that radius
						at blackholecortex.WeightsNode.normWeightsToHypersphere(WeightsNode.java:168)
						at blackholecortex.WeightsNode.normWeightsToCenteredHypersphere(WeightsNode.java:184)
						*
						
						n.normWeightsToBellCurve(0, .1);
					}*/
					
					//TODO continue until its learned? Could run predict mode to verify
					System.out.println("Done cycle "+c+" of "+cycles+" cycles for image "+whichImage+" of "+trainingData.dimSize(0)+" images.");
				}
			}
		}
		
		screen.refreshOnlyNoPredict = false;
	}
	
	protected static Polydim trainingData(){
		/*
		//Polydim trainingData = BoltzUtil.getDefaultSmallTrainingData16x16Images();
		Polydim trainingData = BoltzUtil.getDefaultSmallTrainingData16x16Images(20);
		//trainingData = MnistOcrDataset.getAllOfDigitFrom16x16(trainingData, (byte)8);
		//Bits b = Fast0To16Bits.EMPTY;
		//int imageCount = 20;
		//int imageCount = 20;
		//for(int i=0; i<imageCount; i++){
		//	b = b.cat(trainingData.bits(i).data());
		//}
		//return new SimplePolydim(b, imageCount, trainingData.dimSize(1), trainingData.dimSize(2));
		return trainingData;
		*/
		
		
		Polydim p = WrappedRectanglesDataset16x16.dataset4OnRowsEach();
		p = BoltzUtil.shuffleFirstDim(p, MathUtil.strongRand);
		return p;
		
		
		/*
		Polydim p = WrappedRectanglesDataset16x16.datasetRectFromEachPoint(4, 4);
		//p = BoltzUtil.shuffleFirstDim(p, CoreUtil.strongRand);
		return p;
		*/
	}
	
	static volatile boolean started;
	
	public static void main(String args[]) throws Exception{
		if(started) return;
		started = true;
		Nanotimer t = new Nanotimer();
		PluginLoader.loadFirstPlugins(); //call this once when system starts
		double jsmDuration = t.secondsSinceLastCall();
		System.out.println("Took "+jsmDuration+" seconds to load jselfmodify.");
		//sparse network size 2^16 with scrolling
		BlackHoleCortexWindowForOneAtATimeLearning window = new BlackHoleCortexWindowForOneAtATimeLearning(256, 256);
	}

}
