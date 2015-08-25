/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.start;
import static humanainet.common.CommonFuncs.*;
import java.io.File;
import java.io.IOException;
import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.acyc.ptr32.alwaysDedup.SimpleEconDAcycI;
import humanainet.common.MathUtil;
import humanainet.common.Files;
import humanainet.mindmap.MindmapUtil;
import humanainet.mindmap.io.OldDataFormatYear2015;
import humanainet.ui.HumanainetWindow;
import humanainet.xob.XobTypes;
import humanainet.xob.XobUtil;

public class Start{
	private Start(){}
	
	public static void main(String args[]) throws Exception{
		//Do this jselfmodify thing once when system starts to make files inside *.jar available etc
		humanainet.jselfmodify.PluginLoader.loadFirstPlugins();
		boolean includeDefs = true;
		if(args.length > 0 && args[0].endsWith(".humanainet")){ //load *.humanainet file
			//TODO which data format is it? OldDataFormatYear2015 or the newer acyc?
			//For now use OldDataFormatYear2015 because I'm still building acyc.
			//This will load OldDataFormatYear2015 data format into
			//the new acyc datastructs experimentally...
			File f = new File(args[0]);
			System.out.println("Loading file "+f.getAbsolutePath());
			byte b[] = Files.read(f);
			//boolean includeDefs = false; //TODO
			OldDataFormatYear2015.loadNamesPrilistsAndDefsFromBytes(b, Glo.econacyc, includeDefs);
		}else{
			//TODO this was working but commented it to load intro... loadEmptyRootOfPrilist();
			
			byte b[] = (byte[]) jsmGet("/files/data/humanainet/mindmap/humanainetIntro.humanainet");
			OldDataFormatYear2015.loadNamesPrilistsAndDefsFromBytes(b, Glo.econacyc, includeDefs);
		}
		new HumanainetWindow();
		System.out.println("EconAcyc nodes: "+Glo.econacyc.size());
		System.out.println("gets: "+((SimpleEconDAcycI)Glo.econacyc).gets());
		System.out.println("puts: "+((SimpleEconDAcycI)Glo.econacyc).puts());
		System.out.println("aveCyclesPerGet: "+((SimpleEconDAcycI)Glo.econacyc).aveCyclesPerGet());
		System.out.println("aveCyclesPerPut: "+((SimpleEconDAcycI)Glo.econacyc).aveCyclesPerPut());
		System.out.flush();
		
		System.out.println("Testing duplicates...");
		int ptrs[] = Glo.econacyc.pointers(0, Integer.MAX_VALUE);
		for(int i=0; i<ptrs.length; i++){
			int x = MathUtil.strongRand.nextInt(ptrs.length);
			int y = MathUtil.strongRand.nextInt(ptrs.length);
			int temp = ptrs[x];
			ptrs[x] = ptrs[y];
			ptrs[y] = temp;
		}
		for(int ptr : ptrs){
			int left = Glo.econacyc.left(ptr);
			int right = Glo.econacyc.right(ptr);
			int sizeBefore = Glo.econacyc.size();
			int ptr2 = Glo.econacyc.pair(left, right);
			int sizeAfter = Glo.econacyc.size();
			if(ptr != ptr2) throw new RuntimeException("Duplicate: "+ptr+" and "+ptr2);
			//System.out.println("Paired the childs of "+ptr+" ("+left+" and "+right+") again and correctly got same pointer.");
		}
		System.out.println("Tests for avoiding duplicates pass (and if so, why are there the same number of calls of put as nodes?, or has that changed?).");
		System.out.println("EconAcyc nodes: "+Glo.econacyc.size());
		System.out.println("gets: "+((SimpleEconDAcycI)Glo.econacyc).gets());
		System.out.println("puts: "+((SimpleEconDAcycI)Glo.econacyc).puts());
		System.out.println("aveCyclesPerGet: "+((SimpleEconDAcycI)Glo.econacyc).aveCyclesPerGet());
		System.out.println("aveCyclesPerPut: "+((SimpleEconDAcycI)Glo.econacyc).aveCyclesPerPut());
		System.out.flush();
	}
	
	/** Dont use this if a mindmap is loaded.
	Doesnt overwrite most of prilist contents except the MindmapUtil.listOfPrilistsName node,
	but thats still an important loss to a mindmap if it was loaded.
	*/
	public static void loadEmptyRootOfPrilist(){
		int ptrRootName = XobUtil.tokenGlobal(MindmapUtil.listOfPrilistsName);
		int ptrUntypedPrilist = 0; //empty
		int ptrPrilist = Acyc32Util.wrapObjectInType(Glo.econacyc, XobTypes.typeListPow2OfToken, ptrUntypedPrilist);
		int ptrUntypedDef = 0; //empty
		int ptrDef = Acyc32Util.wrapObjectInType(Glo.econacyc, XobTypes.typeListPow2OfListPow2OfToken, ptrUntypedDef);
		int mindmapItem = MindmapUtil.mindmapItemGlobal(ptrRootName, ptrPrilist, ptrDef);
		Glo.events.set(ptrRootName, mindmapItem);
	}

}
