/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.statsysmeasures_todoShouldThisMergeWithTrainers;
import java.util.Random;

import humanainet.statsysinterface.Statsys;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.object.Polydim;

public class LearnOneAtAtATimeAndMeasureInSameOrderAtEnd extends LearnOneAtAtATime{

	public LearnOneAtAtATimeAndMeasureInSameOrderAtEnd(Polydim trainingData, double learnRate, Random rand){
		super(trainingData, learnRate, rand);
	}

	public double measure(Statsys s){
		long examples = trainingData.dimSize(0);
		for(long e=0; e<examples; e++){
			Bits example = trainingData.bits(e).data();
			s.learnOneBitsObject(example, learnRate, rand);
		}
		double measureSum = 0;
		//TODO measure them in random order?
		for(long e=0; e<examples; e++){
			Bits example = trainingData.bits(e).data();
			double measured = new MeasurePredictBits(example, rand).measure(s);
			measureSum += measured;
		}
		double aveMeasure = measureSum/examples;
		return aveMeasure;
	}

}
