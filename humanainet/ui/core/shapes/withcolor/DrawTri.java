/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui.core.shapes.withcolor;

/** immutable 3 points in 2d and color */
public class DrawTri{
	
	public final float ay, by, cy, ax, bx, cx;
	
	/** Alpha/transparent is high 8 bits. Next lower 8 are red, then green, then blue.
	If your system uses a different representation of color in an int, you must still
	use this way and translate it when needed. This is for compatibility.
	*/
	//public final int colorARGB;
	public final int aColorARGB, bColorARGB, cColorARGB;
	
	/** TODO draw optimized all as same color if drawing code finds all 3 color ints equal */
	public DrawTri(float ay, float by, float cy, float ax, float bx, float cx, int colorARGB){
		this(ay, by, cy, ax, bx, cx, colorARGB, colorARGB, colorARGB);
	}
	
	/** Example: OpenGL can efficiently draw a triangle with gradual colors changing between corners,
	or it could be done with CPU maybe still at gaming speed. First interpolate color on all the lines.
	Then interpolate horizontally or vertically between colors at points on those lines. 
	*/
	public DrawTri(float ay, float by, float cy, float ax, float bx, float cx,
			int aColorARGB, int bColorARGB, int cColorARGB){
		this.ay = ay;
		this.by = by;
		this.cy = cy;
		this.ax = ax;
		this.bx = bx;
		this.cx = cx;
		this.aColorARGB = aColorARGB;
		this.bColorARGB = bColorARGB;
		this.cColorARGB = cColorARGB;
	}
	
	public boolean areCornersAllSameColor(){
		return aColorARGB==bColorARGB && aColorARGB==cColorARGB;
	}

}