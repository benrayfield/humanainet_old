/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;
import humanainet.acyc.ptrObjectForest.ptrHashCons.HashCons;

/** A function whose parameter and return are both a lispCons or pair.
The overloaded funcs are for different kinds of addressing of those.
*/
public interface Func{
	
	public int func(XorlispVM32 xorlisp, int pointer);
	
	public long func(XorlispVM64 xorlisp, long pointer);
	
	public HashCons func(XorlispVM xorlisp, HashCons object);

}