/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap.ui;
//import static commonfuncs.CommonFuncs.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
/*
//import bayesiancortex.ui.BayesianCortexDynarect;
import realtimeschedulerTodoThreadpool.RealtimeScheduler;
import realtimeschedulerTodoThreadpool.Task;
//import thoughtstream.ui.ThoughtstreamPanel;
//import thoughtstream.ui.ThoughtstreamPanelLetters;
import datastruct.HumanainetFileUtil;
import datastruct.Namespace;
import datastruct.NsNodeListener;
import datastruct.NsNode;
import datastruct.NsNodeUtil;
import datastruct.addr.immutable.Listaddr;
import datastruct.addr.immutable.Prop;
import datastruct.addr.immutable.impl.SimpleListaddr;
import datastruct.addr.immutable.impl.SimpleNamedProperty;
import datastruct.addr.mutable_TODONotMakeSenseToHaveMutableAddress.MutScalar;
import jscreenpixels.PixelInts;
import jscreenpixels.dynarect.Dynarect;
import jscreenpixels.javacomponents.ResizableStretchDynarect;
import jselfmodify.JSelfModify;
import jselfmodify.Mount;
import jselfmodify.mounts.LocalDir;
import mindmap.ColorUtil;
import mindmap.FileAndOptionsBar;
import mindmap.MindMapUtil;
import mindmap.ParseUtil;
import mindmap.URLUtil;
*/

import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.common.Text;
import humanainet.mindmap.MindmapUtil;
import humanainet.mindmap.ParseUtil;
import humanainet.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import humanainet.realtimeschedulerTodoThreadpool.Task;
import humanainet.ui.URLUtil;
import humanainet.xob.XobTypes;
import humanainet.xob.XobUtil;
import humanainet.xorlisp.old.DebugUtil;

/** Prilist is a prioritized list, most important first.
This class should become a convenient interface for reading,
editing, creating, and moving items between prilists
by dragging them with the mouse.
<br><br>
I have many of these starting from prilistRoot,
including prilistResearch, prilistMath, prilistWd (words),
prilistQn (questions), prilistXp (interactive experiments),
prilistGl (longterm research goals), and many others.
*/
public class PrilistsPanel extends JPanel implements DocumentListener/*, NodeListener*/{
	
	//TODO Before allow program to close, ask user where to save data, and if they dont answer in time save near where it was loaded from
	
	//public final FileAndOptionsBar fileAndOptionsBar;
	//public final JCheckBox chkEditDefOnly;
	//TODO? public final JCheckbox chkClickLow;
	
	//public final JCheckBox chkAllowFileToRunCode;
	
	//"TODO use Glo.events"
	
	//public final List<PrilistItemEdge> prilistItemEdgePanels = new ArrayList<PrilistItemEdge>();
	public final List<PrilistsColumn> columns = new ArrayList<PrilistsColumn>();
	
	public final JPanel midVerticalStack, low;
	
	/** the uniqName currently loaded into textArea */
	public final JTextField nameField;
	
	public final JTextField hoursDoneField, hoursTotalField;
	
	/** The counterpart to the text view, this changes to different views
	depending on whats clicked or needed at the time.
	It can can be things like graphics with transparency or other controls.
	*/
	public JComponent objectView;
	
	public final JTextArea textArea;
	
	/** Must be set every time textArea.setText is called, in some order. *
	protected String rememberTextAreaValue = "";
	*/
	
	//public volatile boolean ignoreNameFieldAndTextAreaNodeEvents;
	
	/** For avoiding fighting between JLists over which name will be displayed in center
	starting with events that cause multiple JList updates.
	*
	protected JList lastJListClicked = new JList();
	public void setLastJListClicked(JList j){ lastJListClicked = j; }
	public JList getLastJListClicked(){ return lastJListClicked; }
	*/
	
	public PrilistsPanel() throws Exception{
		this(5);
	}
	
