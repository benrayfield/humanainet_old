/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import java.security.SecureRandom;
import java.util.Random;

import humanainet.hash.hashtable.JHasher;

public final class DiffOfMultsOfXorsJHasher implements JHasher{
	
	private final long a, b, c, d;
	
	/** Gets constants to use in hash function from the Random. */
	public DiffOfMultsOfXorsJHasher(SecureRandom rand){
		this(rand.nextLong(), rand.nextLong(), rand.nextLong(), rand.nextLong());
	}
	
	/** These should be random longs */
	public DiffOfMultsOfXorsJHasher(long a, long b, long c, long d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public long hash(long j){
		//TODO multiply as int128 or 2 longs for high and low bits of each, so most of the possible values I keep
		return (j^a)*(j^b) - (j^c)*(j^d);
	}

}