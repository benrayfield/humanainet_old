/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap.ui;
import static humanainet.common.CommonFuncs.*;
//import humanainet.LastTouchedWhen;
//import humanainet.SecurityOptions;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicListUI.ListSelectionHandler;

import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.common.Text;
import humanainet.mindmap.MindmapUtil;
import humanainet.mindmap.ParseUtil;
import humanainet.ui.URLUtil;
import humanainet.xob.XobTypes;
import humanainet.xob.XobUtil;

/** TODO rewrite the text below which is changing to have any small number
of EditPrilist in a column, at least 3. Normally 3, 4, or 5.
<br><br>
3 lists on screen above eachother:
prilists (like prilistWd, prilistQn, prilistXp, prilistGl),
contents of selected prilist,
edges of item selected in that prilist
(which is also a prilist as edges are represented that way).
<br><br>
OLD TEXT TODO REWRITE:
A small JList at top to choose the prilist, and a big JList below it for its items.
Both are scrollable.
*/
public class PrilistsColumn extends JPanel /*implements NsNodeListener, LastTouchedWhen*/{
	
	//"Disable EditPrilist when nothing selected above"
	
	//TODO Use 3 EditPrilist instead of directly 3 JList
	
	//public final JList prilists, items, edges;
	
	//public final EditPrilist prilists, items, edges;
	
	//protected int prilistsOnScreen;
	
	//"TODO use Glo.events"
	
	public static final int MIN_PRILISTS_PER_COLUMN = 1;
	public static final int MAX_PRILISTS_PER_COLUMN = 12;

	public final JButton btnLessPrilists, btnMorePrilists;
	
	protected final List<EditPrilist> editprilists;
	
	/** If not null, data is sent to other parts of the PrilistsPanel when events happen,
	including defs of selected uniqName and loading edges of uniqName in opposite side.
	*/
	public PrilistsPanel optionalPrilistsPanel;
	
	public static final int MIN_PRILISTS = 3, MAX_PRILISTS = 16;
	
	protected final JPanel prilistsPanel;
	
	/** For debugging, each EditPrilist's name contains this name */
	public final String name;
	
	/** If firstPrilistRequiresPrefix, only items starting with "prilist" can be in the top list,
	like prilistGoal is a prioritized list of goals. Else you'd just call it goal.
	You might also prefix it with your name or whatever.
	*/
	public PrilistsColumn(String prilistColumnName, int howManyPrilists){
		name = prilistColumnName;
		//prilistsOnScreen = howManyPrilists;
		if(howManyPrilists < MIN_PRILISTS || howManyPrilists > MAX_PRILISTS)
			throw new RuntimeException("howManyPrilists="+howManyPrilists
			+"but must range "+MIN_PRILISTS+" to "+MAX_PRILISTS);
		
		setLayout(new BorderLayout());
		
		prilistsPanel = new JPanel();
		ColorUtil.setColors(prilistsPanel);
		prilistsPanel.setLayout(new GridLayout(0,1));
		editprilists = new ArrayList<EditPrilist>();
		for(int p=0; p<howManyPrilists; p++){
		//for(int p=0; p<MAX_PRILISTS; p++){
			String epName = prilistColumnName+p+"_"+(EditPrilist.howManyEditPrilistsCreated++);
			EditPrilist ep = new EditPrilist(epName, ColorUtil.background, ColorUtil.foreground);
			if(p == 0){
				//TODO ep.setParentOrNull(Namespace.nodeFromUniqName(MindmapUtil.listOfPrilistsName,true));
				int name = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, MindmapUtil.listOfPrilistsName);
				int typedName = Acyc32Util.wrapObjectInType(Glo.econacyc, XobTypes.typeToken, name);
				ep.setParentOrNil(typedName);
			}
			if(0 < p) editprilists.get(p-1).setNext(ep);
			editprilists.add(ep);
			ep.jlist.setTransferHandler(transferHandler);
			ep.jlist.addListSelectionListener(listSelectListener);
			if(p < howManyPrilists){
				//To solve a problem with events (I'm confused why it happens, TODO look into it later),
				//I'm preallocating and hooking together the max EditPrilists and add/remove
				//them from screen when needed.
				prilistsPanel.add(ep);
			}
		}
		
		add(prilistsPanel, BorderLayout.CENTER);
		
