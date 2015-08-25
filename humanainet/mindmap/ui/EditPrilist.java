/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap.ui;
//import static commonfuncs.CommonFuncs.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.acyc.ptr32.event.PropListener32;
import humanainet.acyc.ptr32.event.VarListener32;
import humanainet.acyc.ptr32.event.VarMap32;
import humanainet.common.Time;
import humanainet.mindmap.MindmapUtil;
import humanainet.xob.Xob;
import humanainet.xob.XobTypes;
import humanainet.xob.XobUtil;
import humanainet.xob.ptr32.ListPow2;

/*import datastruct.Namespace;
import datastruct.NsNodeListener;
import datastruct.NsNode;
import datastruct.NsNodeUtil;
import datastruct.addr.immutable.Listaddr;
import datastruct.addr.immutable.Prop;
import datastruct.addr.immutable.impl.SimpleListaddr;
import datastruct.addr.mutable_TODONotMakeSenseToHaveMutableAddress.MutScalar;
*/

/** TODO much of the comments in this class need updating since I'm changing it
from datastruct.NsNode and datastruct.Bagaddr to xorlisp's acyc data format and events.
This is a change for large parts of my software and will greatly simplify.
<br><br>
A textfield, JList (in vertical JScrollPane),
add button (+), and delete button (-).
<br><br>
Because the textfield must be as wide as content in the JList,
the buttons will be on the right side (or TODO a parameter for right/left)
and spaced vertically. The add button directly to the right of
the textfield, and below it the delete button. Later other buttons
may be added so this is a good layout.
<br><br>
As in the original design of PrilistsPanel and PrilistItemEdge,
the JList can be dragged from or into with the string uniqName
of any NsNode in the system. It can also create new NsNode,
but deleting here is only from a list, not the item itself
which is considered deleted only when it has no edges.
<br><br>
TODO images for the buttons later, or just leave them as + and -?
*/
public class EditPrilist extends JPanel implements VarListener32, KeyListener/*,NsNodeListener, LastTouchedWhen*/{
	
	public final DefaultListModel listModel;

	public final JList jlist;
	
	public final JTextField textfield;
	
	public final JPanel northPanel;
	
	public final JButton btnDel, btnAdd;
	
	protected boolean addAtTop = true;
	
	/** For debugging, each EditPrilist needs a name, like left3_n for index 3 in left column
	and nth EditPrilist created during that run of hte program.
	*/
	public final String name;
	
	public static long howManyEditPrilistsCreated = 0;
	
	//protected double lastTouchedWhen = Time.time();
	
	//"TODO use Glo.events"
	
	//protected List<Xob> contentsSnapshotOrNull;
	protected int ptrMindmapItemName;
	public int ptrMindmapItemName(){ return ptrMindmapItemName; }
	
	protected EditPrilist nextOrNull;
	
	/** If true, displays the values (if any) for MindmapUtil.hoursDone and MindmapUtil.hoursTotal left of each item in the list */
	protected boolean displayNumbersWithEachItem = true;
	
	/** UPDATE: Commenting this out to avoid having to lock this pointer in Glo.econacyc,
	and instead will every time get its value from Glo.events.get(ptrMindmapItemName).
	Last known value of ptrMindmapItemName. Should be updated through Glo.events *
	protected int ptrMindmapItem;
	*/
	
	/** Contents of the prilist property in parent is displayed and editable here.
	When this EditPrilist as a NodeListener gets events, list contents are updated to
	the new prilist contents of this parentOrNull NsNode, or if its null, an empty list.
	*/
	//protected Xob parentOrNull;
	