	public PrilistsPanel(int howManyPrilistsPerColumn) throws Exception{
		Color background = ColorUtil.background, foreground = ColorUtil.foreground;
		//add(new JLabel("TODO PrilistsPanel"));
		//boolean verifyData = true;
		//MindMapUtil.loadHumanainetFile(this, jselfmodifyPathOfHumanainetFile);
		setLayout(new BorderLayout());
		String names[] = {"left", "right"};
		for(int i=0; i<names.length; i++){
			PrilistsColumn p = new PrilistsColumn(names[i], howManyPrilistsPerColumn);
			//TODO Namespace.nodeFromUniqName(MindMapUtil.listOfPrilistsName,true).addNodeListener(p);
			p.optionalPrilistsPanel = this;
			columns.add(p);
			//Dimension d = new Dimension(250,250);
			//Dimension d = new Dimension(200,200);
			//p.setMinimumSize(d);
			//p.setPreferredSize(d);
		}
		
		setFocusable(true);
		midVerticalStack = new JPanel();
		midVerticalStack.setFocusable(true);
		midVerticalStack.setLayout(new GridLayout(0,1));
		midVerticalStack.setBackground(background);
		midVerticalStack.setForeground(foreground);
		
		low = new JPanel();
		
		objectView = new JLabel("Object Views will be displayed here");
		objectView.setBackground(background);
		objectView.setForeground(foreground);
		midVerticalStack.add(objectView);
		
		JPanel lowHalf = new JPanel();
		lowHalf.setLayout(new BorderLayout());
		ColorUtil.setColors(lowHalf);
		
		nameField = new JTextField();
		//nameField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
		nameField.setEditable(false);
		ColorUtil.setColors(nameField);
		
		hoursDoneField = new JTextField();
		hoursDoneField.setEditable(true);
		ColorUtil.setColors(hoursDoneField);
		hoursDoneField.getDocument().addDocumentListener(hoursDoneDocListener);
		
		hoursTotalField = new JTextField();
		hoursTotalField.setEditable(true);
		ColorUtil.setColors(hoursTotalField);
		hoursTotalField.getDocument().addDocumentListener(hoursTotalDocListener);
		
		JPanel editNumbers = new JPanel(new GridLayout(1,0));
		ColorUtil.setColors(editNumbers);
		editNumbers.add(hoursDoneField);
		editNumbers.add(hoursTotalField);
		
		//JPanel nameAndNumbers = new JPanel(new BorderLayout());
		//ColorUtil.setColors(nameAndNumbers);
		//nameAndNumbers.add(nameField, BorderLayout.SOUTH);
		//nameAndNumbers.add(editNumbers, BorderLayout.CENTER);
		
		lowHalf.add(nameField, BorderLayout.NORTH);
		lowHalf.add(editNumbers, BorderLayout.SOUTH);
		
		textArea = new JTextArea();
		textArea.getDocument().addDocumentListener(this);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBackground(background);
		textArea.setForeground(foreground);
		textArea.setEditable(true);
		textArea.setCaretColor(getForeground());	
		//textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		textArea.setCaretColor(foreground);
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		lowHalf.add(new JScrollPane(textArea, v, h), BorderLayout.CENTER);
		
		//TODO textArea.setTransferHandler(PrilistsColumn.transferHandler);
		
		midVerticalStack.add(lowHalf);
		
		JPanel midLeftToRight = new JPanel(new GridLayout(1, 0));
		midLeftToRight.setBackground(background);
		midLeftToRight.setForeground(foreground);
		
		midLeftToRight.add(columns.get(0), BorderLayout.WEST);
		midLeftToRight.add(midVerticalStack, BorderLayout.CENTER);
		midLeftToRight.add(columns.get(1), BorderLayout.EAST);
		
		add(midLeftToRight, BorderLayout.CENTER);
		
		low.setBackground(background);
		low.setForeground(foreground);
		
		/*
		chkAllowFileToRunCode = new JCheckBox("Allow file to run code", false);
		chkAllowFileToRunCode.setToolTipText(
			"<html>Stored in the *.humanainet file are names of Java classes, which may exist in the *.jar file"
			+"<br>you double clicked to run or may be in humanainetBigdataSection, and these would be run in the"
			+"<br>top center square when you click names in the lists if you check this box.</html>");
		chkAllowFileToRunCode.setBackground(background);
		chkAllowFileToRunCode.setForeground(foreground);
		low.add(chkAllowFileToRunCode);
		
		chkEditDefOnly = new JCheckBox("Edit def only", true);
		chkEditDefOnly.setBackground(background);
		chkEditDefOnly.setForeground(foreground);
		chkEditDefOnly.setEnabled(false);
		low.add(chkEditDefOnly);
		
		
		chkClickLow = new JCheckBox("clicking lowest list loads other side (TODO)", false);
		chkClickLow.setBackground(background);
		chkClickLow.setForeground(foreground);
		
		chkEditDefOnly.setEnabled(false);
		chkClickLow.setEnabled(false);
		low.add(chkClickLow);
		*/
		
		//add(fileAndOptionsBar = new FileAndOptionsBar(), BorderLayout.SOUTH);
		
		//add(low, BorderLayout.SOUTH);
		
		//textArea.setEnabled(false);
		
		//ThoughtstreamPanel tsp = new ThoughtstreamPanel(256, 256);
		//ThoughtstreamPanel tsp = new ThoughtstreamPanelLetters(256, 256);
		//TODO based on what is dragged to object view or selected other places setObjectView(tsp);
		//RealtimeScheduler.start(tsp, .01);
		
		//Dynarect cortexRect = new BayesianCortexDynarect(50, 50);
		//JComponent cortexComponent = new ResizableStretchDynarect(cortexRect);
		//setObjectView(cortexComponent);
		
		//setObjectView(new ResizableStretchDynarect(PixelInts.exampleImage(150, 110)));
		
		
		JTextField textfield = columns.get(0).editprilists.get(0).textfield;
		String startText = "prilist";
		textfield.setText(startText);
		//textfield.setSelectionStart(0);
		//textfield.setSelectionEnd(startText.length());
		textfield.setCaretPosition(startText.length());
		//textfield.grabFocus(); //in HumanAINetWindow after setVisible(true)
		
	}
	
