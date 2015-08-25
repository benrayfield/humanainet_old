/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui.core.event;

/** Like a mouse move but many can be used at once, and instead of click/up/down there is pressure
which ranges 0 (not pressing)
to 1 (pressing "normal", but actually I found it means pressing hard as in the Android API).
*/
public class Touch{
	
	public final double y, x, pressure;
	
	/** Seconds since year 1970. Example: CoreUtil.time() returns this using
	a combination of System.currentTimeMillis() and System.nanoTime().
	*/
	public final double time;
	
	public Touch(double y, double x, double pressure, double time){
		this.time = time;
		this.y = y;
		this.x = x;
		this.pressure = pressure;
	}

}
