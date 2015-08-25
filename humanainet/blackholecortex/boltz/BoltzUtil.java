/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex.boltz;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import humanainet.recog.CacheRecog;
import humanainet.common.time.InOutTimer;
import humanainet.datasetsForAI.mnistocrdataset.MnistOcrDataset;
//import mnistocrdataset.MnistOcrDataset;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.Fast0To16Bits;
import humanainet.wavetree.bit.object.Polycat;
import humanainet.wavetree.bit.object.Polydim;
import humanainet.wavetree.bit.object.SimplePolydim;
import humanainet.wavetree.bit.object.SimplePolycat;
import humanainet.blackholecortex.SparseNode;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.ObservedRect;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.TriMem;
import humanainet.common.TestedIn;

/** ObservedRect is a List<NeuralNode>, to be used in funcs which take a Collection<NeuralNode> param */
public class BoltzUtil{
	
	/** think without learning */
	public static TriMem predict(TriMem mem, AnnealStrategy a, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.predict");
		t.in();
		boolean updateWeights = false;
		TriMem trimem = think(mem, a, 0., updateWeights, rand);
		t.out();
		return trimem;
	}
	
	/** Similar to think(...) and predict(...) except only uses current state of RBM.
	Goes up and down as many times as the RBM's AnnealStrategy says.
	Input and output states are stored in the RBM, especially the visibleNodes.
	*/
	public static void predictFromCurrentThought(RbmData rbm, AnnealStrategy a, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.predictFromCurrentThought");
		t.in();
		int cycles = a.cyclesPerPredict();
		for(int c=0; c<cycles; c++){
			double temperature = a.temperature(c, cycles);
			up(rbm, temperature, rand);
			down(rbm, temperature, rand);
		}
		t.out();
	}
	
	/** This func does learning and prediction 1 trainingData at a time.
	learnRate can be positive or negative.
	If learnRate is 0, doesnt run the learning code, which is much faster
	as described by AnnealStrategy's different number of cycles between learn and predict.
	<br><br>
	TODO very important: Statsys.learnMany func (basically with a map of trainingData to learnRate) is
	far more accurate and uses annealStrategy to its potential. It has the disadvantage
	of being too slow to use in realtime since all the data must be trained in again,
	but theres probably some way to combine these ways of learning and predicting.
	Use public static RBM learnMany(Map<TriMem,Double> trainingDataToLearnRate).
	*/
	@TestedIn(void.class)
	public static TriMem think(TriMem mem, AnnealStrategy a, double learnRate, boolean updateWeights, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.think");
		t.in();
		boolean learnIncludingShortTermMemory = false; //TODO
		RbmData rbm = mem.mutableLongTermMemory;
		setShortTermMemoryScalarsAndBits(mem.mutableLongTermMemory, mem.immutableShortTermMemory);
		setVisibleNodesScalarsAndBits(mem.mutableLongTermMemory, mem.immutableSenseMemory);
		int cycles;
		if(learnRate == 0){ //predict
			predictFromCurrentThought(mem.mutableLongTermMemory, a, rand);
		}else{ //learn and predict
			learnFromCurrentThought(mem.mutableLongTermMemory, a, learnRate, updateWeights, rand);
		}
		t.out();
		return new TriMem(
			mem.mutableLongTermMemory,
			getShortTermMemoryBits(mem.mutableLongTermMemory).data(),
			getVisibleNodesBits(mem.mutableLongTermMemory)
		);
	}
	
	/** Similar to think(...) with nonzero learnRate except only uses current state of RBM.
	If updateWeights, then after learning, empties learning[] arrays into weightFrom[] arrays.
	*/
	public static void learnFromCurrentThought(RbmData rbm, AnnealStrategy a, double learnRate, boolean updateWeights, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.learnFromCurrentThought");
		t.in();
		int cycles = a.cyclesPerLearnOne();
		if(cycles < 2) throw new IllegalArgumentException(
			"Cant learn with less than 2 cycles, the first to learn the data"
			+" and last to unlearn its earlier reaction to the data. cycles="+cycles);
		for(int c=0; c<cycles; c++){
			double temperature = a.temperature(c, cycles);
			up(rbm, temperature, rand);
			if(c == 0){
				addToLearningArray(rbm, learnRate);
			}
			if(c != cycles-1){ //last cycle, go up but not back down
				down(rbm, temperature, rand);
			}
		}
		addToLearningArray(rbm, -learnRate);
		if(updateWeights) moveLearningToWeights(rbm);
		incrementModsAndMakeBidirectionalIfNeeded(rbm);
		t.out();
	}
	
