/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import java.security.SecureRandom;
import java.util.Random;

import humanainet.hash.hashtable.JHasher;

/** Divisor can be any long but should be a random prime of a little more than
as few bits as you need for hashtable size.
Remainder is useful for making the result depend on the high bits too.
*/
public final class Remainder implements JHasher{
	
	private final long divisor;
	
	/** Gets constants to use in hash function from the Random. */
	public Remainder(SecureRandom rand){
		this(randBetween(1L<<35, rand, 1L<<36));
	}
	
	public Remainder(long divisor){
		this.divisor = divisor;
	}

	public long hash(long j){
		return j%divisor;
	}
	
	/** TODO better quality random by not wrapping, like Random.nextInt(int) */
	public static long randBetween(long minInclusive, SecureRandom rand, long maxExclusive){
		long range = maxExclusive-minInclusive;
		return minInclusive+Math.abs(rand.nextLong())%range;
	}

}