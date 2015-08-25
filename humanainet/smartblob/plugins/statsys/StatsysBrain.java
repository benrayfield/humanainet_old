/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.plugins.statsys;
import humanainet.smartblob.core.brain.Brain;
import humanainet.statsysinterface.Statsys;

import java.util.Random;

public class StatsysBrain implements Brain{
	
	public final Statsys statsys;
	
	public double learnPerSecond;
	
	/** learnRate statsys param is learnPerSecond*secondsSinceLastCall */
	public StatsysBrain(Statsys statsys, double learnPerSecond){
		this.statsys = statsys;
		this.learnPerSecond = learnPerSecond;
	}

	public int size(){ return statsys.size(); }

	public void think(double io[], double secondsSinceLastCall, Random rand){
		if(io.length != statsys.size()) throw new RuntimeException(
			"Array size "+io.length+" not equal statsys size "+statsys.size());
		double learnRate = learnPerSecond*secondsSinceLastCall;
		//TODO verify and update in comments of that func that Statsys.learnOneScalars puts prediction back in array
		boolean alsoPredict = true;
		statsys.learnOneScalars(io, learnRate, rand, alsoPredict);
	}

}