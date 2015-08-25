/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;

/** References are counted separately from inside and outside the acyc.
A pointer cant be moved while any references to it exist.
<br><br>
A RefCounter counts pointers/references to each address in itself as an acyc.
There are 2 main purposes of this, which some subclasses may use either or both of:
moving of addresses can only happen when they have 0 references. 
EconAcyc uses refCount in its cost calculations.
<br><br>
Subtypes have specific address types like int, long, or SHA256HashCons
which each need their own number type, and I dont want to use java autoboxing.
<br><br>
TODO after this software is working in java,
create a version of it in C so can avoid duplicating reference counting code
and do it all manually. For now continue prototyping in java
and always maintain the java version for compatibility,
but expand to multiple languages later. It can be done much faster in C.
*/
public interface RefCounter extends Acyc{
	
}
