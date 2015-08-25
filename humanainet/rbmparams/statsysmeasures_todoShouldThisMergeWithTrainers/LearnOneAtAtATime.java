/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.statsysmeasures_todoShouldThisMergeWithTrainers;
import java.util.Random;

import humanainet.rbmparams.StatsysMeasure;
import humanainet.statsysinterface.Statsys;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.object.Polydim;

public abstract class LearnOneAtAtATime implements StatsysMeasure{
	
	/** 2d array of bits. First dim is which example. Second dim is bits of that example. */
	public final Polydim trainingData;
	
	public final double learnRate;
	
	public final Random rand;
	
	public LearnOneAtAtATime(Polydim trainingData, double learnRate, Random rand){
		this.trainingData = trainingData;
		if(trainingData.dims() != 2) throw new RuntimeException(
			"trainingData.dims() is "+trainingData.dims()
			+" but must be 2 (which example, then bits of that example)");
		if(trainingData.data().siz() == 0) throw new RuntimeException("Empty trainingData");
		if(Integer.MAX_VALUE < trainingData.dimSize(1)) throw new RuntimeException(
			"Data size per example does not fit in int: "+trainingData.dimSize(1));
		this.learnRate = learnRate;
		this.rand = rand;
	}

	public boolean changesStatsys(){ return true; }
	
	public int minSize(){ return (int)trainingData.dimSize(1); }
	
	public int maxSize(){ return minSize(); }

}
