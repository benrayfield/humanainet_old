/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.params;
import humanainet.rbmparams.RbmParam;

/** Quantity of layers is not an RbmParam because it would
make the quantity of params variable. This class LayerSize is
the most basic example of that.
*/
public class LayerSize implements RbmParam{
	
	public final int whichLayer;
	
	public final int layerSize;
	
	public LayerSize(int whichLayer, int layerSize){
		this.whichLayer = whichLayer;
		this.layerSize = layerSize;
	}
	
	//TODO

}
