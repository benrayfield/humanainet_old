/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.brain.Brain;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.physics.Muscle;
import humanainet.smartblob.core.physics.muscles.factories.MuscleFactory;
import humanainet.smartblob.core.util.SmartblobUtil;
import humanainet.ui.core.shapes.Rect;
//Move all uses of java.awt to pc package: import java.awt.Color;
//Move all uses of java.awt to pc package: import java.awt.Graphics;
//Move all uses of java.awt to pc package: import java.awt.Polygon;
//Move all uses of java.awt to pc package: import java.awt.Rectangle;
//Move all uses of java.awt to pc package: import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Layers of zigzagloop around a center radialsmartblob. Like zigzagloop defines
the outer surface, that can be viewed as fluidTri and layers of such triangles
allow bending in shapes closer to curvesmartblob than only radialsmartblob.
Its somewhere between, depending on how many layers. As more layers are added,
in the calculusLimit, it theoretically reaches curvesmartblob. The disadvantage
is it requires more points (and distanceConstraints) than only modelling the outer
surface, but fewer layers can be used by making them thicker.
... This will be the first smartblobDatastruct capable of, if the statsys
is smart enough, grabbing arbitrary shaped objects as tools and having fast
physics calculations. It has the advantage of being made of constant parts
that bend instead of adding and deleting pieces as it reshapes.
*/
public class LayeredZigzag implements Smartblob{
	
	public final Brain brain;
	public Brain brain(){ return brain; }
	
	/** y[layer][point]. Odd layers have a half point angle offset when in balanced circle view. *
	public float y[][];	
	/** x[layer][point]. Odd layers have a half point angle offset when in balanced circle view. *
	public float x[][];
	*/
	//TODO public final CornerData corners[][] instead of float x[][] and y[][]
	
	/** corners[layer][point] */
	public final MovCorner corners[][];
	
	public final int layers, layerSize; 

	/** cache for triangle(int layer, int pointInlayer, boolean inward). Often has nulls. */
	public MovTri trianglesInward[][];
	//protected Shape trianglesInward[][];
	
	/** cache for triangle(int layer, int pointInlayer, boolean inward). Often has nulls. */
	public MovTri trianglesOutward[][];
	//protected Shape trianglesOutward[][];
	
	protected final List<ChangeSpeed> mutablePhysics = new ArrayList();
	public List<ChangeSpeed> mutablePhysics(){ return mutablePhysics; }
	
	/** Incremented by nextState(). Read by caches of triangle shapes */
	protected long cycle;
	public long cycle(){ return cycle; }
	
	protected final Map<LineName,MovLine> lineToLineData = new HashMap();
	
	protected boolean isUpdatingPositions, isUpdatingSpeeds;
	
	/** Can only be cached while !isUpdatingPositions * 
	protected Shape cachedShape;
	*/
	
	/** Can only be cached while !isUpdatingPositions */
	protected Rect cachedBoundingRect;
	
	public List<Muscle> mutableMuscles(){ return muscles; }
	public final List<Muscle> muscles = new ArrayList();
	
	//TODO hold volume constant. Name each triangle by 1 of the points.
	//Go counterclockwise to next point. Then go inward. Then back to the starting point.
	
	/** There must be at least 2 layers, counting the center point
	which is a layer where all points have equal position.
	*/
	public LayeredZigzag(Brain brain, MuscleFactory m, int layers, int layerSize, float centerY, float centerX, float radius){
		if(layers < 2) throw new RuntimeException("layers="+layers+" must be at least 2 and must be more if you want more than radial curve ability");
		if(layerSize < 3) throw new RuntimeException("layerSize="+layerSize+" must be at least 3 and is best to be at least 16");
		this.brain = brain;
		this.layers = layers;
		this.layerSize = layerSize;
		
		//y = new float[layers][layerSize];
		//x = new float[layers][layerSize];
		corners = new MovCorner[layers][layerSize];
		for(int layer=0; layer<layers; layer++){
			for(int p=0; p<layerSize; p++){
				CornerName c = new CornerName(layer, p);
				boolean edge = layer==0 || layer==layers-1;
				MovCorner cd = new MovCorner(this, c, edge);
				corners[layer][p] = cd;
			}
		}
		
		//trianglesInward = new Shape[layers][layerSize];
		//trianglesOutward = new Shape[layers][layerSize];
		trianglesInward = new MovTri[layers][layerSize];
		trianglesOutward = new MovTri[layers][layerSize];
		for(int layer=0; layer<layers; layer++){
			for(int p=0; p<layerSize; p++){
				if(0 < layer){ //inward exists
					TriName t = new TriName(layer, p, true);
					boolean edge = layer==layers-1;
					trianglesInward[layer][p] = new MovTri(this, t, edge);
				}
				if(layer < layers-1){ //outward exists
					TriName t = new TriName(layer, p, false);
					boolean edge = layer==0;
					trianglesOutward[layer][p] = new MovTri(this, t, edge);
				}
			}
		}
		
		//"TODO hook corners to corners"
		//"TODO hook triangles to triangles"
		//"TODO for each triangle add to it corner pointers"
		//"TODO for each corner add to it triangle pointers"
		for(int layer=0; layer<layers; layer++){
			for(int p=0; p<layerSize; p++){
				if(0 < layer){ //inward exists
					trianglesInward[layer][p].connectAdjacent();
				}
				if(layer < layers-1){ //outward exists
					trianglesOutward[layer][p].connectAdjacent();
				}
			}
		}
		for(int layer=0; layer<layers; layer++){
			for(int p=0; p<layerSize; p++){
				corners[layer][p].connectAdjacent();
			}
		}
		for(MovLine ld : allLineDatas()){
			ld.connectAdjacent();
		}
		
		boolean byVolume = false;
		resetShapeAcCircle(centerY, centerX, radius, byVolume);
		updateStartDistances();
		setTargetDistancesToStartDistances();
	}
	
