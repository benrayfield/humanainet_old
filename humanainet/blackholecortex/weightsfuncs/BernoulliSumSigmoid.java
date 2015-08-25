package humanainet.blackholecortex.weightsfuncs;
import humanainet.blackholecortex.WeightsFunc;
import humanainet.blackholecortex.WeightsNode;
import humanainet.common.MathUtil;

/** weighted coin flips are called https://en.wikipedia.org/wiki/Bernoulli_distribution
This is a weighted sum after observing such coin flips of the chance of each node's neural function
as usual in boltzmann machines. This does not set those bits. Thats done by node.refreshBit()
which is called after node.refreshScalar().
*/
public class BernoulliSumSigmoid implements WeightsFunc{
	
	public static final BernoulliSumSigmoid instance = new BernoulliSumSigmoid();

	public double weightsFunc(WeightsNode n, double temperature){
		double sum = n.sumWeightsPerOnBit();
		return MathUtil.sigmoid(sum/temperature);
	}

}