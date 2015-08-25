package humanainet.blackholecortex.ui;

public enum DisplayWhat{
	
	/** As an optimization, copy red brightness instead of calculating it again.
	Colors are calculated in order red, green, blue, so there is no CopyBlue.
	*/
	CopyRed,
	
	/** As an optimization, copy green brightness instead of calculating it again.
	Colors are calculated in order red, green, blue, so there is no CopyBlue.
	*/
	CopyGreen,
	
	WeightFromSelectedNode,
	
	WeightToSelectedNode,
	
	WeightFromSelectedNodeIsNonzero,
	
	WeightToSelectedNodeIsNonzero,
	
	ChanceOrInfluenceOfThisNode,
	
	BitOfThisNode,
	
	Nothing

}
