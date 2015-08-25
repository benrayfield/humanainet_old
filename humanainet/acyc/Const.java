/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;

/** Objects which are always at the same address in every xorlisp implementation,
while those addresses may vary in number of bits, so these are always the low bits.
*/
public class Const{
	private Const(){}
	
	public static final int end = 0;
	
	public static final int pairOfEnd = 1;
	
	public static final int bit0 = 2;
	
	public static final int bit1 = 3;
	
	public static final int typVal = 4;
	
	/*end("()"),
	
	pairOfEnd("(()())"),
	
	bit0("(()(()()))"),
	
	bit1("((()())())"),
	
	typVal("((()())(()())) - (typVal (type value)) can be used to say any object has a certain type");
	//pairOfPairOfEnd("((()())(()()))");
	
	String description;
	
	Const(String description){
		this.description = description;
	}*/

}
