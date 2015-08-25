/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

import humanainet.acyc.RefCounter;

public interface RefCounter32 extends RefCounter, Acyc32{
	
	/** quantity of incoming references to pointer from outside the Acyc */
	public int howManyRefsFromOutside(int pointer);
	
	/** quantity of incoming references to pointer from inside the Acyc */
	public int howManyRefsFromInside(int pointer);
	
	/** howManyRefsFromOutside(pointer)+howManyRefsFromInside(pointer) */
	public int howManyRefs(int pointer);
	
	public void oneMoreRefFromInside(int pointer);
	
	public void oneLessRefFromInside(int pointer);
	
	public void oneMoreRefFromOutside(int pointer);
	
	public void oneLessRefFromOutside(int pointer);
	
	public boolean hasRefFromInside(int pointer);
	
	public boolean hasRefFromOutside(int pointer);
	
	/** True if has any incoming references. If false, can move this address. */
	public boolean hasRef(int pointer);
	
	//TODO funcs for incrementing and decrementing and checking count of references
	
	//TODO Where should the functions go that create objects representing
	//external objects that reference other objects? Or maybe these integers
	//should each represent a single reference (or pair of references?
	//Probably go single, Or maybe second of them is null which is cheap.)

}