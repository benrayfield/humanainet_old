package humanainet.blackholecortex.alloc;

/** The simplest purpose of this is to allocate nodes in a range of long
so different threads dont have to all depend on a global synchronized allocator.
<br><br>
Allocates nodes. In future versions, it may also, at large cost of speed,
be connected to Node.finalize() or at least things about how connected the
network of nodes is, what is reachable, or in the simplest case freeing a node
after its allocated, but maybe Java does a good enough job on that.
If more than that is needed, this would be the place to add it.
*/
public interface Alloc<T>{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	public T alloc();
	
	public Alloc<T> range(long size);
	
	public long localSizeRemaining();
	
	/** If this is an allocator that refills itself when near empty,
	includes the size of parent allocator available now,
	but this is not something you can sum between many allocators
	to get the range available because they can share refill pools.
	*/
	public long maxSizeRemainingIncludingRefills();
	
	public long maxRangeCouldAllocateNow();

}