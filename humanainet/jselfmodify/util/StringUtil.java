/** Ben F Rayfield offers JSelfModify under Apache 2, GNU LGPL, and/or Classpath Exception.
Open-source licenses are best understood with set-theory: Subsets, supersets,
and intersections of the legal permissions each license grants.
Example: If JSelfModify is combined with only the parts of HumanAINet that allow
GNU GPL 2 and 3 simultaneously, then JSelfModify can simultaneously be multilicensed
GNU GPL 2 and 3, or if JSelfModify is combined only with Apache HttpCore (Apache 2 license),
then it can be licensed as Apache 2 and GPL 3 because GPL 3 is a subset of Apache 2.
JSelfModify is the "glue code" for the set of softwares called Human AI Net. Together,
their intersection is GNU GPL 3, but smaller subsets may have bigger license intersections.
Most parts of Human AI Net allow GNU GPL version 2 and higher if used in such a subset.
JSelfModify is more general than Human AI Net and is compatible with most licenses.
*/
package humanainet.jselfmodify.util;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/** TODO REWRITE THIS TEXT. S means String. Audivolv should only use UTF-8 string encoding, which includes all symbols
in all Earth languages. If you see the infinity symbol here: 8 then UTF-8 is working
*/
public class StringUtil{
	private StringUtil(){}

	/** TODO not public */
	public static Random rand = new Random();

	private static WeakHashMap<String,Pattern> regexCache = new WeakHashMap<String,Pattern>();

	public static Pattern regexCache(String regex){
		Pattern p = regexCache.get(regex);
		if(p != null) return p;
		regexCache.put(regex, p = Pattern.compile(regex));
		return p;
	}
	
	/** TODO remove this var and hard-code "\n" everywhere instead. Audivolv should always use "\n",
	and if "\r" is needed instead, it should be transformed on-the-fly.
	*
	public static final String n = "\n";
	*/

	public static final String n(int quantity){
		//TODO optimize
		StringBuilder sb = new StringBuilder(quantity);
		for(int i=0; i<quantity; i++) sb.append('\n');
		return sb.toString();
	}
	
	private static final char hexDigits[] = "0123456789abcdef".toCharArray();
	
	public static String randHex(int howManyDigits){
		char c[] = new char[howManyDigits];
		for(int i=0; i<howManyDigits; i++) c[i] = hexDigits[rand.nextInt(16)]; //TODO optimize
		return new String(c);
	}
	
	public static final String EMPTY[] = new String[0];
	
	public static final String EMPTYSQUARED[][] = new String[0][];
	
	//TODO Choose which software to use for regular-expressions with UTF-8.
	
	//TODO handle the "byte order mark",
	//and avoid using it whenever possible because it makes UTF-8 be stateful which is bad design.
	
	public static byte[] strToBytes(String s){
		try{
			return s.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException("Not get UTF-8 bytes from String: "+s, e);
		}
	}
	
	public static String bytesToStr(byte utf8Bytes[]){
		try{
			return new String(utf8Bytes, "UTF-8");
		}catch(UnsupportedEncodingException e){
			StringBuilder sb = new StringBuilder("Not convert UTF-8 bytes to String. bytes=");
			for(int i=0; i<utf8Bytes.length; i++){
				if(i > 0) sb.append(',');
				sb.append(((int)utf8Bytes[i])&255); //range 0 to 255
			}
			throw new RuntimeException(sb.toString(), e);
		}
	}
	
	public static int[] strToCodePoints(String s){
		int u[] = new int[s.codePointCount(0, s.length())];
		for(int i=0; i<s.length();){
			u[i] = s.codePointAt(i);
			i += Character.charCount(u[i]);
		}
		return u;
	}
	
	public static String codePointsToStr(int unicodeCodePoints[]){
		throw new RuntimeException("TODO");
	}

	public static String intsToString(int ints[]){
		StringBuilder sb = new StringBuilder("[");
		for(int i=0; i<ints.length; i++){
			if(i > 0) sb.append(", ");
			sb.append(ints[i]);
		}
		return sb.append("]").toString();
	}
	
	public static String errToString(Throwable t){
		Writer s = new StringWriter();
		t.printStackTrace(new PrintWriter(s));
		return normNewLines(s.toString());
	}
	
