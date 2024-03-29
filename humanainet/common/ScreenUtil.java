/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

public class ScreenUtil{
	private ScreenUtil(){}
	
	public static void moveToScreenCenter(Window w){
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		w.setLocation((screen.width-w.getWidth())/2, (screen.height-w.getHeight())/2);
	}
	
	/** Unlike new Color(r,g,b).getRGB(), 1-epsilon rounds to brightest.
	Truncates red, green, and blue into range 0 to 1 if needed.
	*/
	public static int color(float red, float green, float blue){
		return 0xff000000 |
			(MathUtil.holdInRange(0, (int)(red*0x100), 0xff) << 16) |
			(MathUtil.holdInRange(0, (int)(green*0x100), 0xff) << 8) |
			MathUtil.holdInRange(0, (int)(blue*0x100), 0xff);
	}
	
	public static int color(float alpha, float red, float green, float blue){
		return (MathUtil.holdInRange(0, (int)(alpha*0x100), 0xff) << 24) |
			(MathUtil.holdInRange(0, (int)(red*0x100), 0xff) << 16) |
			(MathUtil.holdInRange(0, (int)(green*0x100), 0xff) << 8) |
			MathUtil.holdInRange(0, (int)(blue*0x100), 0xff);
	}
	
	public static Window getWindow(Component c){
		while(!(c instanceof Window)) c = c.getParent();
		return (Window)c;
	}

}
