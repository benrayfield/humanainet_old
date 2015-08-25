/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Random;

/** More core to the software than datastruct package, the core datastructs and interfaces etc. */
public class CoreUtil{
	
	public static String nextRandomNodeName(){
		char c[] = new char[8];
		for(int i=0; i<c.length; i++) c[i] = (char)('a'+MathUtil.strongRand.nextInt(26));
		return new String(c);
	}
	
	public static final double epsilon = 1e-12;
	
	public static final long startMillis;
	
	public static final long startNano;
	
	static{
		startMillis = System.currentTimeMillis();
		startNano = System.nanoTime();
	}
	
	/** Seconds since year 1970
	with relative nanosecond precision (System.nanoTime)
	and absolute few milliseconds precision (System.currentTimeMillis).
	<br><br>
	Practically, at least in normal computers in year 2011, this has about microsecond precision
	because you can only run it a few million times per second.
	TODO test it again on newer computers.
	*/
	public static double time(){
		//TODO optimize by caching the 2 start numbers into 1 double */
		long nanoDiff = System.nanoTime()-startNano;
		return .001*startMillis + 1e-9*nanoDiff; 
	}
	
	protected final DecimalFormat secondsFormat = new DecimalFormat();
	
	public static void testWeightedRandomBit(){
		System.out.print("Testing weightRandomBit...");
		for(double targetChance=0; targetChance<1; targetChance+=.03){
			int countZeros = 0, countOnes = 0;
			for(int i=0; i<100000; i++){
				if(MathUtil.weightedRandomBit(targetChance,MathUtil.strongRand)) countOnes++;
				else countZeros++;
			}
			double observedChance = (double)countOnes/(countZeros+countOnes);
			System.out.println("targetChance="+targetChance+" observedChance="+observedChance);
			if(Math.abs(targetChance-observedChance) > .01) throw new RuntimeException("targetChance too far from observedChance");
		}
	}
	
	public static byte[] bytes(InputStream in){
		System.out.println("Reading "+in);
		try{
			byte b[] = new byte[1];
			int avail;
			int totalBytesRead = 0;
			while((avail = in.available()) != 0){
				int maxInstantCapacityNeeded = totalBytesRead+avail;
				if(b.length < maxInstantCapacityNeeded){
					byte b2[] = new byte[maxInstantCapacityNeeded*2];
					System.arraycopy(b, 0, b2, 0, totalBytesRead);
					b = b2;
				}
				//System.out.println("totalBytesRead="+totalBytesRead+" avail="+avail);
				int instantBytesRead = in.read(b, totalBytesRead, avail);
				if(instantBytesRead > 0) totalBytesRead += instantBytesRead; //last is -1
			}
			byte b2[] = new byte[totalBytesRead];
			System.arraycopy(b, 0, b2, 0, totalBytesRead);
			return b2;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static int readIntBigendian(byte b[], int offset){
		return (b[offset]<<24)|(b[offset+1]<<16)|(b[offset+2]<<8)|b[offset+3]; 
	}

}