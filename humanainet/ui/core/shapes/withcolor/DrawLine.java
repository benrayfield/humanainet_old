/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui.core.shapes.withcolor;

import humanainet.ui.core.ColorUtil;

/** immutable 3 points in 2d and color */
public class DrawLine{
	
	public final float ay, by, ax, bx;
	
	public final int aColorARGB, bColorARGB;
	
	/** TODO draw optimized all as same color if drawing code finds all 3 color ints equal */
	public DrawLine(float ay, float by, float cy, float ax, float bx, float cx, int colorARGB){
		this(ay, by, cy, ax, bx, cx, colorARGB, colorARGB, colorARGB);
	}
	
	/** Example: OpenGL can efficiently draw a triangle with gradual colors changing between corners,
	or it could be done with CPU maybe still at gaming speed. First interpolate color on all the lines.
	Then interpolate horizontally or vertically between colors at points on those lines. 
	*/
	public DrawLine(float ay, float by, float cy, float ax, float bx, float cx,
			int aColorARGB, int bColorARGB, int cColorARGB){
		this.ay = ay;
		this.by = by;
		this.ax = ax;
		this.bx = bx;
		this.aColorARGB = aColorARGB;
		this.bColorARGB = bColorARGB;
	}
	
	/** fraction is position between points a (0) and b (1) *
	public int color(float fraction){
		would be slow...
		ColorUtil.red(color) 
		ColorUtil.color(alpha, red, green, blue)
	}*/

}