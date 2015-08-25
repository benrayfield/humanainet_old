/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.stream;

/** A terminal may read, write, or both */
public interface Terminal{
	
	public InStream in();
	
	/** Normal and error messagages should both go to the same stream,
	and over time we will agree on what data format works for that.
	It could be as simple as the even bits are each 0 or 1 to say
	if the next bit is normal or error, but I wouldnt want to waste
	that much bandwidth. The important thing is in a unified system,
	things have only 1 input and 1 output and if its split
	it is done outside of streaming.
	*/
	public OutStream out();

}
