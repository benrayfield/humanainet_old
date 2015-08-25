package humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis;
import java.awt.Rectangle;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import humanainet.blackholecortex.WeightsNode;

/** UPDATE: Only dimY and dimX in this class, leaving dimP to FlatPYX. 
<br><br>
OLD TEXT:
A rectangle observed in a specific FlatXYP and p level.
An unobserved rectangle is more general since it can refer to any FlatXYP or p level in it.
<br><br>
Does not obey the definition of List which is it equals if its contents equal,
since it also depends on which Rectangle, FlatXYP, and p level.
Equals if all 3 of those equal (p level and FlatXYP by ==, and Rectangle by equals).
<br><br>
WARNING about hashCode and equals:
If you need hashCode (and equals? TODO choose a design) to work with other Lists,
then the NeuralNodes in the FlatYX must not be replaced, which is the normal way.
At least for now, equals func only returns true if the other is an ObservedRect
with the same FlatYX object and the 2 Rectangles equals.
*/
public class ObservedRect extends AbstractList<WeightsNode>{
	
	public final FlatYX data;
	
	public final Rectangle rect;
	
	public final int size, hash;
	
	protected static final String cantChangeFromHereMessage =
		"Cant remove from here, but can swap them in "+FlatPYX.class+"'s array.";
	
	public ObservedRect(FlatYX data, Rectangle rect){
		this.data = data;
		this.rect = rect;
		//TODO what if size exceeds int range?
		//Would probably only happen if FlatXYP is sparse, and its not supposed to be.
		size = rect.height*rect.width;
		//hash = data.hashCode()+rect.hashCode();
		//TODO this hashcode does not change when list contents do,
		//but normally FlatYX keeps the same NeuralNodes.
		//The alternative would be to copy the NeuralNodes into an ArrayList<NeuralNode>.
		hash = super.hashCode();
	}

	public WeightsNode get(int index){
		int yInRect = index/rect.width;
		int xInRect = index%rect.width;
		return data.yx[rect.y+yInRect][rect.x+xInRect];
	}
	
	public boolean remove(Object o){
		throw new UnsupportedOperationException(cantChangeFromHereMessage);
	}
	
	public Iterator<WeightsNode> iterator(){
		return new Iter(this);
	}

	public int size(){ return size; }
	
	public int hashCode(){ return hash; }
	
	public boolean equals(Object o){
		if(!(o instanceof ObservedRect)) return false;
		ObservedRect or = (ObservedRect)o;
		return rect.equals(or.rect) && data==or.data; 
	}
	
	public static class Iter implements Iterator<WeightsNode>{
		
		//Was thinking I'd have to skip nulls, in next and hasNext,
		//but FlatPYX never has nulls, only NeuralNode with no edges.
		
		protected int xInRect, yInRect;
		public final ObservedRect r;
		
		
		public Iter(ObservedRect r){
			this.r = r;
		}
		public boolean hasNext(){
			//return x < r.data.xSize;
			//return yInRect < r.rect.height;
			return xInRect < r.rect.width || (yInRect < r.rect.height-1 && r.size != 0);
		}
		public WeightsNode next(){
			final Rectangle rect = r.rect;
			if(yInRect < rect.height){
				if(xInRect == rect.width){
					xInRect = 0;
					yInRect++;
				}
				return r.data.yx[rect.y+yInRect][rect.x+(xInRect++)];
			}
			/*if(y < r.data.ySize){
				if(x == r.data.xSize){
					x = 0;
					y++;
				}
				return r.data.yx[y][x++];
			}*/
			throw new NoSuchElementException();
		}
		public void remove(){
			throw new UnsupportedOperationException(ObservedRect.cantChangeFromHereMessage);
		}
		
	}

}