/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import humanainet.hash.hashtable.JHasher;

/** Not a good hashcode. For experimenting */
public class SelfAsHash implements JHasher{
	
	public long hash(long j){ return j; }

}