	public static void learnFromCurrentThoughtManualCyclesAndTemperature(RbmData rbm, double learnRate, boolean updateWeights, Random rand, int cycles, double temperature){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.learnFromCurrentThoughtManualTemperature");
		t.in();
		if(cycles < 2) throw new IllegalArgumentException(
			"Cant learn with less than 2 cycles, the first to learn the data"
			+" and last to unlearn its earlier reaction to the data. cycles="+cycles);
		for(int c=0; c<cycles; c++){
			up(rbm, temperature, rand);
			if(c == 0){
				addToLearningArray(rbm, learnRate);
			}
			if(c != cycles-1){ //last cycle, go up but not back down
				down(rbm, temperature, rand);
			}
		}
		addToLearningArray(rbm, -learnRate);
		if(updateWeights) moveLearningToWeights(rbm);
		incrementModsAndMakeBidirectionalIfNeeded(rbm);
		t.out();
	}
	
	/** This func requires 0 shortTermMemoryLayer sizes */
	public static void learnMany(RbmData rbm, AnnealStrategy a, Random rand, Polydim trainingData,
			double learnRatePerAnnealCycle){
		int cycles = a.cyclesPerLearnMany();
		for(RbmLayer rLayer : rbm.shortTermMemoryLayers){
			if(rLayer.size != 0) throw new RuntimeException(
				"This func requires 0 shortTermMemoryLayer sizes");
		}
		Map<TriMem,Double> trainingDataToLearnRate = new HashMap();
		for(int w=0; w<trainingData.dimSize(0); w++){
			Bits oneTrainingData = trainingData.bits(w).data();
			TriMem t = new TriMem(rbm, Fast0To16Bits.EMPTY, oneTrainingData);
			trainingDataToLearnRate.put(t, learnRatePerAnnealCycle);
		}
		for(int cycle=0; cycle<cycles; cycle++){
			double temperature = a.temperature(cycle, cycles);
			System.out.println("Learning, cycle="+cycle+" totalCycles="+cycles+" temperature="+temperature);
			learnManyOneCycle(trainingDataToLearnRate, a, temperature, true, rand);
		}
	}
	
	/** All keys in the Map must have the same RBM. TODO test for that.
	Updates all weights as many times as AnnealStrategy.cyclesPerLearn
	*/
	@TestedIn(void.class)
	public static void learnMany(Map<TriMem,Double> trainingDataToLearnRate, AnnealStrategy a, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.learnMany");
		t.in();
		RbmData rbm = trainingDataToLearnRate.keySet().iterator().next().mutableLongTermMemory;
		int cycles = a.cyclesPerLearnMany();
		for(int cycle=0; cycle<cycles; cycle++){
			double temperature = a.temperature(cycle, cycles);
			learnManyOneCycle(trainingDataToLearnRate, a, temperature, true, rand);
		}
		t.out();
	}

	/** 1 anneal cycle at specific temperature.
	All keys in the Map must have the same RBM. TODO test for that.
	*/
	public static void learnManyOneCycle(Map<TriMem,Double> trainingDataToLearnRate, AnnealStrategy a, double temperature,
			boolean updateWeights, Random rand){
		if(trainingDataToLearnRate.isEmpty()) return;
		//RBM rbm = trainingDataToLearnRate.keySet().iterator().next().mutableLongTermMemory;
		RbmData rbm = null;
		for(Map.Entry<TriMem,Double> entry : trainingDataToLearnRate.entrySet()){
			RbmData shouldBeSameRBM = entry.getKey().mutableLongTermMemory;
			if(rbm == null) rbm = shouldBeSameRBM;
			else if(rbm != shouldBeSameRBM) throw new RuntimeException(
				"Different RBMs: "+rbm+" and "+shouldBeSameRBM);
			think(entry.getKey(), a, entry.getValue(), false, rand);
		}
		if(updateWeights) moveLearningToWeights(rbm);
	}
	
