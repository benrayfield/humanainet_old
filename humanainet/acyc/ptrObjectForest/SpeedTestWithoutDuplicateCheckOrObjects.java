/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;
import humanainet.hash.hashtable.JIMap;
import humanainet.hash.hashtable.SimpleJIMap;

public class SpeedTestWithoutDuplicateCheckOrObjects{
	
	public static final int logBase2OfCapacity = 22;
	public static final long pairs[] = new long[1 << logBase2OfCapacity];
	public static int pairsSize = 0;
	//public static final Map<Long,Pair> map = new HashMap();
	public static final JIMap map = new SimpleJIMap(MathUtil.strongRand, logBase2OfCapacity+1);
	
	public static void main(String args[]){
		pairs[0] = 0; //nil
		pairsSize++;

		int cycles = 1000000;
		Random rand = MathUtil.weakRand;
		int[] randInts = new int[cycles*2];
		randInts[0] = 0;
		randInts[1] = 0;
		for(int i=2; i<randInts.length; i++){
			randInts[i] = rand.nextInt(i/2);
		}
		
		Nanotimer t = new Nanotimer();
		long arbitrarySum = 0;
		for(int i=0; i<cycles; i++){
			//int x = rand.nextInt(pairsSize);
			//int y = rand.nextInt(pairsSize);
			int x = randInts[2*i];
			int y = randInts[2*i+1];
			//pair(pairs[x], pairs[y]);
			arbitrarySum += pair(x,y);
		}
		double perSecond = cycles/t.secondsSinceLastCall();
		System.out.println("pairs per second (could be improved by not reading from random index in array): "+perSecond+" arbitrarySum="+arbitrarySum);
	}
	
	public static int pair(int left, int right){
		long pair = (((long)left)<<32) | right;
		int ptrToPair = map.get(pair);
		if(ptrToPair == -1){
			ptrToPair = pairsSize++;
			pairs[ptrToPair] = pair;
			map.put(pair, ptrToPair);
		}
		return ptrToPair;
		
		/*Long g = (((long)left.id)<<32) | right.id;
		Pair p = map.get(g);
		if(p == null){
			p = new Pair(pairsSize, left, right);
			pairs[pairsSize++] = p;
			map.put(g, p);
		}
		return p;
		*/
		/*
		int ptr = pairsSize;
		pairs[pairsSize++] = (((long)left)<<32) | right;
		return ptr;
		*/
	}

}
