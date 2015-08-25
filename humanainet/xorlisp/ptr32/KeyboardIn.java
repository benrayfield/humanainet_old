/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp.ptr32;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import humanainet.acyc.ptr32.Interpreter32;
import humanainet.xorlisp.SimpleLisp32;
import humanainet.xorlisp.XorlispVM32;

public class KeyboardIn extends JTextArea/*to get keyevents*/ implements KeyListener{
	
	public final InputStream getCharsAsUTF8Here;
	
	protected final OutputStream pumpCharsAsUTF8Here;
	
	public KeyboardIn(){
		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream();
		try{
			in.connect(out);
			//out.connect(in);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		this.getCharsAsUTF8Here = in;
		this.pumpCharsAsUTF8Here = out;
		addKeyListener(this);
	}
	
	protected char lastCharPressed;
	
	protected String displayText = "(Nothing typed yet)";

	public void keyPressed(KeyEvent e){
		char c = e.getKeyChar();
		String text;
		if(Character.isSurrogatePair(lastCharPressed, c)){
			text = ""+lastCharPressed+c;
		}else{
			text = ""+c;
		}
		byte b[];
		try{
			b = text.getBytes("UTF8");
		}catch(UnsupportedEncodingException err){
			throw new RuntimeException("No UTF8? What planet do you live on?", err);
		}
		try{
			pumpCharsAsUTF8Here.write(b);
			pumpCharsAsUTF8Here.flush();
		}catch(IOException err){
			throw new RuntimeException(err);
		}
		displayText = "Typed: "+text;
		//setText("");
		repaint();
	}
	
	public void paint(Graphics g){
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.white);
		g.drawString(displayText, 50, 50);
	}

	public void keyReleased(KeyEvent e){}
	
	public void keyTyped(KeyEvent e){}
	
	public static void main(String args[]){
		Window w = new JFrame("Capturing key events. View stdout/console. Unzip this jar file to get source code.");
		KeyboardIn k = new KeyboardIn();
		w.add(k);
		w.setLocation(300, 300);
		w.setSize(800,200);
		w.setVisible(true);
		SimpleLisp32.main(args); //display tests of basic objects (...)
		XorlispVM32 vm = new SimpleLisp32((byte)20);
		Interpreter32 interp = new Interpreter32(vm, k.getCharsAsUTF8Here, false, System.out, false);
		interp.run();
	}

}
