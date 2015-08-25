/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.xorlisp.ptr32;
import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.RefCounter32;
import humanainet.xob.XobUtil;
import humanainet.xob.ptr32.ListPow2;

/** The indexs in a listPow2 are opposite as they're used in XobUtil
because the end thats added to is the new end of the queue,
while the end used as 0 is anywhere in the listPow2.
*/
public class SimpleTagSys32 implements TagSys32{
	
	public final RefCounter32 acyc;
	
	public SimpleTagSys32(RefCounter32 acyc){
		this.acyc = acyc;
		acyc.oneMoreRefFromOutside(listPow2);
	}
	
	/** Starts as 0, empty */
	protected int listPow2;
	
	/** Cache of XobUtil.sizeOfListOfPowerOf2Items(acyc, powOf2List)
	which is always accurate since powOf2List and this cacheSize change together.
	*/
	protected int cacheSizeAll;
	
	protected int cacheSizeQueue;
	
	/** Starts as -1 which can only happen when its empty *
	protected int oldestIndex = -1;
	*/

	public boolean isEmpty(){ return listPow2 == 0; }
	
	public int sizeAll(){ return cacheSizeAll; }
	
	public int sizeQueue(){ return cacheSizeQueue; }


	public int removeOldest(){
		if(cacheSizeQueue == 0) throw new RuntimeException("Queue is empty");
		//TODO OPTIMIZE optimize by keeping pointer to current position in queue
		//so this costs average constant time instead of log
		int oldestIndex = cacheSizeAll-cacheSizeQueue;
		int ob = get(oldestIndex);
		cacheSizeQueue--; //remove ob by saying queue starts later, but keep it in history.
		return ob;
	}

	public void add(int ob){
		int oldList = listPow2;
		listPow2 = XobUtil.addPrefixToListPow2(acyc, ob, listPow2);
		acyc.oneLessRefFromOutside(oldList);
		acyc.oneMoreRefFromOutside(listPow2);
		cacheSizeAll++;
		cacheSizeQueue++;
	}

	public int oldestIndex(){ return cacheSizeAll-cacheSizeQueue; }

	public int get(int index){
		if(index < 0 || cacheSizeAll <= index) throw new IndexOutOfBoundsException(""+index);
		int reverseIndex = cacheSizeAll-1-index;
		return XobUtil.getByIndexInListPow2(acyc, listPow2, reverseIndex);
	}

	public int continuation(){
		throw new RuntimeException("TODO This datastruct must include listPow2 and cacheSizeQueue, and the listPow2 at least in later versions will include namespace map in each object and they'll mostly share content and take only log extra memory per update by defining some global ordering of acyc nodes to sort them by in the map and only update those branches.");
	}

}