	/** Empties Neuron.learning[] into Neuron.weightFrom[] */
	public static void moveLearningToWeights(List<WeightsNode> nodes){
		for(WeightsNode n : nodes){
			for(int i=0; i<n.size; i++){
				n.weightFrom[i] += n.learning[i];
			}
			Arrays.fill(n.learning, 0, n.size, 0.);
		}
	}
	
	public static void moveLearningToWeights(RbmData rbm){
		for(RbmLayer rLayer : rbm.combinedLayers){
			moveLearningToWeights(rLayer.fromUp);
			moveLearningToWeights(rLayer.fromDown);
		}
	}
	
	/** Changes WeightsNode.weightFrom[] and WeightsNode.learning[], for each node pair,
	to the average of those values within each pair.
	Pairs are any in the List with all they're connected to.
	This is needed in boltzmann machines since nodes are calculated as directed neuralNet
	and will accumulate roundoffError and maybe other small differences.
	*/
	public static void makeBidirectional(List<? extends WeightsNode> nodes){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.makeBidirectional");
		t.in();
		for(WeightsNode n : nodes){
			for(int i=0; i<n.size; i++){
				int iReverse = n.reverseIndexIn(i); //cached
				//TODO Is it always a WeightsNode? Could be more generally a SparseNode,
				//for other variations like I'm planning for fourierAodEconbit and hyperspherenet.
				WeightsNode m = (WeightsNode) n.nodeFrom[i];
				if(iReverse != -1){
					double aveWeight = (n.weightFrom[i] + m.weightFrom[iReverse])/2;
					double aveLearning = (n.learning[i] + m.learning[iReverse])/2;
					n.weightFrom[i] = aveWeight;
					m.weightFrom[iReverse] = aveWeight;
					n.learning[i] = aveLearning;
					m.learning[iReverse] = aveLearning;
				}else{ //slow way this time because they werent connected both directions
					double aveWeight = (n.weightFrom(m) + m.weightFrom(n))/2;
					n.setWeightFrom(m, aveWeight);
					m.setWeightFrom(n, aveWeight);
					double aveLearning = (n.learningFrom(m) + m.learningFrom(n))/2;
					n.setLearningFrom(m, aveLearning);
					m.setLearningFrom(n, aveLearning);
				}
			}
		}
		t.out();
	}
	
	public static void makeBidirectional(RbmData rbm){
		
		for(RbmLayer layer : rbm.combinedLayers){
			if(layer.fromDown != null) makeBidirectional(layer.fromDown);
			if(layer.fromUp != null) makeBidirectional(layer.fromUp);
		}
		//rbm.lastBidirectionalNormAtHowManyMods = rbm.mods;
	}
	
	public static void incrementModsAndMakeBidirectionalIfNeeded(RbmData rbm, int bidirectionalNormInterval){
		/*rbm.mods++;
		long modsSinceLastNorm = rbm.mods-rbm.lastBidirectionalNormAtHowManyMods;
		if(modsSinceLastNorm <= bidirectionalNormInterval) makeBidirectional(rbm);
		*/
		rbm.bidirectionalNormAgainInHowManyMods--;
		if(rbm.bidirectionalNormAgainInHowManyMods <= 0){
			makeBidirectional(rbm);
			rbm.bidirectionalNormAgainInHowManyMods = bidirectionalNormInterval;
		}
	}
	
	//public static int DEFAULT_BIDIRECTIONAL_NORM_INTERVAL = 16;
	public static int DEFAULT_BIDIRECTIONAL_NORM_INTERVAL = 64;
	//TODO? public static int DEFAULT_BIDIRECTIONAL_NORM_INTERVAL = 128;
	//public static int DEFAULT_BIDIRECTIONAL_NORM_INTERVAL = 1; //TODO try 16, but until learning is working as expected use 1
	
	/** uses DEFAULT_BIDIRECTIONAL_NORM_INTERVAL */
	public static void incrementModsAndMakeBidirectionalIfNeeded(RbmData rbm){
		incrementModsAndMakeBidirectionalIfNeeded(rbm, DEFAULT_BIDIRECTIONAL_NORM_INTERVAL);
	}
	
