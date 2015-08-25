/** Ben F Rayfield offers Xob (Xorlisp Objects) opensource GNU LGPL */
package humanainet.xob.ptr32;
import java.util.AbstractList;

import humanainet.acyc.Acyc;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.RefCounter32;
import humanainet.xob.Xob;
import humanainet.xob.XobUtil;
import humanainet.xorlisp.XorlispVM32;

/** Immutable. listOfPowerOf2Item in xorlisp
is designed to efficient push and pop any size change to the top statelessly,
so you can efficiently have a new immutable list of any of those.
<br><br>
Counts oneMoreRefFromOutside in constructor and oneLessRefFromOutside in finalize().
<br><br>
A list of powerOf2Item viewed linearly with logBase2 max time for reading any index.
Sublist ops may have garbageCollection problems for the same reason
java.lang.String.substring(int,int) was changed to copy the relevant part
of char array instead of, as in earlier versions of Java, using constant time
and memory of just using a new pointer and length int into existing char array.
TODO Should sublist use a pointer to this list or copy it?
AvlBitstring solves this problem, in the context of bits but sacrifices
proving which objects are equal in constant time (actually takes linear in worst case).
*/ 
public class ListPow2 extends AbstractList<Xob> implements Xob{
	
	public final RefCounter32 acyc;
	public RefCounter32 acyc(){ return acyc; }
	
	public final int pointer;
	public Object address(){ return pointer; }
	
	public final int size;
	
	public ListPow2(RefCounter32 acyc, int pointer){
		acyc.oneMoreRefFromOutside(pointer);
		this.acyc = acyc;
		this.pointer = pointer;
		//takes log time of list size
		size = XobUtil.sizeOfListOfPowerOf2Items(acyc, pointer);
	}

	public Xob get(int index){
		if(index < 0 || size <= index) throw new IndexOutOfBoundsException(""+index+" size="+size);
		int listNode = pointer;
		int sizeOfThisPowerOf2Item = 1;
		int powerOf2Item;
		while(listNode != 0){
			throw new RuntimeException("TODO");
		}
		throw new RuntimeException("TODO");
	}

	public int size(){ return size; }
	
	/** a slow function in java, but the only other way is for the users of this list to do it,
	and since its used as a normal java.util.List, they probably wont know to do that.
	*/
	protected void finalize() throws Throwable{
		acyc.oneLessRefFromOutside(pointer);
		super.finalize();
	}

}