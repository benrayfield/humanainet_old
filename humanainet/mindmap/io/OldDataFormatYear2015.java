package humanainet.mindmap.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import humanainet.acyc.Acyc;
import humanainet.acyc.Const;
import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.common.Nanotimer;
import humanainet.common.Text;
import humanainet.mindmap.MindmapUtil;
import humanainet.mindmap.ParseUtil;
import humanainet.xob.XobUtil;

/** Today is 2015-7-22 and I'm in the middle of a long process of designing a new data format
based on xorlisp's EconAcyc, or maybe just Acyc, which is an immutable binary forest.
My old mindmaps are in a text based data format and are in files ending with ".humanainet".
New files will probably have that same file extension, or maybe something about xorlisp or acyc.
*/
public class OldDataFormatYear2015{
	
	/** Reads the old data format from year 2015 into the new datastruct Acyc */
	public static void loadNamesPrilistsAndDefsFromBytes(byte b[], Acyc putDataHere, boolean includeDefs){
		Nanotimer t = new Nanotimer();
		System.out.println("Loading mindmap defs from: "+b.length+" bytes.");
		//long nameBeingDefined = -1;
		String nameBeingDefined = null;
		int ptrTypedName = 0, ptrPrilist = MindmapUtil.emptyPrilist, ptrDef = MindmapUtil.emptyDef;
		//String sPlusEmptyLine = StringUtil.bytesToStr(b)+"\r\n";
		//List<Long> tokensOfNextDef = null;
		//Set<MutScalar> addingEdges = null;
		String s = new StringBuilder("\r\n").append(Text.bytesToString(b)).append("\r\n").toString();
		//boolean foundDataStartLine = false; //Data starts after a line containing only __DATA__
		for(String line : Text.lines(s)){
			log("Line: "+line);
			line = line.trim();
			if(line.isEmpty()){ //start new name
				if(nameBeingDefined != null){ //assemble name, prilist, andOr def into new mindmapItem
					ptrTypedName = XobUtil.tokenGlobal(nameBeingDefined); //(typVal (typeToken name))
					int ptrMindmapItem = MindmapUtil.mindmapItemGlobal(ptrTypedName, ptrPrilist, ptrDef);
					log("Mindmap finishing ptrMindmapItem="+ptrMindmapItem
						+" name="+ptrTypedName+" prilist="+ptrPrilist+" def="+ptrDef);
					Glo.events.set(ptrTypedName, ptrMindmapItem);
					
					//test
					int ptrNameAsPowerOf2ListOfBit = Acyc32Util.valueOf(Glo.econacyc, ptrTypedName);
					String name = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, ptrNameAsPowerOf2ListOfBit);
					List<String> prilist = MindmapUtil.mindmapItemPrilistOfStrings(Glo.cacheacyc, ptrMindmapItem);
					System.out.println("New mindmap item ptrName="+ptrTypedName+" name="+name+" (ignoring def) prilist="+prilist);
				}
				nameBeingDefined = null;
			}else if(line.startsWith(".def[")){
				if(includeDefs){
					if(nameBeingDefined == null) throw new RuntimeException("def without name. line=["+line+"]");
					if(!line.endsWith("]")) throw new RuntimeException(
						"Line starts with .def[ but not ends with ]  line=["+line+"]");
					String lineContent = line.substring(".def[".length(), line.length()-"]".length());
					
					List<List<String>> listOfLines = MindmapUtil.listOfLines(lineContent,true);
					System.out.println("listOfLines for nameBeingDefined="+nameBeingDefined+" lines = "+listOfLines);
					ptrDef = MindmapUtil.listOfLinesGlobal(listOfLines);
				}
			}else if(line.startsWith(".prilist[")){
				if(nameBeingDefined == null) throw new RuntimeException("prilist without name. line=["+line+"]");
				if(!line.endsWith("]")) throw new RuntimeException(
					"Line starts with .prilist[ but not ends with ]  line=["+line+"]");
				String lineContent = line.substring(".prilist[".length(), line.length()-"]".length());
				//log("Prilist of name="+nameBeingDefined+" = ["+lineContent+"]");
				String tokens[] = ParseUtil.parseWhitepsaceSeparatedList(lineContent);
				//log("Parsed tokens of prilist of name="+nameBeingDefined+" = "+Arrays.asList(tokens));
				/*int ptrListOfToken = 0;
				for(String token : tokens){
					int ptrToken = MindmapUtil.tokenGlobal(token);
					ptrListOfToken = XobUtil.addPrefixToListOfPowerOf2Items(Glo.econacyc, ptrToken, ptrListOfToken);
				}
				int ptrTypedListOfToken = TODO;
				"TODO use MindmapUtil.typedListPow2OfTokenGlobal"
				*/
				ptrPrilist = MindmapUtil.typedListPow2OfTokenGlobal(Arrays.asList(tokens));
			}else{
				String ss[] = Text.split(line, "\\s+");
				//System.out.println("Not def or prilist, should be a name: "+Arrays.asList(ss));
				if(ss.length == 1){
					//start next mindmapItem...
					nameBeingDefined = line.trim();
					ptrPrilist = MindmapUtil.emptyPrilist;
					ptrDef = MindmapUtil.emptyDef;
					//log("Mindmap next name="+nameBeingDefined);
				}else{
					throw new RuntimeException("Multiple tokens on line: "+line);
				}
			}
		}
		System.out.println("After got bytes of file, loading OldDataFormatYear2015 took "+t.secondsSinceLastCall()+" seconds.");
	}
	
	public static void log(String line){
		System.out.println(line);
	}

}