	/** newPtrMindmapItemName must be a MindmapUtil.typeToken or 0/()/nil */
	public void setParentOrNil(int newPtrMindmapItemName){
		System.out.println("setting ptrMindmapItemName = "+newPtrMindmapItemName+" in "+this);
		int oldPtr = ptrMindmapItemName;
		boolean change = oldPtr != newPtrMindmapItemName;
		ptrMindmapItemName = newPtrMindmapItemName;
		if(change){
			
			disableAllBelow();
			
			Glo.events.stopListen(this, oldPtr);
			if(newPtrMindmapItemName != 0){
				Glo.events.startListen(this, ptrMindmapItemName);
				int ptrValue = Glo.events.get(ptrMindmapItemName);
				afterVarChange(Glo.events, ptrMindmapItemName);
			}
		}
	}
	
	protected void disableAllBelow(){
		if(nextOrNull != null){
			nextOrNull.disableAllBelow();
			nextOrNull.setThisEditPrilistEnabled(false);
		}
	}
	
	/** Called later resulting from Glo.events.startListen and stopListen to ptrMindmapItemName.
	Using new value, get lists of String from Glo.econacyc,
	expecting ptrNewValue to be data format of mindmapItem.
	*/
	public void afterVarChange(VarMap32 varMap, int ptrTypedName){
		int ptrName = Acyc32Util.valueOf(Glo.econacyc, ptrTypedName);
		//See comment in EditPrilist.startListen including "assuming that all values are mindmapItems".
		//Cant assume that all values in VarMap are mindmapItem, but since its here it is.
		//My error was not creating an empty mindmapItem value when creating the key when add is clicked.
		int ptrValue = varMap.get(ptrTypedName);
		System.out.println("EditPrilist "+name+" afterVarChange "+ptrTypedName+" and looked up value "+ptrValue);
		int typeOfName = Acyc32Util.typeOf(Glo.econacyc, ptrTypedName);
		int typeOfValue = Acyc32Util.typeOf(Glo.econacyc, ptrValue);
		if(typeOfValue == MindmapUtil.typeMindmapItem){
			System.out.println("EditPrilist.onChange ptrName="+ptrName+" name="+XobUtil.listOfPowerOf2ToString(Glo.cacheacyc,ptrName)+" ptrTypedNewValue="+ptrValue
				+" objectValueAsPrilist="+MindmapUtil.mindmapItemPrilistOfStrings(Glo.cacheacyc, ptrValue));
			if(ptrTypedName == ptrMindmapItemName && varMap.acyc() == Glo.econacyc){
				List<String> prilist = MindmapUtil.mindmapItemPrilistOfStrings(Glo.cacheacyc, ptrValue);
				setList(prilist,true);
			}
		}else if(typeOfName == XobTypes.typeProp){ //We're listening for props: MindmapUtil.hoursDone and MindmapUtil.hoursTotal
			int ptrMindmapItem = varMap.get(ptrMindmapItemName);
			int testMindmapItemType = Acyc32Util.typeOf(varMap.acyc(), ptrMindmapItem);
			if(testMindmapItemType != MindmapUtil.typeMindmapItem) throw new RuntimeException(
				"afterVarChange name="+ptrTypedName
				+" Updating EditPrilist contents of mindmapItemName="+ptrMindmapItemName
				+" but got something other than a mindmapItem for its value="+ptrMindmapItem
				+" That value's type is "+testMindmapItemType+" but expected type "+MindmapUtil.typeMindmapItem);
			List<String> prilist = MindmapUtil.mindmapItemPrilistOfStrings(Glo.cacheacyc, ptrMindmapItem);
			prilist = addPrefixesIfShouldAndNotExist(prilist);
			setList(prilist,false);
		}
	}
	
