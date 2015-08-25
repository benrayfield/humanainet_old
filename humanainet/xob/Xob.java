/** Ben F Rayfield offers Xob (Xorlisp Objects) opensource GNU LGPL */
package humanainet.xob;
import humanainet.acyc.Acyc;

/** Xorlisp Object */
public interface Xob{
	
	//TODO Like ListPow2, should Xob (or TODO Xob32) call RefCounter32 funcs in constructor and
	//in finalize(), about address() which would be an int in that case?
	
	/** The most recent (if replaced by garbageCollection) known Acyc
	where my address exists and is the data I represent.
	*/
	public Acyc acyc(); 
	
	/** Common address types are: int, long, or HashCons.
	Every XorlispObject is immutable and has constant time equals and hashCode funcs
	which match equals only within the same Acyc (which XorlispVM is a subtype of)
	or Acyc that is sharing addresses with it. SHA256HashCons is considered to be
	one global Acyc since its addresses are immutable and dont need allocating,
	but you are not guaranteed to find the 2 child nodes of each pair.
	*/
	public Object address();
	
	
	
	/*public int addressIf32();
	
	public long addressIf64();
	
	public HashCons addressIfHash();
	*/
	
	//TODO

}
