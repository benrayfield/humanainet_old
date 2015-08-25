/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

import humanainet.acyc.Const;
import humanainet.acyc.Glo;
import humanainet.xob.XobUtil;
import humanainet.xorlisp.old.DebugUtil;

public class Acyc32Util{
	private Acyc32Util(){}
	
	/** If its an instance of typeVal, returns its type. Else returns ()/nil/0.
	Types are compared exactly without the ability of inheritance.
	<br><br>
	TODO? Inheritance may be added in a later version
	as type being a bitstring and subtypes ending with that
	which would be efficient since listOfPowerOf2Item
	takes log (or is it log squared) time and memory for large changes to a stack.
	and at most log time and memory for single item changes to the top of stack.
	*/
	public static int typeOf(Acyc32 acyc, int pointer){
		if(acyc.left(pointer) == Const.typVal){
			int typeAndValue = acyc.right(pointer);
			return acyc.left(typeAndValue);
		}
		return 0;
	}
	
	/** If its an instance of typVal, returns its value. Else throws.
	OLD: If its an instance of typVal, returns its value. Else returns itself.
	*/
	public static int valueOf(Acyc32 acyc, int typedObject){
		if(acyc.left(typedObject) != Const.typVal){
			String err = "Not a typed object: "+typedObject+" itsLeft="+acyc.left(typedObject)+" itsRight="+acyc.right(typedObject)+" acycSize="+acyc.size();
			System.err.println(err);
			System.err.println("typedObject TextUtil32.toString: "
				+TextUtil32.toString(acyc, typedObject, DebugUtil.defaultNamesForDebugging));
			System.err.println("If its not typed and is instead a listPow2 of bits: "
				+XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, typedObject)); //if its not a typed object, as in this error I'm looking for right now (TODO remove this testing code)
			throw new RuntimeException(err);
		}
		int typeAndValue = acyc.right(typedObject);
		return acyc.right(typeAndValue);
	}
	
	/** Returns true if its a bit or a use of typVal */
	public static boolean isObject(Acyc32 acyc, int pointer){
		if(pointer == Const.bit0 || pointer == Const.bit1) return true;
		if(pointer == 0) return false; // ()/nil
		return acyc.left(pointer) == Const.typVal;
	}
	
	public static boolean isTypedObject(Acyc32 acyc, int pointer){
		return acyc.left(pointer) == Const.typVal;
	}
	
	public static int wrapObjectInType(Acyc32 acyc, int type, int untypedValue){
		return acyc.pair(Const.typVal, acyc.pair(type, untypedValue));
	}
	
	/** If typedOrUntypedValue is already a typed value, unwraps it before wrapping in the new type *
	public static int wrapObjectInType(Acyc32 acyc, int type, int typedOrUntypedValue){
		int untypedObject;
		if(isTypedObject(acyc, typedOrUntypedValue)){
			untypedObject = valueOf(acyc, typedOrUntypedValue);
		}else{
			untypedObject = typedOrUntypedValue;
		}
		return acyc.pair(Const.typVal, acyc.pair(type, untypedObject));
	}*/

}
