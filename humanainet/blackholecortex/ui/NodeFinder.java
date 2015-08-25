package humanainet.blackholecortex.ui;

import humanainet.blackholecortex.WeightsNode;

public interface NodeFinder{
	
	public WeightsNode node(long address);
	
	/** Puts Node at its Node.address */
	public void add(WeightsNode n);

}