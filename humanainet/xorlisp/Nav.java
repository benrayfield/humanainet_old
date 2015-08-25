/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp;

/** navigate the astronomically large space of abstract bits (created only when observed)
using the 2 commands of a debugger: upOrForward and down.
Forward is up then down. To navigate linearly you continue using upOrForward
and dont have to choose anything. To skip large sections, use down at various times.
I'm not sure if should include a third option called forward to directly skip
the current (...) or if up and down should be used. Something doesnt fit.
*/
public interface Nav{
	
	/** The next ( or ), or does nothing if at end of sparse stream of (s and )s. */
	public void linearForward();
	
	/** Starting from a (, skips to the matching ),
	or if you're already on a ) does linearForward,
	or does nothing if isHighest().
	*/
	public void treeForward();
	
	/** Branches an independent Nav of this same type (like Execute subtype)
	which continues in a separate QUEUE, which is a process of adding pairs to
	the highest (isHighest()) pair which both Navs started at.
	<br><br>
	The most common use of fork is to save your place in a stream,
	like java's InputStream.mark function, except can do it recursively.
	<br><br>
	TODO Should forks have names, at least a bit to know which branch you are?
	As it is, forks have no names since they dont change state of the system
	even if they queue many things and explore them recursively,
	but some users of the system may find it useful to merge the forks.
	*/
	public Nav fork();
	
	/** 1 is (, and 0 is ). That covers all possible data in the stack view of acyclicNet. */
	public boolean isLparen();
	
	/** True if at ( or ) of a ()/nil. Nil is defined as height 0. */
	public boolean isLowest();
	
	/** True when just started at the highest ( or ended at the last ). */
	public boolean isHighest();

}
