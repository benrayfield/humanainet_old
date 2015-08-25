package humanainet.rbmparams.actions.usinglearnrate.old;
import java.util.List;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.rbmparams.RbmActionUsingLearnRate;

/** TODO is the the derivative of this, or this directly? It makes more sense to me to
decay the weights by multiplying them by 1-learnRate or similar linear function,
which I'll also try. Between those, could use absoluteValue of Math.pow of each weight
so exponent can be gradually between 1 (linear) and 2 (squared) or outside those ranges.
<br><br>
Paper "A Practical Guide to Training Restricted Boltzmann Machines" says QUOTE
Weight-decay works by adding an extra term to the normal gradient. The extra term is the derivative
of a function that penalizes large weights. The simplest penalty function, called \L2", is half of the
sum of the squared weights times a coecient which will be called the weight-cost.
It is important to multiply the derivative of the penalty term by the learning rate. Otherwise,
changes in the learning rate change the function that is being optimized rather than just changing
the optimization procedure. UNQUOTE.
*/
public class WeightDecayByMultAndPowerOfWeights implements RbmActionUsingLearnRate{
	
	//FIXME TODO is it power of weights or the derivative of weights?
	
	public final double exponent, mult;
	
	/** mult happens after weights are exponented */
	public WeightDecayByMultAndPowerOfWeights(double exponent, double mult){
		this.exponent = exponent;
		this.mult = mult;
	}

	public void rbmActionUsingLearnRate(RbmData rbm, double learnRate){
		for(RbmLayer layer : rbm.combinedLayers){
			for(int i=0; i<2; i++){
				List<WeightsNode> list = i==0 ? layer.fromDown : layer.fromUp;
				for(WeightsNode n : list){
					/* TODO IS THIS WHY ABSVAL IS USED IN LINEAR?
					double sum = 0;
					final int siz = n.size;
					final double weights[] = n.weightFrom;
					for(int j=0; j<siz; j++){
						double w = weights[j];
						if(w < 0){
							
						}else{
							sum += Math.pow(w, sum);
						}
					}
					Next, mult by some function of those weights.
					*/
					throw new RuntimeException("TODO What to do about negatives?");
				}
			}
		}
	}

}