	/** VarListener.startPropListen is when you dont know the prop names. This listens for specific prop names as normal vars. */
	protected void startOrStop2SpecificPropsListeningToAll(List<String> tokens, boolean startInsteadOfStop){
		for(String token : tokens){
			token = MindmapUtil.afterLastSpace(token); //in case prefix(tokenWithoutPrefix) is there
			int name = XobUtil.tokenGlobal(token);
			int propNameA = XobUtil.propName(Glo.econacyc, name, MindmapUtil.hoursDone);
			int propNameB = XobUtil.propName(Glo.econacyc, name, MindmapUtil.hoursTotal);
			if(startInsteadOfStop){
				Glo.events.startListen(this, propNameA);
				Glo.events.startListen(this, propNameB);
				//Glo.events.startListen((PropListener32)this, propNameA);
				//Glo.events.startListen((PropListener32)this, propNameB);
			}else{
				Glo.events.stopListen(this, propNameA);
				Glo.events.stopListen(this, propNameB);
				//Glo.events.stopListen((PropListener32)this, propNameA);
				//Glo.events.stopListen((PropListener32)this, propNameB);
			}
		}
	}
	
	public void setThisEditPrilistEnabled(boolean enabled){
		if(!enabled && nextOrNull != null){
			nextOrNull.setThisEditPrilistEnabled(false);
		}
		btnDel.setEnabled(enabled);
		btnAdd.setEnabled(enabled);
		if(enabled){
			textfield.setEnabled(true);
			jlist.setEnabled(true);
		}else{
			listModel.clear();
			textfield.setText("");
			textfield.setEnabled(false);
			jlist.setEnabled(false);
		}
	}
	
	/*public void setParentOrNull(Xob parentOrNull){ //TODO synchronized?
		//TODO if(this.parentOrNull != null) this.parentOrNull.removeNodeListener(this);
		this.parentOrNull = parentOrNull;
		//TODO if(this.parentOrNull != null) this.parentOrNull.addNodeListener(this);
		refresh();
	}
	
	/** some callers use this to remember which string was selected while content changes
	TODO remove this and use textfield.getText() and make it change to MindMapUtil.other... when edited to something not a member of the list
	*
	public String rememberSelected = MindMapUtil.otherOrEmptyName;
	*/
	
	/*protected String requiredPrefix = "";
	protected String requiredPrefixMessageToUser = "";
	public String requiredPrefix(){ return requiredPrefix; }
	public void setRequiredPrefix(String prefixOrEmptyString, String messageToUser){
		System.err.println("TODO verify all current and future list contents start with requiredPrefix()");
		this.requiredPrefix = prefixOrEmptyString;
		this.requiredPrefixMessageToUser = messageToUser;
	}*/
	
