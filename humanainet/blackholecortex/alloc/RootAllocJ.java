/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex.alloc;
import humanainet.common.Nanotimer;

/** Allocates unique longs from different pool per thread for efficiency and threadsafe.
<br><br>
TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
*/
public class RootAllocJ{
	
	/** Fast and thread safe, so caller not need to synchronize.
	Allocates AllocBuf to current thread if needed.
	Uses ThreadLocal<AllocBuf<Node>> allocBufferPerThread.
	*/
	public static long newJ(){
		return allocBufferPerThread.get().alloc();
	}
	
	/** Similar to newJ() but a little faster if you know you're getting many */
	public static long[] newJs(int size){
		Nanotimer t = new Nanotimer();
		AllocJ range = allocBufferPerThread.get().range(size);
		long n[] = new long[size];
		for(int i=0; i<size; i++){
			n[i] = range.alloc();
		}
		double duration = t.secondsSinceLastCall();
		//System.out.println("Took "+duration+" seconds to allocate "+size+" longs.");
		return n;
	}
	
	public static AllocJ newRange(long size){
		return allocBufferPerThread.get().range(size);
	}
	
	/** Leave first quarter of long range for global addresses, second quarter for local,
	and the positives for other things.
	<br><br>
	FIXME!!! RootNeuronAlloc and RootAllocJ and datastruct.Namespace.localName ranges need to agree on what objects get which ranges 
	*/
	public static final long localRangeStart = Long.MIN_VALUE/4, localRangeEnd = 0;
	
	/** UPDATE: See localRangeStart and locaRangeEnd about the now only 2^62 range here.
	<br><br>
	OLD: Holds the entire negative range of longs, which has 2^63 possible nodes.
	TODO The positive range is for a variety of things involving multidimensional indexs,
	and some of it is reserved for all possible values of a few common things including:
	unicode (0 to 17*2^16-1), int32 (in the 2^32 block above unicode),
	and float32 values (in the 2^32 block above ints).
	See datstruct.Namespace in my other code for what ranges I've reserved.
	*/
	public static final AllocJ rootNodeAlloc = new SimpleAllocJ(localRangeStart, localRangeEnd);
	
	//protected static final long refillSizeEachTime = 0x10000;
	//protected static final long refillSizeEachTime = 256;
	protected static final long refillSizeEachTime = 4096; //TODO 0x10000 but I want to see it refill until I know its working
	
	public static final ThreadLocal<AllocJBuf> allocBufferPerThread
			= new ThreadLocal<AllocJBuf>(){
		protected AllocJBuf initialValue(){
			return new AllocJBuf(rootNodeAlloc, refillSizeEachTime);
		}
	};

}
