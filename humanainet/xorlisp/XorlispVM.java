/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;

import humanainet.acyc.Acyc;

public interface XorlispVM extends Acyc{
	
	/** This has no effect on this XorlispVM.
	It creates a new one where most objects have different addresses.
	The rootPair is at the top in both.
	<br><br>
	This is similar to the process of 2 XorlispVMs agreeing on the addresses
	of certain pairs they have in common, which as acylicNet must agree all the way
	down to address 0/nil.
	*/
	public XorlispVM garbageCollect();
	
	//TODO
	
	/** The last pointer created has the largest integer value and would be the root
	node used in garbageCollection to find which nodes are reachable.
	The root changes quickly and many times because every QUEUE action by the Nav/Execute
	which started at the highest pair, which then replaces it with pair of the previous root
	and whatever it is queueing (that it is currently at using Nav interface).
	This may mean that an XorlispVM is a single lambda that can reach all relevant others,
	but when Nav.fork they may each need their own VM. It will have to be explored.
	Maybe currentRoot should be a function of Nav instead of XorlispVM,
	but since XorlispVM has a single lambda call at its root, maybe it should be a Nav.
	*
	public NonnegInt currentRoot();
	
	public NonnegInt pair(NonnegInt pointerX, NonnegInt pointerY);
	
	public NonnegInt left(NonnegInt pointer);
	
	public NonnegInt right(NonnegInt pointer);
	
	public NonnegInt size();
	
	public NonnegInt capacity();
	*/

}
