package humanainet.xorlisp.ptr32;

/** Can produce a lambda continuation at any time. Otherwise has a mutable state.
<br><br>
A tag system can be turing complete depending on which key/value pairs.
In this one, those pairs can be created while its running.
This is meant to be the core of a lisp compute loop that builds lisp
while you use it and saves it in acyc for later use,
but you can always start over or continue from any variation of
language and computing system and data, including really big data
using acyc and things it refers to by SHA256 hash or urls other places.
Its openended.
*/
public interface TagSys32{
	
	public boolean isEmpty();
	
	public int sizeAll();
	
	public int sizeQueue();
	
	/** Oldest is not at either end as described by size and get,
	because the absolute oldest is 0, while once they're removed they stay in
	the acyc as part of powOf2List and we just point ahead for efficiency,
	possibly removing half the powOf2List when we pass that half.
	<br><br>
	Returns next acyc node as int,
	for you to 0 or more times add(int) for each node in its definition which
	will later be returned from this removeOldest() function.
	Its a form of recursion, possibly depthFirst or breadthFirst I'll have to think about that.
	*/
	public int removeOldest();
	
	public void add(int acycNode);
	
	/** Usually more than 0. Since nodes are only returned once from removeOldest(),
	this increases by 1 each call. This will decrease to 0 if a large section size a power of 2
	is removed after oldestIndex() is past it, or it may be kept and stored as history.
	*/
	public int oldestIndex();
	
	public int get(int index);
	
	/** This is a continuation, which could also be called stateOfTagSys.
	If you give this returned acyc32 node to anyone, they can continue where you left off.
	Or for saving your own place in case things go wrong and you want to restore from backup.
	<br><br>
	TODO returns an Acyc32 node which has a powOf2List of all nodes in the queue,
	and a number for which of them is oldest that hasnt yet been processed (for next removeOldest).
	Also the nodes should each have a map made of acyc nodes that allows updates of any key/value
	in log size of changes to the map and while having a normalized representation of such map
	so we get dedup, and this requires all acyc nodes have a sortable form.
	*/
	public int continuation();
	//public int stateOfTagSys();

}
