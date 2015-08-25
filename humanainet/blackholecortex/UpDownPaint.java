package humanainet.blackholecortex;

public interface UpDownPaint{
	
	//TODO Now that NeuralTool is being created, can we remove P_PAINTVAR and use the standard P_FROMUP and P_FROMDOWN as if mouse is just another neuraltool which connects to all those in whatever shape the mouse does?

	/** constant for FlatPYX.p for Neuron whose main edges are from higher RbmLayer.
	All RbmLayer except last have fromUp edges.
	*/
	public static final int P_FROMUP = 0;
	
	/** constant for FlatPYX.p for Neuron whose main edges are from lower RbmLayer.
	All RbmLayer except first and shortTermMemory have fromDown edges.
	*/
	public static final int P_FROMDOWN = 1;
	
	
	/** constant for FlatPYX.p, which mouse draws on (and they decay gradually to 0)
	to adjust node scalar values in P_MAIN_LAYER and P_FLIP_LAYER (which are held to equal).
	*/
	public static final int P_PAINTVAR = 2;
	

}
