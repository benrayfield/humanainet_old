/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import humanainet.acyc.Const;
import humanainet.xorlisp.SimpleLisp32;
import humanainet.xorlisp.XorlispVM32;

public class Interpreter32 implements Runnable{
	
	public final InputStream in;
	public final OutputStream out;
	
	protected PrintWriter o;
	
	public void scheduleStopAsap(){ stopAsap = true; }
	protected boolean stopAsap;
	
	public final boolean closeInWhenStop, closeOutWhenStop;
	
	public final XorlispVM32 vm;
	
	public Interpreter32(XorlispVM32 vm, InputStream in, boolean closeInWhenStop, OutputStream out, boolean closeOutWhenStop){
		this.vm = vm;
		this.in = in;
		this.out = out;
		this.closeInWhenStop = closeInWhenStop;
		this.closeOutWhenStop = closeOutWhenStop;
		o = new PrintWriter(out);
	}
	
	public String toString(int pair){
		return toString(new StringBuilder(),pair);
	}
	
	public String toString(StringBuilder sb, int pair){
		sb.append('(');
		if(pair != 0){ //Const.end
			toString(sb,vm.left(pair));
			toString(sb,vm.right(pair));
		}
		sb.append(')');
		return sb.toString();
	}
	
	public void run(){
		try{
			while(!stopAsap){
				nextCycle();
				Thread.yield();
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			if(closeInWhenStop) try{ in.close(); }catch(Exception e){}
			if(closeOutWhenStop) try{ out.close(); }catch(Exception e){}
		}
	}
	
	protected void nextCycle() throws Exception{
		int b = in.read();
		o.println("read: "+b);
		o.println(toString(vm.pair(vm.pair(0,0),0)));
		o.flush();
	}
	
	public static void main(String args[]){
		XorlispVM32 vm = new SimpleLisp32((byte)20);
		Interpreter32 interp = new Interpreter32(vm, System.in, false, System.out, false);
		interp.run();
	}

}