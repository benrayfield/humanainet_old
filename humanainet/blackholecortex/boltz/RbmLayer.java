package humanainet.blackholecortex.boltz;
import java.util.List;

import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.ObservedRect;
import humanainet.common.MathUtil;
import humanainet.common.time.InOutTimer;

/** After I built RBMWithOneP (and renamed it from RBM to that),
I decided to organize RBM into 2 nodes per pixel on screen,
1 for up/down and the other for down/up in alternating even/odd pairs of RBM layers,
so it naturally flows as nodes only being updated from the layer above or below
but not both. bits andOr scalars must be copied between these pairs of nodes.
*/
public class RbmLayer{
	
	public final List<WeightsNode> fromDown, fromUp;
	//public final ObservedRect fromDown, fromUp;
	
	public final int size, hash;

	/** Either can be null, but not both at once, for end layers */
	public RbmLayer(List<WeightsNode> fromDown, List<WeightsNode> fromUp){
		this.fromDown = fromDown;
		this.fromUp = fromUp;
		/*if(!fromDown.rect.equals(fromUp.rect)) throw new IllegalArgumentException(
			"Different rectangles: "+fromDown.rect+" and "+fromUp.rect);
		*/
		this.size = fromDown==null ? fromUp.size() : fromDown.size(); //same size, if both nonnull
		int h = 0;
		if(fromDown != null) h += 343634431*fromDown.hashCode();
		if(fromUp != null) h += 23534369*fromUp.hashCode();
		hash = h;
	}
	
	/** TODO this may be backward...
	If up, copies from fromDown to fromUp, else copies from fromUp to fromDown
	*/
	public void copyNodeStates(boolean up){
		InOutTimer t = InOutTimer.forUnit("RbmLayer.copyNodeStates");
		t.in();
		//ObservedRect from, to;
		List<WeightsNode> from, to;
		if(up){
			from = fromDown;
			to = fromUp;
		}else{
			from = fromUp;
			to = fromDown;
		}
		if(from == null || to == null) throw new RuntimeException(
			"Both must be nonnull to copy between them. fromDown="+fromDown+" fromUp="+fromUp);
		/*int w = fromDown.rect.width, h = fromDown.rect.height;
		for(int y=0; y<h; y++){
			for(int x=0; x<w; x++){
				NeuralNode fromNode = from.data.yx[y][x];
				NeuralNode toNode = to.data.yx[y][x];
				toNode.influence = fromNode.influence;
				toNode.bit = fromNode.bit;
			}
		}*/
		for(int i=0; i<size; i++){
			WeightsNode fromNode = from.get(i);
			WeightsNode toNode = to.get(i);
			toNode.scalar = fromNode.scalar;
			toNode.bit = fromNode.bit;
		}
		t.out();
	}
	
	/*public static void normToHypersphere(List<Neuron> nodes,
			double newAve, double newRadiusFromThatAve){
		double sum = 0;
		for(Neuron n : nodes){
			
		}
		for(int i=0; i<d.length; i++){
			d[i] = nodes.get(i).influence;
		}
		
		for(int i=0; i<d.length; i++){
			nodes.get(i).influence = d[i];
		}
	}*/
	
	public void normBySortedPointers(){
		normBySortedPointers(fromDown);
		//for(Neuron n : fromDown) if(n.influence < .85) n.influence = 0;
		copyNodeStates(true);
	}
	
	public static void normBySortedPointers(List<WeightsNode> nodes){
		double d[] = new double[nodes.size()];
		for(int i=0; i<d.length; i++){
			d[i] = nodes.get(i).scalar;
		}
		MathUtil.normBySortedPointers(0, 1, d);
		for(int i=0; i<d.length; i++){
			nodes.get(i).scalar = d[i];
		}
	}
	
	public int hashCode(){ return hash; }
	
	public boolean equals(Object o){
		if(!(o instanceof RbmLayer)) return false;
		RbmLayer r = (RbmLayer) o;
		if(fromDown == null){
			//only fromUp nonnull here
			return r.fromDown==null && fromUp.equals(r.fromUp);
		}else if(fromUp == null){
			//only fromDown nonnull here
			return r.fromUp==null && fromDown.equals(r.fromDown);
		}else{ //both nonnull here
			return fromDown.equals(r.fromDown) && fromUp.equals(r.fromUp);
		}
	}

}
