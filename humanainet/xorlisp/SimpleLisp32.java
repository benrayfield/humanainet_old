/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;
import java.util.HashMap;
import java.util.Map;

import humanainet.acyc.DedupType;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.acyc.ptrObjectForest.ptrHashCons.HashCons;

public class SimpleLisp32 implements XorlispVM32{
	
	//TODO since no lisp funcs are implemented here, should this
	//just be an Acyc32 instead of specificly an XorlispVM32?
	
	public final int pointerMask;
	
	public final long pairOfPointersMask;
	
	public final short bitsPerPointer;
	public short bitsPerPointer(){ return bitsPerPointer; }
	
	public static final byte myMinBitsPerPointer = 3, myMaxBitsPerPointer = 30;
	
	public SimpleLisp32(byte bitsPerPointer){
		this.bitsPerPointer = bitsPerPointer;
		if(bitsPerPointer < myMinBitsPerPointer || myMaxBitsPerPointer < bitsPerPointer){
			throw new RuntimeException("bitsPerAddress="+bitsPerPointer);
		}
		pointerMask = (1<<bitsPerPointer)-1;
		pairOfPointersMask = pointerMask<<32 | pointerMask;
		acyclicNet = new long[1<<bitsPerPointer]; //starts full of end, which is 0=pair(-1,-1).
		lockAddress = new boolean[acyclicNet.length];
		//height = new int[acyclicNet.length];
		acyclicNet[0] = ((-1L)<<32) | (-1L);
		consToAddress.put(acyclicNet[0],0);
		end = 0; //Same order as in Const...
		size = 1;
		pairOfEnd = pair(end,end);
		bit0 = pair(end,pairOfEnd);
		bit1 = pair(pairOfEnd,end);
		typVal = pair(pairOfEnd,pairOfEnd);
		lockAddress(end);
		lockAddress(pairOfEnd);
		lockAddress(bit0);
		lockAddress(bit1);
		lockAddress(typVal);
	}
	
	protected int size;
	public int size(){ return size; }
	public int capacity(){ return acyclicNet.length; }
	
	public final int end, pairOfEnd, bit0, bit1, typVal;
	
	/** TODO each address has car and cdr, or use 2 even/odd address for that? */
	protected final long acyclicNet[];
	
	/** true when an address is locked so cant be moved or garbageCollected.
	One use of this is by LispCache32. Without counting references (maybe TODO in later version)
	there is no way to know when to unlock it.
	*/
	protected final boolean lockAddress[];
	public void lockAddress(int pointer){ lockAddress[pointer&pointerMask] = true; }
	
	//protected final int height[];
	//public int height(int pair){ return height[pair]; }
	
	protected HashMap<Long,Integer> consToAddress = new HashMap();
	
	/** Some local address, in this case bitsPerPointer bits each,
	are paired with a global address which is a secureHash of 2 other secureHash,
	all the way down to all bit0 meaning ()/nil. 
	*/
	protected HashMap<Integer,HashCons> localToGlobalAddress = new HashMap();
	
	public DedupType dedupType(){ return DedupType.localMap; }
	
	public boolean pointerExists(int x){
		return 0 < x && x < size;
	}

	public int pair(int x, int y){
		x &= pointerMask;
		y &= pointerMask;
		//TODO use bitmask
		//long cons = acyclicNet[x]<<32 | acyclicNet[y];
		long pair = ((long)x)<<32 | y;
		Integer address = consToAddress.get(pair);
		int addr;
		if(address == null){
			if(size < acyclicNet.length){
				addr = size; //TODO check size
				acyclicNet[size] = pair;
				//int xh = height[x], yh = height[y];
				//height[size] = xh>yh ? xh : yh; 
				consToAddress.put(pair, addr);
				size++;
			}else{
				throw new IndexOutOfBoundsException("This xorlispvm is full. Need to garbageCollect and use the xorlispvm it returns which will have different addresses but the same object network. Or if its already densely packed it may be time to expand to bigger addresses.");
			}
		}else{
			addr = address;
		}
		return addr;
	}

	public int left(int x){
		return (int)(acyclicNet[x&pointerMask] >> 32); //TODO which kind of bit shift?
	}

	public int right(int x) {
		return (int)acyclicNet[x&pointerMask];
	}
	
	public XorlispVM garbageCollect(){
		throw new RuntimeException("TODO start at 0 which all objects are derived from and map the addresses between the old and the new vms");
	}
	
	public static void main(String[] args){
		SimpleLisp32 L = new SimpleLisp32((byte)5);
		System.out.println(TextUtil32.toString(L, 0));
		System.out.println(TextUtil32.toString(L, 1));
		System.out.println(TextUtil32.toString(L, 2));
		System.out.println(TextUtil32.toString(L, 3));
		System.out.println(L.pair(0,0));
		int x;
		System.out.println(TextUtil32.toString(L, (x=L.pair(L.pair(0,0),0)))+"@"+x);
		System.out.println(TextUtil32.toString(L, L.pair( L.pair(L.pair(0,0),0), L.pair(L.pair(0,0),0) ) ));
		
		System.out.println("My progress, which I think is nearly complete on the continuations (using state as top of stack and a reverse order of pairing to form a queue) is in these code files which you should look at: Nav32.java, Execute.java. While I dont have all the details together, I am confident that the fork code in Nav32 will work statelessly and allow many people to run a debugger on the debugging of eachothers debugging (debuggers like eclipse/netbeans) since even the state of navigating some other state is entirely stateless. No java variables are used outside the XorlispVM's pair objects in an acyclicNet where everything is derived from ()/nil/end. This system is going to be really fast once I replace the use of Java HashMap with a pure long/int hashtable to avoid the use of Long and Integer objects. But already, the continuations are great progress... I'm confident that code works and will be demonstrated with the other pieces are assembled.");
	}
	public int[] pointers(int fromInclusive, int toExclusive){
		throw new RuntimeException("TODO");
	}

	public void clear(){
		throw new RuntimeException("TODO");
	}

}