	public void log(String line){
		System.out.println(line);
	}
	
	//public static final double DEFAULT_TASK_UPDATE_INTERVAL_SECONDS = .05;
	
	public void setObjectView(Class c){
		setObjectView(URLUtil.urlToJComponent("javaclass://"+c.getName()));
	}

	/** Stops old (Task)Component if it is a Task and starts new (Task)Component if it is,
	and adds it to top middle of screen.
	*/
	public void setObjectView(final JComponent c){
		final Component old = midVerticalStack.getComponent(0);
		//midVerticalStack.setFocusable(true);
		//c.setFocusable(true);
		midVerticalStack.remove(0);
		midVerticalStack.validate();
		midVerticalStack.add(c, 0);
		midVerticalStack.validate();
		//midVerticalStack.setFocusable(true);
		//c.setFocusable(true);
		if(c instanceof Task){
			Task t = (Task)c;
			if(!RealtimeScheduler.taskIsRunning(t)) RealtimeScheduler.start(t, t.preferredInterval());
		}
		if(old instanceof Task){
			RealtimeScheduler.scheduleStop((Task)old);
			//RealtimeScheduler.stop((Task)old, true);
		}
		//validate();
		
		//"TODO how to handle magnfied views of Touch object"
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				final Component old = midVerticalStack.getComponent(0);
				midVerticalStack.remove(0);
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						midVerticalStack.add(c, 0);
						//c.grabFocus();
						//c.repaint();
						if(c instanceof Task){
							Task t = (Task)c;
							if(!RealtimeScheduler.taskIsRunning(t)) RealtimeScheduler.start(t, t.preferredInterval());
						}
						if(old instanceof Task){
							RealtimeScheduler.scheduleStop((Task)old);
							//RealtimeScheduler.stop((Task)old, true);
						}
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								c.invalidate();
								midVerticalStack.invalidate();
								//c.repaint();
								//midVerticalStack.repaint();
								PrilistsPanel.this.validate();
							}
						});
					}
				});
			}
		});
	}

	/*
	public void textValueChanged(TextEvent e){
		if(e.getSource() == textArea){
			List<String> oldTokens = ParseUtil.parseNaturalLanguageAndSymbolsUrlsAndNumbers(rememberTextAreaValue);
			rememberTextAreaValue = textArea.getText();
			List<String> newTokens = ParseUtil.parseNaturalLanguageAndSymbolsUrlsAndNumbers(rememberTextAreaValue);
			Listaddr oldList = new SimpleListaddr(oldTokens);
			Listaddr newList = new SimpleListaddr(oldTokens);
			System.err.println("PrilistsPanel.textValueChanged TODO delete oldList="+oldList+" then create newList="+newList);
		}
	}*/
	
	public void insertUpdate(DocumentEvent e){
		log("PrilistsPanel insertUpdate "+e);
		deleteOldDefEdgeAndCreateNew();
	}
	
	public void removeUpdate(DocumentEvent e){
		log("PrilistsPanel removeUpdate "+e);
		deleteOldDefEdgeAndCreateNew();
	}
	
	public void changedUpdate(DocumentEvent e){
		log("PrilistsPanel changedUpdate "+e);
	}
	
	/** TODO Every Listaddr (including Prop) must be in the edges Collection of every NsNode it has as a Bagaddr,
	so make sure NsNodeUtil.setProperty(owner, "def", newList) deletes the old property(s) of that name in all nodes they touch.
	*/
	protected void deleteOldDefEdgeAndCreateNew(){
		String nameDisplayed = nameField.getText();
		String textDisplayed = textArea.getText();
		int ptrName = XobUtil.tokenGlobal(nameDisplayed);
		int oldMindmapItem = Glo.events.get(ptrName);
		int ptrOldDef = MindmapUtil.mindmapItemDef(Glo.econacyc, oldMindmapItem);
		List<List<String>> parsedNewDef = MindmapUtil.listOfLines(textDisplayed, false);
		int ptrNewDef = MindmapUtil.listOfLinesGlobal(parsedNewDef);
		if(ptrOldDef != ptrNewDef){ //The Glo.econacyc Acyc guarantees no duplicates inside itself
			//Def changed. Its important to check this so java events dont create infinite loop
			//setting it to the same text then reacting to the text changing.
			int ptrOldPrilist = MindmapUtil.mindmapItemPrilist(Glo.econacyc, oldMindmapItem);
			int newMindmapItem = MindmapUtil.mindmapItemGlobal(ptrName, ptrOldPrilist, ptrNewDef);
			Glo.events.set(ptrName, newMindmapItem);
			//Recurse into this function when this PrilistsPanel hears the event and tries to update the def
			//but finds it equals the text already there so doesnt go in this if statement.
			//This is needed in case the def is updated from somewhere other than the textarea.
			System.out.println("TODO add "+PrilistsPanel.class.getName()+" as listener to VarMap for event of the mindmapItemName in its nameField's value in VarMap changing to a new mindmapItem, so the def can be changed. Without this, def changing from somewhere outside the textarea wont cause update of textarea, which is expected of AIs or multi user system to do later.");
		}
	}
	
	protected final DocumentListener hoursDoneDocListener = new DocumentListener(){
		
		//TODO merge duplicate code between hoursDone and hoursTotal
		
		public void removeUpdate(DocumentEvent e){
			changedHoursTotalTextField();
		}

		public void insertUpdate(DocumentEvent e){
			changedHoursTotalTextField();
		}

		public void changedUpdate(DocumentEvent e){}
		
		public void changedHoursTotalTextField(){
			String nameDisplayed = nameField.getText(); //name of the def and props being edited
			int ptrName = XobUtil.tokenGlobal(nameDisplayed);
			int ptrPropName = XobUtil.propName(Glo.econacyc, ptrName, MindmapUtil.hoursDone);
			
			int testItsType = Acyc32Util.typeOf(Glo.econacyc, ptrPropName);
			if(testItsType != XobTypes.typeProp) throw new RuntimeException("Wrong type: expected="+XobTypes.typeProp+" got="+testItsType);
			System.out.println("Acyc node "+ptrPropName+" is correctly type XobTypes.typeProp="+XobTypes.typeProp);
			
			int oldPropValue = Glo.events.get(ptrPropName);
			//TODO if they type other whitespace remove it. or only allow numbers.
			//TODO if its empty, remove the prop, dont just save empty string.
			String newStringValue = MindmapUtil.afterLastSpace(hoursDoneField.getText());
			int newPropValue = newStringValue.isEmpty() ? 0 : XobUtil.tokenGlobal(newStringValue);
			if(oldPropValue != newPropValue){
				Glo.events.set(ptrPropName, newPropValue);
			}
			System.out.println("changedHoursDoneTextField");
		}
	};
	
	protected final DocumentListener hoursTotalDocListener = new DocumentListener(){
		
		//TODO merge duplicate code between hoursDone and hoursTotal
		
		public void removeUpdate(DocumentEvent e){
			changedHoursTotalTextField();
		}

		public void insertUpdate(DocumentEvent e){
			changedHoursTotalTextField();
		}

		public void changedUpdate(DocumentEvent e){}
		
		public void changedHoursTotalTextField(){
			String nameDisplayed = nameField.getText(); //name of the def and props being edited
			int ptrName = XobUtil.tokenGlobal(nameDisplayed);
			int ptrPropName = XobUtil.propName(Glo.econacyc, ptrName, MindmapUtil.hoursTotal);

			int testItsType = Acyc32Util.typeOf(Glo.econacyc, ptrPropName);
			if(testItsType != XobTypes.typeProp) throw new RuntimeException("Wrong type: expected="+XobTypes.typeProp+" got="+testItsType);
			System.out.println("Acyc node "+ptrPropName+" is correctly type XobTypes.typeProp="+XobTypes.typeProp);
			
			int oldPropValue = Glo.events.get(ptrPropName);
			//TODO if they type other whitespace remove it. or only allow numbers.
			//TODO if its empty, remove the prop, dont just save empty string.
			String newStringValue = MindmapUtil.afterLastSpace(hoursTotalField.getText());
			int newPropValue = newStringValue.isEmpty() ? 0 : XobUtil.tokenGlobal(newStringValue);
			if(oldPropValue != newPropValue){
				Glo.events.set(ptrPropName, newPropValue);
			}
			System.out.println("changedHoursTotalTextField");
		}
	};
	
	public void loadNameAndDef(int nameInVarMap){
		System.out.println("loadNameAndDef of ptr="+nameInVarMap);
		//log("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nSTART: loadNameAndDef for "+n.uniqName)
		synchronized(getTreeLock()){
			int untypedName = Acyc32Util.valueOf(Glo.econacyc, nameInVarMap);
			String name = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, untypedName);
			nameField.setText(name);
			int ptrMindmapItem = Glo.events.get(nameInVarMap);
			int ptrDef = MindmapUtil.mindmapItemDef(Glo.econacyc, ptrMindmapItem);
			String def;
			if(ptrDef == 0){
				def = ""; //new def
			}else if(!MindmapUtil.isMindmapItem(Glo.econacyc, ptrMindmapItem)){
				throw new RuntimeException("TODO handle values in VarMap that arent mindmapItems: "+ptrMindmapItem);
			}else{
				def = MindmapUtil.formatDefForTextEditing(ptrDef, Text.n); //lines
				//def = MindmapUtil.formatDefForTextEditing(ptrDef, Text.n+Text.n); //paragraphs
			}
			//TODO handle events. I'm afraid this will update def its trying to display in infinite loop
			//as happened in earlier versions. Rewrite this comment after its working.
			textArea.setText(def);
			String urlOrNull = ParseUtil.getFirstUrlOrNull(def);
			if(urlOrNull != null){
				setObjectView(URLUtil.urlToJComponent(urlOrNull));
			}
			
			int hoursDoneName = XobUtil.propName(Glo.econacyc, nameInVarMap, MindmapUtil.hoursDone);
			int hoursDoneTypedValue = Glo.events.get(hoursDoneName);
			String hoursDoneTextValue = "";
			if(hoursDoneTypedValue != 0){ //has a value of type token
				int type = Acyc32Util.typeOf(Glo.econacyc, hoursDoneTypedValue);
				if(type != XobTypes.typeToken) throw new RuntimeException("Not a token: "+hoursDoneTypedValue);
				//TODO later this may be changed to a number type where half the binary digits are above the decimal point
				int hoursDoneUntypedValue = Acyc32Util.valueOf(Glo.econacyc, hoursDoneTypedValue);
				//System.out.println("hoursDoneValue = "+TextUtil32.toString(Glo.econacyc, hoursDoneTypedValue, DebugUtil.defaultNamesForDebugging));
				hoursDoneTextValue = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, hoursDoneUntypedValue);
			}
			hoursDoneField.setText(hoursDoneTextValue);
			
			int hoursTotalName = XobUtil.propName(Glo.econacyc, nameInVarMap, MindmapUtil.hoursTotal);
			int hoursTotalTypedValue = Glo.events.get(hoursTotalName);
			String hoursTotalTextValue = "";
			if(hoursTotalTypedValue != 0){ //has a value of type token
				int type = Acyc32Util.typeOf(Glo.econacyc, hoursTotalTypedValue);
				if(type != XobTypes.typeToken) throw new RuntimeException("Not a token: "+hoursTotalTypedValue);
				//TODO later this may be changed to a number type where half the binary digits are above the decimal point
				int hoursTotalUntypedValue = Acyc32Util.valueOf(Glo.econacyc, hoursTotalTypedValue);
				hoursTotalTextValue = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, hoursTotalUntypedValue);
			}
			hoursTotalField.setText(hoursTotalTextValue);
			
		}
	}

}