/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams;
import humanainet.blackholecortex.anneal.AnnealStrategy;

/** PARAM: Temperature adjustment for layer sizes, including sizes
with and without bernoulli. ... If layer sizes differ, which they usually do,
temperature could be adjusted depending on direction infoflow between them at the
time, from one to the other or the reverse. sum of weights is divided by
rbmTemperature before being used as parameter of sigmoid. This also covers
adjusting temperature for different amounts of bernoulli at different layers,
times, or ways of using the layers.
<br><br>
Caller can still use temperature(int thisCycle, int totalCycles) but it doesnt
have the extra accuracy this AnnealStrategyByLayerRatio interface is for.
They should instead use the temperature func with more params. 
*/
public interface AnnealStrategyBySize extends AnnealStrategy{
	
	/** Doesnt have to use (double)thisLayerSize/otherLayerSize only as a ratio.
	Those 2 vars could also be used for some nonlinear transform of temperature
	by layer sizes. I dont know what function of temperature would work best,
	but I do know these params will be useful inputs to it.
	<br><br>
	Another thing that may be useful is the average number, or a percentile graph of,
	the number of vars on in each layer.
	*/
	public double temperature(int thisCycle, int totalCycles, int thisLayerSize, int otherLayerSize);

}