package humanainet.blackholecortex.alloc;

public class SimpleAllocJ implements AllocJ{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	public final long from, to;
	
	/** TODO Is this being volatile redundant, given the synchronized uses of it? Are they synchronized everywhere its used? */
	protected volatile long nextLong;
	
	//TODO is there a reason to remember which NodeAlloc are created here?
	
	public SimpleAllocJ(long from, long to){
		nextLong = this.from = from;
		this.to = to;
		if(to < from) throw new IllegalArgumentException("from="+from+" to="+to);
	}
	
	public synchronized long alloc() throws CantAlloc{
	//public synchronized Node newNode(){
		//System.out.println("SimpleAllocJ.alloc() localSizeRemaining="+localSizeRemaining());
		if(nextLong < to){
			return nextLong++;
		}else{
			//TODO below commnt was written about Alloc<Neuron>
			//TODO should another kind of NodeAlloc wrap a simple NodeAlloc and call to the root
			//to get another piece of NodeAlloc when it runs out of indexs?
			//Or maybe each thread should just take a big chunk and allocate from there?
			//That would work on 1 computer, but as it scales if we share a 64 bit space
			//there would be competition for control of the node at each long.
			//Anyone may create a node at any long and duplicate others,
			//but its meant to be approximations of eachother, at least in the
			//core numbers (scalar influence and bit), if not which nodes are connected
			//to which others to have that effect.
			throw new CantAlloc();
			//throw new CantAlloc(
			//	"Cant allocate node (UPDATE: long instead of Neuron) in range "+from+" to less than "+to
			//	+" because full. See comment in code above this throw line about what could be done to allocate another NodeAlloc from root when thread runs out of range to allocate from faster.");
		}
	}
	
	public synchronized SimpleAllocJ range(long size) throws CantAlloc{
	//public synchronized NodeAlloc newRange(long size) throws CantAllocNode{
		if(size < 0) throw new IndexOutOfBoundsException("allocate negative size="+size);
		if(to-size < from) throw new CantAlloc("Not enough room to alloc "+size+" in allocator="+this);
		long rangeFrom = nextLong, rangeTo = nextLong+size;
		nextLong = rangeTo;
		return new SimpleAllocJ(rangeFrom,rangeTo);
	}
	
	public synchronized long localSizeRemain(){
		return to-nextLong;
	}
	
	public synchronized long maxSizeRemainIncludeRefills() {
		return localSizeRemain(); //does not refill
	}
	
	public synchronized long maxRangeCouldAllocNow(){
		return localSizeRemain();
	}

}