	/** Throws if not find that value in the specific property tag.
	Example: <property name="audivolv.buildDir" value="${basedir}"/>
	If maxRecursions is 0, "${basedir}" would be returned. If 1, would return the value of basedir.
	If 2, would find values in that if it had more ${...} vars.
	*/
	public static String getPropertyValueFromXml(String xml, String propertyName, int maxRecursions)
			throws Exception{
		String escapedPropertyName = Pattern.quote(propertyName);
		Pattern p = Pattern.compile("<\\s*property.*?\\sname=\\\""+escapedPropertyName+"\\\".*?>");
		Matcher m = p.matcher(xml);
		if(!m.find()) throw new Exception("Can not find property with name "+propertyName);
		String tag = xml.substring(m.start(), m.end());
		Pattern getValue = Pattern.compile("<.*?\\svalue=\\\"(.+?)\\\".*?>");
		Matcher matchValue = getValue.matcher(tag);
		if(!matchValue.matches()) throw new Exception(
			"xml tag with name "+propertyName+": "+tag+" does not match value regexp");
		String val = matchValue.group(1); //in parenthesis
		//Audivolv.log("In xml, property name "+propertyName+", value is "+val);
		if(maxRecursions > 0 && val.contains("$")){
			Pattern getVar = Pattern.compile("\\$\\{(.+?)\\}");
			Matcher matchVar = getVar.matcher(val);
			int i = 0;
			StringBuilder sb = new StringBuilder();
			while(matchVar.find()){
				sb.append( val.substring(i, matchVar.start()) );
				String recurseVarName = matchVar.group(1);
				String recurseVarVal = getPropertyValueFromXml(xml, recurseVarName, maxRecursions-1);
				sb.append(recurseVarVal);
				i = matchVar.end();
			}
			val = sb.append(val.substring(i)).toString();
		}
		val = val.replace("&lt;","<").replace("&gt;",">").replace("&quot;","\"").replace("&amp;","&").replace("&apos;","'");
		return val;
	}
	
