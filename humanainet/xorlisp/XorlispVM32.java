/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;

import humanainet.acyc.ptr32.Acyc32;

/** Every cons is an acyclicNet which has the same one leaf at all ends.
That leaf is a cons whose childs are itself. It is at index 0 and its 2 childs are 0 and 0.
The next standard cons is 2 of those. Now we have 2 conses. Both orders of them are bit0 and bit1.
All possible conses are singleInstanced by using consHashing. The long is their address,
and you only use as many bits of it as you need. For example, a 25 bit address space
would bitmask only to use 25 of those bits.
*/
public interface XorlispVM32 extends Acyc32, XorlispVM{
	
	//TODO merge XorlispVM32 with XorlispVM64 by only using longs for everything and casting down when needed?
	
	
	
}