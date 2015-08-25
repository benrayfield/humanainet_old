/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.pc.ui;
import humanainet.common.CoreUtil;
import humanainet.common.Nanotimer;
import humanainet.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import humanainet.realtimeschedulerTodoThreadpool.Task;
import humanainet.smartblob.core.Smartblob;
import humanainet.smartblob.core.physics.ChangeSpeed;
import humanainet.smartblob.core.physics.GlobalChangeSpeed;
import humanainet.smartblob.core.physics.SmartblobSim;
import humanainet.smartblob.core.physics.globalchangespeeds.BounceOnSimpleWall;
import humanainet.smartblob.core.physics.changespeeds.Push;
import humanainet.smartblob.core.trianglemesh.LayeredZigzag;
import humanainet.smartblob.core.trianglemesh.MovCorner;
import humanainet.smartblob.core.trianglemesh.LineName;
import humanainet.smartblob.core.trianglemesh.MovLine;
import humanainet.smartblob.core.trianglemesh.MovTri;
import humanainet.smartblob.core.util.SmartblobUtil;
import humanainet.ui.core.ColorUtil;
import humanainet.ui.core.shapes.Rect;

//import humanainet.statsys.Statsys;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/** Many smartblobs can bounce and reshape and grab eachother as tools on screen */
public class SmartblobsPanel extends JPanel implements MouseMotionListener, MouseListener, Task{
	
	/** Changes to sim.smartblobs must be synchronized. It is when painted. */
	public final SmartblobSim sim;
	
	public boolean paintPartsRandomly = true;
	
	protected final Nanotimer timer = new Nanotimer();
	
	protected double lastTimeMouseMoved = CoreUtil.time();
	
	public double simulateThisManySecondsAfterMouseMove = 120;
	//public double simulateThisManySecondsAfterMouseMove = 20;
	//public double simulateThisManySecondsAfterMouseMove = 2e50;
	
	//public int simCyclesPerDraw = 200;
	//public int simCyclesPerDraw = 100;
	public int simCyclesPerDraw = 50;
	//public int simCyclesPerDraw = 15;
	//public int simCyclesPerDraw = 500;
	//public int simCyclesPerDraw = 30;
	//public int simCyclesPerDraw = 100;
	//public int simCyclesPerDraw = 5;
	
	public boolean drawBoundingRectangles = false;
	
	public boolean drawBoundingShapes = false;
	
	public boolean drawOuterTriMouseIsClosestTo = true;
	
	public int mouseY, mouseX;
	
	public final boolean mouseButtonDown[] = new boolean[3];
	
	public float maxSecondsToSimAtOnce = .03f;
	
	public long frames;
	
	protected boolean drawLines = false;
	
	//protected float testPointA[] = new float[2], testPointB[] = new float[2];
	
	/** Starts self as task. Includes an example smartblob. They can be changed later */
	public SmartblobsPanel(){
		this(SmartblobUtil.newSimWithDefaultOptions());
		LayeredZigzag y = SmartblobUtil.simpleSmartblobExample();
		synchronized(sim.smartblobs){
			sim.smartblobs.add(y);
		}
		LayeredZigzag z = SmartblobUtil.simpleSmartblobExample();
		for(MovCorner cd : z.corners()){
			cd.x += 300;
			cd.speedX = 200;
		}
		synchronized(sim.smartblobs){
			sim.smartblobs.add(z);
		}
		LayeredZigzag w = SmartblobUtil.wavegear(
			null, 250, 500, 75, 90, 
			3, 32, 5);
		int colorInward = ColorUtil.color(0f, 1f, 0f);
		int colorOutward = ColorUtil.color(.5f, .5f, .5f);
		for(int layer=1; layer<w.layers; layer++){
			for(int p=0; p<w.layerSize; p++){
				w.trianglesInward[layer][p].colorARGB = colorInward;
			}
		}
		for(int layer=0; layer<w.layers-1; layer++){
			for(int p=0; p<w.layerSize; p++){
				w.trianglesOutward[layer][p].colorARGB = colorOutward;
			}
		}
		w.updateStartDistances();
		w.setTargetDistancesToStartDistances();
		synchronized(sim.smartblobs){
			sim.smartblobs.add(w);
		}
	}
	
