/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex;
import humanainet.wavetree.bit.object.Polydim;

public interface RbmMutableBitstring /*extends HeaderThenData*/{
	
	//"TODO what if I want float64 edges? Should I include an edgeType string in the header? Maybe that should be a different datastruct. I want to keep these really simple and have the locality property where each nodeNodeAndEdge describes a whole edge"
	
	//"TODO Should node integers be allowed to be bigger than needed for dense nodes (where each node has at least 1 edge), so sparse nodes would be allowed, which could be useful for decentralized neuralnets?"
	
	//"TODO Should the outer header be a polycat?"
	
	//"TODO create polydimcat. do I want a hybrid of polydim and polycat where the items in polydim are each a polycat, but the headers are combined as first polydim number of dims then size of each dim then polycat number of items then size of each item? Yes I want that."
	
	public long nodes();
	
	public long edges();
	
	/** how many bits in each integer which represents a node?
	This equals ceiling(logBase2(nodes()-1)).
	*/
	public long nameBits();
	
	/** Each edge 
	public long edgeBits();
	
	/** Its a 1d array where each item is concat of 2 nameBits and 1 edgeBits */
	public Polydim data();
	//public Bits data()?
}
