package humanainet.blackholecortex.alloc;



/** Gets a small block of nodes to allocate from parent NodeAlloc
just before it runs out, so it will only throw if parent runs out.
*/
public class AllocBuf<T> implements Alloc<T>{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	protected final Alloc<T> refillFrom;
	
	protected Alloc<T> localAlloc;
	
	public final long refillSizeEachTime;
	
	public AllocBuf(Alloc<T> refillFrom, long refillSizeEachTime){
		this.refillFrom = refillFrom;
		this.refillSizeEachTime = refillSizeEachTime;
		localAlloc = refillFrom.range(refillSizeEachTime); //preallocate
	}

	/** Get from local or refill then get */
	public synchronized T alloc(){
		try{
			return localAlloc.alloc();
		}catch(CantAlloc e){
			localAlloc = refillFrom.range(refillSizeEachTime);
			return localAlloc.alloc();
		}
	}

	/** Get directly from parent without refilling local if not enough room in local.
	OLD: To avoid complexity of keeping multiple Alloc here
	and trying to merge them (what if they are a class type I dont know?).
	*/
	public synchronized Alloc<T> range(long size){
		try{
			return localAlloc.range(size);
		}catch(CantAlloc e){
			return refillFrom.range(size);
		}
	}

	public synchronized long localSizeRemaining(){
		return localAlloc.localSizeRemaining();
	}

	public synchronized long maxSizeRemainingIncludingRefills(){
		return refillFrom.localSizeRemaining()+localSizeRemaining();
	}
	
	
	public synchronized long maxRangeCouldAllocateNow(){
		return Math.max(refillFrom.maxRangeCouldAllocateNow(), localAlloc.maxRangeCouldAllocateNow());
	}

	

}
