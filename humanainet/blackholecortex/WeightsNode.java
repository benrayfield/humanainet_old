package humanainet.blackholecortex;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import humanainet.blackholecortex.weightsfuncs.BernoulliSumSigmoid;
import humanainet.common.MathUtil;

/** datastruct for sparse edges with scalar weight FROM any nodes.
Set a weight FROM x to y to 0 and y is removed from x's list, but y's list
is not changed.
<br><br>
TODO move SparseNode and WeightsNode to a more neutral place than bellautomata or blackholecortex.
*/
public class WeightsNode extends NumNode{
	
	/** Should average near 0 per weight, not necessarily the whole array together. */
	public double weightFrom[] = new double[1];
	
	/** This array can be used as derivative of weightFrom array.
	Will be added to weightFrom[] at same indexs,
	after a group of things learned is finished together.
	*/
	public double learning[] = new double[1];
	
	/** For example, FlatXYP.viewNewNodeAt(Rectangle2D.Double rect, int p) only creates
	positive weights of how much of the surface the rectangle intersects at each pixel,
	but the node as viewing that rectangle needs to be off sometimes (at least half the time),
	so that nodes addToWeight would be around negative half the weights to those it views.
	Default addToWeight is 0 since most nodes have positive and negative weights
	between eachother. This is better than having all nodes connect to a node which
	is always on (bit is 1) since that could slow things when many threads need
	to touch the same memory, especially in cell processors.
	*/
	public double addToWeight;
	
	/** If not null, this func is run in refreshScalar. Starts null. */
	public WeightsFunc func = BernoulliSumSigmoid.instance;
	
	public WeightsNode(long localName){
		super(localName);
	}
	
	public void refresh(Random rand, double temperature){
		refreshScalar(temperature);
		refreshBit(rand);
	}
	
	public void refreshScalar(double temperature){
		final WeightsFunc f = func;
		if(f != null) scalar = attention*f.weightsFunc(this, temperature);
	}
	
	public void refreshBit(Random rand){
		bit = MathUtil.weightedRandomBit(scalar, rand);
	}
	
	public double sumWeightsPerOnBit(){
		//return sumWeights(recogOn);
		double sum = 0;
		final int siz = size;
		final SparseNode n[] = nodeFrom;
		final double w[] = weightFrom;
		for(int i=0; i<siz; i++){
			if(((WeightsNode)n[i]).bit){
				sum += w[i];
			}
		}
		sum += addToWeight;
		return sum;
	}
	
	//TODO setWeightFrom(int) func as optimization to not call indexOf(WeightsNode) if already know it
	//TODO remove(int) func, and call inside setWeightFrom and in BoltzUtil.addToBothWeightsBetween
	//and in BoltzUtil.setWeightBetween
	
	/** Set to weight 0 to remove Node. Set to nonzero to add it. */
	public void setWeightFrom(SparseNode from, double weight){
		//FIXME this func needs to use swapIndexs(int,int) instead of moving between i and endIndex
		int i = indexOf(from);
		if(weight == 0){ //remove
			if(i != -1){
				int endIndex = size-1;
				nodeFrom[i] = nodeFrom[endIndex]; //may be same index
				weightFrom[i] = weightFrom[endIndex];
				nodeFrom[endIndex] = null; //become garbageCollectable
				size--;
				//TODO if(size <= nodeFrom.length/4){ //shrink arrays to size*2 and HashMap
				//TODO }
			}
		}else{
			if(i == -1){ //add node
				if(nodeFrom.length == size){ //enlarge arrays when not enough room
					changeArraysSize(size*2);
				}
				nodeFrom[i = size++] = from;
				if(nodeToIndex == null){
					if(createMapIfBiggerThan < size){
						nodeToIndex = new HashMap(nodeFrom.length*2, .75f);
						for(int n=0; n<size; n++) nodeToIndex.put(nodeFrom[n],n);
					}
				}else{
					nodeToIndex.put(from, i);
				}
			}
			weightFrom[i] = weight;
		}
	}
	
	public void setLearningFrom(WeightsNode from, double d){
		int i = indexOf(from);
		if(i == -1) throw new RuntimeException("TODO define an edge as deleted when learning and weightFrom are both 0, not just weightFrom");
		learning[i] = d;
	}
	
	protected void changeArraysSize(int newCapacity){
		super.changeArraysSize(newCapacity);
		double weightFrom2[] = new double[newCapacity];
		System.arraycopy(weightFrom, 0, weightFrom2, 0, size);
		weightFrom = weightFrom2;
		double learning2[] = new double[newCapacity];
		System.arraycopy(learning, 0, learning2, 0, size);
		learning = learning2;
	}
	
