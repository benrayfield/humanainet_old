package humanainet.rbmparams.actions.usinglearnrate;

import humanainet.blackholecortex.boltz.RbmData;
import humanainet.rbmparams.RbmActionUsingLearnRate;
import humanainet.rbmparams.actions.usinglearnrate.old.WeightDecayByMultAndPowerOfWeights;

/** See comment in WeightDecayByMultAndPowerOfWeights */
public class WeightDecayByLinearWeights extends WeightDecayByMultAndPowerOfWeights{
	
	public WeightDecayByLinearWeights(double mult){
		super(1, mult);
	}

	public void rbmActionUsingLearnRate(RbmData rbm, double learnRate){
		throw new RuntimeException("TODO");
	}

}
