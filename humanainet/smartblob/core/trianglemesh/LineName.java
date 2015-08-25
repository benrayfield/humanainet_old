/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;

/** Moving. Lines equal when their 2 low Corners equal and 2 high Corners equal */
public final class LineName{
	
	/** low and high corners are ordered first by layer then by pointInLayer */
	public final CornerName cornerLow, cornerHigh;
	
	public final int hash;
	public int hashCode(){ return hash; }
	
	public LineName(CornerName x, CornerName y){
		CornerName low, high;
		long xLong = (((long)x.layer)<<32) | x.point;
		long yLong = (((long)y.layer)<<32) | y.point;
		if(xLong < yLong){
			cornerLow = x;
			cornerHigh = y;
		}else{
			cornerLow = y;
			cornerHigh = x;
		}
		hash = cornerHigh.hash<<5 + cornerLow.hash;
	}
	
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof LineName)) return false;
		LineName line = (LineName)o;
		return cornerLow.equals(line.cornerLow) && cornerHigh.equals(line.cornerHigh);
	}

}
