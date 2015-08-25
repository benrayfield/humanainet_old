/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics;

import humanainet.smartblob.core.Smartblob;

/** Part of a SmartblobSim physics simulation, stored in the Smartblob it acts on.
Can only update speeds. Positions are updated only 1 way based on speed.
*/
public interface ChangeSpeed{
	
	public void changeSpeed(Smartblob blob, float secondsSinceLastCall);

}
