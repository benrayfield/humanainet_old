/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class MathUtil{
	
	public static final Random weakRand;
	public static final SecureRandom strongRand;
	static{
		strongRand = new SecureRandom();
		//TODO set seed as bigger byte array
		strongRand.setSeed(3+System.nanoTime()*49999+System.currentTimeMillis()*new Object().hashCode());
		weakRand = new Random(strongRand.nextLong());
	}
	
	public static double sigmoid(double x){
		return 1/(1+Math.exp(-x));
	}
	
	public static final double veryPositive = Double.MAX_VALUE/0x1000000;
	
	public static final double veryNegative = -veryPositive;
	
	/** s = 1/(1+e^-x).
	s*(1+e^-x) = 1.
	1+e^-x = 1/s.
	e^-x = 1/s - 1.
	-x = logBaseE(1/s - 1).
	x = -logBaseE(1/s - 1).
	*/
	public static double inverseSigmoid(double fraction){
		//x = -logBaseE(1/s - 1)
		//if(s == 0) return .5; //TODO verify this is on the curve
		if(fraction == 0) return veryNegative; //TODO verify this is on the curve
		double inverseS = 1/fraction;
		return -Math.log(inverseS - 1);
	}
	
	public static void main(String args[]){
		testSigmoidAndItsInverse();
		testWeightedRandomBit();
	}
	
	public static void testWeightedRandomBit(){
		System.out.print("Testing weightRandomBit...");
		for(double targetChance=0; targetChance<1; targetChance+=.03){
			int countZeros = 0, countOnes = 0;
			for(int i=0; i<100000; i++){
				if(weightedRandomBit(targetChance,strongRand)) countOnes++;
				else countZeros++;
			}
			double observedChance = (double)countOnes/(countZeros+countOnes);
			System.out.println("targetChance="+targetChance+" observedChance="+observedChance);
			if(Math.abs(targetChance-observedChance) > .01) throw new RuntimeException("targetChance too far from observedChance");
		}
	}
		
	public static void testSigmoidAndItsInverse(){
		System.out.println("Testing sigmoid and inverseSigmoid");
		double epsilon = 1e-12;
		for(double s=0; s<=1; s+=.01){
			double x = inverseSigmoid(s);
			double ss = sigmoid(x);
			System.out.println("s="+s+" x="+x+" ss="+ss);
			if(Math.abs(s-ss) > epsilon) throw new RuntimeException("s != ss and is not close");
		}	
	}
	
	/** Uses SecureRandom and only an average of 2 random bits from it */
	public static boolean weightedRandomBit(double chance){
		return weightedRandomBit(chance, strongRand);
	}
	
	/** Consumes an average of 2 random bits (so its practical to use SecureRandom which is slow)
	by consuming random bits until get the first 1 then going directly to that digit
	in the chance as a binary fraction and returning it as the weighted random bit observe.
	TODO I wrote that code somewhere, copy it here so its more practical more often to use SecureRandom.
	*/
	public static boolean weightedRandomBit(double chance, Random rand){
		if(chance < 0 || 1 < chance) throw new ArithmeticException("chance="+chance);
		while(rand.nextBoolean()){
			if(.5 <= chance) chance -= .5;
			chance *= 2;
		}
		return .5 <= chance;
	}
	
	public static boolean isPowerOf2(int i){
		return i>0 && (i&(i-1)) == 0;
	}
	
	public static void normBySortedPointers(double min, double max, double d[]){
		int siz = d.length;
		int pointers[] = sortedPointersInto(d);
		double range = max-min;
		for(int i=0; i<siz; i++){
			double fraction = (double)i/(siz-1);
			d[pointers[i]] = min+fraction*range;
		}
	}
	
	public static int[] sortedPointersInto(double d[]){
		return sortedPointersInto_tryingToImproveSpeed(d);
	}
	
	public static strictfp int[] sortedPointersInto(final long d[]){
		Integer Ints[] = new Integer[d.length];
		for(int i=0; i<d.length; i++) Ints[i] = i;
		Comparator<Integer> compare = new Comparator<Integer>(){
			public int compare(Integer x, Integer y){
				long xd = d[x], yd = d[y];
				if(xd < yd) return -1;
				if(xd > yd) return 1;
				return 0;
			}
		};
		Arrays.sort(Ints, compare);
		int ints[] = new int[d.length];
		for(int i=0; i<d.length; i++) ints[i] = Ints[i];
		return ints;
	}
	
	public static int[] sortedPointersInto_tryingToImproveSpeed(final double d[]){
		/*int pointers[] = new int[d.length];
		for(int i=0; i<d.length; i++) pointers[i] = i;
		//TODO? Arrays.parallelSort(arg0);
		*/
		
		for(int i=0; i<d.length; i++){
			double x = d[i];
			if(x != x){ //NaN, because it may be causing sorting inconsistency
				d[i] = Double.MAX_VALUE;
			}
		}
		
		Integer Ints[] = new Integer[d.length];
		for(int i=0; i<d.length; i++) Ints[i] = d.length-1-i;
		Comparator<Integer> compare = new Comparator<Integer>(){
			public int compare(Integer x, Integer y){
				double xd = d[x], yd = d[y];
				if(xd < yd) return -1;
				if(xd > yd) return 1;
				return 0;
			}
		};
		/*while(true){
			try{
				Arrays.sort(Ints, compare);
				break;
			}catch(Exception e){
				System.out.println("This is probably 'Comparison method violates its general contract' which strictfp avoids always singlethreaded but it appears some thread is using it, but which one could it be since its a local var? For now, since it happens only 1 20000 times its faster to just catch this and do it again those times. TODO find that thread and synchronize here and there! "+e.getMessage());
				e.printStackTrace(System.out);
			}
		}*/
		Arrays.sort(Ints, compare);
		int ints[] = new int[d.length];
		for(int i=0; i<d.length; i++) ints[i] = Ints[i];
		return ints;
	}
	
	/** Fast because it leaves it the complexity of NaN and positive/negative zero.
	TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM?
	*/
	public static double max(double x, double y){
		return x>y ? x : y;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static float max(float x, float y){
		return x>y ? x : y;
	}
	
	/** Fast because it leaves it the complexity of NaN and positive/negative zero.
	TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM?
	*/
	public static double min(double x, double y){
		return x<y ? x : y;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static float min(float x, float y){
		return x<y ? x : y;
	}
	
	/** Same as max(minValue, min(value, maxValue))
	TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM?
	*/
	public static double holdInRange(double min, double value, double max){
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static float holdInRange(float min, float value, float max){
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static int holdInRange(int min, int value, int max){
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}

}
