/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

import humanainet.acyc.EconAcyc;

/** In java bytecode, D means double (64 bit floating point)
and I means int (32 bit integer), so the name EconDAcycI
means the econ part uses double and the address part uses int.
*/
public interface EconDAcycI extends EconAcyc, RefCounter32, Acyc32{
	
	/** totalCost of an object changes depending on how many incoming pointers.
	They each pay an equal fraction of it, for both of their pointers,
	plus 1 for cost of themself as a lispPair.
	<br><br>
	TODO functions for giving objects a chance to pay more for when the
	objects they point at are about to be deleted,
	therefore everything which points at them must be deleted first.
	*/
	public double objectCost(int pointer);
	
	/** updateCosts() does the same as this (and may call this or not) for all objects
	in order of increasing pointer.
	*/
	public void updateCost(int pointer);

}
