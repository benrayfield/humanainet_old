/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32.alwaysDedup;
import java.util.HashMap;
import humanainet.acyc.Const;
import humanainet.acyc.DedupType;
import humanainet.acyc.ptr32.EconDAcycI;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.acyc.ptrObjectForest.ptrHashCons.HashCons;
import humanainet.xorlisp.XorlispVM;

/** The first prototype I'm experimenting with EconAcyc.
<br><br>
This class requires all pointers point lower in the array
so EconAcyc calculations are simpler.
TODO continuous garbageCollection using EconAcyc.
<br><br>
To avoid duplicate calculations, after this is working in Java,
create a version of this software in C and do garbageCollection
in this code instead of having it done automatically.
Its not just an optimization. Its a different way of garbageCollection
based on simple economics that rewards reuse of objects
and makes every object pay for the objects it points at
but only at cost not a for-profit model, more like physics.
*/
public class SimpleEconDAcycIWithHashMap implements EconDAcycI{
	
	public final int pointerMask;
	
	public final long pairOfPointersMask;
	
	public final short bitsPerPointer;
	public short bitsPerPointer(){ return bitsPerPointer; }
	
	public static final byte myMinBitsPerPointer = 3, myMaxBitsPerPointer = 30;
	
	public SimpleEconDAcycIWithHashMap(byte bitsPerPointer){
		this.bitsPerPointer = bitsPerPointer;
		if(bitsPerPointer < myMinBitsPerPointer || myMaxBitsPerPointer < bitsPerPointer){
			throw new RuntimeException("bitsPerAddress="+bitsPerPointer);
		}
		pointerMask = (1<<bitsPerPointer)-1;
		pairOfPointersMask = pointerMask<<32 | pointerMask;
		acyclicNet = new long[1<<bitsPerPointer]; //starts full of end, which is 0=pair(-1,-1).
		//refCountFromInside = new int[acyclicNet.length];
		//refCountFromOutside = new int[acyclicNet.length];
		pairsOfRefCount = new long[acyclicNet.length];
		cost = new double[acyclicNet.length];
		//lockAddress = new boolean[acyclicNet.length];
		//height = new int[acyclicNet.length];
		acyclicNet[0] = ((-1L)<<32) | (-1L);
		consToAddress.put(acyclicNet[0],0);
		int end = 0; //Same order as in Const...
		size = 1;
		int pairOfEnd = pair(end,end);
		int bit0 = pair(end,pairOfEnd);
		int bit1 = pair(pairOfEnd,end);
		int typVal = pair(pairOfEnd,pairOfEnd);
		if(end!=Const.end || pairOfEnd!=Const.pairOfEnd || bit0!=Const.bit0 || bit1!=Const.bit1 || typVal!=Const.typVal){
			throw new RuntimeException("Not match objects in Const class");
		}
		//Any pointer not from the array of pairs is outside. They're in Const class so lock them here.
		oneMoreRefFromOutside(Const.end);
		oneMoreRefFromOutside(Const.pairOfEnd);
		oneMoreRefFromOutside(Const.bit0);
		oneMoreRefFromOutside(Const.bit1);
		oneMoreRefFromOutside(Const.typVal);
	}
	
	protected int size;
	public int size(){ return size; }
	public int capacity(){ return acyclicNet.length; }
	
	//Use Const.end etc instead of: public final int end, pairOfEnd, bit0, bit1, typVal;
	
	/** TODO each address has car and cdr, or use 2 even/odd address for that? */
	protected final long acyclicNet[];
	
	/** Each long is 2 ints, an optimization for memory locality and comparing both to 0 at once.
	High 32 bits count from outside. Low 32 bits are count of pointers from inside the acyc,
	which is an optimization to use ++ and -- since internal pointers happen more often.
	Number of incoming pointers to each object. They split the cost equally.
	*/
	protected final long pairsOfRefCount[];
	
	public final int howManyRefsFromOutside(int pointer){
		long g = pairsOfRefCount[pointer&pointerMask];
		return (int)(g>>32);
	}
	
	public final int howManyRefsFromInside(int pointer){
		long g = pairsOfRefCount[pointer&pointerMask];
		return (int)g;
	}
	
	public final int howManyRefs(int pointer){
		long g = pairsOfRefCount[pointer&pointerMask];
		int refsFromOutside = (int)(g>>32);
		int refsFromInside = (int)g;
		return refsFromOutside+refsFromInside;
	}
	
