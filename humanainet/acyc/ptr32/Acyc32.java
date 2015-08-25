/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

import humanainet.acyc.Acyc;

/** Acyc using int for up to 30 bit (or maybe 31 if they dont need the last) addresses. */
public interface Acyc32 extends Acyc{
	
	//TODO add functions for lazyDedup, since not every Acyc dedups everything right away, and some never are deduped.
	//Example: humanainet.acyc.lazyDedup.Bifor
	
	public boolean pointerExists(int x);
	
	/** creates if not exist, and returns its new or existing address. Never duplicates.
	TODO what should this return, or should it throw, if full?
	*/
	public int pair(int x, int y);
	
	public int left(int x);
	
	public int right(int x);
	
	public int size();
	
	public int capacity();
	
	public int[] pointers(int fromInclusive, int toExclusive);
	
	/** Removes all pairs except in some cases a few that are required at the bottom, like whats in Const */
	public void clear();
	
	/** This is being replaced by RefCount interface which extends Acyc.
	<br><br>
	TODO 2 ints of reference counting for each address, 1 for inside the vm and 1 for outside.
	There will also need to be a way of forcing a reference to release if its taken for too long,
	so maybe instead all items should come with a time limit.
	See benfrayfieldResearch.avlBitstringWithSizeOnesAndMinAndMaxRetention = QUOTE
	This is called sha256L1T, meaning it has length (L), number of bit1 (1)
	and min and max times (T) in each node in merkleForest.
	UNQUOTE.
	*
	public void lockAddress(int pointer);
	*/
	
	
	
	/** Height of pair in acyclicNet. To check if a pair is nil, height(pair)==0.
	()/nil is at height 0. (()()) is at height 1. bit0 and bit1 are at height 2.
	This feature may be removed for efficiency reasons, or it may be implemented
	as lambda functions directly, but that may run into problems if you dont
	know the heights of those while they're computing.
	Height is derived from the pairs, so no need to store it longterm.
	*
	public int height(int x);
	*/

}
