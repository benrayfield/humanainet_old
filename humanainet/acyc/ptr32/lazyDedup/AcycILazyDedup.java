/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32.lazyDedup;
import java.util.Arrays;
import humanainet.acyc.Acyc;
import humanainet.acyc.DedupType;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.common.MathUtil;
import humanainet.common.Nanotimer;

/** AcycI means 32 bit pointers.
TODO make some funcs or the whole class final for speed? Test speed first.
*/
public class AcycILazyDedup implements Acyc32{
	
	public final short bitsPerPointer;
	
	public static final byte myMinBitsPerPointer = 3, myMaxBitsPerPointer = 30;
	
	private final long pairs[];
	private int pairsSize;
	
	public AcycILazyDedup(int bitsPerPointer){
		this.bitsPerPointer = (short) bitsPerPointer;
		if(bitsPerPointer < myMinBitsPerPointer || myMaxBitsPerPointer < bitsPerPointer){
			throw new RuntimeException("bitsPerPointer="+bitsPerPointer);
		}
		pairs = new long[1 << bitsPerPointer];
		addRequiredConstants();
	}
	
	protected void addRequiredConstants(){
		int lastPtr = pairs.length-1;
		pairs[0] = (lastPtr<<32) | lastPtr;
		pairsSize++;
	}
	
	public DedupType dedupType(){ return DedupType.fastArrayWithDup; }

	public short bitsPerPointer(){ return bitsPerPointer; }

	public Acyc garbageCollect() {
		throw new RuntimeException("TODO");
	}

	public boolean pointerExists(int x){
		return 0 < x && x < pairsSize;
	}

	public int pair(int x, int y){
		int ptr = pairsSize++;
		pairs[ptr] = (x<<32)|y;
		return ptr;
		
	}

	public int left(int x){
		return (int)(pairs[x]>>>x);
	}

	public int right(int x){
		return (int)pairs[x];
	}

	public int size(){ return pairsSize; }

	public int capacity(){ return pairs.length; }
	
	public void clear(){
		//Arrays.fill(pairs, -1);
		pairsSize = 0;
		addRequiredConstants();
	}

	public int[] pointers(int fromInclusive, int toExclusive){
		fromInclusive = Math.max(0, fromInclusive);
		toExclusive = Math.min(pairs.length, toExclusive);
		int siz = toExclusive-fromInclusive;
		int ptrs[] = new int[siz];
		for(int i=0; i<siz; i++){
			ptrs[i] = fromInclusive+i;
		}
		return ptrs;
	}
	
	public static void main(String args[]){
		System.out.println("Speed test of "+AcycILazyDedup.class.getName()+" without any dedup, just creating pairs as fast as possible. TODO test it sequentially first then random index pairs.");
		Acyc32 acyc = new AcycILazyDedup(16);
		int siz = acyc.capacity();
		Nanotimer t = new Nanotimer();
		int repeats = 1 << 16;
		//int repeats = 1 << 0;
		int arbitrarySum = 0;
		for(int r=0; r<repeats; r++){
			int secondLast = 0;
			int last = acyc.pair(secondLast, secondLast);
			for(int i=acyc.size(); i<siz; i++){
				int left = secondLast;
				int right = last;
				int p = acyc.pair(secondLast, right);
				secondLast = last;
				last = p;
				arbitrarySum += p+acyc.left(right)+acyc.right(left);
			}
			acyc.clear();
		}
		double seconds = t.secondsSinceLastCall();
		long cycles = (long)repeats*siz;
		System.out.println("Using small array to test level 1 cache, and linear order: cycles="+cycles+" seconds="+seconds+" cyclesPerSecond="+cycles/seconds+" arbitrarySum="+arbitrarySum);
		long pointers[] = new long[siz];
		pointers[0] = 0;
		for(int i=1; i<siz; i++){
			int left = MathUtil.strongRand.nextInt(i);
			int right = MathUtil.strongRand.nextInt(i);
			pointers[i] = (left<<32)|right;
		}
		System.out.println("next...");
		t.secondsSinceLastCall();
		arbitrarySum = 0;
		for(int r=0; r<repeats; r++){
			for(int i=acyc.size(); i<siz; i++){
				long g = pointers[i];
				int left = (int)(g>>32);
				int right = (int)g;
				int p = acyc.pair(left, right);
				arbitrarySum += p+acyc.left(right)+acyc.right(left);
			}
			acyc.clear();
		}
		seconds = t.secondsSinceLastCall();
		System.out.println("Using small array to test level 1 cache, and random order: cycles="+cycles+" seconds="+seconds+" cyclesPerSecond="+cycles/seconds+" arbitrarySum="+arbitrarySum);
	}

}