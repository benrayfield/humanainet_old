/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.statsysfactory;
import java.util.Map;
import java.util.Random;

import humanainet.wavetree.bit.Bits;
import humanainet.blackholecortex.WeightsFunc;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.datastruct.CantWriteBitsState;
import humanainet.rbmparams.AnnealStrategyBySize;
import humanainet.rbmparams.RbmActionUsingLearnRate;
import humanainet.rbmparams.RbmSimpleAction;
import humanainet.statsysinterface.DatastructCategory;
import humanainet.statsysinterface.Statsys;
import humanainet.statsysinterface.StatsysFuncType;

/** a custom RBM is built using many RbmParam which define its datastructs and algorithms.
This is early research into rbmParams which would eventually be allowed to differ
in different hidden layers, but in this class only the size of layers can differ,
(TODO) including the sizes of shortTermMemory parts of each layer
which is part of the RBM and TriMem datastructs.
Some things are allowed to differ between visible and hidden layers,
like BernoulliType, since thats how its normally done.
*/
public class CustomRBMSameParamsAcrossLayers implements Statsys{
	
	public final RbmData nodesAndWeights;
	
	public final WeightsFunc neuralFunc;
	
	public final AnnealStrategyBySize anneal;
	
	/** This is for scaling the weights based on learningRate */ 
	public final RbmActionUsingLearnRate weightDecayJustAfterLearn;
	
	/** Weight norming may depend on pairs of adjacent layers instead of individual nodes,
	especially because of making edges symmetric like with BoltzUtil.makeBidirectional
	<br><br>
	TODO how to do this threaded? Do the weight symmetry in pairs, and do things like MaxNormRegularization per node before that.
	*/
	public final RbmSimpleAction weightsNormAsLastStepAfterLearn;
	
	//TODO use attention vars with sortedPointers to choose a percentile graph,
	//or even more advanced in later versions have a neuralnet choose where the attention of another neuralnet goes
	//as it learns and predicts.

	
	//TODO use threads, default as 16 of them, which each run the same Neuron and that is shared with no other thread, and they must all finish before any of them can proceed to the next pair of RbmLayer
	
	public CustomRBMSameParamsAcrossLayers(
		RbmData nodesAndWeights,
		WeightsFunc neuralFunc,
		AnnealStrategyBySize anneal,
		RbmActionUsingLearnRate weightDecayJustAfterLearn,
		RbmSimpleAction weightsNorm
	){
		this.nodesAndWeights = nodesAndWeights;
		this.neuralFunc = neuralFunc;
		this.anneal = anneal;
		this.weightDecayJustAfterLearn = weightDecayJustAfterLearn;
		this.weightsNormAsLastStepAfterLearn = weightsNorm;
	}

	public int size(){
		return nodesAndWeights.combinedLayers[0].size;
	}

	public void learnManyScalars(Map<double[],Double> weightedVectors, Random rand){
		throw new RuntimeException("TODO learn scalars. Currently its all done as Bits (which could hold scalars on intervals of 64 or whatever encoding of scalars)");
	}

	public void learnManyBitsArray(Map<boolean[],Double> weightedVectors, Random rand){
		throw new RuntimeException("TODO");
	}

	public void learnManyBitsObject(Map<Bits, Double> weightedVectors, Random rand){
		throw new RuntimeException("TODO");
	}

	public void learnOneScalars(double senseIn[], double learnRate, Random rand, boolean alsoPredict){
		throw new RuntimeException("TODO learn scalars. Currently its all done as Bits (which could hold scalars on intervals of 64 or whatever encoding of scalars)");
	}

	public void learnOneBitsArray(boolean senseIn[], double learnRate, Random rand, boolean alsoPredict){
		throw new RuntimeException("TODO");
	}

	public void learnOneBitsObject(Bits senseIn, double learnRate, Random rand){
		throw new RuntimeException("TODO");
	}
	
	public Bits learnOneBitsObjectAndPredict(Bits senseIn, double learnRate, Random rand){
		throw new RuntimeException("TODO");
	}

	public void predictScalars(double senseInPredictOut[], Random rand){
		throw new RuntimeException("TODO weightedBitObserve the fraction scalars then predict from bits");
	}

	public void predictBitsArray(boolean senseInPredictOut[], Random rand){
		Bits in = BoltzUtil.arrayToBits(senseInPredictOut);
		Bits out = predictBitsObject(in, rand);
		BoltzUtil.bitsIntoArray(out, senseInPredictOut);
	}

	public Bits predictBitsObject(Bits senseIn, Random rand){
		throw new RuntimeException("TODO");
	}

	public void norm(double fraction){}

	public boolean normDoesSomething(){ return false; }
	
	//StatsysCost funcs:
	
	public double memoryCost(){
		throw new RuntimeException("TODO");
	}

	public double maxMemoryIncreasePerVector(StatsysFuncType funcType){
		throw new RuntimeException("TODO");
	}

	public double maxComputeCostPerVector(StatsysFuncType funcType){
		throw new RuntimeException("TODO");
	}

	public boolean statsysCostIsImmutable(){
		return true;
	}
	
	//BitVsScalar funcs:
	
	public boolean preferBitsOverScalars(DatastructCategory d){
		switch(d){
		case node: return true;
		case edge: return false;
		case powersetedge: throw new RuntimeException("Dont have "+DatastructCategory.class.getName()+": "+d);
		default: throw new RuntimeException(DatastructCategory.class.getName()+" unknown: "+d);
		}
	}

	public boolean hasDatastructCategory(DatastructCategory d){
		switch(d){
		case node: return true;
		case edge: return true;
		case powersetedge: return false;
		default: throw new RuntimeException(DatastructCategory.class.getName()+" unknown: "+d);
		}
	}
	
	//CopyCost funcs:
	
	public Statsys copyAfterPayMemory(){
		throw new RuntimeException("TODO");
	}

	public long memoryCostToCopy(){
		throw new RuntimeException("TODO");
	}

	public Bits readState(){
		throw new RuntimeException("TODO");
	}

	public void writeState(Bits state) throws CantWriteBitsState{
		throw new RuntimeException("TODO");
	}

	public boolean stateCanChangeBetweenWriteState(){
		throw new RuntimeException("TODO");
	}

}
