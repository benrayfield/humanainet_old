package humanainet.hash;
import humanainet.hash.hashtable.JHasher;

public class MultiTuringJ implements JHasher{
	
	protected final int heads;
	
	/** Chooses 1 of 2 possible actions, which each turing head does at its position
	which never overlaps any other turing head as they jump eachother.
	The 2 actions could be anything, but the default is to add or subtract 1 there.
	*/
	protected final long out[];
	
	/** each turing head moves 1 bitshift this direction */
	protected final long move[];
	
	protected final int turingCycles;
	
	protected final int arrayIndexMask;
	
	public MultiTuringJ(int heads, long out[], long move[], int turingCycles){
		if(heads > 63) throw new RuntimeException(
			"wrapped tape size is 64 so max heads is 63 in it (and you must answer for all 2^63 combinations), as they jump eachother. heads="+heads);
		this.heads = heads;
		this.out = out;
		this.move = move;
		this.turingCycles = turingCycles;
		arrayIndexMask = (1<<heads)-1;
	}
	
	/*public MultiTuringJ(int heads, JBit out, JBit move){
		if(heads > 63) throw new RuntimeException(
			"wrapped tape size is 64 so max heads is 63 in it (and you must answer for all 2^63 combinations), as they jump eachother. heads="+heads);
		this.heads = heads;
		this.out = out;
		this.move = move;
	}*/
	
	public long hash(long j){
		long tape = j; //wrapped turing tape
		long headsMask = (1L<<heads)-1L; //start h heads at first h low bits.
		final int headPos[] = new int[heads];
		for(int i=0; i<heads; i++) headPos[i] = i;
		for(int c=0; c<turingCycles; c++){
			int arrayIndex = 0;
			for(int i=0; i<heads; i++){
				int hPos = headPos[i];
				long thisHeadMask = 1<<hPos;
				arrayIndex |= (tape&thisHeadMask)>>(hPos-i);
			}
			//arrayIndex has each of its low bits from a different head.
			
			long allHeadsOut = out[arrayIndex];
			long onesMask = 0, zerosMask = 0;
			for(int i=0; i<heads; i++){
				long thisHeadMask = 1<<headPos[i];
				if((allHeadsOut&1)==1) onesMask |= thisHeadMask;
				else zerosMask |= thisHeadMask;
				allHeadsOut <<= 1;
			}
			allHeadsOut = out[arrayIndex];
			for(int i=0; i<heads; i++){ //move each turing head based on combination of what they all read
				int newHeadPos = (headPos[i]+(int)((allHeadsOut&1)*2-1))&63; //+0 or +1
				//int newHeadPos = (headPos[i]+(int)(allHeadsOut&1))&63; //+0 or +1
				if((headsMask | (1L<<newHeadPos)) != 0){ //move head, else stay here
					headPos[i] = newHeadPos;
				}
				allHeadsOut <<= 1;
			}
			
			//TODO heads jump over eachother
			
			//add or subtract 1, bit shifted to each turing heads position,
			//based on combination of what they all read
			tape = tape + onesMask - zerosMask;
			headsMask = onesMask | zerosMask;
			
			System.out.println(Long.toBinaryString(tape));
		}
		return tape;
	}

}
