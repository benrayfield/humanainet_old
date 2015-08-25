/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui.core;

public class ColorUtil{
	
	/** Unlike new Color(r,g,b).getRGB(), 1-epsilon rounds to brightest.
	Truncates red, green, and blue into range 0 to 1 if needed.
	*/
	public static int color(float red, float green, float blue){
		return 0xff000000 |
			(Math.max(0, Math.min((int)(red*0x100), 0xff)) << 16) |
			(Math.max(0, Math.min((int)(green*0x100), 0xff)) << 8) |
			Math.max(0, Math.min((int)(blue*0x100), 0xff));
	}
	
	public static int color(float alpha, float red, float green, float blue){
		return (Math.max(0, Math.min((int)(alpha*0x100), 0xff)) << 24) |
			(Math.max(0, Math.min((int)(red*0x100), 0xff)) << 16) |
			(Math.max(0, Math.min((int)(green*0x100), 0xff)) << 8) |
			Math.max(0, Math.min((int)(blue*0x100), 0xff));
	}
	
	public static float alpha(int color){
		//return ((color>>>24) & 0xff)/255f;
		return (color>>>24)/255f;
	}
	
	public static float red(int color){
		return ((color>>>16) & 0xff)/255f;
	}
	
	public static float green(int color){
		return ((color>>>8) & 0xff)/255f;
	}
	
	public static float blue(int color){
		return (color&0xff)/255f;
	}

}
