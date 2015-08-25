/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.brain;
import java.util.Random;

/** Stateful. May learn over many calls or do the same thing every time or anywhere between.
This is where any AI code can hook into smartblob,
especially statistical kinds like boltzmann machine or bayesian network.
All input and output numbers (except time) range 0 to 1.
*/
public interface Brain{
	
	/** size of float array in think func */
	public int size();

	/** Before calling this, put numbers from Muscle.read() in io in the order of the Muscle in a smartblob.
	Then call this and get the changed numbers out of io and tell each Muscle to push toward that
	with some force. Default amount of force is 1.
	TODO In future versions, this may vary based on confidence in each predicted value in this array.
	TODO in future versions pairs of even/odd indexs in the array may be used together
	as target data in muscle and the other is amount of force.
	*/
	public void think(double io[], double secondsSinceLastCall, Random rand);

}
