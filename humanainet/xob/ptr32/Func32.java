/** Ben F Rayfield offers Xob (Xorlisp Objects) opensource GNU LGPL */
package humanainet.xob.ptr32;
import humanainet.acyc.ptr32.CacheAcyc32;
import humanainet.xob.Func;

public interface Func32 extends Func{
	
	public int func(CacheAcyc32 cAcyc, int ptr);

}
