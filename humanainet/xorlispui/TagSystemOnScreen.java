package humanainet.xorlispui;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import humanainet.common.Text;
import humanainet.mindmap.ui.ColorUtil;
import humanainet.realtimeschedulerTodoThreadpool.Task;
import humanainet.xorlisp.ptr32.TagSys32;

/** TuringCompleteness and deriving lisp from a few small operators, on screen,
using a queue automata that uses 1 word and skips the next and repeats alternating,
and for each word not skipped, appends its definition (a list of words) to end of queue
which eventually arrive the the other end of queue where we do it all again, and so on.
Each item has its own namespace, a map of word to list of words.
To start, that will be 1 global map, but in later versions when its all done
with acyc, those maps will be mostly reused acyc nodes only updating log number
of nodes and using some sorting of the keys (any acyc node, how are they sorted?).
<br><br>
The display is half the screen is each end of queue, so in the middle
we see each single word become 0 (or was it minimum of 1?) or more words.
The multiple words appended to end of queue together are shown each in their
own line, while words appear 1 word to a line as they are later seen at the
other end of queue where they are used. 
*/
public class TagSystemOnScreen extends JPanel implements Task{
	
	public final JTextArea namespaceUi, queueUi;
	
	//TODO public final TagSys32 tagSys;
	
	//For now just use strings and normal java classes, until I get tag system working and understand how to use it.
	
	private final SortedMap<String,List<String>> namespace = new TreeMap();
	
	//private final Queue<String> queue = new ArrayBlockingQueue(1<<20);
	private final List<String> history = new ArrayList();
	private int innerQueueSize = 0;
	
	private List<String> queue(){
		return history.subList(history.size()-innerQueueSize, history.size());
	}
	
	//TODO test cases of some of the church encoding of lambda, starting with T, F, and, or, not, etc.
	
	//TODO public TagSystemOnScreen(TagSys32 tagSys){
	public TagSystemOnScreen(){
		//this.tagSys = tagSys;
		/*setText("TODO one side is the queue, and other side displays key/value pairs, and maybe they should be editable but edit by putting the command into the queue. Its like stack and heap, actually stack and localNamespaceHeap and acyc is shared heap. ... TagSystem queue automata TODO - First implement lisp's T and F and a function to set key/value pairs in namespace (global namespace at first but later use acyc nodes to version them at log cost per change using sorted keys). T is lambda that takes 2 params and returns first. F is lambda that returns identity lambda (returns second). Function to put key/value pairs in namespace must happen at the older end of the queue where they are computed and add at their list at the new end, and the thing that needs changing in that design is to also interpret some short list (like 3 of them) of those nodes as setFuncName, setKey, and setValue. Use "+TagSys32.class);
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		ColorUtil.setColors(this);
		*/
		
		setLayout(new BorderLayout());
		
		JPanel main = new JPanel(new GridLayout(1,0));
		namespaceUi = new JTextArea();
		queueUi = new JTextArea();
		
		//int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED; 
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
		main.add(new JScrollPane(namespaceUi, v, h));
		main.add(queueUi);
		add(main, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton btnStart = new JButton(new AbstractAction("start"){
			public void actionPerformed(ActionEvent e){
				clear();
				
				/*queue("a");
				queue("a");
				queue("a");
				namespace("a", Arrays.asList("b", "c"));
				namespace("b", Arrays.asList("a"));
				namespace("c", Arrays.asList("a", "a", "a"));
				*/
				
				queue("start");
				queue(".");
				
				namespace("end", Collections.EMPTY_LIST); //want to get from start to here
				
				namespace("start", Arrays.asList("and", "T", "F"));
				
				namespace(".", Arrays.asList("."));
				
				namespace("and", Arrays.asList("whatsAnd?"));
				
				namespace("T", Arrays.asList("whatsTrue?"));
				namespace("F", Arrays.asList("whatsFalse?"));
				namespace("I", Arrays.asList("whatsIdentityFunc?"));
			}
		});
		buttonPanel.add(btnStart);
		JButton btnNext = new JButton(new AbstractAction("next"){
			public void actionPerformed(ActionEvent e){
				if(1 < innerQueueSize){
					String key = removeOldest();
					String ignore = removeOldest();
					List<String> value = namespace.get(key);
					if(value == null) throw new RuntimeException("Got null for key="+key);
					for(String v : value){
						queue(v);
					}
				}
			}
		});
		buttonPanel.add(btnNext);
		add(buttonPanel, BorderLayout.NORTH);
	}
	
	
	public void clear(){
		innerQueueSize = 0;
		history.clear();
		namespace.clear();
	}
	
	public String removeOldest(){
		if(innerQueueSize == 0) throw new RuntimeException("Queue is empty cant remove");
		String s = history.get(history.size()-innerQueueSize);
		innerQueueSize--;
		return s;
	}
	
	public void queue(String s){
		history.add(s);
		innerQueueSize++;
		updateQueueUi();
	}
	
	public void namespace(String key, List<String> value){
		namespace.put(key, value);
		updateNamespaceUi();
	}

	public void event(Object context){
		throw new RuntimeException("TODO");
	}

	public double preferredInterval(){
		return .5;
	}
	
	protected void updateQueueUi(){
		List<String> queue = queue();
		//TODO how to measure this? int maxLinesOnScreen = queueUi.getLineCount();
		int maxLinesOnScreen = 30;
		int maxLinesInQueuePlusOneBorderLine = innerQueueSize+1;
		int linesToDisplay = Math.min(maxLinesInQueuePlusOneBorderLine, maxLinesOnScreen);
		int nonborderLinesToDisplay = linesToDisplay-1;
		int maxNewEndLines = nonborderLinesToDisplay/2;
		int newEndLines = Math.min(maxNewEndLines, queue.size()/2);
		int maxOldEndLines = nonborderLinesToDisplay-maxNewEndLines;
		int oldEndLines = Math.min(maxOldEndLines, (queue.size()+1)/2);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		int startNearNewEnd = queue.size()-newEndLines;
		for(int i=0; i<newEndLines; i++){
			if(!first) sb.append(Text.n);
			int index = startNearNewEnd+i;
			String s = queue.get(index);
			sb.append(s);
			first = false;
		}
		sb.append(Text.n).append("-----------------------------");
		int oldestIndex = queue.size()-innerQueueSize;
		for(int i=0; i<oldEndLines; i++){
			int index = oldestIndex+i;
			String s = queue.get(index);
			sb.append(Text.n).append(s);
		}
		queueUi.setText(sb.toString());
	}
	
	protected void updateNamespaceUi(){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Map.Entry<String,List<String>> entry : namespace.entrySet()){
			String name = entry.getKey();
			List<String> value = entry.getValue();
			if(!first) sb.append(Text.n);
			sb.append(name+" = "+value);
			first = false;
		}
		namespaceUi.setText(sb.toString());
	}

}