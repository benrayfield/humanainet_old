/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;

/** Lambda is a queue automata. Its most accurate form is continuations,
which this implements by queueing 1 or a pair of immutable forest nodes
at the end of the current highest node as a new pair.
<br><br>
TODO I havent decided on a way to choose between queuing just the current node
which we are at the ( of where it starts, vs queueing any 2 of them in a pair,
and that may be where the difference between cons and pair comes from.
Will have to explore these things.
<br><br>
There is no need to make any other lambda calls than the current one which this is
since they are all represented in binary as sparse stream of ( and )
which it is this Execute's purpose to handle them all at once as a QUEUE AUTOMATA.
That will be added at a more abstract level which uses this core way of computing.
*/
public interface Execute extends Nav{

	/** Queues the current pair which we are on the lparen on,
	or if we are on a rparen then does nothing.
	<br><br>
	TODO should it queue something more complex, like pairs of things,
	if do it when on the rparen?
	*/
	public void queue();
	
	//IMPORTANT DESIGN UPDATE ABOUT QUEUE FUNC...
	//Actually this is a big issue, and queue() is not enough. It needs the ability to create pair
	//of any 2 objects in the Nav sequence, and since every object is passed twice,
	//once at its leftparen and once on its rightparen, that is technically doable,
	//but I dont know if its a good representation. For each 2 things that are queued,
	//pair them. For each odd numbered thing not paired, queue it by itself.
	//In theory this is enough to create any acyc shape, and thats what I need.
	//Also, it could be simplified by only allowing 1 object to be queued on left
	//and 1 on right, and if both are queued then pair them, or if only 1 is queued
	//then use it by itself. That way multiple queues dont need to happen.
	//Start this from ()/nil and queue nil twice to get (()()),
	//then in second time around the QUEUE AUTOMATA, queue () and (()())
	//either direction to get bit0=(()(()())) or bit1=((()())()) or typVal=((()())(()())),
	//and continue from there to build anything.
	//It works! This is what I wanted, and theres only 1 queue() function.
	//To avoid ever increasing height in acyc, can remove the left side after Nav passes it,
	//and just count how many rightparen remain after Nav.isHighest would be reached.
	//
	//Should 3rd and later calls of queue be ignored, or should they replace the existing left and right
	//of the pair being queued?
	//
	//Older writing about it...
	//TODO need to be able to 2 only these 2 things: queue a single object, or queue the pair of any 2 objects
	//I've decided to only queue 1 object at a time and when the stack finishes and the queue is entered,
	//some other process, maybe like the cons function in lisp, may be used in various combinations.
	//I'm not sure how this is going to work out, since I really need to have a way to
	//queue a pair of any 2 objects chosen during Nav so they dont all become smaller and smaller
	//as we go around the queue repeatedly. It needs to be able to represent this:
	// ( (lambda (x) (x x)) (lambda (y) (y y)) ) which returns itself and infinite loops,
	//and I want to represent that infinite loop as a pair which if navigated you find
	//yourself at a pair you created earlier so you know you're infinite looping
	//and reached the same state again. This doesnt cover expanding complexity
	//while not knowing if you'll halt or not, but its an advanced feature I want.

}
