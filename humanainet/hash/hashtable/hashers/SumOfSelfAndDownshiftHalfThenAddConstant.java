/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import java.security.SecureRandom;

import humanainet.hash.hashtable.JHasher;

public final class SumOfSelfAndDownshiftHalfThenAddConstant implements JHasher{

	private final long add;
    
	public SumOfSelfAndDownshiftHalfThenAddConstant(SecureRandom rand){
		this(rand.nextLong());
	}

	public SumOfSelfAndDownshiftHalfThenAddConstant(long add){
		this.add = add;
	}
    
	public long hash(long j){
		return (j>>32)+j+add;
	}
    
}