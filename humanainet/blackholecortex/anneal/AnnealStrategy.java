/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex.anneal;

/** When predicting vs learning, you may want to use a different number of annealing cycles,
usually less when predicting because the network is already tuned and you dont actually
have to change the temperature parameter from 1.0 at all, but it will converge faster.
*/
public interface AnnealStrategy{
	
	/** at least 1, usually more than cyclesPerLearnOne(). BoltzUtil.learnMany */
	public int cyclesPerLearnMany();
	
	/** at least 1. This is for when learning continuously, the next data comes in. */
	public int cyclesPerLearnOne();
	
	/** at least 1 */
	public int cyclesPerPredict();
	
	/** Range 1 (inclusive) to 0 (exclusive), starting at 1 and decreasing in later cycles.
	This is called in outer loop (size totalCycles) of predict and learn functions.
	thisCycle ranges 0 to totalCycles-1.
	*/
	public double temperature(int thisCycle, int totalCycles);
	
	//Considering replacing temperature funcs (including with more parmas in subinterfaces) with
	//public double temperature(Map<String,Object> params);
	//public String param_thisCycle = "thisCycle", param_totalCycles = "totalCycles";

}
