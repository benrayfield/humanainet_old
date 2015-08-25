/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest.lazyDedup;
import humanainet.acyc.ptrObjectForest.ptrHashCons.HashCons;

public final class End implements Bifor{
	
	private End(){}
	
	public boolean isEnd(){ return true; }
	
	public Bifor left(){ return this; }
	
	public Bifor right(){ return this; }
	
	public Bifor pair(Bifor right){
		throw new RuntimeException("TODO");
	}
	
	public Bifor lazyDedup(){ return this; }
	
	public HashCons globalDedup(){
		throw new RuntimeException("TODO");
	}
	
	public boolean globalDedupIsCached(){
		throw new RuntimeException("TODO");
	}
	
	public boolean isCertainlyEqual(Bifor n){
		return n.isEnd();
	}
	
	public boolean isCertainlyUnequal(Bifor n){
		return !n.isEnd();
	}
	
	public double cost(){
		return 1;
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
