/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.statsysfactory;
import java.util.Map;
import java.util.Set;

import humanainet.rbmparams.StatsysFactory;
import humanainet.statsysinterface.Statsys;

/** My first attempt at designing a StatsysFactory, which is a really hard problem
because the rbmParams are from many different learning algorithms,
and a StatsysFactory must return an object containing such a generated algorithm
defined by the combination of params.
*/
public class SimpleStatsysFactory implements StatsysFactory{
	
	//TODO should factory be done per layer for some things, after params that affect whole rbm are stored in an inprogress object? What about rbmParamGroupBetweenAdjacentLayer? Its between layers, not at layers, so maybe a separate procss for that.
	
	/** Keys I'm considering using:
	bernoulli...
	
	neuralFunc
	
	annealStrategy //TODO different number for each of learnOne, learnMany, predict, and then theres temperature func
	
	statsysMeasureAndRelearnAtRandom
	
	maxNormRegularization
	*/

	public Statsys mapParamFunc(Map<String,Object> params){
		throw new RuntimeException("TODO");
	}

	public Set<String> keysUnderstood(){
		throw new RuntimeException("TODO");
	}

	public Set<Object> exampleValuesForKey(String key){
		throw new RuntimeException("TODO");
	}
	
	//TODO

}
