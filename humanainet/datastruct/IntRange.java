/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.datastruct;

public class IntRange{
	
	public final int start, endExclusive;
	
	public IntRange(int start, int endExclusive){
		this.start = start;
		this.endExclusive = endExclusive;
	}
	
	public String toString(){
		return "[IntRange "+start+" "+endExclusive+"]";
	}

}
