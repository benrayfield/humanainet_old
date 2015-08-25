/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;

/** Moving. A corner shared by up to 6 triangles, a point with mutable x and y.
Corners are equal if their layer and point equal.
*/
public final class CornerName{
	
	public final int layer, point;
	
	protected final int hash;
	public int hashCode(){ return hash; }
	
	public CornerName(int layer, int point){
		this.layer = layer;
		this.point = point;
		int h = point*3;
		h += layer<<15;
		hash = h;
	}
	
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof CornerName)) return false;
		CornerName t = (CornerName)o;
		return point==t.point && layer==t.layer;
	}

}
