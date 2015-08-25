/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;

/** econBasedGarbageCollect on unifiedAcycnet solves the problem of
creating many new lispPair and not being able to decide which of them
to delete. As AI andOr people create more data, the complexity of the
system increases out of control because its hard to look deep into the
network to figure out what data is being reused enough and what data
is how valuable, considering all its combinations. Econacyc is a
simple rule for lispPair that every incoming pointer must pay an
equal fraction of the total cost of what it has a pointer to, for
each of its 2 pointers. If there are p incoming pointers to a
specific lispPair, then each of the owners of those pointers pays
costOfThePair/p gameMoney to the pair which has that cost. Each pair
must pay 1 gameMoney (per time) for its own existence plus that
fraction of cost for each of the other lispPair it points at.
nil must pay 1 and has no other costs since it is the only
lispPair that has no childs (so is not really a pair, unless
you use church encoding in which case its a pair of T and T,
but in econacyc, nil is a leaf and has a total cost of 1.
I expect to use this system in a majority of my uses of
unifiedAcycnet and to define how much I value each object
in the system, allowing them to be deleted (after I have a
chance to raise how much I value them) if I am not willing
to pay their totalCost. Cost of a lispPair increases when
objects are deleted which pointed at it so they are no longer
paying a fraction of its cost, so there are fewer incoming
pointers which each pay a bigger fraction totalling the same.
<br><br>
TODO To avoid duplicate code for reference counting,
after this is working in java (which does its own reference counting),
create a version of this software in C so can use this code
as the only reference counting and garbageCollection.
*/
public interface EconAcyc extends Acyc{
	
	/** updates the scalar cost of each object based on the updated costs of the 2 objects it points at,
	the only exception being nil which has a constant cost of 1.
	Cost of an object is the sum of 1 (for itself as a lispPair)
	and cost of each object it points at divided by that object's quantity of incoming pointers.
	<br><br>
	This is not the only way to update costs. You might update them in random order,
	or only update some and not others, which only calculates an approximate cost
	with many caching delays. This is the most reliable way. Its exact,
	but it also takes the longest as it calculates new cost for all objects.
	*/
	public void updateCosts();
	
	//TODO

}