	/** Set all NeuralNode.bit to 0 */
	public static void clearBits(Collection<WeightsNode> nodes){
		for(WeightsNode n : nodes){
			n.bit = false;
		}
	}
	
	/*public static void refreshScalars(Collection<WeightsNode> nodes, CacheRecog<WeightsNode> recog, double temperature){
		for(WeightsNode n : nodes){
			n.refreshScalar(recog, temperature);
		}
	}
	
	public static void refreshBits(Collection<WeightsNode> nodes, Random rand){
		for(WeightsNode n : nodes){
			n.refreshBit(rand);
		}
	}*/
	
	/*public static void refresh(Collection<Neuron> nodes, Recog<Neuron> recog, Random rand, double temperature){
		for(Neuron n : nodes){
			n.refresh(rand, recog, temperature);
		}
	}
	
	/** Intelligently sets bits in present based on past.
	Does not need to know which rect is past since each NeuralNode knows what it depends on.
	Clears bits in futureOrNull if its not null, then runs rbm from past to present.
	futureOrNull is null when present is top rbm layer and going up
	or is bottom rbm layer and going down. If future bits were not cleared, then the layers
	above and below present (past and future, either direction) would both affect present.
	Thats something to explore in other functions.
	*
	public static void refresh(Collection<Neuron> present, Collection<Neuron> future,
			Recog<Neuron> recog, Random rand, double temperature){
		clearBits(future);
		refresh(present, recog, rand, temperature);
	}*/
	
	/** Learns separately as if it was a directed neuralNet, so caller is expected
	to handle accumulated roundoffError and any differences in direction between
	each pair of nodes, or to use it as a directed neuralNet.
	This applies to learning[] and weightFrom[] arrays.
	<br><br> 
	learnRate can be positive or negative and should average 0.
	It adds to WeightsNode.learning[], from all pair of NeuralNode with
	nonzero weight (are connected) in adjacent layers whose bit are both on.
	The learning[] array should be added to WeightsNode.weightFrom[].
	You must therefore be careful to avoid setting a weight to 0,
	maybe due to the negative side of learning, and expecting it to
	become nonzero later, because thats how nodes are normally disconnected.
	*/
	public static void addToLearningArray(RbmData rbm, double learnRate){
		for(RbmLayer layer : rbm.combinedLayers){
			for(int whichHalf=0; whichHalf<2; whichHalf++){
				//ObservedRect layerHalf = whichHalf==0 ? layer.fromDown : layer.fromUp;
				List<WeightsNode> layerHalf = whichHalf==0 ? layer.fromDown : layer.fromUp;
				if(layerHalf != null){ //one is null at first and last layer
					addToLearningArray(layerHalf, learnRate);
				}
			}
		}
	}

	/** See comment in addToLearningArray(RBM,double) */
	public static void addToLearningArray(List<WeightsNode> nodes, double learnRate){
		//TODO optimize by using generics instead of casting to NeuralNode?
		for(WeightsNode n : nodes){
			if(n.bit){
				for(int i=0; i<n.size; i++){
					WeightsNode m = (WeightsNode)n.nodeFrom[i];
					if(m.bit) n.learning[i] += learnRate;
				}
				
			}
		}
	}
	
	/** Does same thing regardless of parameter order.
	Sets weightFrom and learning, in those arrays, in both to their average of the 2.
	*
	To avoid using indexOf (which uses java.util.Map which is slow) on the first direction,
	even though it is cached in either second direction,
	do this while iterating over all nodes in some Rectangle.
	*
	public static void makePairEdgesSymmetric(WeightsNode x, WeightsNode y){
		int indexOfXInY = x.reverseIndexIn(from)
	}*/
	
	public static Bits getBits(List<WeightsNode> nodes){
		Bits b = Fast0To16Bits.EMPTY;
		for(WeightsNode n : nodes){
			b = b.cat(Fast0To16Bits.get(n.bit));
		}
		return b;
	}
	
