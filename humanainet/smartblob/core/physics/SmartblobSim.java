/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.globalchangespeeds.BounceOnSimpleWall;
import humanainet.smartblob.core.util.SmartblobUtil;
import humanainet.ui.core.shapes.Rect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Simulation of smartblob physics including collisions with eachother andOr walls
and maybe 1d heightmap from floor andOr other walls.
*/
public class SmartblobSim{
	
	public final Set<Smartblob> smartblobs = new HashSet();
	
	public final List<GlobalChangeSpeed> physicsParts;
	
	protected Rect cachedDynamicBounds;
	/** update this with updateDynamicBoundsAtMostInsideThis func */
	public Rect getCachedDynamicBounds(){ return cachedDynamicBounds; }
	
	public SmartblobSim(GlobalChangeSpeed... physicsParts){
		this.physicsParts = new ArrayList(Arrays.asList(physicsParts));
	}
	
	public void nextState(float secondsThisTime){
		Smartblob blobArray[];
		synchronized(smartblobs){
			blobArray = smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			blob.onStartUpdateSpeeds();
		}
		for(GlobalChangeSpeed p : physicsParts){
			p.globalChangeSpeed(this, secondsThisTime);
		}
		for(Smartblob blob : blobArray){
			blob.onEndUpdateSpeeds();
		}
		SmartblobUtil.moveAll(this, secondsThisTime); //calls onStart*  and onEnd* *UpdatePositions
		for(Smartblob blob : blobArray){
			blob.onStartUpdateSpeeds();
		}
		for(Smartblob blob : blobArray){
			//blob.nextState(secondsSinceLastCall); //does all SmartblobPhysicsPart and updateShape and maybe more
			//TODO threads
			for(ChangeSpeed c : blob.mutablePhysics()){
				c.changeSpeed(blob, secondsThisTime);
			}
			blob.onEndUpdateSpeeds();
		}
		SmartblobUtil.moveAll(this, secondsThisTime); //calls onStart*  and onEnd* *UpdatePositions
	}
	
	/** If no wall is found in direction of left, right, up, andOr down, that part of maxBounds is used instead.
	Updates the dynamicBounds var to at most hardMaxbounds or usually what the BounceOnSimpleWall say inside that.
	*/
	public void updateDynamicBoundsAtMostInsideThis(Rect hardMaxBounds){
		//top of screen is 0. Increase is down.
		float top = hardMaxBounds.y;
		float bottom = hardMaxBounds.y+hardMaxBounds.height;
		float left = hardMaxBounds.x;
		float right = hardMaxBounds.x+hardMaxBounds.width;
		for(GlobalChangeSpeed physicsPart : physicsParts){
			if(physicsPart instanceof BounceOnSimpleWall){
				BounceOnSimpleWall wall = (BounceOnSimpleWall) physicsPart;
				if(wall.verticalInsteadOfHorizontal){ //vertical
					if(wall.maxInsteadOfMin){ //bottom
						bottom = Math.min(bottom, wall.position);
					}else{ //top
						top = Math.max(top, wall.position);
					}
				}else{ //horizontal
					if(wall.maxInsteadOfMin){ //right
						right = Math.min(right, wall.position);
					}else{ //left
						left = Math.max(left, wall.position);
					}
				}
			}
		}
		if(right <= left || top <= bottom) cachedDynamicBounds = new Rect(hardMaxBounds.y, hardMaxBounds.x, 0, 0);
		cachedDynamicBounds = new Rect(top, left, bottom-top, right-left);
	}
	
	/** TODO use a 2d grid of squares and each square contains any Smartblob whose boundingRect touches that square,
	but for now it compares the Rect to all Smartblobs.
	*/
	public Smartblob[] smartblobsPossiblyIntersecting(Rect r){
		List<Smartblob> list = new ArrayList();
		synchronized(smartblobs){
			for(Smartblob blob : smartblobs){
				if(r.intersects(blob.boundingRectangle())){
					list.add(blob);
				}
			}
		}
		return list.toArray(new Smartblob[0]);
	}
	
	
	/** Adds the object into the space and returns it */
	public Smartblob addRandomObject(Map optionalParams, Random rand){
		throw new RuntimeException("TODO");
	}

}
