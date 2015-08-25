/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import humanainet.acyc.Const;
import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.acyc.ptr32.CacheAcyc32;
import humanainet.acyc.ptr32.RefCounter32;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.common.Text;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.xob.XobTypes;
import humanainet.xob.XobUtil;
import humanainet.xob.ptr32.ListPow2;
import humanainet.xorlisp.SimpleLisp32;
import humanainet.xorlisp.old.DebugUtil;

public class MindmapUtil{
	
	public static final String listOfPrilistsName = "prilistPrilist";

	public static final String otherOrEmptyName = "(other/empty)";
	
	//TODO funcs for prilist and def, including paragraphs in def,
	//and the same as any other bitstring the UTF16 bits of tokens in prilist and paragraph.
	
	
	//many type ints were moved to humanainet.xob.Types
	
	/** A listPow2 of name, then prilist, then def, which are each typed objects.
	Instead of the outer type being general listPow2, its this typeMindmapItem.
	*/
	public static final int typeMindmapItem = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, "mindmapItem");
	static{ Glo.econacyc.oneMoreRefFromOutside(typeMindmapItem); }
	
	/** Derived from simpler types, so if you happen to build an empty prilist, it will equal this automatically */
	public static final int emptyPrilist = typedListPow2OfTokenGlobal(Arrays.<String>asList());;
	static{ Glo.econacyc.oneMoreRefFromOutside(emptyPrilist); }
	
	/** Derived from simpler types, so if you happen to build an empty def, it will equal this automatically */
	public static final int emptyDef = typedListPow2OfTokenGlobal(Arrays.<String>asList());
	static{ Glo.econacyc.oneMoreRefFromOutside(emptyDef); }
	
	
	/** hoursDone and hoursTotal are optionally displayed left of every mindmapItem. */
	public static final int hoursDone = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, "hoursDone");
	static{ Glo.econacyc.oneMoreRefFromOutside(hoursDone); }
	
	/** hoursDone and hoursTotal are optionally displayed left of every mindmapItem. */
	public static final int hoursTotal = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, "hoursTotal");
	static{ Glo.econacyc.oneMoreRefFromOutside(hoursTotal); }
	
	public static int typedListPow2OfToken(Acyc32 acyc, int type, List<String> tokens){
		int ptrListOfToken = 0;
		//Since listOfPowerOf2Item are efficiently created by adding prefix, loop in reverse
		for(int i=tokens.size()-1; i>=0; i--){
		//for(String token : tokens){
			String token = tokens.get(i);
			int ptrToken = XobUtil.tokenGlobal(token);
			ptrListOfToken = XobUtil.addPrefixToListPow2(Glo.econacyc, ptrToken, ptrListOfToken);
		}
		return acyc.pair(Const.typVal, acyc.pair(type, ptrListOfToken));
	}
	
	public static int typedListPow2OfTokenGlobal(List<String> tokens){
		return typedListPow2OfToken(Glo.econacyc, XobTypes.typeListPow2OfToken, tokens);
	}
	
	/** Returns a listPow2 of listPow2 of token. At each level, including the top, is a type.
	This can be used to create a mindmap def.
	TODO use list of lines instead of list of paragraphs, or maybe list of paragraphs
	which each can have multiple lines, which are of course list of token which are each listPow2 of bits.
	*/
	public static int listOfLinesGlobal(List<List<String>> listOfParagraphs){
		int listPow2OfParagraph = 0;
		for(int i=listOfParagraphs.size()-1; i>=0; i--){
			int nextParagraph = typedListPow2OfTokenGlobal(listOfParagraphs.get(i));
			listPow2OfParagraph = XobUtil.addPrefixToListPow2(Glo.econacyc, nextParagraph, listPow2OfParagraph);
		}
		return Glo.econacyc.pair(
			Const.typVal,
			Glo.econacyc.pair(XobTypes.typeListPow2OfListPow2OfToken, listPow2OfParagraph)
		);
	}
	
	/** See comment on typeMindmapItem */
	public static int mindmapItemGlobal(int name, int prilist, int def){
		return Glo.econacyc.pair(
			Const.typVal,
			Glo.econacyc.pair(
				typeMindmapItem,
				Glo.econacyc.pair( name, Glo.econacyc.pair(prilist, def) ) //listPow2 of name, prilist, def
			)
		);
	}
	
	/** See comment on typeMindmapItem (or mindmapItemGlobal func) for data format */ 
	public static String mindmapItemGetNameString(CacheAcyc32 cAcyc, int ptrMindmapItem){
		int untypedMindmapItem = Acyc32Util.valueOf(cAcyc.acyc, ptrMindmapItem);
		RefCounter32 x = cAcyc.acyc;
		int mindmapItemName = x.left(untypedMindmapItem); //This object has type typeToken
		int untypedName = Acyc32Util.valueOf(cAcyc.acyc, mindmapItemName);
		return XobUtil.listOfPowerOf2ToString(cAcyc, untypedName);
	}
	
	public static int mindmapItemPrilist(Acyc32 acyc, int ptrMindmapItem){
		int untypedMindmapItem = Acyc32Util.valueOf(acyc, ptrMindmapItem); //(name (prilist def))
		int prilistAndDef = acyc.right(untypedMindmapItem);
		return acyc.left(prilistAndDef);
	}
	
	public static int mindmapItemName(Acyc32 acyc, int ptrMindmapItem){
		int untypedMindmapItem = Acyc32Util.valueOf(acyc, ptrMindmapItem); //(name (prilist def))
		return acyc.left(untypedMindmapItem);
	}
	
	public static int mindmapItemDef(Acyc32 acyc, int ptrMindmapItem){
		int untypedMindmapItem = Acyc32Util.valueOf(acyc, ptrMindmapItem); //(name (prilist def))
		int prilistAndDef = acyc.right(untypedMindmapItem);
		return acyc.right(prilistAndDef);
	}
	
	public static List<String> mindmapItemPrilistOfStrings(CacheAcyc32 cAcyc, int ptrMindmapItem){
		int ptrTypedPrilist = mindmapItemPrilist(cAcyc.acyc, ptrMindmapItem);
		int ptrPrilist = Acyc32Util.valueOf(cAcyc.acyc, ptrTypedPrilist);
		//List<Xob> new ListPow2(acyc, ptrMindmapItem)
		//Its a listPow2 of listPow2 of bit
		int pointers[] = XobUtil.pointersInListPow2(cAcyc.acyc, ptrPrilist);
		for(int i=0; i<pointers.length; i++){
			int ptrUntypedStringAsPowOf2ListOfBit = Acyc32Util.valueOf(cAcyc.acyc, pointers[i]);
			String nameInPrilist = XobUtil.listOfPowerOf2ToString(cAcyc, ptrUntypedStringAsPowOf2ListOfBit);
			//System.out.println("pointers["+i+"]="+pointers[i]+" and its string form is "+nameInPrilist);
		}
		List<String> prilistOfStrings = new ArrayList();
		for(int ptrTypedString : pointers){
			int ptrStringAsPowOf2ListOfBit = Acyc32Util.valueOf(cAcyc.acyc, ptrTypedString);
			String nameInPrilist = XobUtil.listOfPowerOf2ToString(cAcyc, ptrStringAsPowOf2ListOfBit);
			//nameInPrilist = "-1 -5 "+nameInPrilist;
			prilistOfStrings.add(nameInPrilist);
		}
		return Collections.unmodifiableList(prilistOfStrings);
	}
	
	public static boolean isMindmapItem(Acyc32 acyc, int pointer){
		if(acyc.left(pointer) != Const.typVal) return false;
		int typeAndValue = acyc.right(pointer);
		return acyc.left(typeAndValue) == typeMindmapItem;
	}
	
	/** def (definition) objects have type typeListPow2OfListPow2OfToken.
	Depending if def is organized by lines or paragraphs,
	you will want 1 or 2 "\r\n" as betweenLinesOrParagraphs.
	*/
	public static String formatDefForTextEditing(int ptrTypedDef, String betweenLinesOrParagraphs){
		if(ptrTypedDef == 0) return ""; //new def
		int untypedOuterList = Acyc32Util.valueOf(Glo.econacyc, ptrTypedDef);
		int pointersToTypedLists[] = XobUtil.pointersInListPow2(Glo.econacyc, untypedOuterList);
		boolean firstList = true;
		StringBuilder sb = new StringBuilder();
		for(int ptrTypedList : pointersToTypedLists){
			if(!firstList) sb.append(betweenLinesOrParagraphs);
			int untypedInnerList = Acyc32Util.valueOf(Glo.econacyc, ptrTypedList);
			int pointersToTypedTokens[] = XobUtil.pointersInListPow2(Glo.econacyc, untypedInnerList);
			for(int ptrTypedToken : pointersToTypedTokens){
				//token can be all whitespace or all nonwhitespace
				//System.out.println("ptrTypedToken="+ptrTypedToken);
				int untypedToken = Acyc32Util.valueOf(Glo.econacyc, ptrTypedToken);
				//try{
					String token = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, untypedToken);
					//String token = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, ptrTypedToken);
				//	System.out.println("Token=["+token+"]");
					sb.append(token);
				//}catch(Exception e){
				//	System.err.println(TextUtil32.toString(Glo.econacyc, ptrTypedToken, DebugUtil.defaultNamesForDebugging));
				//	throw new RuntimeException(e);
				//}
			}
			firstList = false;
		}
		return sb.toString();
	}
	
	/** Paragraphs are separated by ... with whitespace around it.
	In the new data format, a def is a list of paragraphs, and a paragraph is a list of tokens.
	A token may be a url, word, or anything else made of visible chars without whitespace.
	<br><br>
	TODO in a later version this will be done by lines and maybe another layer of paragraphs made of lines.
	*/
	public static List<List<String>> listOfLines(
			String anyText, boolean changeDotDotDotBetweenWhitespaceToTwoNewlines){
		if(changeDotDotDotBetweenWhitespaceToTwoNewlines){
			anyText = anyText.replaceAll("\\s+\\.\\.\\.\\s+", Text.n+Text.n);
		}
		//String paragraphs[] = Text.split(anyText, "\\s+\\.\\.\\.\\s+");
		String lines[] = Text.lines(anyText);
		int ptrListOfParagraph = 0;
		List<List<String>> listOfLines = new ArrayList();
		int consecutiveEmptyLists = 0;
		for(String line : lines){
			if(line.isEmpty()){
				consecutiveEmptyLists++;
				if(consecutiveEmptyLists <= 1) listOfLines.add(Collections.EMPTY_LIST);
			}else{
				consecutiveEmptyLists = 0;
				//log("Paragraph in def of name="+nameBeingDefined+" = ["+paragraph+"]");
				List<String> tokensInParagraph =
					ParseUtil.parseNaturalLanguageAndSymbolsUrlsAndNumbers(line);
				listOfLines.add(tokensInParagraph);
				//log("Tokens in paragraph = "+tokensInParagraph);
			}
		}
		return Collections.unmodifiableList(listOfLines);
	}
	
	/** For mindmapItems displayed with hoursDone and hoursTotal props like: 15 20 workOnThatThing */
	public static String afterLastSpace(String s){
		int i = s.lastIndexOf(' ');
		return i==-1 ? s : s.substring(i+1, s.length());
	}

}