	/** Starts self as Task. */
	public SmartblobsPanel(SmartblobSim sim){
		this.sim = sim;
		setBackground(Color.black);
		addMouseMotionListener(this);
		addMouseListener(this);
		RealtimeScheduler.start(this);
	}
	
	//protected LayeredZigzag testBlob = new LayeredZigzag(null, 5, 16, 100, 100, 90);
	//protected LayeredZigzag testBlob = new LayeredZigzag(null, 9, 64, 100, 100, 90);
	//protected LayeredZigzag testBlob = new LayeredZigzag(null, 7, 32, 100, 100, 90);
	
	public void paint(Graphics g){
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		/*if(testPointA[0] == 0){
			testPointA[0] = getHeight()*CoreUtil.strongRand.nextFloat();
			testPointA[1] = getWidth()*CoreUtil.strongRand.nextFloat();
			testPointB[0] = getHeight()*CoreUtil.strongRand.nextFloat();
			testPointB[1] = getWidth()*CoreUtil.strongRand.nextFloat();
		}*/

		//int w = getWidth(), h = getHeight();
		//testBlob = new LayeredZigzag(null, 5, 16, h/2, w/2, Math.min(w,h)/2);
		//testBlob = new LayeredZigzag(null, 9, 16, h/2, w/2, Math.min(w,h)/2);
		//testBlob = new LayeredZigzag(null, 7, 32, h/2, w/2, Math.min(w,h)/2);
		
		Smartblob blobsArray[];
		synchronized(sim.smartblobs){
			blobsArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobsArray){
			draw(g, blob);
		}
		
		/*
		//test nearest point on line math
		float getYX[] = new float[2];
		SmartblobUtil.getClosestPointToInfiniteLine(
			getYX, testPointA[0], testPointA[1], testPointB[0], testPointB[1], mouseY, mouseX);
		g.setColor(Color.pink);
		g.drawLine((int)testPointA[1], (int)testPointA[0], (int)testPointB[1], (int)testPointB[0]);
		g.fillRect((int)getYX[1]-5, (int)getYX[0]-5, 10, 10);
		*/
		
		g.setColor(Color.white);
		g.drawString("frames: "+frames, 20, 20);
		frames++;
	}
	
	public static void drawLineWithCurrentSettings(Graphics g, MovCorner a, MovCorner b){
		g.drawLine((int)a.x, (int)a.y, (int)b.x, (int)b.y);
	}
	
	public void draw(Graphics g, Smartblob smartblob){
		boolean drawShape = drawBoundingShapes || !(smartblob instanceof LayeredZigzag);
		Shape s = null;
		Polygon p = null;
		if(drawShape){
			throw new RuntimeException("TODO");
			/*s = smartblob.shape();
			if(s instanceof Polygon){ //TODO what to draw here
				p = (Polygon) s;
			}else{
				throw new RuntimeException("TODO use pathiterator of Shape for "+s);
			}
			*/
		}
		if(smartblob instanceof LayeredZigzag){
			draw(g, (LayeredZigzag)smartblob);
		}else{
			g.drawPolygon(p);
		}
		if(drawBoundingShapes){
			g.setColor(new Color(.8f,0,.8f));
			g.drawPolygon(p);
		}
		if(drawBoundingRectangles){
			g.setColor(Color.red);
			Rect r = smartblob.boundingRectangle();
			//If rectangle hangs off positive y (bottom) of the panel,
			//panel enlarges and it continues appearing to fall.
			int h = getHeight(), w = getWidth();
			int startY = Math.max(0, (int)r.y);
			int startX = Math.max(0, (int)r.x);
			int endY = Math.min((int)(r.y+r.height-1), h-1); //inclusive
			int endX = Math.min((int)(r.x+r.width-1), w-1);
			g.drawRect(startX, startY, endX-startX+1, endY-startY+1);
			//System.out.println("w="+w+" h="+h+" startY="+startY+" endY="+endY+" r="+r);
			//System.out.println("blob="+smartblob);
		}
	}
	
