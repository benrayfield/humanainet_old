/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.changespeeds;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.smartblob.core.trianglemesh.MovLine;

/** Holds speed to a given constant like in physicsmataV1.2 distanceConstraints */
public class HoldSpeedConstant extends AbstractChangeSpeedLZ{
	
	public float holdSpeed;
	
	public HoldSpeedConstant(float holdSpeed){
		this.holdSpeed = holdSpeed;
	}
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		float totalY = 0, totalX = 0, totalYSpeed = 0, totalXSpeed = 0;
		for(int p=0; p<blob.layerSize; p++){
			MovCorner cd = blob.corners[0][p];
			float speed = (float)Math.sqrt(cd.speedY*cd.speedY + cd.speedX*cd.speedX);
			if(speed == 0) continue; //TODO random direction?
			float mult = holdSpeed/speed;
			cd.speedY *= mult;
			cd.speedX *= mult;
		}
	}

}