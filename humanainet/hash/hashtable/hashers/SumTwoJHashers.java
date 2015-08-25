/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import humanainet.hash.hashtable.JHasher;

public class SumTwoJHashers implements JHasher{
	
	public final JHasher a, b;
	
	public SumTwoJHashers(JHasher a, JHasher b){
		this.a = a;
		this.b = b;
	}

	public long hash(long j){
		return a.hash(j)+b.hash(j);
	}

}