	public void draw(Graphics g, LayeredZigzag smartblob){
		int triX[] = new int[3], triY[] = new int[3]; //filled in from corners float positions
		//Color defaultColor = new Color(.9f, .9f, .9f);
		for(int layer=1; layer<smartblob.layers; layer++){
			for(int p=0; p<smartblob.layerSize; p++){
				/*Shape triangle = testBlob.triangleShape(layer, p, true);
				if(triangle instanceof Polygon){
					g.fillPolygon((Polygon)triangle);
				}
				*/
				MovTri t = smartblob.trianglesInward[layer][p];
				for(int c=0; c<3; c++){
					MovCorner cd = t.adjacentCorners[c];
					triY[c] = (int) cd.y;
					triX[c] = (int) cd.x;
				}
				//g.setColor(t.colorOrNull==null ? defaultColor : t.colorOrNull);
				//java's color bits match ARGB where A is highest 8 bits and B is lowest.
				g.setColor(new Color(t.colorARGB));
				g.fillPolygon(triX, triY, 3);
			}
		}
		//defaultColor = new Color(0,0,1f);
		for(int layer=0; layer<smartblob.layers-1; layer++){
			for(int p=0; p<smartblob.layerSize; p++){
				/*Shape triangle = testBlob.triangleShape(layer, p, false);
				if(triangle instanceof Polygon){
					g.fillPolygon((Polygon)triangle);
				}*/
				MovTri t = smartblob.trianglesOutward[layer][p];
				for(int c=0; c<3; c++){
					MovCorner cd = t.adjacentCorners[c];
					triY[c] = (int) cd.y;
					triX[c] = (int) cd.x;
				}
				//g.setColor(t.colorOrNull==null ? defaultColor : t.colorOrNull);
				g.setColor(new Color(t.colorARGB));
				g.fillPolygon(triX, triY, 3);
			}
		}
		
		if(drawOuterTriMouseIsClosestTo){
			MovTri t = smartblob.findCollision(mouseY, mouseX);
			if(t != null){
				for(int c=0; c<3; c++){
					MovCorner cd = t.adjacentCorners[c];
					triY[c] = (int) cd.y;
					triX[c] = (int) cd.x;
				}
				//g.setColor(Color.red);
				//g.fillPolygon(triX, triY, 3);
				
				float getYX[] = new float[2];
				SmartblobUtil.getClosestPointToInfiniteLine(getYX, t, mouseY, mouseX);
				g.setColor(Color.orange);
				g.fillRect((int)getYX[1]-3, (int)getYX[0]-3, 7, 7);
				/*TODO if(mouseButtonDown[0] || mouseButtonDown[2]){
					CornerData cd = t.adjacentCorners[2];
					t.smartblob.onStartUpdateSpeeds();
					float secondsSinceLastDraw = .02;
					cd.speedY -= 10*secondsSinceLastDraw; //TODO do this in nextState
					t.smartblob.onEndUpdateSpeeds();
				}*/
			}
		}
		

		if(g instanceof Graphics2D){
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(1.5f));
		}
		if(drawLines){
			g.setColor(Color.green);
			for(LineName line : smartblob.allLines()){
				MovCorner a = smartblob.corners[line.cornerLow.layer][line.cornerLow.point];
				MovCorner b = smartblob.corners[line.cornerHigh.layer][line.cornerHigh.point];
				drawLineWithCurrentSettings(g, a, b);
			}
		}
		if(g instanceof Graphics2D){
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(3f));
		}
		if(drawBoundingShapes){
			g.setColor(Color.magenta);
			/*Shape s = smartblob.shape();
			if(s instanceof Polygon){
				g.drawPolygon((Polygon)s);
			}else{
				System.out.println("Unknown shape type: "+s.getClass().getName());
			}*/
			throw new RuntimeException("TODO smartblob tells its triangles. use those on the outer ring");
		}
		/*TODO when hook in CornerData pointers in LineData for(LineData lineData : testBlob.allLineDatas()){
			drawLineInCurrentColor(g, lineData.adjacentCorners[0], lineData.adjacentCorners[1]);
		}*/
	}

	public void mouseMoved(MouseEvent e){
		lastTimeMouseMoved = CoreUtil.time();
		mouseY = e.getY();
		mouseX = e.getX();
	}
	
	public void mouseDragged(MouseEvent e){
		mouseMoved(e);
	}

	public void event(Object context){
		double now = CoreUtil.time();
		double sinceMouseMove = now-lastTimeMouseMoved;
		if(sinceMouseMove <= simulateThisManySecondsAfterMouseMove){
			double secondsSinceLast = timer.secondsSinceLastCall();
			int h = getHeight(), w = getWidth();
			System.out.println("Width and height of smartblobspanel are w="+w+" h="+h);
			if(h == 0 || w == 0) return;
			for(GlobalChangeSpeed p : sim.physicsParts){
				if(p instanceof BounceOnSimpleWall){
					BounceOnSimpleWall b = (BounceOnSimpleWall) p;
					//the left and top sides of screen stay at 0
					if(b.maxInsteadOfMin){
						float newVal = b.verticalInsteadOfHorizontal ? h : w;
						if(b.verticalInsteadOfHorizontal){
							System.out.println("Setting bottom wall to "+newVal);
						}else{
							System.out.println("Setting right wall to "+newVal);
						}
						b.position = newVal;
					}
				}
			}
			long cyc = 0;
			
			
			
			
			
			
			
			
			
			
			
			//TODO!!! FIXME float sec = Math.min(maxSecondsToSimAtOnce,(float)secondsSinceLast);
			float sec = 1f/40; //trying constant update time to see if it improves stability of smartblob bouncing vs sticking together
			
			
			
			
			
			if(drawOuterTriMouseIsClosestTo){
				if(mouseButtonDown[0]){
					Smartblob blobsArray[];
					synchronized(sim.smartblobs){
						blobsArray = sim.smartblobs.toArray(new Smartblob[0]);
					}
					for(Smartblob blob : blobsArray){
						if(blob instanceof LayeredZigzag){
							LayeredZigzag z = (LayeredZigzag) blob;
							MovTri t = z.findCollision(mouseY, mouseX);
							if(t != null){
								//t.colorOrNull = Color.red;
								
								Iterator<ChangeSpeed> iter = z.mutablePhysics().iterator();
								while(iter.hasNext()){
									ChangeSpeed cs = iter.next();
									if(cs instanceof Push) iter.remove();
								}
								MovCorner c = t.adjacentCorners[2];
								ChangeSpeed p = new Push(c, -5000, 0);
								z.mutablePhysics().add(p);
							}
						}
					}
				}
			}
			
			
			
			
			for(int cycle=0; cycle<simCyclesPerDraw; cycle++){
				sim.nextState(sec/simCyclesPerDraw);
				cyc++;
			}
			//System.out.println("cyc this time "+cyc);
			repaint();
		}
	}
	
	public double preferredInterval(){
		return .01;
	}

	public void mouseClicked(MouseEvent e){}

	public void mousePressed(MouseEvent e){
		switch(e.getButton()){
		case MouseEvent.BUTTON1:
			mouseButtonDown[0] = true;
		break; case MouseEvent.BUTTON2:
			mouseButtonDown[1] = true;
		break; case MouseEvent.BUTTON3:
			mouseButtonDown[2] = true;
		break;
		}
	}

	public void mouseReleased(MouseEvent e){
		switch(e.getButton()){
		case MouseEvent.BUTTON1:
			mouseButtonDown[0] = false;
		break; case MouseEvent.BUTTON2:
			mouseButtonDown[1] = false;
		break; case MouseEvent.BUTTON3:
			mouseButtonDown[2] = false;
		break;
		}
	}

	public void mouseEntered(MouseEvent e){}

	public void mouseExited(MouseEvent e){}

}