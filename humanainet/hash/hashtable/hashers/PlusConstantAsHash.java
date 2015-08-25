/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import java.security.SecureRandom;

import humanainet.hash.hashtable.JHasher;

/** consider subsetSum of npComplete */
public class PlusConstantAsHash implements JHasher{
	
	private final long plusThis;
	
	public PlusConstantAsHash(SecureRandom forCreatingHash){
		this(forCreatingHash.nextLong());
	}
	
	public PlusConstantAsHash(long plusThis){
		this.plusThis = plusThis;
	}
	
	public long hash(long j){ return j+plusThis; }

}