	/** Sets each scalar to 0 or 1, then bits to observation of that chance which is certain either way. */
	public static void setScalarsAndBits(List<WeightsNode> nodes, Bits b){
		verifySize(nodes,b);
		long g = 0;
		for(WeightsNode n : nodes){
			boolean bit = b.bitAt(g++);
			n.scalar = bit ? 1 : 0;
			n.bit = bit;
		}
	}
	
	public static Bits getVisibleNodesBits(RbmData rbm){
		return getBits(rbm.mainLayers[0].fromUp);
	}
	
	public static void setVisibleNodesScalarsAndBits(RbmData rbm, Bits b){
		setScalarsAndBits(rbm.mainLayers[0].fromUp, b);
	}
	
	/** Starts with bits in RBM.shortTermMemoryLayers and RBM.mainRbmLayers[0] set to input data.
	Changes scalar and bit values of RBM.mainRbmLayers[1 and higher].
	*/
	public static void up(RbmData rbm, double temperature, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.up");
		t.in();
		int layers = rbm.combinedLayers.length;
		for(int layer=1; layer<layers; layer++){
			List<WeightsNode> nodes = rbm.combinedLayers[layer].fromDown;
			for(WeightsNode n : nodes){
				n.refresh(rand, temperature);
			}
			//RbmLayer.normBySortedPointers(nodes);
			//"TODO what code copies between Neurons in first and first or last and last layers? Needs to happen so can display them right"
			//I'm creating second RbmLayer, that will not be used for learning, in those first and last layers
			//if(layer != layers-1){
				rbm.combinedLayers[layer].copyNodeStates(true);
			//}
		}
		t.out();
	}
	
	/** Opposite of up(RBM). Starts with RBM.mainRbmLayers[highest] and sets bit and scalar
	values in RBM.shortTermMemoryLayers and RBM.mainRbmLayers[second highest downward].
	*/
	public static void down(RbmData rbm, double temperature, Random rand){
		InOutTimer t = InOutTimer.forUnit("BoltzUtil.down");
		t.in();
		int layers = rbm.combinedLayers.length;
		int lastLayer = layers-1;
		for(int layer=lastLayer-1; 0<=layer; layer--){
			List<WeightsNode> nodes = rbm.combinedLayers[layer].fromUp;
			for(WeightsNode n : nodes){
				n.refresh(rand, temperature);
			}
			//RbmLayer.normBySortedPointers(nodes);
			//"TODO what code copies between Neurons in first and first or last and last layers? Needs to happen so can display them right"
			//I'm creating second RbmLayer, that will not be used for learning, in those first and last layers
			//if(layer != 0){
				rbm.combinedLayers[layer].copyNodeStates(false);
			//}
		}
		t.out();
	}
	
	/** Gets NeuralNode.bit bits from BM.shortTermMemoryLayers concat in ascending layer order
	Multicat knows layer sizes, but can also use Multicat.data() by itself if you have the RBM
	since it knows its owns layer sizes.
	*/
	public static Polycat getShortTermMemoryBits(RbmData rbm){
		Bits b = Fast0To16Bits.EMPTY;
		Bits cat[] = new Bits[rbm.shortTermMemoryLayers.length];
		int catFilled = 0;
		for(RbmLayer layer : rbm.shortTermMemoryLayers){
			//shortTermMemoryLayers only have fromUp
			Bits bitsFromUpOfLayer = getBits(layer.fromUp);
			//b = b.cat(bitsFromUpOfLayer);
			cat[catFilled++] = bitsFromUpOfLayer;
		}
		//for(ObservedRect r : rbm.shortTermMemoryLayers){
		//	Bits bitsFromLayer = getBits(r);
		//	b = b.cat(bitsFromLayer);
		//}
		return new SimplePolycat(cat);
	}
	
	/** Reverse of getShortTermMemoryBits(RBM rbm).
	Can use Multicat.data() or any Bits the right size which gets its layer sizes from the RBM.
	*/
	public static void setShortTermMemoryScalarsAndBits(RbmData rbm, Bits b){
		long correctSize = shortTermMemorySize(rbm);
		if(correctSize != b.siz()) throw new IndexOutOfBoundsException(
			"RBM short term memory size "+correctSize+" but Bits size "+b.siz());
		for(RbmLayer r : rbm.shortTermMemoryLayers){
			if(r.size != 0){
				//Many RBMs have shortTermMemory layers size 0 so this would have no effect.
				Bits bitsToLayer = b.pre(r.size);
				setScalarsAndBits(r.fromUp, bitsToLayer); //shortTermMemoryLayers only has fromUp
				b = b.suf(r.size);
			}
		}
	}
	