	/** If addDirectly, adds to the position directly. Else adds to a var (if it exists, else directly)
	that stores the position to be added later when physics calculations are at that step.
	*/
	public void addToAllPositions(float addToEachY, float addToEachX, boolean addDirectly){
		for(int layer=0; layer<layers; layer++){
			for(int p=0; p<layerSize; p++){
				MovCorner c = corners[layer][p];
				if(addDirectly){
					c.y += addToEachY;
					c.x += addToEachX;
				}else{
					c.addToY += addToEachY;
					c.addToX += addToEachX;
				}
			}
		}
		invalidateCachedBoundingRect();
	}
	
	/*protected static class PolygonGetBounds extends Polygon{
		public PolygonGetBounds(int xpoints[], int ypoints[], int npoints){
			super(xpoints, ypoints, npoints);
		}
		public Rectangle getBackedBoundsRectangle(){
			if(bounds == null) getBounds();
			return bounds;
		}
	}*/
	
	public LineName[] allLines(){
		return lineToLineData.keySet().toArray(new LineName[0]);
	}
	
	public MovLine[] allLineDatas(){
		return lineToLineData.values().toArray(new MovLine[0]);
	}
	
	/** Line is an immutable type and will match as key even if you use a new Line.
	The first time this is called for each Line must be by a CornerData adding the
	Line to itself, and the second time by the other CornerData.
	*/
	public MovLine lineData(LineName line){
		MovLine d = lineToLineData.get(line);
		if(d == null){
			boolean edge = false;
			int lastLayer = layers-1;
			int lowLayer = line.cornerLow.layer, highLayer = line.cornerHigh.layer;
			if(lowLayer==0 && highLayer==0) edge = true;
			else if(lowLayer==lastLayer && highLayer==lastLayer) edge = true;
			d = new MovLine(this, line, edge);
			lineToLineData.put(line, d);
		}
		return d;
	}
	
	/** If byVolume, radius is chosen by linear increase in volume instead of radius directly */
	public void resetShapeAcCircle(float centerY, float centerX, float radius, boolean byVolume){
		double mult = 2*Math.PI/layerSize;
		double halfPointAngleOffset = mult/2;
		double maxVolume = Math.PI*radius*radius;
		for(int layer=0; layer<layers; layer++){
			double fraction = (double)layer/(layers-1);
			double thisRadius;
			if(byVolume){
				double thisVolume = fraction*maxVolume;
				//volume = pi*r^2
				//r = sqrt(volume/pi)
				thisRadius = Math.sqrt(thisVolume/Math.PI);
			}else{
				thisRadius = radius*fraction;
			}
			boolean layerIsOdd = (layer&1)==1;
			for(int p=0; p<layerSize; p++){
				double angle = p*mult;
				if(layerIsOdd) angle += halfPointAngleOffset;
				MovCorner c = corners[layer][p];
				c.y = centerY + (float)(thisRadius*Math.cos(angle));
				c.x = centerX + (float)(thisRadius*Math.sin(angle));
			}
		}
		//updateBoundingRectangle();
	}
	
	/** Updates each CornerData.startDistance to its current distance */
	public void updateStartDistances(){
		for(MovLine ld : allLineDatas()){
			MovCorner a = ld.adjacentCorners[0], b = ld.adjacentCorners[1];
			float dy = a.y - b.y, dx = a.x - b.x;
			ld.startDistance = (float)Math.sqrt(dy*dy + dx*dx);
		}
	}
	
	public void setTargetDistancesToStartDistances(){
		for(MovLine ld : allLineDatas()){
			ld.targetDistance = ld.startDistance;
		}
	}

	/*public void nextState(float secondsSinceLastCall){
		cycle++;
		//thinkAndChangeSpeed(secondsSinceLastCall);
		doInternalPhysics(secondsSinceLastCall);
	}
	
	/** does all SmartblobPhysicsPart and updateShape and maybe more *
	protected void doInternalPhysics(float secondsSinceLastCall){
		//TODO synchronize on blob.mutablePhysics()?
		for(ChangeSpeed p : mutablePhysics()){
			p.doPhysicsPart(this, secondsSinceLastCall);
		}
	}
	*/
	
	/*protected void thinkAndChangeSpeed(float secondsSinceLastCall){
		//throw new RuntimeException("TODO how can this be done independent of knowing what smartblobsh ave bounced on parts of me? Maybe they add to some force vars in me and I add to them, before this func is called?");
	}*/
	
