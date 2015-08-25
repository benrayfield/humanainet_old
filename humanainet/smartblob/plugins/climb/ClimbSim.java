/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.plugins.climb;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.oracle.jrockit.jfr.TimedEvent;

import humanainet.common.MathUtil;
import humanainet.realtimeschedulerTodoThreadpool.Task;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.physics.GlobalChangeSpeed;
import humanainet.smartblob.core.physics.SmartblobSim;
import humanainet.smartblob.core.physics.changespeeds.AccelerateLinearlyFromDistanceConstraints;
import humanainet.smartblob.core.physics.changespeeds.Friction;
import humanainet.smartblob.core.physics.changespeeds.HoldCenterTogether;
import humanainet.smartblob.core.physics.changespeeds.RelativeFriction;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.util.SmartblobUtil;
import humanainet.ui.core.shapes.Rect;

/** For kinds of AI needing a continuous score stream. Score is sum of height
over time, so if you go high and stay there longer, your score is higher.
This is harder to do with many competing smartblobs trying to climb over you.
By using their Brains to choose how to reshape themselves and how to
respond to forces on them (at the cost of energy to push back),
they may strategicly learn to climb higher in the pile of smartblobs.
<br><br>
This can also be mixed with smartblob sexual evolution which is
a way to think about which combinations of smartblobs can
grab release bend reshape and use eachother as tools,
not as a label of which gender it is but a way to think about
the combinations of smartblobs that can form into compatible shapes
that by the physics simulation are able to bend those ways.
*/
public class ClimbSim extends SmartblobSim{
	
	/** Dynamic bounds using BounceOnSimpleWall can make this smaller, but not bigger */
	public final Rect hardMaxBounds;
	
	protected int maxTriesEachFindOpenSpace = 256;
	
	public ClimbSim(GlobalChangeSpeed... physicsParts){
		this(new Rect(0, 0, 0x10000, 0x10000), physicsParts);
	}
	
	public ClimbSim(Rect hardMaxBounds, GlobalChangeSpeed... physicsParts){
		super(physicsParts);
		this.hardMaxBounds = this.cachedDynamicBounds = hardMaxBounds;
	}
	
	public static List<ChangeSpeed> defaultChangeSpeeds = Collections.unmodifiableList(Arrays.<ChangeSpeed>asList(
			new AccelerateLinearlyFromDistanceConstraints(100f),
			
			//new Friction(15f),
			new Friction(25f),
			
			new RelativeFriction(.5f),
			
			new HoldCenterTogether()
	));
	
	
	
	/*public void nextState(float secondsThisTime){
		TODO
		super.nextState(secondsThisTime);
	}*/
	
	//TODO
	
	/** TODO various types of objects with various ways of thinking */
	public Smartblob addRandomObject(Map optionalParams, Random rand){
		/*Smartblob blob = SmartblobUtil.wavegear(
			null, 250, 500, 75, 90,
			3, 32, 5);
		*/
		//blob.mutablePhysics().addAll(defaultChangeSpeeds);
		Smartblob blob = SmartblobUtil.simpleSmartblobExample();
		blob.mutablePhysics().clear();
		blob.mutablePhysics().addAll(defaultChangeSpeeds);
		putInRandomEmptySpace(blob, rand);
		smartblobs.add(blob);
		return blob;
	}
	
	public void putInRandomEmptySpace(Smartblob blob, Random rand){
		Rect whereAndSizeNow = blob.boundingRectangle();
		Rect moveTo = findEmptySpace(whereAndSizeNow.height, whereAndSizeNow.width, rand);
		float addToEachY = moveTo.y - whereAndSizeNow.y;
		float addToEachX = moveTo.x - whereAndSizeNow.x;
		blob.addToAllPositions(addToEachY, addToEachX, true);
	}
	
	/** This uses dynamicBounds which you can update using updateDynamicBoundsAtMostInsideThis func */
	public Rect findEmptySpace(float height, float width, Random rand){
		final Rect bounds = cachedDynamicBounds;
		if(bounds.width < width || bounds.height < height) throw new RuntimeException(
			"Size you asked for height="+height+" width="+width+" doesnt fit in "+bounds);
		float verticalRange = bounds.height-height;
		float horizontalRange = bounds.width-width;
		final int maxTries = maxTriesEachFindOpenSpace;
		for(int i=0; i<maxTries; i++){
			float y = bounds.y+rand.nextFloat()*verticalRange;
			float x = bounds.x+rand.nextFloat()*horizontalRange;
			Rect randomRectInBounds = new Rect(y, x, height, width);
			Smartblob blobs[] = smartblobsPossiblyIntersecting(randomRectInBounds);
			if(blobs.length == 0) return randomRectInBounds;
		}
		throw new RuntimeException(
			"Tried "+maxTries+" times and couldnt find an empty space height="+height+" width="+width+" in bounds="+bounds);
	}

}
