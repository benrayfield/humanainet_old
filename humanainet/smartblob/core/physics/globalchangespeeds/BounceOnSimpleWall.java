/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.globalchangespeeds;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.GlobalChangeSpeed;
import humanainet.smartblob.core.physics.SmartblobSim;
//Move all uses of java.awt to pc package: import java.awt.Rectangle;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.ui.core.shapes.Rect;

public class BounceOnSimpleWall implements GlobalChangeSpeed{
	
	//TODO optimize collisions by checking boundingRectangle
	
	public float position;
	
	public final boolean verticalInsteadOfHorizontal;
	
	public final boolean maxInsteadOfMin;
	
	public BounceOnSimpleWall(float position, boolean verticalInsteadOfHorizontal, boolean maxInsteadOfMin){
		this.position = position;
		this.verticalInsteadOfHorizontal = verticalInsteadOfHorizontal;
		this.maxInsteadOfMin = maxInsteadOfMin;
		System.out.println(this);
	}
	
	public void globalChangeSpeed(SmartblobSim sim, float secondsSinceLastCall){
		Smartblob blobArray[];
		synchronized(sim.smartblobs){
			blobArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			Rect r = blob.boundingRectangle();
			if(anyPartIsPastThisWall(r)){
				if(blob instanceof LayeredZigzag){
					bounceSomePartsOnWall((LayeredZigzag)blob);
				}else{
					System.out.println(Smartblob.class.getName()+" type unknown: "+blob.getClass().getName());
				}
			}
		}
	}
	
	public boolean anyPartIsPastThisWall(Rect r){
		if(verticalInsteadOfHorizontal){
			if(maxInsteadOfMin){ //max vertical
				return r.y+r.height <= position;
			}else{ //min vertical
				return position <= r.y+r.height;
			}
		}else{
			if(maxInsteadOfMin){ //max horizontal
				return r.x+r.width <= position;
			}else{ //min horizontal
				return position <= r.x+r.width;
			}
		}
	}
	
	public void bounceSomePartsOnWall(LayeredZigzag z){
		final float position = this.position;
		for(MovCorner layerOfCorners[] : z.corners){
			for(MovCorner cd : layerOfCorners){
				if(verticalInsteadOfHorizontal){
					if(maxInsteadOfMin){ //max vertical
						if(position < cd.y){ //bottom wall
							cd.speedY = -Math.abs(cd.speedY);
							cd.y = position; //FIXME causes energy to not be conserved so subtract it from energy gradually
							//float past = cd.y-position;
							//cd.addToY -= 2*past;
							//cd.y -= 2*past;
						}
					}else{ //min vertical
						if(cd.y < position){ //top wall
							cd.speedY = Math.abs(cd.speedY);
							cd.y = position; //FIXME causes energy to not be conserved so subtract it from energy gradually
							//TODO float past = position-cd.y;
							//TODO cd.addToY += 2*position;
						}
					}
				}else{
					if(maxInsteadOfMin){ //max horizontal
						if(position < cd.x){ //right wall
							cd.speedX = -Math.abs(cd.speedX);
							cd.x = position; //FIXME causes energy to not be conserved so subtract it from energy gradually
							//TODO float past = cd.x-position;
							//TODO cd.addToX -= 2*position;
						}
					}else{ //min horizontal
						if(cd.x < position){ //left wall
							cd.speedX = Math.abs(cd.speedX);
							cd.x = position; //FIXME causes energy to not be conserved so subtract it from energy gradually
							//TODO float past = position-cd.x;
							//TODO cd.addToX += 2*position;
						}
					}
				}
			}
		}
	}
	
	public String toString(){
		if(verticalInsteadOfHorizontal) return (maxInsteadOfMin?"bottom":"top")+" wall "+position;
		return (maxInsteadOfMin?"right":"left")+" wall "+position;
	}

}
