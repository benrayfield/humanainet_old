/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;

public class SpeedTestSimpleJIMap{

	public static void main(String args[]){
		JIMap map = new SimpleJIMap(MathUtil.strongRand, 23);
		System.out.println("Starting speed test of "+SimpleJIMap.class.getName());
		int cycles = 4000000;
		Nanotimer t = new Nanotimer();
		long arbitrarySum = 0;
		for(int i=0; i<cycles; i++){
			long key = i*17L + i*234324232525L;
			int value = i >> 3;
	    	//System.out.println("Putting key="+key+" value="+value);
			arbitrarySum += key+value;
			map.put(key, value);
			if((i&0xffff) == 0) System.out.println("i="+i+" key-"+key+" value="+value);
		}
		double cyclesPerSecond = cycles/t.secondsSinceLastCall();
		System.out.println("arbitrarySum="+arbitrarySum);
		System.out.println("JIMap.put's per second: "+cyclesPerSecond);
	}

}