		JPanel south = new JPanel(new GridLayout(1,0));
		btnLessPrilists = new JButton(new AbstractAction("-"){
			public void actionPerformed(ActionEvent e){
				setNumberOfPrilists(howManyPrilists()-1);
			}
		});
		ColorUtil.setColors(btnLessPrilists);
		south.add(btnLessPrilists);
		btnMorePrilists = new JButton(new AbstractAction("+"){
			public void actionPerformed(ActionEvent e){
				setNumberOfPrilists(howManyPrilists()+1);
			}
		});
		ColorUtil.setColors(btnMorePrilists);
		south.add(btnMorePrilists);
		
		add(south, BorderLayout.SOUTH);
	}
	
	public int howManyPrilists(){
		//return prilistsOnScreen;
		return editprilists.size();
	}
	
	/** Range - to howManyPrilists()-1 */
	public EditPrilist getEditPrilist(int index){
		return editprilists.get(index);
	}
	
	/** Truncates parameter into range MIN_PRILISTS_PER_COLUMN to MAX_PRILISTS_PER_COLUMN, so no error */
	public void setNumberOfPrilists(int quantity){
		synchronized(getTreeLock()){
			quantity = Math.max(MIN_PRILISTS_PER_COLUMN, Math.min(quantity, MAX_PRILISTS_PER_COLUMN));
			if(quantity == howManyPrilists()) return;
			int siz;
			while(quantity < (siz=howManyPrilists())){
				//prilistsPanel.remove(editprilists.get(siz-1));
				EditPrilist p = editprilists.remove(siz-1);
				prilistsPanel.remove(p);
				//disconnect parent from next prilist removed
				if(1 < siz){
					EditPrilist parent = editprilists.get(siz-2);
					parent.setNext(null);
				}
			}
			while(quantity > (siz=howManyPrilists())){
				//prilistsPanel.add(editprilists.get(siz));
				String epName = name+siz+"_"+(EditPrilist.howManyEditPrilistsCreated++);
				EditPrilist p = new EditPrilist(epName, ColorUtil.background, ColorUtil.foreground);
				editprilists.add(p);
				prilistsPanel.add(p);
				p.jlist.setTransferHandler(transferHandler);
				p.jlist.addListSelectionListener(listSelectListener);
				EditPrilist parent = editprilists.get(siz-1);
				parent.setNext(p);
				String selectedString = parent.selected();
				if(MindmapUtil.otherOrEmptyName.equals(selectedString)){
					p.setThisEditPrilistEnabled(false);
				}else{
					int mindmapItemName = XobUtil.tokenGlobal(selectedString);
					p.setParentOrNil(mindmapItemName);
				}
			}
			prilistsPanel.validate();
		}
	}
	
	protected final ListSelectionListener listSelectListener = new ListSelectionListener(){
		public void valueChanged(ListSelectionEvent e){
			synchronized(getTreeLock()){
				if(e.getValueIsAdjusting()) return;
				Object source = e.getSource();
				int indexOfPrilist = -1;
				for(int i=0; i<editprilists.size(); i++){
					if(source == editprilists.get(i).jlist) indexOfPrilist = i;
				}
				if(indexOfPrilist != -1){
					JList jlist = editprilists.get(indexOfPrilist).jlist;
					int i = jlist.getSelectedIndex();
					if(i != -1){
						EditPrilist epClicked = editprilists.get(indexOfPrilist);
						String selected = MindmapUtil.afterLastSpace(epClicked.listModel.get(i).toString());
						if(!MindmapUtil.otherOrEmptyName.equals(selected)){
							System.out.println("PrilistColumn saw selected="+selected);
							int ptrSelected = XobUtil.tokenGlobal(selected);
							loadNameAndDef(ptrSelected);
						}
					}

					/*
					if(i != -1 && indexOfPrilist+1 < editprilists.size()){
						
						EditPrilist epClicked = editprilists.get(indexOfPrilist);
						EditPrilist epNext = editprilists.get(indexOfPrilist+1);
						epNext.setEnabled(true);
						NsNode selected = Namespace.nodeFromUniqName(epClicked.listModel.get(i),true);
						epNext.setParentOrNull(selected);
						epNext = epNext.nextOrNull;
						while(epNext != null){
							epNext.setEnabled(false);
							System.out.println("Did setEnabled(false) on "+epNext);
							epNext = epNext.nextOrNull;
						}
					}
					if(i != -1){
						ListModel model = jlist.getModel();
						if(model instanceof DefaultListModel){
							final NsNode selected = Namespace.nodeFromUniqName(
								((DefaultListModel)model).get(i),true);
							boolean isRoot = MindMapUtil.listOfPrilistsName.equals(selected.uniqName);
							if(!isRoot) SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									loadNameAndDef(selected);
								}
							});
						}
					}
					*/
				}
			}
		}
	};
	
	/** Java 1.6 uses Object[] getSelectedValues which is Deprecated but still exists in Java 1.7
	which  uses JList.getSelectedValuesList(JList). To stay 1.6 compatible, use reflection to use either.
	*
	public static List getSelectedValuesList(JList jlist){
		List list;
		if(methodJListGetSelectedValuesList != null){
			try{
				list = (List) methodJListGetSelectedValuesList.invoke(jlist);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}else{
			list = Arrays.asList(jlist.getSelectedValues());
		}
		return Collections.unmodifiableList(list);
	}
	public static final Method methodJListGetSelectedValuesList;
	static{
		Method m = null;
		try{
			m = JList.class.getMethod("getSelectedValuesList");
		}catch(NoSuchMethodException e){}
		methodJListGetSelectedValuesList = m;
	}*/
	
	public static final TransferHandler transferHandler = new TransferHandler(){
		public boolean canImport(TransferHandler.TransferSupport info){
			return info.isDataFlavorSupported(DataFlavor.stringFlavor)
				| info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
		public int getSourceActions(JComponent c){
			return COPY_OR_MOVE;
		}
		protected Transferable createTransferable(JComponent c){
			if(c instanceof JList){
				//To stay compatible with Java 1.6 but use Java 1.7 function instead of deprecated if exists:
				//Actually I want MouseWheelEvent.getPreciseWheelRotation so I'll go with 1.7.
				//List selected = getSelectedValuesList((JList)c);
				List selected = ((JList)c).getSelectedValuesList();
				if(selected.size() > 0) return new StringSelection(selected.get(0).toString());
			}
			return new StringSelection(c.toString());
		}
		public boolean importData(TransferHandler.TransferSupport info){
			if(!info.isDrop()) return false;
			if(!info.isDataFlavorSupported(DataFlavor.stringFlavor)){
				if(info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
					Transferable t = info.getTransferable();
					try{
						Object filesList = t.getTransferData(DataFlavor.javaFileListFlavor);
						//TODO HumanainetFileUtil.filesDraggedIn(info.getComponent(), filesList);
						return true;
					}catch(Exception e){
						throw new RuntimeException(e);
					}
				}
				return false;
			}
			DropLocation dl = info.getDropLocation();
			Component target = info.getComponent();
			if(!(target instanceof JList)) return false;
			JList targetJList = (JList) target;
			DefaultListModel listModel = (DefaultListModel) targetJList.getModel();
			if(dl instanceof JList.DropLocation){
				
				JList.DropLocation jdl = (JList.DropLocation)dl;
				if(jdl.isInsert()){
					int insertIndex = ((JList.DropLocation)dl).getIndex();
					//if(info.getComponent() is this vs the other JList)
					Transferable t = info.getTransferable();
					String data;
					try{
						data = (String) t.getTransferData(DataFlavor.stringFlavor);
						data = MindmapUtil.afterLastSpace(data); //in case it has EditPrilist.prefix(String)
						if(!MindmapUtil.otherOrEmptyName.equals(data)){
							EditPrilist ep = getEditPrilistOf(targetJList);
							//System.out.println("Dragged "+data+" to index "+insertIndex+" of "+ep);
							//Selected is the item being dragged if dragging into this same EditPrilist,
							//or if its from a different list then the item may or may not be in this EditPrilist.
							String selected = ep.selected(); //what will be selected after the dragAndDrop
							List<String> list = EditPrilist.removePrefixs(EditPrilist.removeOtherEmpty(ep.getDisplayedList()));
							list = new ArrayList(list); //mutable
							//System.out.println("Old list:  "+list+" insertIndex="+insertIndex);
							int duplicateAt = list.indexOf(data);
							list.remove(data);
							if(duplicateAt != -1 && duplicateAt < insertIndex){
								insertIndex--;
							}
							//System.out.println("Changing: "+list+" insertIndex="+insertIndex);
							if(list.size() < insertIndex) insertIndex = list.size(); //past MindmapUtil.otherOrEmptyName on screen
							list.add(insertIndex, data);
							list = Collections.unmodifiableList(list);
							System.out.println("New list: "+list);
							//Change the list on screen (which may be in multiple places) through events system
							int ptrName = ep.ptrMindmapItemName();
							if(ptrName != 0){
								//0? if drag into EditPrilist which is editing nil (no nonempty name selected above), do nothing.
								int ptrOldMindmapItem = Glo.events.get(ptrName);
								int ptrNewPrilist = MindmapUtil.typedListPow2OfTokenGlobal(list);
								int ptrOldDef = MindmapUtil.mindmapItemDef(Glo.econacyc, ptrOldMindmapItem);
								int ptrNewMindmapItem = MindmapUtil.mindmapItemGlobal(ptrName, ptrNewPrilist, ptrOldDef);
								Glo.events.set(ptrName, ptrNewMindmapItem);
								ep.jlist.setSelectedValue(data, true); //select, scroll, and open contents in next lower EditPrilist if exists
								
								/*int ptrPrilist, ptrDef;
								int oldMindmapItem = Glo.events.get(ptrName);
								if(oldMindmapItem == 0){
									//May be a new string dragged in,
									//or it may be an error but I dont know how to tell the difference from here (FIXME?). 
									ptrPrilist = MindmapUtil.emptyPrilist;
									ptrDef = MindmapUtil.emptyDef;
								}else if(!MindmapUtil.isMindmapItem(Glo.econacyc, oldMindmapItem)){
									throw new RuntimeException("TODO how to handle values that arent mindmapItem? value="+oldMindmapItem);
								}else{
									ptrPrilist = MindmapUtil.mindmapItemPrilist(acyc, ptrMindmapItem)
								}
								int 
								int ptrNewPrilist = MindmapUtil.typedListPow2OfTokenGlobal(list);
								int ptr
								*/
							}
							
							
							/*
							List list = new ArrayList();
							for(int i=0; i<listModel.size(); i++) list.add(listModel.elementAt(i));
							int oldIndex = list.indexOf(data);
							if(oldIndex != -1){
								list.remove(oldIndex);
								//Where to insert is a moving target when its already in the list
								if(oldIndex < insertIndex) insertIndex--;
							}
							list.add(insertIndex, data);
							Listaddr newList = new SimpleListaddr(list);
							EditPrilist ep = getEditPrilistOf(targetJList);
							ep.setListaddr(newList);
							//PrilistItemEdge pie = getPrilistItemEdge(ep);
							//if(pie.items == ep) pie.loadNameAndDef(Namespace.nodeFromUniqName(data,true));
							ep.jlist.setSelectedValue(data,true);
							
							//TODO still more event stuff to do here?
							*/
						}
						
					}catch(Exception e){
						logToUser("DnD transferable="+t+"\r\n"+e.getMessage());
						e.printStackTrace(); //TODO use JSelfModify logging
					}
				}
				
			}
			return false;
		}
	};
	
	/** If this PrilistsColumn knows about a PrilistsPanel, calls its loadNameAndDef.
	A PrilistsColumn can be used in other parts of the software that dont have a PrilistsPanel.
	*/
	protected void loadNameAndDef(int nameInVarMap){
		final PrilistsPanel prilistsPanel = optionalPrilistsPanel;
		if(prilistsPanel != null) prilistsPanel.loadNameAndDef(nameInVarMap);
	}
	
	public static PrilistsColumn getPrilistItemEdge(EditPrilist ep){
		Component c = ep;
		while(!(c instanceof PrilistsColumn)){
			if(c.getParent() instanceof Frame) throw new RuntimeException("Not inside a PrilistItemEdge: "+ep);
			c = c.getParent();
		}
		return (PrilistsColumn) c;
	}
	
	public static EditPrilist getEditPrilistOf(JList jlist){
		Component c = jlist;
		while(!(c instanceof EditPrilist)){
			if(c.getParent() instanceof Frame) throw new RuntimeException("Not inside an EditPrilist: "+jlist);
			c = c.getParent();
		}
		return (EditPrilist) c;
	}
	
	/*public void onCreate(MutScalar created){
		for(EditPrilist ep : editprilists){
			ep.onCreate(created);
		}
	}
	
	public void onDelete(MutScalar deleting){
		for(EditPrilist ep : editprilists){
			ep.onDelete(deleting);
		}
	}
	
	public double lastTouchedWhen(){
		double t = -Double.MAX_VALUE;
		for(EditPrilist p : editprilists){
			t = Math.max(t, p.lastTouchedWhen);
		}
		return t;
	}*/

}