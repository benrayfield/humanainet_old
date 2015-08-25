/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.changespeeds;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;

/** abstract ChangeSpeed that checks type, prints message if not,
and if right type calls a function with a LayeredZigzag and time param.
*/
public abstract class AbstractChangeSpeedLZ implements ChangeSpeed{
	
	public void changeSpeed(Smartblob blob, float secondsSinceLastCall){
		if(blob instanceof LayeredZigzag){
			changeSpeed((LayeredZigzag)blob, secondsSinceLastCall);
		}else{
			System.out.println(Smartblob.class.getName()+" type unknown: "+blob.getClass().getName());
		}
	}
	
	public abstract void changeSpeed(LayeredZigzag z, float secondsSinceLastCall);

}