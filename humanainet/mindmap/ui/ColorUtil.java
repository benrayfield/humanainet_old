/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap.ui;
import java.awt.Color;
import java.awt.Component;
import java.awt.TextComponent;
import javax.swing.text.JTextComponent;
//import jscreenpixels.javacomponents.DrawPixelsAsInts;

public class ColorUtil{
	
	public static Color background = new Color(0xff223344);
	public static Color foreground = new Color(0xffccffff);
	
	/*public static Color background = new Color(0xff223344);
	public static Color foreground = new Color(0xffccffff);
	*/
	
	public static void setColors(Component c){
		setColors(c, foreground, background);
	}
	
	/** TODO argb or rgba? Update comments andOr code in Dynarect's color functions if I mixed the byte order anywhere. */
	public static void setColors(Component c, int foregroundARGB, int backgroundARGB){
		setColors(c, new Color(foregroundARGB,true), new Color(backgroundARGB,true));
	}
	
	public static void setColors(Component c, Color foreground, Color background){
		c.setForeground(foreground);
		if(c instanceof JTextComponent){
			((JTextComponent)c).setCaretColor(foreground);
		}
		c.setBackground(background);
	}
	
	public static void setColors(Component... c){
		for(Component com : c) setColors(com);
	}

}
