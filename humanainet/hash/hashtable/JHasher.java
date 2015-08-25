/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable;

/** Immutable. long (J) hasher, for use in hashtables that hash again in the same array
instead of use a linked list at each bucket. param and returned longs should use all 64 bits.
*/
public interface JHasher{
	
	public long hash(long j);

}
