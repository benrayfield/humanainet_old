/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;

/** Every cons is an acyclicNet which has the same one leaf at all ends.
That leaf is a cons whose childs are itself. It is at index 0 and its 2 childs are 0 and 0.
The next standard cons is 2 of those. Now we have 2 conses. Both orders of them are bit0 and bit1.
All possible conses are singleInstanced by using consHashing. The long is their address,
and you only use as many bits of it as you need. For example, a 25 bit address space
would bitmask only to use 25 of those bits.
*/
public interface XorlispVM64 extends XorlispVM{
	
	//TODO merge XorlispVM32 with XorlispVM64 by only using longs for everything and casting down when needed?
	
	/** creates if not exist, and returns its new or existing address. Never duplicates.
	TODO what should this return, or should it throw, if full?
	*/
	public long pair(long x, long y);
	
	public long left(long x);
	
	public long right(long x);
	
	/** Height of pair in acyclicNet. To check if a pair is nil, height(pair)==0.
	()/nil is at height 0. (()()) is at height 1. bit0 and bit1 are at height 2.
	This feature may be removed for efficiency reasons, or it may be implemented
	as lambda functions directly, but that may run into problems if you dont
	know the heights of those while they're computing.
	Height is derived from the pairs, so no need to store it longterm.
	*
	public long height(long x);
	*/
	
	public long size();
	
	public long capacity();
	
}