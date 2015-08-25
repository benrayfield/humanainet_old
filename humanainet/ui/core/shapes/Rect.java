/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui.core.shapes;

import humanainet.common.MathUtil;

/** Since Android OS has most of java but not java.awt package,
and PC versions of java dont have the Android classes,
I'm building my own for use in PC and Android which share this core package.
*/
public class Rect{
	
	public final float y, x, height, width;
	
	public Rect(float y, float x, float height, float width){
		this.y = y;
		this.x = x;
		this.height = height;
		this.width = width;
	}
	
	public boolean containsYX(float y, float x){
		return this.y<=y && y<this.y+height && this.x<=x && x<=this.x+width;
	}
	
	/** Should use this to collision detect since its not slowed by creating object.
	Then if collision is found, get the Rect intersection.
	*/
	public boolean intersects(Rect r){
		float yStart = MathUtil.max(y, r.y);
		float xStart = MathUtil.max(x, r.x);
		float yEnd = MathUtil.min(y+height, r.y+r.height);
		float xEnd = MathUtil.min(x+width, r.x+r.width);
		return  yStart<=yEnd && xStart<=xEnd;
	}
	
	/** Returns Rect with 0 width and height if not intersect */
	public Rect intersection(Rect r){
		float yStart = MathUtil.max(y, r.y);
		float xStart = MathUtil.max(x, r.x);
		float yEnd = MathUtil.min(y+height, r.y+r.height);
		float xEnd = MathUtil.min(x+width, r.x+r.width);
		if(yStart<=yEnd && xStart<=xEnd){
			return new Rect(yStart, xStart, yEnd-yStart, xEnd-xStart);
		}else{ //empty
			return new Rect(y, x, 0, 0);
		}
	}
	
	public String toString(){
		return "[Rect y"+y+" x"+x+" h"+height+" w"+width+"]";
	}

}