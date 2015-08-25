/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap;
//import datastruct.Namespace;
//import datastruct.NsNode;
import java.util.*;

import humanainet.common.Text;
//import jselfmodify.util.StringUtil;
import humanainet.datastruct.IntRange;

public class ParseUtil{
	private ParseUtil(){}
	
	public static final String n = "\r\n";

	public static final Set<String> betweenStrings;
	static{
		betweenStrings = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"", n," ","\t","[","]","{","}","(",")","<",">",
			"?",".","!",";",":","\"","\'",",",
			"*","/","+","-","%","=",
			"~","`","@","$","^","&","|","\\")));
		/*for(String b : betweenStrings){
			Namespace.createSysNode(b);
		}
		Namespace.createSysNode(MindMapUtil.otherOrEmptyName);
		*/
	}
	
	public static String[] parseWhitepsaceSeparatedList(String s){
		return Text.split(s, "\\s+");
	}
	
	/** TODO paragraphs are separated by empty line,
	or in the oldYear2015 data format by ... with whitespace around it.
	<br><br>
	Parses space, endLine, commas, etc, into approximately the same string if you concat
	the returned tokens, except "\n" or "\r" by itself (or "\n\r" if aligned that way) will all become "\r\n".
	<br><br>
	TODO Should there be an extra parsing step to join adjacent tokens around a decimal point?
	The difference is a period ending a sentence has any kind of whitespace after it or is last token.
	<br><br>
	Merges decimal points into numbers, decimal points and other chars into URLs, and maybe other things.
	<br><br>
	Heres some examples of the things I'm writing code (TODO) to handle as single tokens...
	<br><br>
	3f4.d33
	<br><br>
	.d33
	<br><br>
	Not 3f4. because thats an integer and would not normally have a decimal point, and in the worst case gets a period.
	<br><br>
	http://code.google.com/index.html
	<br><br>
	sha256:len://af234234ffdcca53af234234ffdcca53af234234ffdcca53af234234ffdcca53:43f
	<br><br>
	http://en.wikipedia.org/wiki/Parenthesis#Parentheses_(_)
	<br><br>
	The URL examples very much complicate my syntax. It may be better to have less rules, as long as all the content
	gets put together the same way from some way of dividing it into tokens. Below list the ways of dividing tokens
	that are really important, and start from there in a simpler design...
	<br><br>
	. ? ! ; :
	I was afraid of that. Some of these occur in URLs. Maybe I need to parse URLs and numbers first like I did for lines.
	*/
	public static List<String> parseNaturalLanguageAndSymbolsUrlsAndNumbers(String s){
		List<String> tokens = new ArrayList<String>();
		int lastUrlEnd = 0;
		for(IntRange urlRange : findURLs(s)){
			String before = s.substring(lastUrlEnd, urlRange.start);
			tokens.addAll(parseBetweenURLs(before));
			tokens.add(s.substring(urlRange.start, urlRange.endExclusive));
			lastUrlEnd = urlRange.endExclusive;
		}
		String last = s.substring(lastUrlEnd);
		tokens.addAll(parseBetweenURLs(last));
		return Collections.unmodifiableList(tokens);
	}
	
	protected static List<String> parseBetweenURLs(String s){
		List<String> tokens = new ArrayList<String>();
		int lastThingEnd = 0;
		for(IntRange urlRange : findThingsContainingSpecialChars(s)){
			String before = s.substring(lastThingEnd, urlRange.start);
			tokens.addAll(parseNaturalLanguageAndSymbolsButNoURLsOrNumbers(before));
			tokens.add(s.substring(urlRange.start, urlRange.endExclusive));
		}
		String last = s.substring(lastThingEnd);
		tokens.addAll(parseNaturalLanguageAndSymbolsButNoURLsOrNumbers(last));
		return tokens;
	}
	
	public static List<String> parseNaturalLanguageAndSymbolsButNoURLsOrNumbers(String s){
		String lines[] = Text.lines(s); //TODO inline some of this code
		List<String> tokens = new ArrayList<String>();
		for(int i=0; i<lines.length; i++){
			if(i != 0) tokens.add(n);
			String line = lines[i];
			int start = 0;
			for(int j=0; j<line.length(); j++){
				char c = line.charAt(j);
				switch(c){
				case' ':case'\t':case'[':case']':case'{':case'}':case'(':case')':case'<':case'>':
				case'?':case'.':case'!':case';':case':':case'"':case'\'':case',':
				case'*':case'/':case'+':case'-':case'%':case'=':
				case'~':case'`':case'@':case'$':case'^':case'&':case'|':case'\\':
					if(start < j) tokens.add(line.substring(start,j));
					tokens.add(""+c);
					start = j+1;
				}
				//else the current token ranges start to this or some later j
			}
			if(start < line.length()) tokens.add(line.substring(start));
		}
		//String tokensArray[] = tokens.toArray(StringUtil.EMPTY);
		//tokensArray = mergeSomeTokens(tokensArray);
		//return tokensArray;
		return tokens;
	}
	
	/**
	public static String[] mergeSomeTokens(String t[]){
		List<String> merged = new ArrayList<String>();
		
		throw new RuntimeException("TODO use findURLs and findPossibleHexFloatNumbers");
	}*/
	
	/** Examples:
	<br><br>
	http://code.google.com/index.html
	<br><br>
	sha256:len://af234234ffdcca53af234234ffdcca53af234234ffdcca53af234234ffdcca53:43f
	<br><br>
	http://en.wikipedia.org/wiki/Parenthesis#Parentheses_(_)
	*/
	public static IntRange[] findURLs(String s){
		List<IntRange> ranges = new ArrayList<IntRange>();
		String urlCore = "://";
		int lastCoreStart = -urlCore.length();
		while(true){
			int coreStart = s.indexOf(urlCore, lastCoreStart+urlCore.length());
			lastCoreStart = coreStart;
			if(coreStart == -1) break;
			//search left
			int minLeft = ranges.isEmpty() ? 0 : ranges.get(ranges.size()-1).endExclusive;
			int left;
			for(left=coreStart-1; left>=minLeft; left--){
				char c = s.charAt(left);
				if(Character.isWhitespace(c) || left==minLeft){
					if(left != minLeft) left++;
					break;
				}
			}
			//TODO clean up and verify the logic for finding left border of URL,
			//especially in cases of string starts with URL or 2 adjacent URLs
			if(Character.isWhitespace(s.charAt(left))) left++;
			//search right
			int maxRightExclusive = s.length();
			int rightExclusive;
			for(rightExclusive=coreStart+urlCore.length(); rightExclusive<maxRightExclusive; rightExclusive++){
				char c = s.charAt(rightExclusive);
				if(Character.isWhitespace(c)){
					break;
				}
			}
			String url = s.substring(left,rightExclusive);
			if(!isURL(url)){
				throw new RuntimeException("Tried to parse URL but instead parsed["+url+"]");
			}
			ranges.add(new IntRange(left,rightExclusive));
		}
		return ranges.toArray(new IntRange[0]);
	}
	
	public static String getFirstUrlOrNull(String s){
		IntRange ranges[] = findURLs(s);
		if(ranges.length == 0) return null;
		return s.substring(ranges[0].start, ranges[0].endExclusive);
	}
	
	/** TODO improve accuracy of this function by making it return false more often */
	public static boolean isURL(String s){
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if(Character.isWhitespace(c)) return false;
		}
		if(!s.contains("://")) return false;
		return true;
		
	}
	
	/** Use findThingsContainingSpecialChars which is more general.
	Example: 3f4.D33 *
	public static IntRange[] findPossibleHexFloatNumbers(String s){
		throw new RuntimeException("TODO");
	};
	*/
	
	static long countSpecialCharMessageDisplays = 0;
	
	/** Example: java.util.List
	Example: package-info.html
	Example: index.html
	*/
	public static IntRange[] findThingsContainingSpecialChars(String s){
		if(countSpecialCharMessageDisplays < 100){
			System.err.println("TODO findThingsContainingSpecialChars is returning empty array. do real parsing of things like java.util.List instead.");
			countSpecialCharMessageDisplays++;
		}
		//throw new RuntimeException("TODO");
		return new IntRange[0];
	}
	

}