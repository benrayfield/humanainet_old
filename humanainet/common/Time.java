/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;

public class Time{
	private Time(){}
	
	public static final long startMillis;
	
	public static final long startNano;
	
	static{
		startMillis = System.currentTimeMillis();
		startNano = System.nanoTime();
	}
	
	/** Seconds since year 1970
	with relative nanosecond precision (System.nanoTime)
	and absolute few milliseconds precision (System.currentTimeMillis).
	<br><br>
	Practically, at least in normal computers in year 2011, this has about microsecond precision
	because you can only run it a few million times per second.
	TODO test it again on newer computers.
	*/
	public static double time(){
		//TODO optimize by caching the 2 start numbers into 1 double */
		long nanoDiff = System.nanoTime()-startNano;
		return .001*startMillis + 1e-9*nanoDiff; 
	}

}