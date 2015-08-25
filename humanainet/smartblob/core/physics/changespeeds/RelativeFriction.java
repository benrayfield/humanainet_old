/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.changespeeds;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.smartblob.core.trianglemesh.MovLine;

/** Moves (part of?) speed between adjacent corner/point of same triangle.
Moves only up to some maximum speed per second.
<br><br>
TODO? The part of speed it moves is only the part along the line between
pairs of corner/point. Or, should it be both x and y speed equally? 
<br><br> 
This is relative friction instead of just friction because
it doesnt slow an object's total movement
but will slow its vibration and less slow its rotation.
*/
public class RelativeFriction extends AbstractChangeSpeedLZ{
	
	public float maxSpeedToMovePerSecondInEachLine;
	
	public RelativeFriction(float maxSpeedToMovePerSecondInEachLine){
		this.maxSpeedToMovePerSecondInEachLine = maxSpeedToMovePerSecondInEachLine;
	}

	public void changeSpeed(LayeredZigzag z, float secondsSinceLastCall){
		float maxSpeedToMoveInEachLine = maxSpeedToMovePerSecondInEachLine*secondsSinceLastCall;
		for(MovLine line : z.allLineDatas()){
			moveSpeedBetweenLineEnds(line, maxSpeedToMoveInEachLine);
		}
	}
	
	public void moveSpeedBetweenLineEnds(MovLine line, float maxSpeedToMove){
		MovCorner a = line.adjacentCorners[0], b = line.adjacentCorners[1];
		float ddy = b.speedY - a.speedY;
		float ddx = b.speedX - a.speedX;
		double ddRadiusSquared = ddy*ddy + ddx*ddx;
		if(ddRadiusSquared == 0) return;
		float ddRadius = (float)Math.sqrt(ddRadiusSquared);
		float moveHowMuchSpeed = Math.min(ddRadius/2, maxSpeedToMove);
		float mult = moveHowMuchSpeed/ddRadius;
		float moveY = ddy*mult, moveX = ddx*mult;
		//System.out.println("moveY="+moveY+" moveX="+moveX);
		b.speedY -= moveY;
		a.speedY += moveY;
		b.speedX -= moveX;
		a.speedX += moveX;
		
	}

}