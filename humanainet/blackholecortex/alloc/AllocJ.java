package humanainet.blackholecortex.alloc;

/** J means long, as in the Java class file format.
AllocJ allocates longs individually or in blocks.
These are used as temporary ids for objects which need a fast way
to sort them or refer to eachother across network or in memory.
<br><br>
AllocJ is a generalization of Alloc<T> which also created
specific object type which used such a long.
*/
public interface AllocJ{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	public long alloc() throws CantAlloc;
	
	public AllocJ range(long size) throws CantAlloc;
	
	public long localSizeRemain();
	
	public long maxSizeRemainIncludeRefills();
	
	public long maxRangeCouldAllocNow();
	

}