	public static long shortTermMemorySize(RbmData rbm){
		long g = 0;
		for(RbmLayer r : rbm.shortTermMemoryLayers){
			g += r.size;
		}
		return g;
	}
	
	public static void verifySize(List<WeightsNode> nodes, Bits b){
		if(nodes.size() != b.siz()) throw new IndexOutOfBoundsException(
			nodes.size()+" == ObservedRect.size != Bits.siz() == "+b.siz());
	}
	
	/** Same purpose as Statsys.predict func. 
	*
	public static Bits predict(RBM rbm, Bits in){
		if(in.siz() != rbm.mainRbmLayers[0].size) throw new RuntimeException()
		TODO
	}
	*
	public static TriMem predict(TriMem mem){
		throw new RuntimeException("TODO");
	}
	static{System.out.println("TODO Bits in must include state (the extra layers hanging off), and out must include that state, but its important to somehow make that difference known to the caller. This is vague because weights between nodes are another kind of state. There are 3-4 kinds of state now: weights, layersHangingOff, normal hidden rbm layers (which may be grouped with layersHangingOff), and visible node state");}
	
	/** TODO learn(TriMem mem,double learnRate), similar to predict(TriMem mem) except
	updates mutableLongTermMemory. Better would be something thats between predict and learn,
	that learns continuously while predicting. Maybe learn func does that depending on learnRate,
	but what am I really looking for?
	*
	public static TriMem learn(TriMem mem, double learnRate){
		throw new RuntimeException("TODO");
	}*/
	
	public static Polydim getDefaultSmallTrainingData16x16Images(){
		return getDefaultSmallTrainingData16x16Images(30);
	}

	/** Returns howManyOfEachDigit of each baseTen digit, as long as its at most 800 of each,
	or however many are in the training set which is a little more than that.
	Returns them spread evenly, each block of 10 has 1 of each digit.
	*/
	public static Polydim getDefaultSmallTrainingData16x16Images(int howManyOfEachDigit){
		Polydim mnistOcrTrainingData = MnistOcrDataset.readTestLabeledImages16x16AsMultiDim();
		Polydim trainingDataOfDigit[] = new Polydim[10];
		for(byte baseTenDigit=0; baseTenDigit<10; baseTenDigit++){
			trainingDataOfDigit[baseTenDigit] = MnistOcrDataset.getAllOfDigitFrom16x16(
				mnistOcrTrainingData, baseTenDigit);
			System.out.println("mnistOcrTrainingData for baseTenDigit "+baseTenDigit
				+" has "+trainingDataOfDigit[baseTenDigit].dimSize(0)+" images.");
		}
		Bits trainingData = Fast0To16Bits.EMPTY;
		for(long i=0; i<howManyOfEachDigit; i++){
			for(byte baseTenDigit=0; baseTenDigit<10; baseTenDigit++){
				Bits image = trainingDataOfDigit[baseTenDigit].bits(i).data();
				trainingData = trainingData.cat(image);
			}
		}
		return new SimplePolydim(trainingData, 10*howManyOfEachDigit, 16, 16);
	}
	
	/*public static Polydim ge(byte baseTenDigit){
		Polydim mnistOcrTrainingData = MnistOcrDataset.readTestLabeledImages16x16AsMultiDim();
		return MnistOcrDataset.getAllOfDigitFrom16x16(mnistOcrTrainingData, baseTenDigit);		
	}*/
	
	/** Between each pair, 1 from each of x and y, sets weight symmetricly to bellCurve observation */
	public static void randomizeConnectionsSymmetricly(List<WeightsNode> x, List<WeightsNode> y,
			double ave, double stdDev, Random rand){
		for(WeightsNode nx : x){
			for(WeightsNode ny : y){
				//double weight = 5;//testing
				double weight = ave+stdDev*rand.nextGaussian();
				//System.out.println("Setting random weight "+weight);
				nx.setWeightFrom(ny, weight);
				ny.setWeightFrom(nx, weight);
				//System.out.println("Setting random weight "+weight+" gets... "+nx.weightFrom(ny)+" "+ny.weightFrom(nx));
			}
		}
	}
	
