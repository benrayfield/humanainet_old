package humanainet.rbmparams.actions.usinglearnrate;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.rbmparams.RbmActionUsingLearnRate;
import humanainet.rbmparams.RbmSimpleAction;

/** Paper "A Practical Guide to Training Restricted Boltzmann Machines" says QUOTE
A different form of weight-decay called \L1" is to use the derivative of the sum of the absolute
values of the weights. This often causes many of the weights to become exactly zero whilst allowing
a few of the weights to grow quite large. This can make it easier to interpret the weights. When
learning features for images, for example, L1 weight-decay often leads to strongly
localized receptive fields. UNQOUOTE.
*/
public class WeightDecayByAbsValLinearWeights implements RbmActionUsingLearnRate{
	
	public void rbmActionUsingLearnRate(RbmData rbm, double learnRate){
		throw new RuntimeException("TODO");
	}

}