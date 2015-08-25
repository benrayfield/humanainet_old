package humanainet.blackholecortex;

/** Any readonly function of the weights of a WeightsNode and connected nodes,
including its addToWeight var. Only reads the WeightsNode, doesnt modify.
These funcs include weighted sums, min, max, may include normalizing, etc.
*/
public interface WeightsFunc{
	
	public double weightsFunc(WeightsNode n, double temperature);

}