	public EditPrilist(String name, Color background, Color foreground){
		setLayout(new BorderLayout());
		this.name = name;
		textfield = new JTextField();
		textfield.setBackground(background);
		textfield.setForeground(foreground);
		//add(textfield, BorderLayout.NORTH);
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		jlist = new JList(listModel = new DefaultListModel());
		jlist.addKeyListener(this);
		jlist.setDragEnabled(true);
		jlist.setBackground(background);
		jlist.setForeground(foreground);
		jlist.setDropMode(DropMode.INSERT);
		//jlist.setTransferHandler(transferHandler);
		//((DefaultListModel)jlist.getModel()).add(0, MindMapUtil.otherOrEmptyName);
		listModel.add(0, MindmapUtil.otherOrEmptyName);
		//jlist.addListSelectionListener(listSelectListener);
		add(new JScrollPane(jlist, v, h), BorderLayout.CENTER);
		northPanel = new JPanel(new BorderLayout());
		//northPanel = new JPanel();
		northPanel.setBackground(background);
		northPanel.setForeground(foreground);
		btnDel = new JButton(new AbstractAction("-"){
			public void actionPerformed(ActionEvent e){
				del();
			}
		});
		btnDel.setBackground(background);
		btnDel.setForeground(foreground);
		btnAdd = new JButton(new AbstractAction("+"){
			public void actionPerformed(ActionEvent e){
				add();
			}
		});
		btnAdd.setBackground(background);
		btnAdd.setForeground(foreground);
		northPanel.add(btnDel, BorderLayout.WEST);
		
		/*JButton btnDisable = new JButton(new AbstractAction("disable"){
			public void actionPerformed(ActionEvent e){
				setThisEditPrilistEnabled(false);
			}
		});
		northPanel.add(btnDisable, BorderLayout.NORTH);
		*/
		
		//Dimension d = new Dimension(220,20);
		//Dimension d = new Dimension(70,20);
		textfield.setCaretColor(foreground);
		//textfield.setMinimumSize(d);
		//textfield.setPreferredSize(d);
		textfield.addKeyListener(this);
		northPanel.add(textfield, BorderLayout.CENTER);
		northPanel.add(btnAdd, BorderLayout.EAST);
		add(northPanel, BorderLayout.NORTH);
		jlist.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				if(e.getValueIsAdjusting()) return;
				int i = jlist.getSelectedIndex();
				if(i != -1){
					String selected = MindmapUtil.afterLastSpace(listModel.get(i).toString());
					if(selected.equals(MindmapUtil.otherOrEmptyName)){
						textfield.setText("");
						if(nextOrNull != null){
							nextOrNull.setParentOrNil(0);
							nextOrNull.setThisEditPrilistEnabled(false);
						}
					}else{
						textfield.setText(selected);
						if(nextOrNull != null){
							nextOrNull.setThisEditPrilistEnabled(true);
							//int ptrUntypedSelected = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, selected);
							int ptrSelected = XobUtil.tokenGlobal(selected);
							nextOrNull.setParentOrNil(ptrSelected);
						}
					}
				}
			}
		});
		//setList(Arrays.asList("one", "two", "three"));
	}
	
	public static void log(String line){
		System.out.println(line);
	}
	
	public void del(){
		String name = textfield.getText().trim();
		del(name);
	}
	
	public void del(String mayHavePrefix){
		//name = MindmapUtil.afterLastSpace(name); //in case it has a prefix EditPrilist.prefix(String)
		//name = addPrefixIfShouldAndNotExist(name); //in case it has a prefix EditPrilist.prefix(String)
		String withoutPrefix = MindmapUtil.afterLastSpace(mayHavePrefix);
		if(MindmapUtil.otherOrEmptyName.equals(withoutPrefix)) return;
		List<String> list = getDisplayedList();
		mayHavePrefix = addPrefixIfShouldAndNotExist(withoutPrefix);
		int index = list.indexOf(mayHavePrefix); //it can only be in the list once
		//boolean isLast = index == list.size()-1;
		String selectAfterItsGone = index<list.size()-1 ? list.get(index+1) : MindmapUtil.otherOrEmptyName;
		if(index != -1){ //not in the list. add at top.
			list = removeOtherEmpty(list);
			list = new ArrayList(list);
			list.remove(index);
			list = Collections.unmodifiableList(list);
			//System.out.println("EditPrilist del newList="+list);
			updatePrilistInVarMapAndLaterEventWillCauseDisplay(list);
		}
		//select what was below it and is now at same index which may be MindmapUtil.otherOrEmptyName
		jlist.setSelectedValue(selectAfterItsGone,true);
	}
	
	/** only adds prefix if the name should have a prefix */
	protected String addPrefixIfShouldAndNotExist(String name){
		String withoutPrefix = MindmapUtil.afterLastSpace(name);
		return prefix(withoutPrefix)+withoutPrefix;
	}
	
	public void add(){
		log("EditPrilist btnAdd");
		String target = textfield.getText().trim();
		if(target.equals("")){
			JOptionPane.showMessageDialog(EditPrilist.this, "Type a name first");
		}else if(!isValidName(target)){
			JOptionPane.showMessageDialog(EditPrilist.this, "No spaces");
		}else{
			add(target);
		}
	}
	
	public static boolean isValidName(String name){
		return name.length()>0 && !name.contains(" ") && !name.contains("\\t") && !name.contains("\\r") && !name.contains("\\ns"); 
		
	}
	
	/** Adds if not exists, then selects */
	public void add(String mayHavePrefix){
		//name = MindmapUtil.afterLastSpace(name); //in case it has a prefix EditPrilist.prefix(String)
		String withoutPrefix = MindmapUtil.afterLastSpace(mayHavePrefix);
		createEmptyMindmapItemValueIfValueIsNil(withoutPrefix);
		mayHavePrefix = addPrefixIfShouldAndNotExist(withoutPrefix);
		List<String> list = getDisplayedList();
		int index = list.indexOf(mayHavePrefix); //it can only be in the list once
		if(index == -1){ //not in the list. add at top.
			list = removeOtherEmpty(list);
			list = new ArrayList(list);
			list.add(index=0, mayHavePrefix);
			list = Collections.unmodifiableList(list);
			//System.out.println("EditPrilist add newList="+list);
			updatePrilistInVarMapAndLaterEventWillCauseDisplay(list);
		}
		//else already in the list. select and scroll to that part.
		jlist.setSelectedValue(mayHavePrefix,true);
	}
	
	public static void createEmptyMindmapItemValueIfValueIsNil(String mindmapItemNameString){
		int mindmapItemName = XobUtil.tokenGlobal(mindmapItemNameString);
		int existingValue = Glo.events.get(mindmapItemName);
		if(existingValue == 0){
			int mindmapItem = MindmapUtil.mindmapItemGlobal(
				mindmapItemName, MindmapUtil.emptyPrilist, MindmapUtil.emptyDef);
			Glo.events.set(mindmapItemName, mindmapItem);
			System.out.println("Set name="+mindmapItemNameString+" ptrName="+mindmapItemName+" to empty prilist and def because its value was nil.");
		}else{
			System.out.println("Left name="+mindmapItemNameString+" ptrName="+mindmapItemName+" as its existing value: "+existingValue);
		}
	}
	
	protected void updatePrilistInVarMapAndLaterEventWillCauseDisplay(List<String> newPrilistContents){
		System.out.println("updatePrilistInVarMapAndLaterEventWillCauseDisplay list="+newPrilistContents+" ptrMindmapItemName="+ptrMindmapItemName);
		int currentMindmapItem = Glo.events.get(ptrMindmapItemName);
		
		System.out.println("ptrMindmapItemName="+ptrMindmapItemName);
		
		int observedTypVal = Glo.econacyc.left(currentMindmapItem);
		System.out.println("observedTypVal="+observedTypVal);
		int observedTypeAndValue = Glo.econacyc.right(currentMindmapItem);
		int observedMindmapItemType = Glo.econacyc.left(observedTypeAndValue);
		System.out.println("observedMindmapItemType="+observedMindmapItemType+" expected="+MindmapUtil.typeMindmapItem);
		int observedUntypedMindmapItem = Glo.econacyc.right(observedTypeAndValue);
		System.out.println("observedUntypedMindmapItem="+observedUntypedMindmapItem);
		int observedName = Glo.econacyc.left(observedUntypedMindmapItem);
		System.out.println("observedName="+observedName);
		int untypedName = Acyc32Util.valueOf(Glo.econacyc, observedName);
		System.out.println("observedName as string ="+XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, untypedName));
		
		//If this EditPrilist is highest on screen, its ptrName==ptrMindmapItemName will be
		//the typeToken form of MindmapUtil.listOfPrilistsName
		//Lower EditPrilists on screen will use whats selected in the EditPrilist directly above.
		int ptrName = MindmapUtil.mindmapItemName(Glo.econacyc, currentMindmapItem);
		if(ptrName != ptrMindmapItemName) throw new RuntimeException(
			ptrName+" == ptrName != ptrMindmapItemName == "+ptrMindmapItemName+" this="+this);
		int ptrNewPrilist = MindmapUtil.typedListPow2OfTokenGlobal(newPrilistContents);
		int ptrDef = MindmapUtil.mindmapItemDef(Glo.econacyc, currentMindmapItem);
		int newMindmapItem = MindmapUtil.mindmapItemGlobal(ptrName, ptrNewPrilist, ptrDef);
		//System.out.println("updatePrilistInVarMapAndLaterEventWillCauseDisplay ptrNewPrilist as text ="+MindmapUtil.mindmapItemPrilistOfStrings(Glo.cacheacyc, newMindmapItem));
		Glo.events.set(ptrName, newMindmapItem);
	}
	
	
	/** The next EditPrilist has its parentOrNull node set to whats selected in this EditPrilist */
	public void setNext(EditPrilist next){
		nextOrNull = next;
		if(next != null){
			String selected = selected();
			//int ptrSelected = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, selected);
			int ptrSelected = XobUtil.tokenGlobal(selected);
			if(!selected.equals(MindmapUtil.otherOrEmptyName)){
				//next.parentOrNil = PolycatNamespace.nodeFromUniqName(selected(),true);
				next.setParentOrNil(ptrSelected);
			}
		}
	}
	
	public String selected(){
		int i = jlist.getSelectedIndex();
		if(i == -1) return MindmapUtil.otherOrEmptyName;
		return MindmapUtil.afterLastSpace(listModel.get(i).toString());
	}
	
	/** changes the contents of this list *
	public <T> void setListaddr(final List<Xob> value){
		TODO
		lastTouchedWhen = Time.time();
		final NsNode owner = parentOrNull;
		if(owner != null){
			String oldPrilist[] = NsNodeUtil.getPrilistContents(owner);
			List<String> oldPrilistWithoutEmpty = new ArrayList<String>();
			for(String s : oldPrilist){
				if(!s.equals(MindmapUtil.otherOrEmptyName)) oldPrilistWithoutEmpty.add(s);
			}
			List<String> newPrilistWithoutEmpty = new ArrayList<String>();
			for(NsNode<T> n : value){
				if(!(n.uniqName instanceof String)) throw new RuntimeException(
					"TODO types other than String: "+n.uniqName.getClass());
				String s = (String) n.uniqName;
				if(!s.equals(MindMapUtil.otherOrEmptyName)) newPrilistWithoutEmpty.add(s);
			}
			if(!oldPrilistWithoutEmpty.equals(newPrilistWithoutEmpty)){
				log("START: Setting new prilist in "+this+" from "+oldPrilistWithoutEmpty+" to "+newPrilistWithoutEmpty);
				NsNodeUtil.setProperty(owner, "prilist", value); //avoid infinite loop in events
				log("END: Setting new prilist in "+this+" from "+oldPrilistWithoutEmpty+" to "+newPrilistWithoutEmpty);
			}
		}
		synchronized(jlist.getTreeLock()){
			boolean changeOnScreen = false;
			int newSiz = listModel.size();
			if(newSiz != value.size()) changeOnScreen = true;
			else for(int i=0; i<newSiz; i++){
				if(!listModel.get(i).equals(value.listaddrNode(i))){
					changeOnScreen = true;
					break;
				};
			}
			Object selected = jlist.getSelectedValue();
			contentsSnapshotOrNull = value;
			//TODO synchronize on jlist or listModel or contentsSnapshotOrNull?
			listModel.clear(); //TODO keep selected string if any, and keep position scrolled in the list
			boolean foundOldSelected = false;
			for(NsNode n : value){
				String s = n.uniqName.toString();
				if(!MindMapUtil.otherOrEmptyName.equals(s)){
					listModel.addElement(s);
				}
				if(n.uniqName.equals(selected)) foundOldSelected = true; //TODO test if it scrolls to here
			}
			listModel.addElement(MindMapUtil.otherOrEmptyName);
			if(foundOldSelected) jlist.setSelectedValue(selected, true);
		}
		
	}*/
	
	/** TODO this could get very slow if many Bagaddr are created.
	Need a better NodeListener interface to only subscribe to certain events.
	*
	public void onCreate(MutScalar created){
		if(NsNodeUtil.isPrilistEdge(created.addr)){ //avoid updating potentially in infinite loop if def update
			refresh();
		}
	}
	
	public void onDelete(MutScalar deleting){
		if(NsNodeUtil.isPrilistEdge(deleting.addr)){ //avoid updating potentially in infinite loop if def update
			refresh();
		}
	}*/
	
	/*public void refresh(){
		TODO
		if(parentOrNull == null){
			setListaddr(SimpleListaddr.EMPTY);
		}else{
			List list = Arrays.asList(NsNodeUtil.getPrilistContents(parentOrNull));
			setListaddr(new SimpleListaddr(list));
		}
	}*/
	
	/** Removes MindmapUtil.otherOrEmptyName */
	public static List<String> removeOtherEmpty(List<String> list){
		//System.out.println("removeOtherEmpty list="+list);
		List<String> newList = new ArrayList();
		for(String s : list){
			if(!MindmapUtil.otherOrEmptyName.equals(s)){
				newList.add(s);
			}
		}
		//System.out.println("removeOtherEmpty newList="+newList);
		return Collections.unmodifiableList(newList);
	}
	
	public static List<String> removePrefixs(List<String> list){
		List<String> list2 = new ArrayList();
		for(String s : list){
			list2.add(MindmapUtil.afterLastSpace(s));
		}
		if(list.equals(list2)) return list;
		return Collections.unmodifiableList(list2);
	}
	
	public List<String> getDisplayedList(){
		List<String> list = new ArrayList();
		for(int i=0; i<listModel.size(); i++){
			String s = listModel.get(i).toString();
			//s = addPrefixIfShouldAndNotExist(s);
			list.add(s);
		}
		return Collections.unmodifiableList(list);
	}
	
	/** If hoursDone and hoursTotal should be displayed with the item, returns them with a space in front of each.
	Else returns empty string.
	*/
	public String prefix(String mindmapItemName){
		if(!displayNumbersWithEachItem || MindmapUtil.otherOrEmptyName.equals(mindmapItemName)) return "";
		int nameInVarMap = XobUtil.tokenGlobal(mindmapItemName);
		
		int hoursDoneName = XobUtil.propName(Glo.econacyc, nameInVarMap, MindmapUtil.hoursDone);
		int hoursDoneTypedValue = Glo.events.get(hoursDoneName);
		String hoursDoneTextValue = "?";
		if(hoursDoneTypedValue != 0){ //has a value of type token
			int type = Acyc32Util.typeOf(Glo.econacyc, hoursDoneTypedValue);
			if(type != XobTypes.typeToken) throw new RuntimeException("Not a token: "+hoursDoneTypedValue);
			//TODO later this may be changed to a number type where half the binary digits are above the decimal point
			int hoursDoneUntypedValue = Acyc32Util.valueOf(Glo.econacyc, hoursDoneTypedValue);
			//System.out.println("hoursDoneValue = "+TextUtil32.toString(Glo.econacyc, hoursDoneTypedValue, DebugUtil.defaultNamesForDebugging));
			hoursDoneTextValue = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, hoursDoneUntypedValue);
		}
		
		int hoursTotalName = XobUtil.propName(Glo.econacyc, nameInVarMap, MindmapUtil.hoursTotal);
		int hoursTotalTypedValue = Glo.events.get(hoursTotalName);
		String hoursTotalTextValue = "?";
		if(hoursTotalTypedValue != 0){ //has a value of type token
			int type = Acyc32Util.typeOf(Glo.econacyc, hoursTotalTypedValue);
			if(type != XobTypes.typeToken) throw new RuntimeException("Not a token: "+hoursTotalTypedValue);
			//TODO later this may be changed to a number type where half the binary digits are above the decimal point
			int hoursTotalUntypedValue = Acyc32Util.valueOf(Glo.econacyc, hoursTotalTypedValue);
			hoursTotalTextValue = XobUtil.listOfPowerOf2ToString(Glo.cacheacyc, hoursTotalUntypedValue);
		}
		
		while(hoursDoneTextValue.length() < 3) hoursDoneTextValue = " "+hoursDoneTextValue;
		while(hoursTotalTextValue.length() < 3) hoursTotalTextValue = " "+hoursTotalTextValue;
		
		return hoursDoneTextValue+" "+hoursTotalTextValue+" ";
	}
	
	/** Each item can occur only once. Keeps same string selected, if any, at its new position in the list, unless its not in the new list.
	Stops prop listening to old list, and starts prop listening to new list, for 2 specific props:
	MindmapUtil.hoursDone and MindmapUtil.hoursTotal.
	*/
	public void setList(final List<String> list, final boolean selectAndScroll){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				//System.out.println("setList list="+list);
				List<String> currentList = removeOtherEmpty(getDisplayedList());
				List<String> list2 = addPrefixesIfShouldAndNotExist(removeOtherEmpty(list));
				if(!currentList.equals(list2)){
					synchronized(jlist.getTreeLock()){
						startOrStop2SpecificPropsListeningToAll(currentList, false);
						startOrStop2SpecificPropsListeningToAll(list2, true);
						Object selected = jlist.getSelectedValue();
						//contentsSnapshotOrNull = value;
						//TODO synchronize on jlist or listModel or contentsSnapshotOrNull?
						listModel.clear(); //TODO keep selected string if any, and keep position scrolled in the list
						boolean foundOldSelected = false;
						//for(NsNode n : value){
						for(String s : list2){
							//String s = n.uniqName.toString();
							if(!MindmapUtil.otherOrEmptyName.equals(s)){
								//System.out.println("setList listModel.addElement: "+s);
								listModel.addElement(s);
							}
							if(s.equals(selected)) foundOldSelected = true; //TODO test if it scrolls to here
						}
						listModel.addElement(MindmapUtil.otherOrEmptyName);
						if(selectAndScroll && foundOldSelected) jlist.setSelectedValue(selected, true);
					}
				}
			}
		});
	}
	
	/** Adds prefix(eachMindmapItem) if each item not already start with that prefix. To remove, its after last space. */
	protected List<String> addPrefixesIfShouldAndNotExist(List<String> list){
		List<String> newList = new ArrayList();
		for(String s : list){
			String withoutPrefix = MindmapUtil.afterLastSpace(s);
			String prefix = prefix(withoutPrefix);
			newList.add(prefix+withoutPrefix);
		}
		if(list.equals(newList)) return list;
		return Collections.unmodifiableList(newList);
	}
	
    public void keyTyped(KeyEvent e){}

    public void keyPressed(KeyEvent e){
    	//TODO lastTouchedWhen = TimeUtil.time();
    	//log("typed "+e);
    	int k = e.getKeyCode();
    	if(e.getComponent() == textfield){
        	if(k == KeyEvent.VK_ENTER) add();
        	else if(k == KeyEvent.VK_UP){
        		int newIndex = jlist.getSelectedIndex()-1;
        		if(newIndex >= 0){
        			jlist.setSelectedValue(listModel.get(newIndex),true);
        		}
        	}else if(k == KeyEvent.VK_DOWN){
        		int newIndex = jlist.getSelectedIndex()+1;
        		if(newIndex < listModel.size()){
        			jlist.setSelectedValue(listModel.get(newIndex),true);
        		}
        	}
    	}else if(e.getComponent() == jlist){
    		if(k == KeyEvent.VK_DELETE || k == KeyEvent.VK_BACK_SPACE || k == KeyEvent.VK_MINUS) del();
    		else if(k == KeyEvent.VK_ENTER || k == KeyEvent.VK_PLUS) add();
    	}
    }

    public void keyReleased(KeyEvent e){}
	
    //public double lastTouchedWhen(){ return lastTouchedWhen; }
    
    public String toString(){
    	return name+"_"+super.toString();
    }
    
    /*public void propCreated(VarMap32 varMap, int prop){
    	"TODO redisplay whole list. But if call setList need to make sure it doesnt call this again which calls setList in an infinite loop"
    }*/

}