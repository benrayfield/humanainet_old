/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;

/** An acyclic network of lispPair where all leafs are ()/nil.
Guaranteed to contain no duplicate objects except if you run multiple Acyc
in parallel which can be merged later. This is the core datastruct of xorlisp
and can represent all possible datastructs with only a few times extra memory
and computing time, so its not best for everything but has the extreme
advantage of everything being immutable, like Subversion on every bit.
*/
public interface Acyc{
	
	public DedupType dedupType();
	
	public short bitsPerPointer();
	
	/** TODO in some subclasses including EconAcyc this may be done continuously so it should not
	be a required part of Acyc to do it all at once, but I'll leave this here, at least for now,
	in case you want to do it all at once sometimes, branching a new Acyc object from
	whats reachable from the last pointer.
	<br><br>
	See the comment about it in XorlispVM, which may need merging with this. QUOTE:
	This has no effect on this XorlispVM.
	It creates a new one where most objects have different addresses.
	The rootPair is at the top in both.
	<br><br>
	This is similar to the process of 2 XorlispVMs agreeing on the addresses
	of certain pairs they have in common, which as acylicNet must agree all the way
	down to address 0/nil.
	UNQUOTE.
	*/
	public Acyc garbageCollect();
	
	/*TODO?
	public Comparable left(Comparable address);
	public Comparable right(Comparable address);
	public Comparable pair(Comparable left, Comparable right);
	public Comparable size();
	public Comparable capacity();
	*/
	
	//TODO functions are in subclasses depending on size of address: 32, 64, or SHA256HashCons.

}
