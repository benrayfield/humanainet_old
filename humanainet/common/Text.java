/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text{
	private Text(){}
	
	/** newline = \r\n */
	public static final String n = "\r\n";
	
	private static WeakHashMap<String,Pattern> regexCache = new WeakHashMap<String,Pattern>();
	
	public static Pattern regexCache(String regex){
		Pattern p = regexCache.get(regex);
		if(p != null) return p;
		regexCache.put(regex, p = Pattern.compile(regex));
		return p;
	}
	
	public static byte[] stringToBytes(String s){
		try{
			return s.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(unicodeMessage);
		}
	}
	
	private static String unicodeMessage = "UTF-8 is standard for string encoding. Its simple definition is on Wikipedia and can be copied into this software if your version of Java doesn't support it. Each byte starts with 0, 10, 110, or 1110, used to find alignment at unknown position in data, and the rest are the data.";
	
	public static String bytesToString(byte b[]){
		try{
			return new String(b, "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(unicodeMessage,e);
		}
	}
	
	public static String replaceFileExtension(String pathOrFilename, String newExtension){
		int lastDot = pathOrFilename.lastIndexOf('.');
		int lastSlash = Math.max(pathOrFilename.lastIndexOf('/'), pathOrFilename.lastIndexOf('\\'));
		if(lastSlash < lastDot){
			return pathOrFilename.substring(0,lastDot+1)+newExtension;
		}else{
			return pathOrFilename+'.'+newExtension;
		}
	}
	
	/** Uses BufferedReader which has the correct line behaviors which I know because it says:
	QUOTE Reads a line of text.  A line is considered to be terminated by any one
    of a line feed ('\n'), a carriage return ('\r'), or a carriage return
    followed immediately by a linefeed. UNQUOTE.
    */
	public static String[] lines(String s){
		List<String> lines = new ArrayList();
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line;
		try{
			while((line = br.readLine()) != null){
				lines.add(line);
			}
		}catch(IOException e){
			throw new RuntimeException(e); //StringReader wont do this
		}
		return lines.toArray(new String[0]);
	}
	
	/** Same as String.split but without the inconsistent behaviors related to empty matches at the end.
	If nothing matches, returns an array size 1 containing the original string.
	If 1 thing matches, returns an array size 2. If it matches at the end, the last thing is empty string.
	*/
	public static String[] split(String toSplit, String regex){
		//TODO test this
		//Matcher m = Pattern.compile(regex).matcher(toSplit);
		Matcher m = regexCache(regex).matcher(toSplit);
		List<String> list = new ArrayList<String>();
		int i = 0;
		while(m.find()){
			list.add(toSplit.substring(i,m.start()));
			i = m.end();
		}
		list.add(toSplit.substring(i,toSplit.length()));
		return list.toArray(new String[0]);
	}
	
	/** returns the regex matches. String.split returns whats between the matches */
	public static String[] betweenSplit(String toSplit, String regex){
		//TODO test this
		//Matcher m = Pattern.compile(regex).matcher(toSplit);
		Matcher m = regexCache(regex).matcher(toSplit);
		List<String> list = new ArrayList<String>();
		while(m.find()) list.add(toSplit.substring(m.start(),m.end()));
		return list.toArray(new String[0]);
	}

}
