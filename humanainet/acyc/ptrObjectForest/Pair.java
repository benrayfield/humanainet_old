/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest;

/** Experimenting with using objects with final pointers
(like in wavetree.scalar which is faster than this software)
instead of the array of SimpleEconDAcycI where left and right pointers are looked up.
*/
public final class Pair{
	
	public final int id;
	
	public final Pair left, right;
	
	public int pointersFromInside, pointersFromOutside;
	
	public double cost;
	
	/** Dont create these directly. Let the Acyc32 do it since it guarantees no duplicates recursively. */
	public Pair(int id, Pair left, Pair right){
		this.id = id;
		this.left = left;
		this.right = right;
	}
	
	public int hashCode(){ return id; }
	
	public boolean equals(Object o){
		if(!(o instanceof Pair)) return false;
		return id == ((Pair)o).id;
	}

}