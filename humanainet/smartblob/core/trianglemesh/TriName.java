/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;

/** Moving.If you know which LayeredZigzag, then a Tri tells you which triangle in it.
2 Tris equal if their layer, point, and inward equals.
*/
public final class TriName{
	
	public final int layer, point;
	
	public final boolean inward;
	
	protected final int hash;
	public int hashCode(){ return hash; }
	
	public TriName(int layer, int point, boolean inward){
		this.layer = layer;
		this.point = point;
		this.inward = inward;
		int h = point;
		h += layer<<17;
		if(inward) h += Integer.MIN_VALUE;
		hash = h;
	}
	
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof TriName)) return false;
		TriName t = (TriName)o;
		return point==t.point && layer==t.layer && inward==t.inward;
	}

}
