/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.changespeeds;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;

/** A constant acceleration and direction of a specific CornerData */
public class Push extends AbstractChangeSpeedLZ{
	
	public final MovCorner cd;
	
	public float accelerateY, accelerateX;
	
	public Push(MovCorner cd, float accelerateY, float accelerateX){
		this.cd = cd;
		this.accelerateY = accelerateY;
		this.accelerateX = accelerateX;
	}
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		if(blob != cd.smartblob) return;
		cd.speedY += accelerateY*secondsSinceLastCall;
		cd.speedX += accelerateX*secondsSinceLastCall;
	}

}