	/** For sorting, maybe in a later version. I'm not sure if its needed.
	Subclass EconbitsNode already extends this with its extra array.
	*/
	protected void swapIndexs(int x, int y){
		super.swapIndexs(x, y);
		double tempWeight = weightFrom[x];
		weightFrom[x] = weightFrom[y];
		weightFrom[y] = tempWeight;
		double tempLearn = learning[x];
		learning[x] = learning[y];
		learning[y] = tempLearn;
	}
	
	/** 0 if node not exist here */
	public double weightFrom(WeightsNode from){
		int i = indexOf(from);
		if(i == -1) return 0;
		return weightFrom[i];
	}
	
	public double learningFrom(WeightsNode from){
		int i = indexOf(from);
		if(i == -1) return 0;
		return learning[i];
	}

	/** Empties learning[] into weightsFrom[] */
	public void moveLearningToWeights(){
		for(int i=0; i<size; i++){
			weightFrom[i] += learning[i];
		}
		Arrays.fill(learning, 0, size, 0.);
	}
	
	public double weightsAve(){
		if(size == 0) throw new RuntimeException("empty");
		double sum = 0;
		for(int i=0; i<size; i++) sum += weightFrom[i];
		return sum/size;
	}
	
	public double weightsSumOfSquaresFromAve(double ave){
		if(size == 0) throw new RuntimeException("empty");
		double sum = 0;
		for(int i=0; i<size; i++){
			double diff = weightFrom[i]-ave;
			sum += diff*diff;
		}
		return sum;
	}
	
	public double weightsSumOfSquaresFromAve(){
		return weightsSumOfSquaresFromAve(weightsAve());
	}
	
	/** Returns NaN if size is 0 */
	public double weightsStdDev(){
		return Math.sqrt(weightsSumOfSquaresFromAve()/size);
	}
	
	public double weightsStdDev(double ave){
		return Math.sqrt(weightsSumOfSquaresFromAve(ave)/size);
	}
	
	/** from whatever the ave is */
	public double hypersphereRadiusFromAve(){
		return hypersphereRadiusFromAve(weightsAve());
	}
	
	public double hypersphereRadiusFromAve(double ave){
		if(size == 0) return 0;
		return Math.sqrt(weightsSumOfSquaresFromAve(ave));
	}
	
	//TODO use aod, where Neuron.influence is aod, and dotProd between 2 Neuron is by their weights to same nodes, if ave weight in each node is 0
	
	//TODO to avoid exponential complexity of which combinations of weights are positive vs negative, maybe should require all weights be positive (or all negative), and balance that weith Neuron.addToWeight
	
	/** TODO when 2 adjacent, rbm layers have different sizes,
	adjust hypersphere radius so pushing both directions of weight
	between each pair to equal eachother does not change the learning.
	For example, all weights equal to inverse layer size.
	*/
	public void normWeightsToHypersphere(double newAve, double newRadius){
		if(size == 0) return;
		double observedAve = weightsAve();
		double obsevedRadiusFromAve = hypersphereRadiusFromAve(observedAve);
		if(obsevedRadiusFromAve == 0){
			throw new RuntimeException("TODO add Random param to choose random vector of that radius");
		}
		double radiusMult = newRadius/obsevedRadiusFromAve;
		//double observedDev = weightsStdDev(observedAve);
		if(newAve == 0){
			for(int i=0; i<size; i++){
				weightFrom[i] = (weightFrom[i]-observedAve)*radiusMult;
			}
		}else{
			for(int i=0; i<size; i++){
				weightFrom[i] = newAve + (weightFrom[i]-observedAve)*radiusMult;
			}
		}
	}
	
	/*public void normWeightsToCenteredHypersphere(double newRadius){
		normWeightsToHypersphere(0, newRadius);
	}
	
	/** TODO is the same as normWeightsToHypersphere but just different params?
	Theres at least duplicated code between them.
	*
	public void normWeightsToBellCurve(double newAve, double newDev){
		if(size == 0) return;
		double observedAve = weightsAve();
		double observedDev = Math.sqrt(weightsSumOfSquaresFromAve(observedAve)/size);
		if(observedDev == 0){
			throw new RuntimeException("TODO include Random param to handle when all weights equal, how to put them on a specific stdDev");
		}
		for(int i=0; i<size; i++){
			double unitDevZeroAve = (weightFrom[i]-observedAve)/observedDev;
			weightFrom[i] = newAve + unitDevZeroAve*newDev;
		}
	}*/

}