	public final boolean hasRefFromOutside(int pointer){
		long g = pairsOfRefCount[pointer&pointerMask];
		return (g&0xffffffff00000000L) != 0;
	}
	
	public final boolean hasRefFromInside(int pointer){
		long g = pairsOfRefCount[pointer&pointerMask];
		return ((int)g) != 0;
	}
	
	public final boolean hasRef(int pointer){
		long g = pairsOfRefCount[pointer&pointerMask];
		return g != 0;
	}
	
	/** TODO this must be combined with the object referencing that pointer,
	and if its an external object, an address must be allocated
	(starting from high addresses and going down) which represents that pointer.
	*/
	public final void oneMoreRefFromInside(int pointer){
		pairsOfRefCount[pointer&pointerMask]++; //low 32 bits
	}
	
	/** TODO see comment in oneMoreRef */
	public final void oneLessRefFromInside(int pointer){
		pairsOfRefCount[pointer&pointerMask]--; //low 32 bits
	}
	
	public final void oneMoreRefFromOutside(int pointer){
		pairsOfRefCount[pointer&pointerMask] += 0x100000000L; //high 32 bits
	}
	
	/** TODO see comment in oneMoreRef */
	public final void oneLessRefFromOutside(int pointer){
		pairsOfRefCount[pointer&pointerMask] -= 0x100000000L; //high 32 bits
	}
	
	/** The econ part of EconAcyc. Incoming pointers must each pay an equal fraction
	of this summed into their cost, for each of their 2 points, plus 1 for
	being a separate lispPair.
	*/
	protected final double cost[];
	public final double objectCost(int pointer){
		return cost[pointer&pointerMask];
	}
	
	/** true when an address is locked so cant be moved or garbageCollected.
	One use of this is by LispCache32. Without counting references (maybe TODO in later version)
	there is no way to know when to unlock it.
	*
	protected final boolean lockAddress[];
	public void lockAddress(int pointer){ lockAddress[pointer&pointerMask] = true; }
	*/
	
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
				oneMoreRefFromInside(x);
				oneMoreRefFromInside(y);
				//refCountFromInside[x]++;
				//refCountFromInside[y]++;
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

	public int right(int x){
		return (int)acyclicNet[x&pointerMask];
	}
	
	/** TODO how to mark deleted pointers (indexs in the array)?
	For now there are none deleted and this calculates cost for all of them.
	*/
	public void updateCosts(){
		cost[0] = 1; //nil costs 1 since it has no childs.
		for(int i=1; i<size; i++){
			updateCost(i);
		}
		//TODO should there be a outgoingCost and incomingCost arrays?
	}
	
	public void updateCost(int pointer){
		int left = left(pointer), right = right(pointer);
		
		//TODO I'm considering inlining the code for reading the ref counts here to avoid reading the array twice,
		//but what if somebody subclasses those funcs and expects it to be called?
		//I'll just do it here and make them final.
		//double payToLeft = cost[left]/refCountFromInside[left];
		//double payToRight = cost[right]/refCountFromInside[right];
		long leftG = pairsOfRefCount[left];
		long rightG = pairsOfRefCount[right];
		//int leftRefs = (int)(leftG>>32) + (int)leftG;
		//int rightRefs = (int)(rightG>>32) + (int)rightG;
		
		//TODO how do we charge refsOutside (in units of lispPair) for pointing into the Acyc?
		//For now at least, only count pointers from inside the Acyc,
		//and use pointers from outside only for locking.
		int leftRefs = (int)leftG; //pointers from inside
		int rightRefs = (int)rightG;
		
		//Neither of leftRefs or rightRefs can be 0 because this pair points at them.
		double payToLeft = cost[left]/leftRefs;
		double payToRight = cost[right]/rightRefs;

		//Cost of each object includes 1 for itself being a lispPair
		cost[pointer] = 1 + payToLeft + payToRight;
	}
	
	public XorlispVM garbageCollect(){
		throw new RuntimeException("TODO start at 0 which all objects are derived from and map the addresses between the old and the new vms");
	}
	
	public static void main(String[] args){
		SimpleEconDAcycIWithHashMap L = new SimpleEconDAcycIWithHashMap((byte)5);
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
