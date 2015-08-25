/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui;
import static humanainet.common.CommonFuncs.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import humanainet.common.ScreenUtil;
import humanainet.mindmap.ui.ColorUtil;
import humanainet.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import humanainet.realtimeschedulerTodoThreadpool.Task;

/** This is a layer of protection between the user and classes named in files
that the user may or may not want to run. They dont run until a button is pushed,
and there are other controls. The user can see the java class name before pushing the button.
<br><br>
Has a row of buttons to (re)start, (un)pause, and stop any Task and put it on screen
as a Dynarect andOr JComponent. The timing controls only work through RealtimeScheduler's Task.
*/
public class TaskPlayerUi extends JPanel{
	
	/** Task, Dynaract andOr JComponent *
	public final Object taskAndOrUi;
	*/
	public final Class taskAndOrUiClass;
	
	protected JPanel top;
	
	protected boolean paused = false;
	
	protected Object taskAndOrUi;
	
	/** May be message to the user or equal taskAndOrUi */
	public JComponent jcomponent;
	
	protected final JPanel centerWrapper;
	
	//public TaskPlayerUi(Object taskAndOrUi){
	public TaskPlayerUi(Class taskAndOrUiClass){
		//Dynarect instanceof Task
		if(!Task.class.isAssignableFrom(taskAndOrUiClass) && !JComponent.class.isAssignableFrom(taskAndOrUiClass)){
			throw new IllegalArgumentException("Not a type of "+Task.class.getName()
				/*+" or "+Dynarect.class.getName()*/+" or "+JComponent.class.getName()+": "+taskAndOrUiClass);
		}
		this.taskAndOrUiClass = taskAndOrUiClass;
		top = new JPanel(new BorderLayout());
		ColorUtil.setColors(top);
		
		JLabel label = new JLabel(taskAndOrUiClass.getName());
		ColorUtil.setColors(label);
		top.add(label, BorderLayout.NORTH);
		
		JButton btnStart = new JButton(new AbstractAction("start"){
			public void actionPerformed(ActionEvent e){
				TaskPlayerUi.this.startOrRestart();
			}
		});
		ColorUtil.setColors(btnStart);
		top.add(btnStart, BorderLayout.WEST);
		
		JButton btnPause = new JButton(new AbstractAction("pause/go"){
			public void actionPerformed(ActionEvent e){
				TaskPlayerUi.this.pauseOrUnpause();
			}
		});
		ColorUtil.setColors(btnPause);
		top.add(btnPause, BorderLayout.CENTER);
		
		JButton btnStop = new JButton(new AbstractAction("stop"){
			public void actionPerformed(ActionEvent e){
				TaskPlayerUi.this.stop();
			}
		});
		ColorUtil.setColors(btnStop);
		top.add(btnStop, BorderLayout.EAST);
		
		setLayout(new BorderLayout());
		add(top, BorderLayout.NORTH);
		add(centerWrapper=new JPanel(new GridLayout(1, 1)));
		//add(jcomponent=new JLabel("center"), BorderLayout.CENTER);
		centerWrapper.add(jcomponent=new JLabel("center"));
		ColorUtil.setColors(centerWrapper);
		centerWrapper.validate();
	}
	
	public void startOrRestart(){
		log("taskplayer startOrRestart "+taskAndOrUiClass);
		paused = false;
		if(taskAndOrUi != null) stop();
		try{
			taskAndOrUi = taskAndOrUiClass.newInstance();
			if(taskAndOrUi instanceof JComponent){
				removeJComponentIfExist();
				centerWrapper.add(jcomponent=(JComponent)taskAndOrUi, 0);
			}else{
				removeJComponentIfExist();
				centerWrapper.add(jcomponent=new JLabel("<html>Started "+taskAndOrUi+"<br>as "+Task.class.getName()+"</html>"), 0);
			}
			centerWrapper.validate();
			if(taskAndOrUi instanceof Task){
				RealtimeScheduler.start((Task)taskAndOrUi);
			}
		}catch(Exception e){
			e.printStackTrace(System.err);
			removeJComponentIfExist();
			add(new JLabel("<html>Couldnt start "+taskAndOrUi.getClass().getName()+" because<br>"+e.getMessage()+"</html>"), 0);
		}
	}
	
	public void pauseOrUnpause(){
		log("taskplayer pauseOrUnpause");
		//stop(); //TODO just use RealtimeScheduler. Dont delete taskAndOrUi.
		if(taskAndOrUi == null){
			startOrRestart();
		}else{
			if(paused){
				RealtimeScheduler.start((Task)taskAndOrUi);
			}else{
				RealtimeScheduler.stop((Task)taskAndOrUi, false);
				//TODO when waitForStop is working RealtimeScheduler.stop((Task)taskAndOrUi, true);
			}
			paused = !paused;
		}
	}
	
	protected void removeJComponentIfExist(){
		if(jcomponent != null){
			centerWrapper.remove(jcomponent);
			jcomponent = null;
		}
		centerWrapper.validate();
		repaint();
	}
	
	public void stop(){
		log("taskplayer stop");
		paused = false;
		if(taskAndOrUi != null){
			//remove(get)
			removeJComponentIfExist();
			centerWrapper.add(jcomponent=new JLabel("Stopping "+taskAndOrUi), 0);
			centerWrapper.validate();
			repaint();
			if(taskAndOrUi instanceof Task){
				RealtimeScheduler.stop((Task)taskAndOrUi, false);
				//TODO when waitForStop is working RealtimeScheduler.stop((Task)taskAndOrUi, true);
			}
			removeJComponentIfExist();
			centerWrapper.add(jcomponent=new JLabel("Stopped"), 0);
			centerWrapper.validate();
		}
	}

}