	public static String repeat(int quantity, String s){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<s.length(); i++) sb.append(s);
		return sb.toString();
	}
	
	public static String join(String parts[], String mid){
		if(parts.length == 0) return "";
		String s = parts[0];
		for(int i=1; i<parts.length; i++) s += mid+parts[i];
		return s;
	}
	
	public static String flos(double flos[]){
		StringBuilder sb = new StringBuilder("flo[");
		for(double flo : flos) sb.append(flo).append(",");
		sb.setCharAt(sb.length()-1, ']'); //replace ',' with ']'
		return sb.toString();
	}
	
	public static String strDotDotDot(String s, int maxLen){
		//TODO create a function that will split at multiple places and put ...
		if(s.length() > maxLen) s = s.substring(0,maxLen-"...".length())+"...";
		return s;
	}
	
	public static String dotDotDotStr(int maxLen, String s){
		//TODO create a function that will split at multiple places and put ...
		if(s.length() > maxLen) s = "..."+s.substring(s.length()-(maxLen-"...".length()));
		return s;
	}
	
	public static int countNonoverlappingSubOfString(String substring, String s){
		int count = 0;
		while(s.contains(substring)){
			count++;
			s = s.substring(s.indexOf(substring)+substring.length());
		}
		return count;
	}
	
	public static String wrap(String s, int maxLineLen){
		StringBuilder sb = new StringBuilder();
		while(true){
			if(s.length() <= maxLineLen) return sb.append('\n').append(s).toString();
			String maxLine = s.substring(0, maxLineLen);
			int nIndex = maxLine.indexOf('\n');
			int nextLineLen = maxLineLen;
			if(nIndex == -1){ //does not already have a newline
				for(int i=maxLineLen; i>0; i--){ //find last whitespace before max line length
					if(isWhitespace(s.charAt(i))){
						if(sb.length() > 0) sb.append('\n');
						nextLineLen = i;
						break;
					}
				}
			}else{ //already has a newline
				nextLineLen = nIndex;
				if(sb.length() > 0) sb.append('\n');
			}
			String line = s.substring(0, Math.max(1,nextLineLen)); //1 skips whitespace char
			sb.append(line);
			s = s.substring(line.length());
			if(s.charAt(0) == '\n') s = s.substring(1);
		}
	}
	
	public static boolean isWhitespace(char c){
		//Char 13 (\r) is not whitespace because Audivolv can only use char 10 (\n) as newline.
		return c==' ' || c=='\t' || c=='\n';
	}
	
	//TODO Detect newline string local computer expects and save text files that way.
	//Do not modify binary files which contain the same newline strings.
	//List of newline strings: http://unicode.org/unicode/standard/reports/tr13/tr13-5.html


	
	/** Unicode defines approximately 9 newline chars. Only for those, replaces adjacent
	groups of chars with 1 char: '\n' (char 10), which is Audivolv's standard.
	A group has exactly 0 or 1 of each newline char.
	For example, "\r\n\n\r\n" is 3 newlines, separated this way: "\r\n", "\n\r", "\n". 
	Theoretically a very confused program could write files using 5 different chars per newline.
	They will also be converted to '\n' (char 10) per group of 5.
	Groups do not have to be the same size.
	It also works for "\r\n\n\r", which is these 2 newlines: "\r\n", "\n\r".
	They all become "\n" (char 10). The string never gets longer.
	<br><br>
	TODO create the opposite of this function to write files in local newline format on hard-drive.
	When read them into Audivolv, they must be read as "\n" (char 10).
	*/
	public static String normNewLines(String s){
		if(newlinesAreNormed(s)) return s;
		return changeNewLines(s, "\n");
	}

	public static String describeCharsOf(String s){
		StringBuilder sb = new StringBuilder("[charInts:");
		for(int i=0; i<s.length(); i++){
			if(i != 0) sb.append(',');
			sb.append((int)s.charAt(i));
		}
		return sb.append(']').toString();
	}
	
	/** Changes all groups of newline chars (excluding duplicates of the same char) to newLine.
	newLine must be a string defined by Unicode as a line delimiter.
	7/2009 I found that info at: http://unicode.org/unicode/standard/reports/tr13/tr13-5.html
	*/
	public static String changeNewLines(String s, String newLine){
		//TODO optimize
		//TODO test this with many types of newline chars and many char group sizes
		StringBuilder sb = new StringBuilder(s.length());
		//Each bit index is for a unique newline char type.
		//Bit index 1 is non-newline-chars.
		int bits = 1; //Start as non-newline-char
		//TODO It appears there is an error with using bits==1 this way. It probably needs a unique bit index not used in the switch below.
		for(char c : s.toCharArray()){
			switch(c){
				case '\r':
					if((bits&2) != 0){ //This char was already in the adjacent newline char group
						bits = 0; //Print the standard newline char to replace this group
					}
					else bits |= 2; //Remember this char is in the group. It can not occur 2 times.
				break;
				case '\n':
					if((bits&4) != 0) bits = 0;
					else bits |= 4;
				break;
				default:
					if(bits != 1){
						sb.append(newLine);
						bits = 1;
					}
				/*
				break;
				case '\u0085':
					if((bits&8) != 0) bits = 0;
					else bits |= 8;
				break;
				case '\u000b':
					if((bits&16) != 0) bits = 0;
					else bits |= 16;
				break;
				case '\u000c':
					if((bits&32) != 0) bits = 0;
					else bits |= 32;
				break;
				case '\u2028':
					if((bits&64) != 0) bits = 0;
					else bits |= 64;
				break;
				case '\u2029':
					if((bits&128) != 0) bits = 0;
					else bits |= 128;
				break;
				case '\u0025':
					if((bits&256) != 0) bits = 0;
					else bits |= 256;
				break;
				case '\u0015':
					if((bits&512) != 0) bits = 0;
					else bits |= 512;
				*/
			}
			if(bits == 1){
				//Use non-newline-char as it is
				sb.append(c);
			}else if(bits == 0){
				//Group of 1 or more newline chars becomes Audivolv's standard newline char
				sb.append(newLine);
				bits = 1; //Forget group of newline-chars that was consumed
			}
			//Continue the same group of newline chars
		}
		if(bits != 1) sb.append(newLine);
		return sb.toString();
	}
	
	/** Unicode defines approximately 9 newline chars.
	Returns false if it finds any of those except \n (char 10).
	<br><br>
	TODO research what fraction of other softwares use newline chars other than '\r' and '\n', and if that is rare, use only those 2.
	There should be only 1 newline char, but there are at least 2. Unicode proposes that more than 2 should exist, which complicates things.
	We should work toward less chars for newline instead of more. If most softwares ignore the extra chars for newline, this software should do the same.
	<br><br>
	This software (JSelfModify) uses '\n' as the main newline char, and there is no option to change that.
	It will be converted at runtime when reading/saving files, if necessary.
	*/
	public static boolean newlinesAreNormed(String s){
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			switch(c){
				case '\r':
				//This is the only char allowed to be newline: case '\n':
				case '\u0085':
				case '\u000b':
				case '\u000c':
				case '\u2028':
				case '\u2029':
				case '\u0025':
				case '\u0015':
					return false;
			}
		}
		return true;
	}

	public static List<String> commaSepToList(String commaSepList, boolean immutableList){
		StringTokenizer s = new StringTokenizer(commaSepList,",");
		List<String> list = new ArrayList<String>(s.countTokens());
		while(s.hasMoreTokens()) list.add(s.nextToken());
		if(immutableList) list = Collections.unmodifiableList(list);
		return list;
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
		return list.toArray(StringUtil.EMPTY);
	}
	
	/** returns the regex matches. String.split returns whats between the matches */
	public static String[] betweenSplit(String toSplit, String regex){
		//TODO test this
		//Matcher m = Pattern.compile(regex).matcher(toSplit);
		Matcher m = regexCache(regex).matcher(toSplit);
		List<String> list = new ArrayList<String>();
		while(m.find()) list.add(toSplit.substring(m.start(),m.end()));
		return list.toArray(StringUtil.EMPTY);
	}

	public static String flosToStr(double flo[]){
    	String s = "[";
    	for(int i=0; i<flo.length; i++) s += flo[i]+" ";
    	return s.trim()+"]";
    }
	
	/** TODO optimize */
	public static String[] lines(String s){
		return split(normNewLines(s), "\n");
	}
	
	/** Log level. Minimum value 0. Higher values cause more things to be logged. */
	public static int log = 2;
	
	/** For efficiency, there is no log(int,String) function,
	because that creates the String even if the int causes it to not be logged.
	Use the log function this way:
	S.log("text to write to log always");
	if(S.log>0)S.log("text to write to log if log-level is more than 0");
	if(S.log>1)S.log("text to write to log if log-level is more than 1");
	
	S.log(S.log1 ? null : "text to write to log if log-level is more than 1");
	*/
	public static void log(String line){
		//TODO append to path /log
		System.out.println(line);
	};

}
