/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;
import java.util.HashMap;
import java.util.Map;

public class TextUtil32{
	private TextUtil32(){}
	
	public static String toString(Acyc32 x, int cons){
		return toString(x, cons, new HashMap());
	}
	
	public static String toString(Acyc32 x, int cons, Map<Integer,String> names){
		StringBuilder sb = new StringBuilder();
		toString(x, sb, cons, names);
		return sb.toString();
	}
	
	public static void toString(Acyc32 x, StringBuilder sb, int cons, Map<Integer,String> names){
		String name = names.get(cons);
		if(name != null){
			sb.append(name);
		}else{
			sb.append('(');
			if(cons != 0){ //Const.end
				toString(x, sb, x.left(cons), names);
				toString(x, sb, x.right(cons), names);
			}
			sb.append(')');
		}
	}

}
