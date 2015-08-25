/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.physics.musclefactories;
import humanainet.common.CoreUtil;
import humanainet.smartblob.core.physics.Muscle;
import humanainet.smartblob.core.physics.muscles.LineMuscle;
import humanainet.smartblob.core.physics.muscles.factories.MuscleFactory;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineMuscleFactory implements MuscleFactory{
	
	//TODO TriMuscleFactory (for volume of triangles) and LineAndTriMuscleFactory (both kinds of muscles in same list)
	
	//TODO standardize the order of lines. Start with outer lines then go inward. But there are other choices.
	
	/** When LineData are excluded because their startDistance is 0, this epsilon is used for roundoff detection */ 
	protected float epsilon = .00001f;
	
	/** Used in calculating the range of distances each LineMuscle works with (and uses max above that) */
	protected float scaleMult = LineMuscle.defaultScaleMult;
	
	public Muscle[] newMuscles(LayeredZigzag smartblob, int quantity){
		List<Muscle> muscles = new ArrayList();
		for(MovLine line : smartblob.allLineDatas()){
			if(muscles.size() == quantity) break;
			if(line.startDistance < epsilon){
				muscles.add(new LineMuscle(line,scaleMult));
			}
		}
		if(muscles.size() < quantity) throw new RuntimeException(
			"Could only create "+muscles.size()+" muscles but you said you need "+quantity);
		return muscles.toArray(new Muscle[0]);
	}

	/** Excludes LineData whose startDistance is less than epsilon */
	public int maxMuscles(LayeredZigzag smartblob){
		int nonzeroLines = 0;
		for(MovLine line : smartblob.allLineDatas()){
			if(line.startDistance < epsilon) nonzeroLines++;
		}
		return nonzeroLines;
	}

}