	/** TODO... (for now do it the slow way)...
	Optimized to only call WeightsNode.indexOf(Neuron) once,
	then use reverseIndexIn which is usually cached,
	but this func is not nearly as fast as code like in makeBidirectional which
	avoids indexOf completely because it loops over all nodes by int index
	and then uses reverseIndexIn.
	*/
	public static void addToBothWeightsBetween(WeightsNode n, double addToWeight, WeightsNode m){
		//TODO setWeightFrom(int) func as optimization to not call indexOf(WeightsNode) if already know it
		//TODO remove(int) func, and call inside setWeightFrom and in BoltzUtil.addToBothWeightsBetween
		//and in BoltzUtil.setWeightBetween
		/*
		int i = n.indexOf(m);
		int j = n.reverseIndexIn(i);
		n.weightFrom[i] += addToWeight;
		if(n.weightFrom[i] == 0) n.setWeightFrom(m, 0); //disconnect
		m.weightFrom[j] += addToWeight;
		if(m.weightFrom[j] == 0) m.setWeightFrom(n, 0); //disconnect
		
		"TODO if i andOr j are -1 because weight is 0"
		*/
		//for now do it the slow way
		n.setWeightFrom(m, n.weightFrom(m)+addToWeight);
		m.setWeightFrom(n, m.weightFrom(n)+addToWeight);
	}
	
	/** TODO this is the slow way for now.
	See comment on addToBothWeightsBetween about why this is much slower than in a loop
	*/
	public static void setWeightBetween(WeightsNode n, double newWeight, WeightsNode m){
		//TODO setWeightFrom(int) func as optimization to not call indexOf(WeightsNode) if already know it
		//TODO remove(int) func, and call inside setWeightFrom and in BoltzUtil.addToBothWeightsBetween
		//and in BoltzUtil.setWeightBetween
		/*
		int i = n.indexOf(m);
		int j = n.reverseIndexIn(i);
		if(newWeight != 0){
			n.weightFrom[i] = newWeight;
			m.weightFrom[j] = newWeight;
		}else{
			n.setWeightFrom(m, 0.); //disconnect
			m.setWeightFrom(n, 0.); //disconnect
		}
		*/
		//for now do it the slow way
		n.setWeightFrom(m, newWeight);
		m.setWeightFrom(n, newWeight);
	}
	
	public static void disconnect(SparseNode n, SparseNode m){
		throw new RuntimeException("TODO finish code for remove funcs in SparseNode and generalize WeightsNode.setWeightFrom to use swapIndexs");
	}
	
	public static Polydim shuffleFirstDim(Polydim p, Random rand){
		List<Bits> list = new ArrayList<Bits>();
		for(long i=0; i<p.dimSize(0); i++){
			list.add(p.bits(i).data());
		}
		Collections.shuffle(list, rand);
		Bits bb = Fast0To16Bits.EMPTY;
		for(Bits b : list){
			bb = bb.cat(b);
		}
		//same dim sizes, just reorder in first dim
		long d[] = new long[(int)p.dims()];
		for(int i=0; i<d.length; i++) d[i] = p.dimSize(i);
		return new SimplePolydim(bb, d);	
	}
	
	/** Returns a map whose values range 0 to map.size()-1.
	Key is SparseNode.address or Namespace.localName.
	Value is integer to represent that node when saving it to a new Bits.
	<br><br>
	TODO Throws if any node occurs multiple times in the RBM,
	in same layer or across different layers.
	<br><br> 
	TODO This is deterministic because each layer's current order is used,
	and then higher layers, counting up from integers starting at 0. 
	*/
	public static Map<Long,Integer> rbmNodeIdMap(RbmData rbm){
		
		//TODO what about the 2 nodes which represent each node in RbmLayer.fromUp and RbmLayer.fromDown? 
		
		/*int i = 0;
		for(rbm.mainLayers)
		*/
		throw new RuntimeException("TODO");
	}
	
