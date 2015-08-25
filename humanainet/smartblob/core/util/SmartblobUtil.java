/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.util;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.brain.Brain;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.physics.SmartblobSim;
import humanainet.smartblob.core.physics.changespeeds.AccelerateByPowerOfDistanceConstraints;
import humanainet.smartblob.core.physics.changespeeds.AccelerateConstantFromDistanceConstraints;
import humanainet.smartblob.core.physics.changespeeds.AccelerateLinearlyFromDistanceConstraints;
import humanainet.smartblob.core.physics.changespeeds.Friction;
import humanainet.smartblob.core.physics.changespeeds.HoldCenterTogether;
import humanainet.smartblob.core.physics.changespeeds.HoldSpeedConstant;
import humanainet.smartblob.core.physics.changespeeds.RelativeFriction;
import humanainet.smartblob.core.physics.globalchangespeeds.BounceOnSimpleWall;
import humanainet.smartblob.core.physics.globalchangespeeds.CollisionsChangeSpeed;
import humanainet.smartblob.core.physics.globalchangespeeds.Gravity;
import humanainet.smartblob.core.physics.musclefactories.LineMuscleFactory;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.smartblob.core.trianglemesh.MovLine;
import humanainet.smartblob.core.trianglemesh.MovTri;
import humanainet.statsysinterface.Statsys;
import humanainet.ui.core.ColorUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmartblobUtil{
	private SmartblobUtil(){}
	
	public static List<ChangeSpeed> defaultChangeSpeeds = Collections.unmodifiableList(Arrays.<ChangeSpeed>asList(
		new AccelerateLinearlyFromDistanceConstraints(100f),
		//new AccelerateLinearlyFromDistanceConstraints(200f),
		//new AccelerateConstantFromDistanceConstraints(140),
		//new AccelerateLinearlyFromDistanceConstraints(523f),
		//new AccelerateLinearlyFromDistanceConstraints(30f),
		//new AccelerateLinearlyFromDistanceConstraints(10f),
		//new AccelerateLinearlyFromDistanceConstraints(2f),
			
		//new Friction(2f),
		//new Friction(5f),
		new Friction(15f),
		//new Friction(.1f),
		//new Friction(-3f),
		//new Friction(5f),
		//new Friction(50f),
			
		//new RelativeFriction(.3f),
		new RelativeFriction(.5f),
		//new RelativeFriction(20f),
		//new RelativeFriction(1000f),
			
		new HoldCenterTogether()
			
		//new HoldSpeedConstant(40))
	));
	
	public static LayeredZigzag simpleSmartblobExample(){
		
		//Smartblob blob = new LayeredZigzag(null, 6, 16, 260, 100, 90);
		//LayeredZigzag blob = new LayeredZigzag(null, 5, 16, 100, 100, 90);
		//LayeredZigzag blob = new LayeredZigzag(null, new LineMuscleFactory(), 5, 16, 100, 100, 90);
		//LayeredZigzag blob = new LayeredZigzag(null, new LineMuscleFactory(), 3, 16, 100, 100, 40);
		//LayeredZigzag blob = new LayeredZigzag(null, new LineMuscleFactory(), 2, 16, 100, 100, 40);
		LayeredZigzag blob = new LayeredZigzag(null, new LineMuscleFactory(), 2, 12, 100, 100, 40);
		//LayeredZigzag blob = new LayeredZigzag(null, 7, 32, 100, 100, 90);
		//LayeredZigzag blob = new LayeredZigzag(null, 7, 32, 100, 100, 90);
		//LayeredZigzag blob = new LayeredZigzag(null, 8, 16, 260, 100, 90);
		
		blob.mutablePhysics().addAll(defaultChangeSpeeds);
		
		int colorInward = ColorUtil.color(.8f, .8f, .8f);
		int colorOutward = ColorUtil.color(.1f, .1f, .9f);
		//Color c = new Color(.5f, .5f, .5f);
		for(int layer=1; layer<blob.layers; layer++){
			for(int p=0; p<blob.layerSize; p++){
				blob.trianglesInward[layer][p].colorARGB = colorInward;
			}
		}
		for(int layer=0; layer<blob.layers-1; layer++){
			for(int p=0; p<blob.layerSize; p++){
				blob.trianglesOutward[layer][p].colorARGB = colorOutward;
			}
		}
		
		//inner circle of smartblob has no outward facing triangles so alternate color of inward.
		int colorCenterEven = colorInward;
		int colorCenterOdd = colorOutward;
		for(int p=0; p<blob.layerSize; p++){
			blob.trianglesInward[1][p].colorARGB = ((p&1)==1) ? colorCenterOdd : colorCenterEven;
		}
		
		return blob;
	}
	
	public static LayeredZigzag wavegear(
			Brain brain, double y, double x, double outerMinRadius, double outerMaxRadius, 
			int layers, int layerSize, double frequency){
		LayeredZigzag blob = new LayeredZigzag(brain, new LineMuscleFactory(), layers, layerSize, 0, 0, 0);
		for(int layer=0; layer<layers; layer++){
			double fraction = (double)layer/(layers-1);
			boolean layerIsOdd = (layer&1)==1;
			for(int p=0; p<layerSize; p++){
				//TODO optimize by reversing order of these loops?
				MovCorner c = blob.corners[layer][p];
				double minRadius = fraction*outerMinRadius;
				double maxRadius = fraction*outerMaxRadius;
				double radiusRange = maxRadius-minRadius;
				double angle = (double)p/layerSize*2*Math.PI;
				if(layerIsOdd) angle += Math.PI/layerSize;
				double recurseAngle = angle*frequency;
				double radius = minRadius + radiusRange*Math.sin(recurseAngle);
				c.y = (float)(y + radius*Math.sin(angle));
				c.x = (float)(x + radius*Math.cos(angle));
			}
		}
		blob.mutablePhysics().addAll(defaultChangeSpeeds);
		return blob;
	}
	
	/** Add smartblobs to it later */
	public static SmartblobSim newSimWithDefaultOptions(){
		return new SmartblobSim(
			//new Gravity(1.5f),
			//new Gravity(3000),
			new Gravity(300),
			//new Gravity(700),
			//new Gravity(1000),
			//new Gravity(30),
			new CollisionsChangeSpeed(),
			new BounceOnSimpleWall(0, true, false),
			new BounceOnSimpleWall(0, false, false),
			new BounceOnSimpleWall(500, true, true),
			new BounceOnSimpleWall(500, false, true)
		);
	}
	
	public static void moveAll(SmartblobSim sim, float secondsSinceLastMove){
		Smartblob blobArray[];
		synchronized(sim.smartblobs){
			blobArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			blob.onStartUpdatePositions();
			if(blob instanceof LayeredZigzag){
				move((LayeredZigzag)blob, secondsSinceLastMove);
			}else{
				System.out.println(Smartblob.class.getName()+" type unknown: "+blob.getClass().getName());
			}
			blob.onEndUpdatePositions();
		}
	}
	
	public static void move(LayeredZigzag z, float secondsSinceLastMove){
		for(MovCorner layerOfCorners[] : z.corners){
			for(MovCorner cd : layerOfCorners){
				cd.y += cd.speedY*secondsSinceLastMove + cd.addToY;
				cd.x += cd.speedX*secondsSinceLastMove + cd.addToX;
				cd.addToY = 0;
				cd.addToX = 0;
			}
		}
	}
	
	//public static float epsilon = .000001f;
	
	/** Distance from the given point to closest point on the line segment.
	The LineData must be the first LineData in an inward facing TriData,
	which is true of each border line segment of a LayeredZigzag.
	*/
	public static float distanceToOuterLineSegment(float y, float x, MovTri t){
		MovCorner cornerA = t.adjacentCorners[0], cornerB = t.adjacentCorners[1];
		float ay = cornerA.y;
		float ax = cornerA.x;
		float by = cornerB.y;
		float bx = cornerB.x;
		
		//What if its close to a part of the infinite line that doesnt exist in the segment?
		//Thats why cant use closest point to infinite line code.
		float getYX[] = new float[2]; //point L somewhere on the infinite line
		getClosestPointToInfiniteLine(getYX, ay, ax, by, bx, y, x);
		float ly = getYX[0], lx = getYX[1];
		/*if(g != null){ //TODO remove this testing code
			g.setColor(Color.red);
			g.fillRect((int)lx-5, (int)ly-5, 10, 10);
		}*/
		float epsilon = .000001f;
		float minX = Math.min(ax,bx)-epsilon;
		float maxX = Math.max(ax,bx)+epsilon;
		boolean lInXOfLine = minX < lx && lx < maxX;
		float minY = Math.min(ay,by)-epsilon;
		float maxY = Math.max(ay,by)+epsilon;
		boolean lInYOfLine = minY < ly && ly < maxY;
		if(lInXOfLine){ //somewhere in line segment instead of using its ends
			float plDy = y-ly, plDx = x-lx;
			return (float)Math.sqrt(plDy*plDy + plDx*plDx);
			//return 100;
		}else{ //closest end of line segment
			float aDy = y-ay, aDx = x-ax;
			float distToASquared = aDy*aDy + aDx*aDx;
			float bDy = y-by, bDx = x-bx;
			float distToBSquared = bDy*bDy + bDx*bDx;
			return (float)Math.sqrt(Math.min(distToASquared, distToBSquared));
			//return 0;
		}
		
		/*
		float getYX[] = new float[2]; //point L somewhere on the infinite line
		float ly = getYX[0], lx = getYX[1];
		getClosestPointToInfiniteLine(getYX, ay, ax, by, bx, y, x);
		float abDy = by-ay, abDx = bx-ax;
		float distanceABSquared = abDy*abDy + abDx*abDx; //between ends of line segment
		float alDy = ly-ay, alDx = lx-ax;
		float distanceALSquared = alDy*alDy + alDx*alDx; //between a and point
		float blDy = ly-by, blDx = lx-bx;
		float distanceBLSquared = blDy*blDy + blDx*blDx; //between b and point
		if(distanceALSquared < distanceBLSquared){ //L is closer to A than to B
			if(distanceABSquared < distanceBLSquared){
				//return distance P (outside line) to B
				//float bDy = y-by, bDx = x-bx;
				//return (float)Math.sqrt(bDy*bDy + bDx*bDx);
				return .3f;
			}
		}else{ //L is closer to B than to A
			if(distanceABSquared < distanceALSquared){
				//return distance P (outside line) to A
				//float aDy = y-ay, aDx = x-ax;
				//return (float)Math.sqrt(aDy*aDy + aDx*aDx);''
				return .7f;
			}
		}
		return 1f;
		*/
		
		
		/*float aDy = y-ay, aDx = x-ax;
		float distToA = (float)Math.sqrt(aDy*aDy + aDx*aDx);
		float bDy = y-by, bDx = x-bx;
		float distToB = (float)Math.sqrt(bDy*bDy + bDx*bDx);
		float minOfDistToAOrb = Math.min(distToA, distToB);
		*/
		
		/*
		//https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
		float dy = by-ay, dx = bx-ax;
		double twoPointsOnLineDist = Math.sqrt(dy*dy + dx*dx);
		float lineDist = (float)((dy*x - dx*y + bx*ay - by*ax)/twoPointsOnLineDist);
		return lineDist;
		//return Math.min(minOfDistToAOrb, lineDist);
		*/
		
		
		//older code, only uses either outer corner of triangle:
		//float dy = y-by, dx = x-bx;
		//float dy = y-ay, dx = x-ax;
		//return (float)Math.sqrt(dy*dy + dx*dx);
	}
	
	/** Returns true if the point is on the side of the line that the nearest
	part of the smartblob is inside. It does not mean in the triangle literally.
	*/
	public static boolean isInsideBorder(float y, float x, MovTri closestOuterTri){
		MovCorner cornerA = closestOuterTri.adjacentCorners[0], cornerB = closestOuterTri.adjacentCorners[1];
		float y1 = cornerA.y;
		float x1 = cornerA.x;
		float y2 = cornerB.y;
		float x2 = cornerB.x;
		MovCorner p2 = closestOuterTri.adjacentCorners[2];
		float ay = y, ax = x;
		float by = p2.y, bx = p2.x;
		//Is (y,x) on the same side as closestOuterTri.adjacentCorners[2].(y and x)?
		//http://math.stackexchange.com/questions/162728/how-to-determine-if-2-points-are-on-opposite-sides-of-a-line
		//How can I determine whether the 2 points (ax,ay) and (bx,by)
		//are on opposite sides of the line (x1,y1)→(x2,y2)? 
		//Explicitly, they are on opposite sides iff 
		//((y1−y2)(ax−x1)+(x2−x1)(ay−y1))((y1−y2)(bx−x1)+(x2−x1)(by−y1))<0.
		float first = (y1-y2)*(ax-x1) + (x2-x1)*(ay-y1);
		float second = (y1-y2)*(bx-x1) + (x2-x1)*(by-y1);
		return 0 < first*second;
	}
	
	public static void getClosestPointToInfiniteLine(float getYX[], MovTri borderTri, float y, float x){
		MovCorner c0 = borderTri.adjacentCorners[0], c1 = borderTri.adjacentCorners[1];
		getClosestPointToInfiniteLine(getYX, c0.y, c0.x, c1.y, c1.x, y, x);
	}
	
	public static void getClosestPointToInfiniteLine(float getYX[], float y1, float x1, float y2, float x2, float y, float x){
		//http://www.java2s.com/Code/Java/2D-Graphics-GUI/Returnsclosestpointonsegmenttopoint.htm
		float xDelta = x2 - x1;
		float yDelta = y2 - y1;
		float u = ((x - x1) * xDelta + (y - y1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
		getYX[0] = y1 + u * yDelta;
		getYX[1] = x1 + u * xDelta;
	}
	
	/*public static void getClosestPointToLineSegment(float getYX[], float y1, float x1, float y2, float x2, float y, float x){
		float aDy = y-y1, aDx = x-x1;
		float distToA = (float)Math.sqrt(aDy*aDy + aDx*aDx);
		float bDy = y-y2, bDx = x-x2;
		float distToB = (float)Math.sqrt(bDy*bDy + bDx*bDx);
		float minOfDistToAOrb = Math.min(distToA, distToB);
		TODO
	}*/
	
	/** Returns true if any triangle is pointing opposite of the way it should be.
	TODO I'm not sure how to calculate this because smartblob can form into any 2d shape,
	so if its folded does not depend on the angle of the triangle relative to center.
	It may depend on angle relative to near inner triangles.
	*/
	public static boolean isFolded(LayeredZigzag z){
		throw new RuntimeException("TODO");
	}
	
	//"TODO this unexpectedly destabilizes BounceOnSimpleWall. Why? TODO for each kind of bounce (wall and on other smartblob) mirror the part of position thats past instead of just speeds. This will need extra vars in CornerData since cant change position directly." 
	
	//TODO? bounce on corners instead of infinite line, to handle negative curve which appears to be causing noncircle shaped objects to stick together
	

}
