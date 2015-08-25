package humanainet.blackholecortex.ui;
import java.awt.Rectangle;

import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.common.Nanotimer;
import humanainet.datasetsForAI.mnistocrdataset.MnistOcrDataset;
import humanainet.jselfmodify.PluginLoader;
import humanainet.wavetree.bit.Bits;

public class TestBitsSpeed{
	
	public static void main(String args[]) throws Exception{
		Nanotimer t = new Nanotimer();
		PluginLoader.loadFirstPlugins(); //call this once when system starts
		double jsmDuration = t.secondsSinceLastCall();
		System.out.println("Took "+jsmDuration+" seconds to load jselfmodify.");
		/*
		//new BlackHoleCortexWindow(32, 32, CoreUtil.strongRand);
		//new BlackHoleCortexWindow(1024, 1024); //sparse network size 2^20 with scrolling
		//BlackHoleCortexWindow window = new BlackHoleCortexWindow(256, 256); //sparse network size 2^16 with scrolling
		double bhcDuration = t.secondsSinceLastCall();
		System.out.println("Took "+bhcDuration+" seconds to load BlackHoleCortexWindow after jselfmodify.");
		System.out.println("TODO Use "+NodeScreen.class.getMethod("nodes", Rectangle.class)
			+" to explore boltzmann learning algorithms which use layers or groups of nodes, especially restricted/layered boltzmann machine (RBM) with extra layer hanging off from each layer (except 1 at end) as short term memory. Each of those groups will be rectangle of magnified pixels on screen, and when mouseover (maybe with some button combination) them their weights light up other places on screen.");
		*/
		double totalDuration = t.secondsSinceStart();
		System.out.println("Total boot duration is "+totalDuration+" seconds");
		//Bits trainingDataBits = ((ResizGraphWithMnist)window.screen).mainDataset.data();
		//Bits trainingDataBits = window.screen.mainDataset.data();
		Bits trainingDataBits = BoltzUtil.getDefaultSmallTrainingData16x16Images().data();
		long tSize = trainingDataBits.siz();
		long ones = 0;
		long zeros = 0;
		Nanotimer tt = new Nanotimer();
		for(int repeat=0; repeat<20; repeat++){
			for(long g=0; g<tSize; g++){
				if(trainingDataBits.bitAt(g)) ones++;
				else zeros++;
			}
			double duration = tt.secondsSinceLastCall();
			System.out.println("Read bitAt at rate of "+(tSize/duration)+" per second.");
		}
		long byteSum = 0;
		for(int repeat=0; repeat<20; repeat++){
			long end = tSize-7;
			for(long g=0; g<end; g++){
				byteSum += trainingDataBits.byteAt(g);
			}
			double duration = tt.secondsSinceLastCall();
			System.out.println("Read byteAt at rate of "+(end/duration)+" per second.");
		}
		long intSum = 0;
		for(int repeat=0; repeat<20; repeat++){
			long end = tSize-32;
			for(long g=0; g<end; g++){
				byteSum += trainingDataBits.intAt(g);
			}
			double duration = tt.secondsSinceLastCall();
			System.out.println("Read intAt at rate of "+(end/duration)+" per second.");
		}
		long bitsInRangeSum = 0;
		long range = 125077;
		for(int repeat=0; repeat<20; repeat++){
			long end = tSize-(range-1);
			for(long g=0; g<end; g++){
				bitsInRangeSum += trainingDataBits.sub(g,g+range).ones();
			}
			double duration = tt.secondsSinceLastCall();
			System.out.println("Read bits in range (size "+range+") at rate of "+(end/duration)+" ranges read per second.");
		}
	}

}
