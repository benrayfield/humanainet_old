/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.actions.simple;
import java.util.List;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.boltz.BoltzUtil;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.rbmparams.RbmSimpleAction;

/** PARAM: Max Norm Regularization: A kind of normalizing weights where magnitude
of weight vector can be anything less than a certain radius. If it exceeds that
radius they are all reduced by multiplying by a number that makes them that radius.
*/
public class MaxNormRegularization implements RbmSimpleAction{
	
	public final double maxRadius;
	
	/** If false, you need to make the weights symmetric soon,
	which you may do after other changes to RBM's weights.
	*/
	public final boolean makeWeightsSymmetricAfter;
	
	public MaxNormRegularization(double maxRadius, boolean makeWeightsSymmetricAfter){
		this.maxRadius = maxRadius;
		this.makeWeightsSymmetricAfter = makeWeightsSymmetricAfter;
	}

	public void rbmAction(RbmData rbm){
		for(RbmLayer layer : rbm.combinedLayers){
			for(int i=0; i<2; i++){
				List<WeightsNode> list = i==0 ? layer.fromDown : layer.fromUp;
				for(WeightsNode n : list){
					maxNormRegularize(n, maxRadius);
					//TODO Do BolzUtil.makeBidirectional per node only if needed,
					//which is when its radius exceeds max and is changed,
					//which should happen less than half the time (TODO how much less?),
					//but then bidirectionalNormAgainInHowManyMods would have to be
					//kept track of per node, and that complicates things since
					//it applies to pairs of nodes, but could do it if either needs,
					//and that would work. So this is the planned design.
					//Neurons can be used asymmetricly. You dont have to use that var
					//in all neuralnets, but you do in boltzmann machines.
				}
			}
		}
		if(makeWeightsSymmetricAfter){
			//See comment "TODO Do BolzUtil.makeBidirectional per node only if"... above. 
			BoltzUtil.makeBidirectional(rbm);
		}
	}
	
	public static void maxNormRegularize(WeightsNode n, double maxRadius){
		if(n.size == 0) return;
		double radiusObserved = radius(n);
		if(maxRadius < radiusObserved){
			double newRadius = maxRadius;
			multWeights(n, maxRadius/radiusObserved);
		}
	}
	
	public static double radius(WeightsNode n){
		double sumOfSquares = 0;
		for(int i=0; i<n.size; i++){
			double w = n.weightFrom[i];
			sumOfSquares += w*w;
		}
		return Math.sqrt(sumOfSquares);
	}
	
	public static void multWeights(WeightsNode n, double mult){
		for(int i=0; i<n.size; i++){
			n.weightFrom[i] *= mult;
		}
	}

}
