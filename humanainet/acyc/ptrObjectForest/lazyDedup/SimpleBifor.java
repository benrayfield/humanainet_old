/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest.lazyDedup;
import humanainet.acyc.ptrObjectForest.ptrHashCons.HashCons;

/** Each call of left() or right() updates instance var of left or right to its lazyDedup() */
public class SimpleBifor implements Bifor{
	
	public final boolean isEnd(){ return false; }
	
	protected Bifor left, right;
	
	protected double cost;
	
	public SimpleBifor(Bifor left, Bifor right){
		this.left = left;
		this.right = right;
	}
	
	public Bifor left(){
		return left = left.lazyDedup();
	}
	
	public Bifor right(){
		return right = right.lazyDedup();
	}
	
	public Bifor pair(Bifor right){
		throw new RuntimeException("TODO");
	}
	
	public Bifor lazyDedup(){
		throw new RuntimeException("TODO");
	}
	
	public HashCons globalDedup(){
		throw new RuntimeException("TODO");
	}
	
	public boolean globalDedupIsCached(){
		throw new RuntimeException("TODO");
	}
	
	public boolean isCertainlyEqual(Bifor n){
		throw new RuntimeException("TODO");
	}
	
	public boolean isCertainlyUnequal(Bifor n){
		throw new RuntimeException("TODO");
	}
	
	public double cost(){
		throw new RuntimeException("TODO");
	}
	
	public double costPerInsidePointer(){
		throw new RuntimeException("TODO");
	}
	
	public double costPerOutsidePointer(){
		throw new RuntimeException("TODO");
	}

	public int pointersFromInside(){
		throw new RuntimeException("TODO");
	}

	public int pointersFromOutside(){
		throw new RuntimeException("TODO");
	}

}
