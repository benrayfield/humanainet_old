package humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis;
import java.util.SortedSet;

import humanainet.blackholecortex.WeightsNode;

public class Group{
	
	public final SortedSet<WeightsNode> nodes;
	
	/** temperature to use when updating nodes. Divide their FROM weights by this.
	By using different temperatures in opposite directions between 2 groups,
	information can flow more one direction than the other.
	<br><br>
	multWeight is slightly more flexible than temperature since it can be 0
	but temperature being infinity may cause floating point bugs.
	*/
	public double multWeight = 1;
	//public double temperature = 1;
	
	public Group(SortedSet<WeightsNode> nodes){
		this.nodes = nodes;
	}

}
