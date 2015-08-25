package humanainet.blackholecortex.alloc;
import humanainet.blackholecortex.WeightsNode;
import humanainet.common.Nanotimer;

public class RootNeuronAlloc{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	/** Fast and thread safe, so caller not need to synchronize.
	Allocates AllocBuf to current thread if needed.
	Uses ThreadLocal<AllocBuf<Node>> allocBufferPerThread.
	*/
	public static WeightsNode newNode(){
		return allocBufferPerThread.get().alloc();
	}
	
	/** Similar to newNode() but a little faster if you know you're getting many */
	public static WeightsNode[] newNodes(int nodes){
		Nanotimer t = new Nanotimer();
		Alloc<WeightsNode> range = allocBufferPerThread.get().range(nodes);
		WeightsNode n[] = new WeightsNode[nodes];
		for(int i=0; i<nodes; i++){
			n[i] = range.alloc();
		}
		double duration = t.secondsSinceLastCall();
		System.out.println("Took "+duration+" seconds to allocate "+nodes+" NeuralNodes.");
		return n;
	}
	
	/** Leave first quarter of long range for global addresses, second quarter for local,
	and the positives for other things.
	<br><br>
	FIXME!!! RootNeuronAlloc and RootAllocJ and datastruct.Namespace.localName ranges need to agree on what objects get which ranges
	*/
	public static final long localRangeStart = Long.MIN_VALUE/2, localRangeEnd = Long.MIN_VALUE/4;
	
	/** UPDATE: See localRangeStart and locaRangeEnd about the now only 2^62 range here.
	<br><br>
	OLD: Holds the entire negative range of longs, which has 2^63 possible nodes.
	TODO The positive range is for a variety of things involving multidimensional indexs,
	and some of it is reserved for all possible values of a few common things including:
	unicode (0 to 17*2^16-1), int32 (in the 2^32 block above unicode),
	and float32 values (in the 2^32 block above ints).
	See datstruct.Namespace in my other code for what ranges I've reserved.
	*/
	public static final NeuronAlloc rootNodeAlloc = new NeuronAlloc(localRangeStart, localRangeEnd);
	
	//protected static final long refillSizeEachTime = 0x10000;
	//protected static final long refillSizeEachTime = 256;
	protected static final long refillSizeEachTime = 4096; //TODO 0x10000 but I want to see it refill until I know its working
	
	public static final ThreadLocal<AllocBuf<WeightsNode>> allocBufferPerThread
			= new ThreadLocal<AllocBuf<WeightsNode>>(){
		protected AllocBuf<WeightsNode> initialValue(){
			return new AllocBuf<WeightsNode>(rootNodeAlloc, refillSizeEachTime);
		}
	};

}
