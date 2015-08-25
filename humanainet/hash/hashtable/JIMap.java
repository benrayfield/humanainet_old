/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable;

/** long (J) to int (I) map, only for positive values. Negative values may be used for internal codes.
This is needed to replace HashMap<Long,Integer> and Dictionary<long,int> in c# speed test, both of which are
very slow and only allow the creation of about a half million pairs per second.
*/
public interface JIMap{
	
	public int size();
	
	public int tombstones();
	
	/** Returns existing value of that key if any else newValue.
	Normally faster than checking get then put if not found.
	*/
	public int getIfExistElsePut(long key, int newV);
	
	/** Returns -1 if not exist. Key and value must be nonnegative. */
	public int get(long key);
	
	/** Key and value must be nonnegative */
	public void put(long key, int value);
	
	/** Normally creates a tombstone, depending on the implementation, which can only be removed all together. */
	public void remove(long key);
	
	public long[] keys();
	
	public void clear();

}