/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import humanainet.hash.hashtable.JHasher;

public class ChainTwoJHashers implements JHasher{
	
	public final JHasher a, b;
	
	public ChainTwoJHashers(JHasher a, JHasher b){
		this.a = a;
		this.b = b;
	}

	public long hash(long j){
		return b.hash(a.hash(j));
	}

}