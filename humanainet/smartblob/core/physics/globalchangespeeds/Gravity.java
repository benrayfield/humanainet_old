/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.globalchangespeeds;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.GlobalChangeSpeed;
import humanainet.smartblob.core.physics.SmartblobSim;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;

/** Subtracts from vertical speed continuously
(actually adds since positive is down in java graphics) of all CornerData */
public class Gravity implements GlobalChangeSpeed{
	
	public float acceleration;
	
	public Gravity(float acceleration){
		this.acceleration = acceleration;
	}
	
	public void globalChangeSpeed(SmartblobSim sim, float secondsSinceLastCall){
		boolean downIsPositive = true; //in java graphics down is positive y
		float amount = secondsSinceLastCall*acceleration;
		float addToSpeed = downIsPositive ? amount : -amount;
		Smartblob blobArray[];
		synchronized(sim.smartblobs){
			blobArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			if(blob instanceof LayeredZigzag){
				addToAllYSpeeds((LayeredZigzag)blob, addToSpeed);
			}else{
				System.out.println(Smartblob.class.getName()+" type unknown: "+blob.getClass().getName());
			}
		}
	}
	
	public static void addToAllYSpeeds(LayeredZigzag z, float addToAllSpeeds){
		for(MovCorner layerOfCorners[] : z.corners){
			for(MovCorner cd : layerOfCorners){
				cd.speedY += addToAllSpeeds;
			}
		}
	}

}