	/** layer and pointInLayer define the first corner of the triangle.
	The second point is in the same layer and wrapping counterclockwise up.
	The third point is in the next lower or higher layer.
	Odd layers have a half point angle offset when in balanced circle view,
	so every pair of adjacent points
	in a layer are touching 1 specific point in next lower and higher layers.
	*
	public Shape triangleShape(int layer, int pointInlayer, boolean inward){
		if(inward){
			return trianglesInward[layer][pointInlayer].triangle();
		}else{
			return trianglesOutward[layer][pointInlayer].triangle();
		}
	}*
	
	public Shape triangleShape(Tri tri){
		return triangleShape(tri.layer, tri.point, tri.inward);
	}*/
	
	public Rect boundingRectangle(){
		final Rect r = cachedBoundingRect;
		if(r != null) return r;
		Rect r2 = newBoundingRect();
		if(!isUpdatingPositions) cachedBoundingRect = r2;
		return r2;
	}
	
	/** Causes next call of boundingRectangle() to recalculate */
	public void invalidateCachedBoundingRect(){
		cachedBoundingRect = null;
	}
	
	/*public Shape shape(){
		final Shape s = cachedShape;
		if(s != null) return s;
		Shape s2 = newShape();
		if(!isUpdatingPositions) cachedShape = s2;
		return s2;
	}*/
	
	protected Rect newBoundingRect(){
		final MovCorner outerCorners[] = corners[layers-1];
		float minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		float minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		for(int p=0; p<layerSize; p++){
			int y = (int) outerCorners[p].y;
			int x = (int) outerCorners[p].x;
			minY = Math.min(minY, y);
			minX = Math.min(minX, x);
			maxY = Math.max(maxY, y);
			maxX = Math.max(maxX, x);
		}
		return new Rect((int)minY, (int)minX,
			(int)Math.ceil(maxY-minY), (int)Math.ceil(maxX-minX));
	}

	
	/*protected Shape newShape(){
		final MovCorner outerCorners[] = corners[layers-1];
		int y[] = new int[layerSize];
		int x[] = new int[layerSize];
		for(int p=0; p<layerSize; p++){
			y[p] = (int) outerCorners[p].y;
			x[p] = (int) outerCorners[p].x;
		}
		return new Polygon(x, y, layerSize);
	}*/
	
	/** In order of ascending layer then pointInLayer */
	public MovCorner[] corners(){
		MovCorner cd[] = new MovCorner[layers*layerSize];
		for(int layer=0; layer<layers; layer++){
			System.arraycopy(corners[layer], 0, cd, layer*layerSize, layerSize);
		}
		return cd;
	}
	
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
	public MovTri findCollision(float y, float x){
		final MovTri outerTris[] = trianglesInward[layers-1];
		float closestDistance = Float.MAX_VALUE;
		int closestIndex = -1;
		for(int i=0; i<layerSize; i++){
			MovTri t = outerTris[i];
			float distance = SmartblobUtil.distanceToOuterLineSegment(y, x, t);
			
			/*
			//TODO remove this testing code
			float distScale = 50;
			float bright = (distScale-distance)/distScale;
			bright = Math.max(0, Math.min(bright, 1));
			t.colorOrNull = new Color(bright, bright, bright);
			*/
			
			if(distance < closestDistance){
				closestDistance = distance;
				closestIndex = i;
			}
		}
		MovTri closestOuterTri = outerTris[closestIndex];
		if(SmartblobUtil.isInsideBorder(y, x, closestOuterTri)){
			return outerTris[closestIndex];
		}
		return null; //no collision
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("[Smartblob LZ");
		final MovCorner outerCorners[] = corners[layers-1];
		for(int p=0; p<layerSize; p++){
			sb.append(" x").append((int)outerCorners[p].x).append('y').append((int) outerCorners[p].y);
		}
		return sb.append(']').toString();
	}
	
	public boolean isUpdatingPositions(){ return isUpdatingPositions; }
	
	public boolean isUpdatingSpeeds(){ return isUpdatingSpeeds; }
	
	public void onStartUpdatePositions(){
		if(isUpdatingPositions) throw new RuntimeException("Already updating positions");
		if(isUpdatingSpeeds) throw new RuntimeException("Must finish updating speeds before start updating positions");
		cachedBoundingRect = null;
		//cachedShape = null;
		isUpdatingPositions = true;
	}
	
	public void onEndUpdatePositions(){
		if(!isUpdatingPositions) throw new RuntimeException("Tried to end updating positions before started");
		isUpdatingPositions = false;
	}
	
	public void onStartUpdateSpeeds(){
		if(isUpdatingSpeeds) throw new RuntimeException("Already updating speeds");
		if(isUpdatingPositions) throw new RuntimeException("Must finish updating positions before start updating speeds");
		isUpdatingSpeeds = true;
	}
	
	public void onEndUpdateSpeeds(){
		if(!isUpdatingSpeeds) throw new RuntimeException("Tried to end updating speeds before started");
		isUpdatingSpeeds = false;
	}

}
