/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui;
import static humanainet.common.CommonFuncs.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import humanainet.acyc.Glo;
import humanainet.blackholecortex.ui.BlackHoleCortexWindowForAllAtOnceLearning;
import humanainet.common.CoreUtil;
import humanainet.common.ScreenUtil;
import humanainet.console.HumanainetConsole;
import humanainet.mindmap.ui.ColorUtil;
import humanainet.mindmap.ui.PrilistsPanel;
import humanainet.mindmap.ui.SchedulePanel;
import humanainet.smartblob.pc.ui.SmartblobsPanel;
import humanainet.smartblob.plugins.climb.SmartblobPanelWithClimbSim;
import humanainet.xorlispui.EconDAcycIUi;
import humanainet.xorlispui.TagSystemOnScreen;

/** A mostly empty window that has almost no menu items and no tabs of content,
both of which can be added as plugins. The purpose is to generalize the ui for
many thing I (Ben F Rayfield) have built so any subset of them can be used together,
and when that is working I expect other people would want to add their own plugins.
Examples of things that will (TODO) become plugins are:
humanainetMindmap and the boltzmann machine demo in physicsmataV2.0.0. 
*/
public class HumanainetWindow extends JFrame{
	
	public final JTabbedPane tabs;
	
	public final TagSystemOnScreen talkUsingTagSystem;
	
	public final PrilistsPanel mindmap;
	
	public final SchedulePanel schedulePanel;
	
	public final JSplitPane verticalSplit;
	
	public final EconDAcycIUi econacycUi;
	
	public final ConsolePanel consolePanel;
	
	/*
	"TODO all items in window (each tab and menu item) name a hierarchy path using a Polycat, starting with the name of each window. Name of a window allows for expansion of this software to user accounts, each window having a different name."
	"Example: thisWindowName, tab, theMindmapObject (TODO should all of these have to be Var objects? Var value would be theMindmapObject, and what happens if it changes to another JComponent? What happens if it changes to null? Is it removed? Or should that be done through the system these polycats (lists of Bits) are? Also, if its a polycat, how can an object be last thing in it? Thats what jselfmodify does, but I want it to be polycat. Maybe I need to include Var objects in Namespace, for mutable things."
	"Example: thisWindowName, menu, File, Save (Or should it be represented as something like a Var but with no value, just the action? Maybe set it to Boolean.TRUE and when its done saving it sets itself back to Boolean.FALSE?)"
	"Example: thisWindowName, menu, Options, allowFileToRunCodeCheckbox (Or should it be represented as a Var<Boolean>?)"
	"Example: thisWindowName, menu, Options, autosaveOnExitCheckbox (Or should it be represented as a Var<Boolean>?)"
	*/
	
