/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp.todoMaybe;

/** An integer at least 0 and no upper limit.
This is used as pointers between systems of different pointer sizes.
When the integer is represented as bits, the value 0 has no digits
because the highest base2 digit is a bit1 and there is no bit1 in value 0.
*/
public interface NonnegInt extends Comparable<NonnegInt>{
	
	public boolean isZero();
	
	public boolean isOdd();
	
	public NonnegInt timesTwo();
	
	/** If isOdd() returns this same integer */
	public NonnegInt halfIfEven();
	
	public NonnegInt plusOne();

	/** If isZero() then returns this same integer. See church encoding
	of lambda for another example of this. Some function which
	subtracts 1 returns 0 if parameter is 0.
	*/
	public NonnegInt minusOneIfNonzero();
	
	/** how many base2 digits */
	public NonnegInt digits();

}
