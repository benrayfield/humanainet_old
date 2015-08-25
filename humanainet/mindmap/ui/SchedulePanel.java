/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.mindmap.ui;
import java.awt.BorderLayout;

import javax.swing.DropMode;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

/** I sometimes organize my todo list in a text file.
There is a line marking the borders between weeks for the weeks coming soon,
and farther ahead there are lines for borders between months and years.
<br><br>
Between these time markers, each line has these columns:
endTime,
hoursDone,
expectedHoursTotal,
description.
<br><br>
Instead of description, since this is merging with the HumanAiNet mindmap software,
it will be a mindmapItem and maybe a few kinds of statements about it like
it must end here or need to do some work on it today or whatever the context.
<br><br>
This means I need to extend VarMap to include properties,
so each lispPair in the system can be used in another lispPair with some new
type object (like those in MindmapUtil) that means p is a property of o (object),
and the kind of property is n (name). Thats normally written o.n = p.
These o, n, and p can be any lispPair in the system, normally text.
An example of these types would be "endTime", "hoursDone",
"expectedHoursTotal", and "mindmapItemName".
VarMap will have to be expanded to find these lispPairs representing properties.
<br><br>
There will be a button to add or subtract 1 hour (and maybe smaller amounts)
from each of the hours properties in each row.
<br><br>
There will be an option to choose baseTen or baseTwo for displaying hours,
including with decimal point. The buttons for adding and subtracting 1 hour
will be aligned with each baseTwo digit. Default it to baseTwo.
*/
public class SchedulePanel extends JPanel{
	
	protected final PrilistsColumn prilistsColumn;
	
	protected final JTable table;
	
	protected final DefaultTableModel tableModel;
	
	public SchedulePanel(){
		super(new BorderLayout());
		ColorUtil.setColors(this);
		prilistsColumn = new PrilistsColumn("schedPriCol", 5);
		String[] cols = {"when", "what about it", "name", "hours done", "expected hours total"};
		//whatAboutIt can be "doSome" or "end" and maybe a few others.
		tableModel = new DefaultTableModel(null, cols);
		table = new JTable(tableModel);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		ColorUtil.setColors(table);
		//table.setTransferHandler(new TableRowTransferHandler(table));
		tableModel.addRow(new Object[]{"2015-8-4-9p", "doSome", "something", "20", "23"});
		tableModel.addRow(new Object[]{"", "doSome", "something", "20", "23"});
		tableModel.addRow(new Object[]{"2015-8-20-9p", "end", "something", "20", "23"});
		
		add(prilistsColumn, BorderLayout.WEST);
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		JScrollPane scrollTable = new JScrollPane(table, v, h);
		ColorUtil.setColors(scrollTable);
		add(scrollTable, BorderLayout.CENTER);
	}

}