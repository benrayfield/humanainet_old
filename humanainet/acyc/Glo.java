/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;
import humanainet.acyc.ptr32.CacheAcyc32;
import humanainet.acyc.ptr32.EconDAcycI;
import humanainet.acyc.ptr32.alwaysDedup.SimpleEconDAcycI;
import humanainet.acyc.ptr32.event.SimpleVarMap32;
import humanainet.acyc.ptr32.event.VarMap32;
import humanainet.xob.XobUtil;

/** global objects */
public class Glo{
	private Glo(){}
	
	//To be expanded with larger Acycs than just 32 bits.
	//There will be 64 bit kinds (more of database on harddrive or network) and hashcons kinds
	
	/** TDOO The main place data is stored. Secondarily it will be stored in SHA256 keys and bitstring values,
	which may be on a harddrive, network, in memory, or anywhere, which is also true of the econacycs.
	<br><br>
	TODO this needs to be expandable to more memory,
	or at least to choose a larger starting size, in later versions.
	One way to do that is to run it like a database, most of it being on harddrive, and maybe use paging.
	*/ 
	//public static final EconDAcycI econacyc = new SimpleEconDAcycI((byte)20); //faster for games
	public static final EconDAcycI econacyc = new SimpleEconDAcycI((byte)23);
	
	public static final CacheAcyc32 cacheacyc = new CacheAcyc32(econacyc);
	
	/** An example of an event is a new mindmapItem value for a name in the mindmap.
	Whoever creates that in the acyc should then call Glo.events.fireChangeEvent(ptrMindmapItemName)
	which will tell any VarListener32 which are listening to that address so,
	for example, if that is in an EditPrilist on screen it can update.
	*/ 
	public static VarMap32 events = new SimpleVarMap32(econacyc);
	
	/** A smaller eventStream of keyVal that only includes those from Glo.events when do Save action
	such as the save command in File menu. This is to prevent the saving of large numbers of slightly
	different versions of prilists and defs which are stored in Glo.events after every key press.
	Versions will be saved, but snapshot is only taken when save.
	Everything in here is also in Glo.events, except the specific eventStream listPow2 which contains them.
	*/
	public static VarMap32 eventsWhenSave = new SimpleVarMap32(econacyc);
	
	/** May be replaced by a larger EconAcyc while computing, so dont cache it for long.
	Updates done to the old one while changing to the new one will be copied over but only for a short time.
	This is an experimental system and I'll have to figure out how that works best.
	<br><br>
	Or hopefully the replacing of the EconAcyc can be instead done by continuous garbageCollection
	which is one of the planned designs of econacyc. Actually I'll go with that plan for now and comment this out.
	*
	public static EconDAcycI econacyc(){ return econacyc; }
	*/
	
	//public static final int test = XobUtil.tokenGlobal("maniPartsOfTheWindow");
	//static{ econacyc.oneMoreRefFromOutside(test); }

}
