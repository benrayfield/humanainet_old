/** Ben F Rayfield offers xorlisp (and this ui package) opensource GNU LGPL */
package humanainet.xorlispui;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.EconDAcycI;
import humanainet.mindmap.MindmapUtil;

public class EconDAcycIUi extends JTable{
	
	//TODO only display keyVal and end counting for econacyc at them even though eventStream points at them,
	//but what if it gets tangled with keyVal pointing at eachother andOr at older versions of eventStreams
	//or at multiple eventStreams? This is going to take some strategic designing to accurately show the user
	//and allow to be editable (for garbageCollection of econacyc purposes) the contents of the econacyc,
	//while continuing to have the versioning ability. For now I'm going to move on to creating
	//a version of acyc that appends to file and hooking econacyc into that, or at least loading from it
	//and appending when save.
	
	/** the mutable data this ui edits */
	public final EconDAcycI econacyc;
	
	public EconDAcycIUi(EconDAcycI econacyc){
		super(newTableModel());
		this.econacyc = econacyc;
		refresh();
	}
	
	//TODO Should maxPay be per pointer or total? What about the costs?
	//I'll have to see how this works out as I use the system.
	//I dont know what columns I'll find useful.
	
	public static String incomingInsidePointers = "incoming pointers";
	
	public static String costTotal = "cost total (lispPairs)";
	
	/** As it is now, only inside pointers share the cost, not pointers from outside, because they're hard to track */
	public static String costPerInsidePointer = "cost per pointer (lispPairs)";
	
	/** max willing to pay to prevent deltion of object, in case its currentCost increases
	when other objects that were sharing the cost are deleted.
	*/
	public static String maxPayPerInsidePointer = "max pay per pointer (lispPairs)";
	
	/** currentCost-maxPay */
	public static String needMore = "need more per pointer (lispPairs)";
	
	/** lispPairs available to pay may be less than total maxPay per object,
	so only those in order of smaller scalar value of priority will be saved from deletion first.
	*/
	public static String priority = "priority";
	
	/** name in mindmap. Every lispPair can have a name. First, if it translates to UTF16 text
	thats all visible chars and no whitespace, thats its name. Else its name is a HumanAINet kind
	of base64 (using chars _ and $ and in ascending order of ASCII value aligned to digits)
	view of the SHA256 output of that way of hashConsing pairs of other SHA256 outputs,
	with the middle 9 bytes of those SHA256s xored together to get a single SHA256 cycle.
	*/
	public static String objectName = "object name";
	
	public static String actionsAvailable = "actions available";
	
	protected Comparator<Map> compareRows = new Comparator<Map>(){
		public int compare(Map x, Map y){
			throw new RuntimeException("Compare by costTotal. Also, have a comparator for each column");
		}
	};
	
	
	protected static DefaultTableModel newTableModel(){
		int howManyRows = 0;
		DefaultTableModel d = new DefaultTableModel(
			new Object[]{ incomingInsidePointers, costTotal, costPerInsidePointer, maxPayPerInsidePointer, needMore, priority, objectName, actionsAvailable },
			howManyRows
		);
		return d;
	}
	
	public void refresh(){
		DefaultTableModel t = (DefaultTableModel) getModel();
		for(int i=t.getRowCount()-1; i>=0; i--){
			t.removeRow(i);
		}
		
		Set<Map> rowsSet = newRows();
		Map rows[] = rowsSet.toArray(new Map[0]);
		//TODO Arrays.sort(rows, compareRows), and option to sort by any column
		t.addRow(new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"});
		for(Map m : rows){
			t.addRow(newRowData(m));
		}
		t.fireTableDataChanged();
		invalidate();
	}
	
	public Set<Map> newRows(){
		econacyc.updateCosts();
		Set<Map> set = new HashSet();
		for(int ptr=0; ptr<econacyc.size(); ptr++){
			//TODO filter by how many incoming pointers? Custom filters can type into the window would be good, that takes an int as param and returns true if row should be included.
			Map m = new HashMap();
			int insidePointers = econacyc.howManyRefs(ptr);
			m.put(incomingInsidePointers, insidePointers);
			double cost = econacyc.objectCost(ptr);
			m.put(costTotal, cost);
			m.put(costPerInsidePointer, cost/insidePointers);
			m.put(maxPayPerInsidePointer, "TODO");
			m.put(needMore, "TODO");
			m.put(priority, "TODO");
			if(MindmapUtil.isMindmapItem(econacyc, ptr)){
				m.put(objectName, "$$"+MindmapUtil.mindmapItemGetNameString(Glo.cacheacyc, ptr));
			}else{
				continue;
				//m.put(objectName, "ptr"+ptr);
			}
			m.put(actionsAvailable, econacyc.hasRef(ptr)?"":"delete");
			set.add(m);
		}
		return Collections.unmodifiableSet(set);
	}
	
	public Object[] newRowData(Map m){
		return new Object[]{
			m.get(incomingInsidePointers),
			m.get(costTotal),
			m.get(costPerInsidePointer),
			m.get(maxPayPerInsidePointer),
			m.get(needMore),
			m.get(priority),
			m.get(objectName),
			m.get(actionsAvailable)
		};
	}

}