	/** Returns a snapshot of the RBM's weights and layer sizes,
	not including algorithms, annealing strategy, etc,
	since the same weights can be used with many kinds of algorithms.
	<br><br>
	Its represented sparsely as first a header for layer sizes etc then
	many groups of 3 things each: 2 integers and 1 scalar.
	It could be smaller by not repeating 1 of the integers and just push it,
	list its weighted edges, then pop it, but I want to be able to point at these
	and whoever gets such a pointer, using the local definitions of the integers,
	to know what it means only by those 2 integers instead of having to look around
	for what integer has been pushed.
	<br><br>
	TODO what about shortTermMemory layers? 
	*/
	public static Bits toBits(RbmData rbm, int bitsPrecisionPerScalar){
		
		//TODO in choosing what precisions to use in calls of this function consider benfrayfieldResearch.qnStatsysWeightPrecisionInBits
		
		//TODO what about the 2 nodes which represent each node in RbmLayer.fromUp and RbmLayer.fromDown?
		
		//First, longs for quantity of layers and cumulative size at each layer,
		//so the last long is number of nodes in the RBM.
		
		//Then some longs for sizes of the integers and scalars.
		//TODO what representation of scalar? Use standard 64 bit doubles?
		//I want control of the number of bits of precision.
		
		Map<Long,Integer> idMap = rbmNodeIdMap(rbm);
		
		//TODO should only the fromDown layers be included, since other layers
		//can be derived from that symmetricly?
		//If so, then it has the advantage of every node generating
		//its own local Bits, if we use the long ids
		//of SparseNode.address/Namespace.localName, but I probably dont want to
		//use those since they are local to a computer or small group of computers
		//and 64 bits is not enough for global names of every object created in memory,
		//so maybe instead derive smaller integers from sorting them and using
		//sorted index 0 to numberOfNodes-1.
		//Only use as many bits in those integers as needed, instead of a constant size.
		//How would that mapping work? Map<Long,Long>? Binarysearch long[]?
		//I want to choose Binarysearch long[], but that would mean the nodes indexs
		//would have to be in order of rbm layers, which they may not be.
		//So I am forced to use Map<Long,Long> to create the Bits but not to read it.
		
		//TODO Nodes in NodeScreen.P_PAINTVAR layer must not be included,
		//but how to tell the difference? They wont be listed in any of the layers.
		//Also their connections decay to 0 in about 20 seconds
		//after mouse last paints there, but I want to be able to save and load RBMs
		//all the time even while they're being painted. Since the paint nodes are
		//meant to be ui, they're not considered part of the logic of the rbm
		//and are an external force on it, even though technically they are
		//in the input node lists of the RBM's nodes.
		
		throw new RuntimeException("TODO");
	}
	
	public RbmData rbmFromBits(Bits b){
		throw new RuntimeException("TODO");
	}
	
	public static boolean[] bitsToArray(Bits b){
		long siz = b.siz();
		if(Integer.MAX_VALUE < siz) throw new RuntimeException("Not fit in int size: "+b);
		boolean a[] = new boolean[(int)siz];
		bitsIntoArray(b, a);
		return a;
	}
	
	public static void bitsIntoArray(Bits b, boolean a[]){
		if(a.length != b.siz()) throw new RuntimeException(
			"Different sizes: array "+a.length+" and bits "+b.siz());
		for(int i=0; i<a.length; i++) a[i] = b.bitAt(i);
	}
	
	/** This is faster because it avoids AVL tree rotation by recursing on array halfs of different by 1 */
	public static Bits arrayToBits(boolean... a){
		return arrayToBits(a, 0, a.length);
	}
	
	/** This is faster because it avoids AVL tree rotation by recursing on array halfs of different by 1.
	Uses Fast0To16Bits for sizes at most 16.
	*/
	public static Bits arrayToBits(boolean a[], int start, int endExclusive){
		if(a.length <= 16){
			throw new RuntimeException("TODO finish qnBitstringEndian (change to bigEndian) then run TestBits to verify. Then rebuild the mnistOcrDataset testing data file as *.multidim and this time the name is *.polydim");
		}else{
			throw new RuntimeException("TODO");
		} 
	}

}