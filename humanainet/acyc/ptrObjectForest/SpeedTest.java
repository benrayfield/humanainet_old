/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;

public class SpeedTest{
	
	public static final Pair pairs[] = new Pair[1 << 24];
	public static int pairsSize = 0;
	public static final Map<Long,Pair> map = new HashMap();
	
	public static void main(String args[]){
		pairs[0] = new Pair(0, null, null); //nil
		pairsSize++;

		int cycles = 1000000;
		Random rand = MathUtil.weakRand;
		Nanotimer t = new Nanotimer();
		for(int i=0; i<cycles; i++){
			int x = rand.nextInt(pairsSize);
			int y = rand.nextInt(pairsSize);
			pair(pairs[x], pairs[y]);
		}
		double perSecond = cycles/t.secondsSinceLastCall();
		System.out.println("pairs per second (could be improved by not reading from random index in array): "+perSecond);
	}
	
	public static Pair pair(Pair left, Pair right){
		Long g = (((long)left.id)<<32) | right.id;
		Pair p = map.get(g);
		if(p == null){
			p = new Pair(pairsSize, left, right);
			pairs[pairsSize++] = p;
			map.put(g, p);
		}
		return p;
	}

}
