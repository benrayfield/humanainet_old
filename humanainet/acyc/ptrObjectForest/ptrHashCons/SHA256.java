/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest.ptrHashCons;

import humanainet.acyc.ptrObjectForest.lazyDedup.Bifor;
import humanainet.common.Rand;

/** A SHA256 hash of 2 other SHA256 hashes,
or the root of the acyclicNet is 256 of bit0 which all other pairs are derived from.
<br><br>
TODO Since length is always equal, should that last step be avoided in SHA256 hashing?
I dont know if thats practical since existing implementations of SHA256,
some of them hardware optimized, depend on no change to the algorithm.
*/
public class SHA256 implements Comparable<SHA256>{
	
	public final long a, b, c, d;
	
	public final int hash32;
	
	public static final long hash32RandomMaskA = Rand.strongRand.nextLong();
	public static final long hash32RandomMaskB = Rand.strongRand.nextLong();
	public static final long hash32RandomMaskC = Rand.strongRand.nextLong();
	public static final long hash32RandomMaskD = Rand.strongRand.nextLong();
	
	public SHA256(long a, long b, long c, long d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		hash32 = (int)(
			(a&hash32RandomMaskA)
			*(b&hash32RandomMaskB)
			*(c&hash32RandomMaskC)
			*(d&hash32RandomMaskD)
		);
	}

	public int compareTo(SHA256 s){
		throw new RuntimeException("TODO as 256 bit nonnegative integer");
	}
	
	public boolean equals(Object o){
		if(this == o) return true;
		if(!(o instanceof SHA256)) return false;
		SHA256 s = (SHA256) o;
		return s.a==a && s.b==b && s.c==c && s.d==d;
	}

}
