package humanainet.rbmparams.actions.usinglearnrate;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.rbmparams.RbmActionUsingLearnRate;

import java.util.List;

public class WeightDecayByMultByFuncOfLearnRate implements RbmActionUsingLearnRate{
	
	public void rbmActionUsingLearnRate(RbmData rbm, double learnRate){
		/*
		double mult = TODO;
		for(RbmLayer layer : rbm.combinedLayers){
			for(int i=0; i<2; i++){
				List<Neuron> list = i==0 ? layer.fromDown : layer.fromUp;
				for(Neuron n : list){
					final int siz = n.size;
					final double weights[] = n.weightFrom;
					for(int j=0; j<siz; j++){
						weights[j] *= mult;
					}
				}
			}
		}
		*/
		throw new RuntimeException("TODO");
	}

}