package humanainet.blackholecortex;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Number Node, has scalar and bit vars which can be used for anything. */
public class NumNode extends SparseNode{
	
	/** Attention ranges 0 to 1 and chooses how much influence specific nodes have in learning and predicting.
	<br><br>
	In neural nodes at least, the "scalar" var is multiplied by attention after scalar is calculated from sigmoid of
	weighted sum. Then if this is a bernoulli node (bernoulli distribution describes weighted coin flips) observe
	the bit var that way as usual in a boltzmann machine (which is a bidirectional kind of neuralnet), for example.
	<br><br>
	In the simplest case attention is always 1. It could instead be set by another neuralnet who wants this neuralnet
	to learn or predict based on certain parts of its mind or data.
	Any way of spreading attention across the nodes will technically work,
	but a good spread of contexts is useful for more general learning.
	When not set by another network, this is meant to be used with sortedPointers to choose a percentile graph
	of how much attention should be at various nodes, so it could be controlled that maybe 20% of the nodes have
	most of the attention while the top 40% have a little attention, etc.
	If you try to think about everything at once, you'll end up very confused. Thats what attention is for.
	https://en.wikipedia.org/wiki/Saccade and the scalar confidence in TruthValue in OpenCog are examples of attention.
	*/
	public double attention = 1;
	
	/** If this is a neural node, then scalar is its "neural activation", range 0 to 1,
	set to attention multiplied by the output of the neural func whose params are weighted sum from other nodes and temperature.
	As usual in boltzmann machines, weights are divided by temperature so as it gets colder, the sum of weights is for each
	node either very negative or very positive so it converges to extreme values near 0 or 1 and is less possible states.
	*/
	public double scalar;
	
	/** Optional, depending on if you want a continuous/scalar value or weighted coin flips. This would be how the coin lands
	and the weighted sums are multiplied by this bit (add each weight or dont),
	else multiply that weight by the scalar. Its the same as setting the scalar to 0 or 1 as the result of this coin flip
	and is an optimization of that to add instead of multiply. Its also a good logic as normally used in boltzmann machines
	to avoid local minimums.
	*/
	public boolean bit;
	
	public NumNode(long localName){
		super(localName);
	}

}