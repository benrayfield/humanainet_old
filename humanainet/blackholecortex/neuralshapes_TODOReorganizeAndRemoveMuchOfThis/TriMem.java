/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.object.Polycat;
import humanainet.wavetree.bit.object.SimplePolycat;

/** longTermMemory is mutable because its so large, being made of weights between pairs of nodes.
shortTermMemory and senseMemory are immutable as Bits because they're small enough to snapshot.
<br><br>
benfrayfieldResearch.3LevelsOfMem says:
The "three levels of memory" are senseMemory, shortTermMemory, and longTermMemory.
... senseMemory includes the bit or scalar state of visibleNodes and hiddenNodes which
converge to near interchangible because rbmConvergesToUnitaryBetweenAdjacentLayers.
... shortTermMemory includes the bit or scalar state of neuralNode
in "rbm levels hanging off" like in rbmWithExtraLayersHangingOffAsShortTermMemory.
longTermMemory is the weights between all the neuralNode which have weights.
senseMemory changes fastest. shortTermMemory changes slower. longTermMemory
changes slowest.
*/
public class TriMem{
	
	public final RbmData mutableLongTermMemory;
	
	public final Bits immutableShortTermMemory;
	
	public final Bits immutableSenseMemory;
	
	protected Polycat immutableShortTermThenSenseMemory;
	
	public TriMem(RbmData mutableLongTermMemory, Bits immutableShortTermMemory, Bits immutableSenseMemory){
		this.mutableLongTermMemory = mutableLongTermMemory;
		this.immutableShortTermMemory = immutableShortTermMemory;
		this.immutableSenseMemory = immutableSenseMemory;
	}
	
	/** Create the cat of them only once here, and return again if called again */
	public Polycat immutableShortTermThenSenseMemory(){
		if(immutableShortTermThenSenseMemory == null) immutableShortTermThenSenseMemory =
			new SimplePolycat(immutableShortTermMemory, immutableSenseMemory);
		return immutableShortTermThenSenseMemory;
	}

}
