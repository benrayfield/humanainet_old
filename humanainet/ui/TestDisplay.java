package humanainet.ui;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JPanel;

import humanainet.common.MathUtil;
import humanainet.realtimeschedulerTodoThreadpool.Task;

public class TestDisplay extends JPanel implements Task{
	
	public void paint(Graphics g){
		Random r = MathUtil.strongRand;
		g.setColor(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public void event(Object context){
		repaint();
	}

	public double preferredInterval(){
		return .01;
	}
	
	

}
