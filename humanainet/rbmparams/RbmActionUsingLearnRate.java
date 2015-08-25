/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams;
import humanainet.blackholecortex.boltz.RbmData;

/** For things like weight decay as a function of learnRate
and various functions of the specific weights.
This doesnt learn a specific thing. Its done after that
but before things like CustomRBMSameParamsAcrossLayers.weightsNormAfterLearn.
*/
public interface RbmActionUsingLearnRate{
	
	public void rbmActionUsingLearnRate(RbmData rbm, double learnRate);

}