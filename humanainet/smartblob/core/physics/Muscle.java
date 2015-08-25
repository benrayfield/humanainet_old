package humanainet.smartblob.core.physics;

/** 1 dimension (degree of freedom) which is read and written bidirectionally between
a Brain and a Smartblob bending and vibrating in a SmartblobSim.
The most common kinds are (TODO) distance between 2 CornerData and volume of TriData.
These must be normalized into range 0 to 1 since all Muscle and Brain vars use that range.
You can use any kind of curve you want. TODO use LinearInterpolate1Var code?
*/
public interface Muscle{
	
	/** Range 0 to 1 */
	public float read();
	
	/** Adds to speed of corners based on target distance (a function of writeFraction) force amount.
	It will usually only get partially there, or may even go a different direction depending on other
	forces also affecting it, so dont expect read() to equal what you pushToward(float,float).
	Also if you use a large force, you may push past it. Its meant to pushToward many small times,
	each time read() again to decide how to adjust.
	*/
	public void pushToward(float writeFraction, float force);

}