/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.statsysmeasures_todoShouldThisMergeWithTrainers;
import java.util.Random;

import humanainet.rbmparams.StatsysMeasure;
import humanainet.statsysinterface.Statsys;
import humanainet.wavetree.bit.Bits;

/** When a certain Bits is input to the Statsys, how close does it predict the same thing?
Distance is measured by dotProd divided by max possible dotProd since StatsysMeasure ranges 0 to 1.
 * @author ben
 *
 */
public class MeasurePredictBits implements StatsysMeasure{
	
	public final Bits bits;
	
	public final Random rand;
	
	public MeasurePredictBits(Bits bits, Random rand){
		this.bits = bits;
		if(Integer.MAX_VALUE < bits.siz()) throw new RuntimeException(
			Statsys.class.getName()+" has max size of int, but this size would need a long: "+bits.siz());
		this.rand = rand;
	}

	public double measure(Statsys s){
		Bits prediction = s.predictBitsObject(bits, rand);
		return (double)countEqualBits(bits, prediction)/bits.siz();
	}
	
	public boolean changesStatsys(){ return false; }
	
	public int minSize(){ return (int) bits.siz(); }
	
	public int maxSize(){ return minSize(); }
	
	public static long countEqualBits(Bits x, Bits y){
		long sum = 0;
		long siz;
		if((siz=x.siz()) != y.siz()) throw new RuntimeException("Different sizes: "+x+" and "+y);
		//TODO optimize by comparing longs until last 1-63 bits.
		for(long g=0; g<siz; g++){
			if(x.bitAt(g) == y.bitAt(g)) sum++;
		}
		return sum;
	}

}
