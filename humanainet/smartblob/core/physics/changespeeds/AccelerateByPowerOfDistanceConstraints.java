/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.changespeeds;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.smartblob.core.trianglemesh.MovLine;

/** Same as AccelerateLinearlyFromDistanceConstraints except
theres an exponent that doesnt have to be 1.
*/
public class AccelerateByPowerOfDistanceConstraints extends AbstractChangeSpeedLZ{
	
	public double mult, exponent;
	
	public AccelerateByPowerOfDistanceConstraints(double mult, double exponent){
		this.mult = mult;
		this.exponent = exponent;
	}
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		final double mult = this.mult, exponent = this.exponent;
		for(MovLine ld : blob.allLineDatas()){
			MovCorner a = ld.adjacentCorners[0], b = ld.adjacentCorners[1];
			float dy = b.y - a.y;
			float dx = b.x - a.x;
			float distance = (float)Math.sqrt(dx*dx+dy*dy);
			if(distance == 0) continue;
			float wantToAddToDistance = ld.targetDistance-distance; //positive or negative
			float normDy = dy/distance, normDx = dx/distance;
			double exp = Math.pow(Math.abs(wantToAddToDistance), exponent);
			float addToEachSpeed = (float)(mult*exp);
			if(wantToAddToDistance < 0) addToEachSpeed = -addToEachSpeed;
			float addToSpeedY = normDy*addToEachSpeed;
			float addToSpeedX = normDx*addToEachSpeed;
			b.speedY += addToSpeedY;
			a.speedY -= addToSpeedY;
			b.speedX += addToSpeedX;
			a.speedX -= addToSpeedX;
		}
	}

}