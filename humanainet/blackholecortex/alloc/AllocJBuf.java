package humanainet.blackholecortex.alloc;

public class AllocJBuf implements AllocJ{
	
	//TODO merge alloc of Neuron with the more general AllocJ so it can work with BellscalarNode too
	
	protected final AllocJ refillFrom;
	
	protected AllocJ localAlloc;
	
	public final long refillSizeEachTime;
	
	public AllocJBuf(AllocJ refillFrom, long refillSizeEachTime){
		this.refillFrom = refillFrom;
		this.refillSizeEachTime = refillSizeEachTime;
		localAlloc = refillFrom.range(refillSizeEachTime); //preallocate
	}

	/** Get from local or refill then get */
	public synchronized long alloc(){
		//System.out.println("AllocJBuf.alloc()...");
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
	public synchronized AllocJ range(long size){
		try{
			return localAlloc.range(size);
		}catch(CantAlloc e){
			return refillFrom.range(size);
		}
	}

	public synchronized long localSizeRemain(){
		return localAlloc.localSizeRemain();
	}

	public synchronized long maxSizeRemainIncludeRefills(){
		return refillFrom.localSizeRemain()+localSizeRemain();
	}
	
	
	public synchronized long maxRangeCouldAllocNow(){
		return Math.max(refillFrom.maxRangeCouldAllocNow(), localAlloc.maxRangeCouldAllocNow());
	}

	

}
