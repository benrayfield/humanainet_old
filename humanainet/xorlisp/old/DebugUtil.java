package humanainet.xorlisp.old;

import java.util.HashMap;
import java.util.Map;

import humanainet.acyc.Const;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.xorlisp.XorlispVM32;

/** For use in Eclipse debugger (to be organized later into better code) where
it displays ints as toString(...) of pointers into the XorlispVM, so I can debug easier.
Actually Eclipse doesnt allow that for primitive types....
*/
public class DebugUtil{
	
	public static Map<Integer,String> defaultNamesForDebugging = new HashMap();
	static{
		defaultNamesForDebugging.put(Const.end, ".");
		defaultNamesForDebugging.put(Const.bit0, "<0>");
		defaultNamesForDebugging.put(Const.bit1, "<1>");
		defaultNamesForDebugging.put(Const.typVal, "<typVal>");
	}
	
	public static XorlispVM32 defaultInstanceForDebugging;
	
	public static String toString(int pointer){
		if(!defaultInstanceForDebugging.pointerExists(pointer)) return ""+pointer;
		return pointer+"="+TextUtil32.toString(
			defaultInstanceForDebugging, pointer, defaultNamesForDebugging);
	}

}
