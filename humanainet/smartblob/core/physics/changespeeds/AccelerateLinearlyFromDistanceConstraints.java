/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.changespeeds;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.smartblob.core.trianglemesh.MovLine;

/** Acceleration is proportional to difference between actual distance and targetDistance */
public class AccelerateLinearlyFromDistanceConstraints extends AbstractChangeSpeedLZ{
	
	public float mult;
	
	public AccelerateLinearlyFromDistanceConstraints(float mult){
		this.mult = mult;
	}
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		for(MovLine ld : blob.allLineDatas()){
			MovCorner a = ld.adjacentCorners[0], b = ld.adjacentCorners[1];
			float dy = b.y - a.y;
			float dx = b.x - a.x;
			float distance = (float)Math.sqrt(dx*dx+dy*dy);
			if(distance == 0) continue;
			float wantToAddToDistance = ld.targetDistance-distance; //positive or negative
			float normDy = dy/distance, normDx = dx/distance;
			float addToEachSpeed = wantToAddToDistance*mult;
			float addToSpeedY = normDy*addToEachSpeed;
			float addToSpeedX = normDx*addToEachSpeed;
			b.speedY += addToSpeedY;
			a.speedY -= addToSpeedY;
			b.speedX += addToSpeedX;
			a.speedX -= addToSpeedX;
		}
	}

}