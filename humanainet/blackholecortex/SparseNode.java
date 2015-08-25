package humanainet.blackholecortex;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** datastruct for sparse edges which can be extended with other arrays between nodes
by updating swapIndexs and changeArraySize funcs to call super then also do those.
This more general datastruct doesnt assume any specific kind of node or edge data.
<br><br>
TODO move SparseNode and WeightsNode to a more neutral place than bellautomata or blackholecortex.
*/
public class SparseNode implements Comparable<SparseNode>{
	
	//TODO create remove(int) and remove(SparseNode) funcs and use them in BoltzUtil.disconnectBothDirections
	
	public final long localName;
	protected final int hashCode;
	
	/** Size of nodeFrom[] and weightFrom[], which only contain nonzero weighted nodes */
	public int size;
	
	/** indexs aligned with weightFrom[] */
	public SparseNode nodeFrom[] = new SparseNode[1];
	
	/** An optimization for often, but not guaranteed correct so must check it each time,
	finding this node's index in other nodes, of the other nodes in nodeFrom[].
	This array's indexs are aligned to nodeFrom[] indexs.
	This is used when setting weightFrom[] andOr learning[] symmetricly between node pairs.
	<br><br>
	cacheReverseIndex can never contain -1. If not found, set it to 0 (or any nonnegative int),
	and then when looked up it will find thats not correct and will slowly return -1.
	<br><br>
	TODO create functions to use this.
	*/
	public int cacheReverseIndex[] = new int[1];
	
	/** Null for small nodes. TODO create more efficient map specialized in Object to int. */
	public Map<SparseNode,Integer> nodeToIndex = null;
	protected static final int createMapIfBiggerThan = 12;
	
	/** TODO thread locking wont be used since if 2 threads write to NeuralNode,
	they will have calculated the same value except for weightedRandomBit.
	<br><br>
	which is in a WeightsNode.threadLock. This is to prevent deadlocks.
	Many WeightsNodes normally used together (like in the same rbm) should use the
	same threadLock object, a different such object per thread.
	Each thread would run its own rbm or other structure of nodes,
	merging data less often. The WeightsNodes can be in the same FlatXYP and other
	datastructs, meant to be in the same grid of pixels on screen,
	while the user could scroll across the many rbms that are each run
	in a different thread and are allocated and deleted as needed in the
	large scrollable space of pixels, where each magnified pixel is a WeightsNode.
	*
	public volatile Object threadLock = firstThreadLock;
	/** To simplify how WeightsNodes are allocated, their threadLock starts as this value.
	No thread may lock on this object. Its meant to be changed if used multithreaded.
	*
	public static final Object firstThreadLock = new Object();
	*/
	
	public SparseNode(long localName){
		//System.out.println("New sparsenode, address="+address);
		this.localName = localName;
		//this.level = level;
		hashCode = (103*(int)(localName >> 32)) ^ (int)localName /*^ level*/;
	}
	
	protected void changeArraysSize(int newCapacity){
		if(newCapacity < size) throw new RuntimeException("newCapacity="+newCapacity+" size="+size);
		SparseNode nodeFrom2[] = new SparseNode[newCapacity];
		System.arraycopy(nodeFrom, 0, nodeFrom2, 0, size);
		nodeFrom = nodeFrom2;
		double weightFrom2[] = new double[newCapacity];
		int cacheReverseIndex2[] = new int[newCapacity];
		System.arraycopy(cacheReverseIndex, 0, cacheReverseIndex2, 0, size);
		cacheReverseIndex = cacheReverseIndex2;
	}
	
	/** For sorting, maybe in a later version. I'm not sure if its needed.
	Subclass EconbitsNode already extends this with its extra array.
	*/
	protected void swapIndexs(int x, int y){
		SparseNode tempNode = nodeFrom[x];
		nodeFrom[x] = nodeFrom[y];
		nodeFrom[y] = tempNode;
		int tempCacheReverse = cacheReverseIndex[x];
		cacheReverseIndex[x] = cacheReverseIndex[y];
		cacheReverseIndex[y] = tempCacheReverse;
		if(nodeToIndex != null){
			nodeToIndex.put(nodeFrom[x], x);
			nodeToIndex.put(nodeFrom[y], y);
		}
	}
	
	/** -1 if not found */
	public int indexOf(SparseNode from){
		if(nodeToIndex == null){
			for(int i=0; i<size; i++) if(nodeFrom[i] == from) return i;
		}else{
			Integer i = nodeToIndex.get(from);
			if(i != null) return i;
		}
		return -1;
	}
	
	/** Cached until this node's position in other node changes after last call of this,
	for efficient updating of nodes in pairs without looking them up using hashCode etc.
	<br><br>
	Returns reverseIndex where nodeFrom[from].nodeFrom[reverseIndex] == this,
	or -1 if nodeFrom[from] has 0 weight back to this WeightsNode.
	Updates the reverseCache if its found.
	<br><br>
	Its best that if either in a node pair has nonzero weight to the other,
	then the other has nonzero weight back, because otherwise they will continue
	to cache miss and slow it down just to return -1 that its not found.
	*/
	public int reverseIndexIn(int from){
		SparseNode otherNode = nodeFrom[from];
		int cachedRevInd = cacheReverseIndex[from];
		if(cachedRevInd < otherNode.size){
			if(otherNode.nodeFrom[cachedRevInd] == this){
				return cachedRevInd;
			}
		}
		//cache miss. Update cache, unless this not exist in otherNode (which will continue to be slow).
		int correctReverseIndex = otherNode.indexOf(this);
		if(correctReverseIndex == -1){
			cacheReverseIndex[from] = 0; //must always point at positive index, even if will cache miss
			return -1;
		}else{
			return cacheReverseIndex[from] = correctReverseIndex;
		}
	}
	
	public int compareTo(SparseNode n){
		if(localName < n.localName) return -1;
		if(localName > n.localName) return 1;
		if(n != this) throw new RuntimeException("No 2 nodes can have the same long address: "+localName);
		return 0;
	}
	
	public boolean equals(Object ob){
		//Dont check for duplicate nodes with same long/address here (even though its not allowed)
		//Just do the fast thing, ==
		//return this == ob;
		//Or maybe I want 2 nodes with same long/address to approximate eachother
		//and be merged, but for datastructs they are not equal.
		
		if(ob == this) return true;
		if(!(ob instanceof SparseNode)) return false;
		if(((SparseNode)ob).localName == localName) throw new RuntimeException(
			"No 2 nodes can have the same long address: "+localName);
		return false;
	}
	
	public int hashCode(){ return hashCode; }

}