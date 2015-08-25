/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import humanainet.hash.hashtable.JHasher;

public final class SumOfSelfAndDownshiftHalf implements JHasher{
    
	public long hash(long j){
		return (j>>32)+j;
	}

}
