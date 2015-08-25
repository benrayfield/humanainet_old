/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest.lazyDedup;

import humanainet.acyc.ptrObjectForest.ptrHashCons.HashCons;

/** Immutable binary forest (bifor) node in terms of which paths lead to ()/nil/0,
but its internal representation is mutable in how it delays merging with other equal (by content) nodes
which can be merged by hashtable (JIMap maps long to int, for use with Acyc32, for example) later.
Those holding pointer to this object should call its lazyDedup() func occasionally to see if a duplicate
has been found for it and use the node returned, which may be an equal content object you should use
or may be this same object. Use the object lazyDedup() returns as a replacement for this, either way.
*/
public interface Bifor{
	
	/** nil/()/0 */
	public boolean isEnd();
	
	/** TODO what should this return if isEnd()? return this node? */
	public Bifor left();
	
	/** TODO what should this return if isEnd()? return this node? */
	public Bifor right();
	
	/** Returns an immutable binary forest node with this node as left and parameter node as right */
	public Bifor pair(Bifor right);
	
	/** Returns either an equal content node (whose left, right, and isEnd funcs are the same combination) or this node.
	You should call this to get a replacement for this Node occasionally, in case a dedup is waiting for you.
	<br><br>
	TODO? If this is a HashCons, lazyDedup() may return a wrapper for itself (which it remembers) in case it wants
	to release the memory for the secureHash. But in general once you calculate a secureHash,
	which is an expensive calculation per bit, you probably want to keep it.
	*/
	public Bifor lazyDedup();
	
	/** An expensive calculation per secureHash and requiring it be done for all childs recursively
	but only once per child even if that child occurs many times in the forest.
	<br><br>
	TODO should this throw if !left().globalDedupIsCached() || !right().globalDedupIsCached() ?
	<br><br>
	TODO Should there be a separate cost() calculated for globalDedup, which of the Bifors want it?
	Just because a Bifor exists doesnt mean it will ever use globalDedup().
	*/
	public HashCons globalDedup();
	
	public boolean globalDedupIsCached();
	
	public boolean isCertainlyEqual(Bifor n);
	
	public boolean isCertainlyUnequal(Bifor n);
	
	/** If this node is part of an EconAcyc, returns the current cost, else this returns 0.
	TODO does this interface need to do reference counting like EconAcyc does?
	Or maybe if this function returned costPerPointer (and would it be for pointers from inside, outside, or either?)
	we wouldnt need to include reference counting functions, including inside and outside, in this Node interface.
	These are things that will need to be explored as econacyc is developed
	and used for mindmap, neuraltool, smartblob, etc.
	<br><br>
	cost = cost(left)/incomingPointers(left) + cost(right)/incomingPointers(right) + 1.
	<br><br>
	That means that reusing nodes shares the cost which is lower for each pointer
	It works the same for duplicate objects and objects that simply have different content.
	Total cost of all nodes which have no incoming pointers equals the number of node objects in memory,
	and object may mean in an array or java objects or whatever form.
	*/
	public double cost();
	
	/** Cost for a pointer from inside the known program to point at this object.
	TODO Should inside pointers be counted internally and only the costPerOutsidePointer is known?
	*/
	public double costPerInsidePointer();
	
	/** Cost for a pointer from outside the known program, such as instance vars in a java object of this Bifor.
	TODO Should inside pointers be counted internally and only the costPerOutsidePointer is known?
	*/
	public double costPerOutsidePointer();
	
	//"TODO? separate ReferenceCounter32 from Acyc so this class can implement it without being an Acyc32 because the point of this interface is to get away from directly storing pointers and instead use lazyDedup() and navigation functions only"
	public int pointersFromInside();
	
	public int pointersFromOutside();

}