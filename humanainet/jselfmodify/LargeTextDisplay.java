/** Ben F Rayfield offers JSelfModify under Apache 2, GNU LGPL, and/or Classpath Exception.
Open-source licenses are best understood with set-theory: Subsets, supersets,
and intersections of the legal permissions each license grants.
Example: If JSelfModify is combined with only the parts of HumanAINet that allow
GNU GPL 2 and 3 simultaneously, then JSelfModify can simultaneously be multilicensed
GNU GPL 2 and 3, or if JSelfModify is combined only with Apache HttpCore (Apache 2 license),
then it can be licensed as Apache 2 and GPL 3 because GPL 3 is a subset of Apache 2.
JSelfModify is the "glue code" for the set of softwares called Human AI Net. Together,
their intersection is GNU GPL 3, but smaller subsets may have bigger license intersections.
Most parts of Human AI Net allow GNU GPL version 2 and higher if used in such a subset.
JSelfModify is more general than Human AI Net and is compatible with most licenses.
*/
package humanainet.jselfmodify;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** for log files or streams or other text viewing */
public class LargeTextDisplay extends JPanel implements Appendable{
	
	public final JPanel top = new JPanel();
	
	public final JTextArea text = new JTextArea();
	
	//TODO? public final List<String> lines = new ArrayList<String>();
	
	protected int chars;
	
	protected int maxChars;
	
	protected int removeCharsWhenPassMax;
	
	public LargeTextDisplay(){
		this(10000000, 10000);
	}
	
	public LargeTextDisplay(int maxChars, int removeCharsWhenPassMax){
		setLayout(new BorderLayout());
		add(top, BorderLayout.NORTH);
		top.add(new JLabel("LargeTextDisplay... TODO put buttons here"));
		add(text, BorderLayout.CENTER);
		setMinimumSize(new Dimension(100, 50));
	}

	public Appendable append(CharSequence c) throws IOException{
		text.append(c.toString());
		removeOldCharsIfTooBig();
		return this;
	}

	public Appendable append(CharSequence c, int start, int end) throws IOException{
		append(c.subSequence(start, end));
		return this;
	}

	public Appendable append(char c) throws IOException{
		append(""+c);
		return this;
	}
	
	protected void removeOldCharsIfTooBig(){
		if(chars > maxChars){
			String newText = text.getText().substring(removeCharsWhenPassMax);
			text.setText(newText);
			chars = newText.length();
		}
	}

}
