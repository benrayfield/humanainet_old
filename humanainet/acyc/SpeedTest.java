/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;
import java.io.IOException;
import java.util.Random;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.alwaysDedup.SimpleEconDAcycI;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;

public class SpeedTest{
	
	public static void main(String args[]) throws Exception{
		humanainet.start.Start.main(args);
		Acyc32 acyc = Glo.econacyc;
		Random rand = MathUtil.weakRand;
		int cycles = 1000000;
		System.out.println("Push enter to start speed test...");
		System.in.read();
		System.out.println("Starting speed test");
		Nanotimer t = new Nanotimer();
		int startSize = acyc.size();
		long arbitrarySum = 0;
		for(int i=0; i<cycles; i++){
			int x = rand.nextInt(startSize), y = rand.nextInt(startSize);
			arbitrarySum += acyc.pair(x, y);
		}
		double seconds = t.secondsSinceLastCall();
		double cyclesPerSecond = cycles/seconds;
		System.out.println("created random pairs per second: "+cyclesPerSecond);
		long sumOfLefts = 0;
		for(int i=0; i<cycles; i++){
			int x = rand.nextInt(startSize);
			sumOfLefts += acyc.left(x);
		}
		seconds = t.secondsSinceLastCall();
		double leftsPerSecond = cycles/seconds;
		System.out.println("called left, times per second: "+leftsPerSecond+" arbitrarySum="+sumOfLefts);
		long sumOfRights = 0;
		for(int i=0; i<cycles; i++){
			int x = rand.nextInt(startSize);
			sumOfRights += acyc.right(x);
		}
		seconds = t.secondsSinceLastCall();
		double rightsPerSecond = cycles/seconds;
		System.out.println("called right, times per second: "+rightsPerSecond+" arbitrarySum="+sumOfRights);
		for(int i=0; i<cycles; i++){
			int x = rand.nextInt(startSize);
		}
		seconds = t.secondsSinceLastCall();
		System.out.println("Loop without any acyc calls (just random int), times per second: "+cycles/seconds);
		System.out.println("aveCyclesPerPut="+((SimpleEconDAcycI)Glo.econacyc).aveCyclesPerPut());
		System.out.println("nodes = "+Glo.econacyc.size());
	}

}
