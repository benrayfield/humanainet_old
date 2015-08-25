/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex;

public class ComplexNode extends SparseNode{
	
	public final double complex[] = new double[2];
	
	public ComplexNode(long address){
		super(address);
	}
	
	/* All weights absolute value changed to sum to 1.
	If it were all ComplexNode, all the weights would be positive,
	but other algorithms use negative weights too, so I'll just scale them
	by their absolute value.
	*/
	public void normWeights(){
		throw new RuntimeException("TODO");
		/*if(size == 0) return;
		double sumAbs = 0;
		for(int i=0; i<size; i++){
			sumAbs += Math.abs(weightFrom[i]);
		}
		if(sumAbs != 0){
			for(int i=0; i<size; i++){
				weightFrom[i] /= sumAbs;
			}
		}else{
			double plus = 1./size;
			for(int i=0; i<size; i++){
				weightFrom[i] += plus;
			}
		}*/
	}
	
	/** Sums complex numbers from other nodes */
	public void refresh(){
		/*for(int i=0; i<size; i++){
			sumAbs += Math.abs(weightFrom[i]);
		}*/
		throw new RuntimeException("TODO");
	}

}