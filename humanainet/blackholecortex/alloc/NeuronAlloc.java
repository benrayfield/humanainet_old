package humanainet.blackholecortex.alloc;
import humanainet.blackholecortex.WeightsNode;

/** The simplest purpose of this is to allocate nodes in a range of long
so different threads dont have to all depend on a global synchronized allocator.
<br><br>
Allocates nodes. In future versions, it may also, at large cost of speed,
be connected to Node.finalize() or at least things about how connected the
network of nodes is, what is reachable, or in the simplest case freeing a node
after its allocated, but maybe Java does a good enough job on that.
If more than that is needed, this would be the place to add it.
*/
public class NeuronAlloc implements Alloc<WeightsNode>/*, Comparable<NodeAlloc>*/{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	public final long from, to;
	
	/** TODO Is this being volatile redundant, given the synchronized uses of it? Are they synchronized everywhere its used? */ 
	protected volatile long nextLong;
	
	//TODO is there a reason to remember which NodeAlloc are created here?
	
	public NeuronAlloc(long from, long to){
		nextLong = this.from = from;
		this.to = to;
		if(to < from) throw new IllegalArgumentException("from="+from+" to="+to);
	}
	
	public synchronized WeightsNode alloc(){
	//public synchronized Node newNode(){
		if(nextLong < to){
			return new WeightsNode(nextLong++);
		}else{
			//TODO should another kind of NodeAlloc wrap a simple NodeAlloc and call to the root
			//to get another piece of NodeAlloc when it runs out of indexs?
			//Or maybe each thread should just take a big chunk and allocate from there?
			//That would work on 1 computer, but as it scales if we share a 64 bit space
			//there would be competition for control of the node at each long.
			//Anyone may create a node at any long and duplicate others,
			//but its meant to be approximations of eachother, at least in the
			//core numbers (scalar influence and bit), if not which nodes are connected
			//to which others to have that effect.
			throw new IndexOutOfBoundsException(
				"Cant allocate node in range "+from+" to less than "+to
				+" because full. See comment in code above this throw line about what could be done to allocate another NodeAlloc from root when thread runs out of range to allocate from faster.");
		}
	}
	
	public synchronized NeuronAlloc range(long size) throws CantAlloc{
	//public synchronized NodeAlloc newRange(long size) throws CantAllocNode{
		if(size < 0) throw new IndexOutOfBoundsException("allocate negative size="+size);
		if(to-size < from) throw new CantAlloc("Not enough room to alloc "+size+" in allocator="+this);
		long rangeFrom = nextLong, rangeTo = nextLong+size;
		nextLong = rangeTo;
		return new NeuronAlloc(rangeFrom,rangeTo);
	}
	
	public synchronized long localSizeRemaining(){
		return to-nextLong;
	}
	
	public synchronized long maxSizeRemainingIncludingRefills() {
		return localSizeRemaining(); //does not refill
	}
	
	public synchronized long maxRangeCouldAllocateNow(){
		return localSizeRemaining();
	}

	/** As Comparable, its by the longs in its range, which cant overlap. Throws
	if NodeAlloc is found in the same range (only checks FROM) and not ==.
	*
	public int compareTo(NodeAlloc n){
		if(n == this) return 0;
		if(from < n.from) return -1;
		if(from > n.from) return 1;
		throw new RuntimeException("Duplicate allocator: "+n+" this="+this);
	}*/

}