package humanainet.smartblob.plugins.climb;
import java.util.HashMap;

import humanainet.common.MathUtil;
import humanainet.smartblob.core.physics.GlobalChangeSpeed;
import humanainet.smartblob.core.physics.SmartblobSim;
import humanainet.smartblob.core.physics.globalchangespeeds.BounceOnSimpleWall;
import humanainet.smartblob.core.physics.globalchangespeeds.CollisionsChangeSpeed;
import humanainet.smartblob.core.physics.globalchangespeeds.Gravity;
import humanainet.smartblob.pc.ui.SmartblobsPanel;
import humanainet.ui.core.shapes.Rect;

public class SmartblobPanelWithClimbSim extends SmartblobsPanel{
	
	/** For typing javaclass:// url in mindmap which needs a parameterless constructor */
	public SmartblobPanelWithClimbSim(){
		super(new ClimbSim(
			new Gravity(300),
			new CollisionsChangeSpeed(),
			//float position, boolean verticalInsteadOfHorizontal, boolean maxInsteadOfMin
			new BounceOnSimpleWall(0, true, false),
			new BounceOnSimpleWall(500, true, true),
			new BounceOnSimpleWall(0, false, false),
			new BounceOnSimpleWall(600, false, true)
		));
		for(GlobalChangeSpeed c : sim.physicsParts){
			System.out.println(c);
		}
		//drawLines = true;
		sim.updateDynamicBoundsAtMostInsideThis(new Rect(0,0,0x10000,0x10000));
		System.out.println("sim.getCachedDynamicBounds="+sim.getCachedDynamicBounds());
		for(int i=0; i<17; i++){
			sim.addRandomObject(new HashMap(), MathUtil.strongRand);
		}
	}

}