	public HumanainetWindow(){
		super("Human AI Net (0.8.0) - the Human and Artificial Intelligence Network");
		
		/*try{
		UIManager.setLookAndFeel(UIManager.get);
		}catch(Exception e){
			e.printStackTrace(System.err);
		}*/
		
		setSize(new Dimension(900,800));
		ScreenUtil.moveToScreenCenter(this);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setJMenuBar(newMenubar());
		
		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		//ColorUtil.setColors(verticalSplit);
		
		tabs = new JTabbedPane();
		
		talkUsingTagSystem = new TagSystemOnScreen();
		tabs.add(talkUsingTagSystem, "Words Compute");
		
		//tabs.add(new JLabel("TODO put various checkboxes, textfields, and other options here"), "Options");
		
		try{
			mindmap = new PrilistsPanel(4);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		tabs.add(mindmap, "Mindmap");
		
		schedulePanel = new SchedulePanel();
		tabs.add(schedulePanel, "Todo");
		
		verticalSplit.add(tabs);
		
		consolePanel = new ConsolePanel(new HumanainetConsole());
		//ColorUtil.setColors(console);
		consolePanel.setBackground(new Color(0xff446655));
		Color fg = new Color(0xffffffff);
		consolePanel.setForeground(fg);
		consolePanel.setCaretColor(fg);
		
		final int vAlways = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		final int hNever = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		JScrollPane scrollConsolePanel = new JScrollPane(consolePanel, vAlways, hNever);
		verticalSplit.add(scrollConsolePanel);
		
		verticalSplit.setResizeWeight(.75);
		
		//SmartblobsPanel smartblobsPanel = new SmartblobsPanel();
		
		//tabs.add(smartblobsPanel, "Smartblob");
		
		//tabs.add(new JLabel("test"), "XX");
		//tabs.add(new HumanainetDesktop(), "Desktop"); //other plugins could be used instead, but to start with something simple...
		//tabs.add(new NeuralToolsPanel(), "NeuralTools");
		
		/*JButton boltzDemo = new JButton(new AbstractAction("<html>Click here for interactive demo of boltzmann machine on mnist ocr data.<br>Hold middlemouse button to see weights<br> of selected square while it leans. Then use 2 main mousebuttons to draw<br> on the topleft square to interact with what it learned<br> and have it dream back at you.<br> This is only a few of the mnist ocr dataset,<br> to be scaled up and used on cellular automata and music tools.</html>"){
			public void actionPerformed(ActionEvent e) {
				new Thread(){
					public void run(){
						try{
							BlackHoleCortexWindowForAllAtOnceLearning.main(new String[0]);
						}catch (Exception ee){
							throw new RuntimeException(ee);
						}
					}
				}.start();
			}
		});
		tabs.add(boltzDemo, "Boltzmann Machine demo");
		*/
		
		//tabs.setSelectedComponent(smartblobsPanel);
		
		//tabs.add(new JLabel("<html>TODO put smartblob, popbol, and bobaga here as a larger view of what can be used in top center of mindmap.<br>Use smartblob as 16 bit numbers virtualAddress cached in acyc<br>(from 2^16 to 3*2^16 are all bitstrings up to length 16 as listOfPowerOf2Item of bit)</html>."), "Shapes");
		//tabs.add(new TaskPlayerUi(SmartblobsPanel.class), "Shapes");
		tabs.add(new TaskPlayerUi(SmartblobPanelWithClimbSim.class), "Shapes");
		
		tabs.add(new JButton(new AbstractAction("<html>Click here for boltzmann demo window to apper you can draw on.<br>TODO put BlackHoleCortex and Neuraltools here in a way they explore and partially overlap eachother while moving and observing and painting</html>"){
			public void actionPerformed(ActionEvent e){
				try{
					BlackHoleCortexWindowForAllAtOnceLearning.main(new String[0]);
				}catch(Exception ex){
					throw new RuntimeException(ex);
				}
			}
		}), "Neural Pixels");
		
		tabs.add(new JLabel("TODO put physicsmata and other cellular automata (conway, rule110, etc) here"), "Cell Automata");
		
		tabs.add(new JLabel("TODO put Audivolv, HyperSphereNet, CochleaSim, fourierAodEconbit, audivolvInstrumentMeasureMusicianFunc etc here"), "Sounds");
		
		econacycUi = new EconDAcycIUi(Glo.econacyc);
		int hAsNeeded = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
		tabs.add(new JScrollPane(econacycUi, vAlways, hAsNeeded), "Binary Forest");
		
		//add(tabs);
		add(verticalSplit);
		
		try{
			byte iconBytes[] = (byte[]) jsmGet("/files/data/humanainet/ui/humanainetIcon.jpg");
			Image icon = ImageIO.read(new ByteArrayInputStream(iconBytes));
			setIconImage(icon);
		}catch(Exception e){
			System.err.println("Couldnt use icon");
			e.printStackTrace(System.err);
		}
		
		tabs.setSelectedComponent(mindmap);
		//tabs.setSelectedComponent(talkUsingTagSystem);
		
		setVisible(true);
	}
	
	protected JMenuBar newMenubar(){
		JMenuBar m = new JMenuBar();
		
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		m.add(file);
		
		JMenuItem newHumanainet = new JMenuItem(new AbstractAction("New"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action new");
			}
		});
		newHumanainet.setMnemonic(KeyEvent.VK_N);
		file.add(newHumanainet);
		
		JMenuItem open = new JMenuItem(new AbstractAction("Open"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action open");
			}
		});
		open.setMnemonic(KeyEvent.VK_O);
		file.add(open);
		
		JMenuItem save = new JMenuItem(new AbstractAction("Save"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action save");
			}
		});
		save.setMnemonic(KeyEvent.VK_S);
		file.add(save);
		
		JMenuItem saveAs = new JMenuItem(new AbstractAction("Save As"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action saveas");
			}
		});
		saveAs.setMnemonic(KeyEvent.VK_A);
		file.add(saveAs);
		
		JMenuItem saveAsHtml = new JMenuItem(new AbstractAction("Save As Html (out only, no open from here)"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action saveAsHtml");
			}
		});
		saveAsHtml.setMnemonic(KeyEvent.VK_H);
		file.add(saveAsHtml);
		
		JMenu options = new JMenu("Options");
		options.setMnemonic(KeyEvent.VK_O);
		m.add(options);
		
		JMenuItem optionText = new JMenuItem(new AbstractAction("TODO put various checkboxes, textfields, and other options here"){
			public void actionPerformed(ActionEvent e){
				System.out.println("TODO put options in the menu");
			}
		});
		options.add(optionText);
		
		JMenu opensource = new JMenu("OpenSource");
		opensource.setMnemonic(KeyEvent.VK_S);
		m.add(opensource);
		
		opensource.add(new JLabel("<html>This program is GNU GPL 2+ kind of opensource while some parts offer multilicense as LGPL.<br>You can unzip this jar file (in any unzipping program) to get the source code.<br>Take it apart, play with it, see how it works, and build your own GPL'ed opensource programs.<br>--Ben F Rayfield</html>"));
		
		JMenu workInProgress = new JMenu("This Is A Work In Progress");
		m.add(workInProgress);
		
		workInProgress.add(new JLabel("<html>This program doesnt do much useful yet but has advanced parts that could very soon.<br>Its mostly for programmers as it is now, but I hope to get acyc/xorlisp, smartblob and mindmap working as a<br>unified system soon so we can all build new game objects and evolve designs of them in a global network together,<br>attach comments to specific objects or datastructs in the global space, and see where it takes us.</html>"));
		
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		m.add(help);
		
		help.add(new JLabel("TODO"));
		
		return m;
	}

}
