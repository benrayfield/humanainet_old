/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core;
import humanainet.smartblob.core.brain.Brain;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.physics.Muscle;
import humanainet.smartblob.core.trianglemesh.MovTri;
import humanainet.statsysinterface.Statsys;
import humanainet.ui.core.shapes.Rect;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.Map;

/** A smartblob is a changing polygon which has a statsys observing and moving it
while the smartblob bounces on and is grabbed by other smartblobs.
The polygon can only change to another polygon with the same number of points.
<br><br>
Positions and speeds are never updated together as enforced by 4 funcs
which mark the start and end of updating positions and speeds.
*/
public interface Smartblob{
	
	//"TODO define outer border as existing java class or make new one?"
	
	//"TODO every Smartblob must tell its MovPoint, MovLine, and MovTri."
	
	/** Brains are mutable. Their size is immutable and matches size and order of muscles(). */
	public Brain brain();
	
	/** Mutable list of Muscle. Must be same size and order as vars in brain() when brain is used. */
	public List<Muscle> mutableMuscles();
	
	/** This is an approximation of the shape which can actually be scalars.
	<br><br>
	Make sure to use 1 pixel bigger in width and height for bounding rectangle
	because scalar positions get rounded down. Or are they rounded either way?
	I'm creating boundingRectangle() for that. Use that instead of this Polygons rect.
	<br><br>
	OLD BUT PARTIALLY RELEVANT:
	For compatibility with shapes that have int positions,
	a shape may have scalar positions but they can never get close enough
	to eachother that any 2 points occupy the same pixel at int positions,
	except for the innermost layer which are all held to equal x and y.
	Violating this may result in errors where Polygon objects say
	you have not enclosed a well defined shape since it crosses itself.
	*
	public Shape shape();
	*/
	
	/** 1 pixel bigger in all directions than the Polygon's rectangle since its based on ints
	and I'm undecided what kind of rounding I'll end up using.
	TODO is this Rect cached until positions change? I think so, but make sure.
	*/
	public Rect boundingRectangle();
	
	/** Returns the TriData of the closest outer LineData to bounce on
	or null if no collision. It doesnt have to literally intersect that triangle,
	but it does have to be the best outer line to bounce on.
	<br><bre>
	Direction of bounce is away from the other point on the LineData's only TriData.
	<br><br>
	Closest outer LineData is defined as the LineData which contains,
	anywhere on that line between the 2 points,
	the closest point (on the line) to the given point.
	*/
	public MovTri findCollision(float y, float x);
	
	public void addToAllPositions(float addToEachY, float addToEachX, boolean addDirectly);
	
	/** Mutable list of physics ops that act on this smartblob. Add them to the list. */
	public List<ChangeSpeed> mutablePhysics();
	
	/** Between onStartUpdatePositions() and onEndUpdatePositions() */
	public boolean isUpdatingPositions();
	
	/** Between onStartUpdateSpeeds() and onEndUpdateSpeeds() */
	public boolean isUpdatingSpeeds();
	
	/** Tells this Smartblob that positions are being updated (based on speeds),
	maybe by external code.
	*/
	public void onStartUpdatePositions();
	
	/** This func includes updating boundingRectangle() and shape()
	or onStartUpdatePosition may mark the need for new Rectangle and Shape
	in each call of those funcs. It can only be cached after end and before start.
	*/
	public void onEndUpdatePositions();
	
	/** Tells this Smartblob that speeds are being updated, maybe by external code */
	public void onStartUpdateSpeeds();
	
	public void onEndUpdateSpeeds();

}