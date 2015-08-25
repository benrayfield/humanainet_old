/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.ui;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import humanainet.console.Console;

public class ConsolePanel extends JTextArea implements DocumentListener, KeyListener{
	
	//TODO when up or down button is pressed, go through earlier command sand replace writingNow
	
	//TODO dont allow the backspacing of commandPrefix
	
	//TODO save writingNow in a var when start looking through earlier commands, and bring it back when you come back to the end, as long as you havent typed anything other than arrow keys
	
	public final List<String> earlierCommands = new ArrayList();
	
	public final List<String> earlierResponses = new ArrayList();
	
	/** If its a valid index in earlierCommands, it refers to that.
	If it equals earlierCommands.size(), then writing a new command.
	*/
	protected int whichCommand;
	
	public String writingNow = "";
	
	protected String commandPrefix = "> ", responsePrefix = "< ", n = "\r\n";
	
	public final Console console;
	
	public ConsolePanel(Console console){
		this.console = console;
		setFont(new Font("Monospaced", Font.PLAIN, 12));
		getDocument().addDocumentListener(this);
		addKeyListener(this);
		setLineWrap(true);
		setWrapStyleWord(true);
		updateText();
	}
	
	protected void updateText(){
		//TODO remember where cursor is
		StringBuilder sb = new StringBuilder();
		if(earlierCommands.size() != earlierResponses.size()) throw new RuntimeException(
			earlierCommands.size()+" == earlierCommands.size() != earlierResponses.size() == "+earlierResponses.size());
		for(int i=0; i<earlierCommands.size(); i++){
			sb.append(commandPrefix).append(earlierCommands.get(i)).append(n);
			sb.append(responsePrefix).append(earlierResponses.get(i)).append(n);
		}
		sb.append(commandPrefix).append(writingNow);
		setText(sb.toString());
	}

	public void insertUpdate(DocumentEvent e){ onChangeText(); }
	public void removeUpdate(DocumentEvent e){ onChangeText(); }
	public void changedUpdate(DocumentEvent e){}
	
	protected void onChangeText(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				String s = n+getText();
				String c = n+commandPrefix;
				int i = s.lastIndexOf(c);
				if(i == -1){
					throw new RuntimeException(
						"Couldnt find newline then "+commandPrefix+" at end of text");
				}else{
					i += c.length();
					String commandBeingTyped = s.substring(i);
					if(commandBeingTyped.endsWith("\r") || commandBeingTyped.endsWith("\n")){
						//run command
						commandBeingTyped = commandBeingTyped.trim();
						earlierCommands.add(commandBeingTyped);
						String response = console.command(commandBeingTyped);
						response = response.trim();
						earlierResponses.add(response);
						whichCommand = earlierCommands.size();
						writingNow = "";
						updateText();
					}
				}
			}
		});
	}

	public void keyTyped(KeyEvent e){}

	public void keyPressed(KeyEvent e){
		if(e.getKeyCode() == KeyEvent.VK_UP) up();
		else if(e.getKeyCode() == KeyEvent.VK_DOWN) down();
	}

	public void keyReleased(KeyEvent e){}
	
	public void up(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				whichCommand--;
				if(whichCommand < 0) whichCommand = 0;
				if(whichCommand < earlierCommands.size()){
					writingNow = earlierCommands.get(whichCommand);
				}
				updateText();
			}
		});
	}
	
	public void down(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				whichCommand++;
				if(earlierCommands.size() < whichCommand){
					whichCommand = earlierCommands.size();
				}
				if(whichCommand < earlierCommands.size()){
					writingNow = earlierCommands.get(whichCommand);
				}else{
					writingNow = "";
				}
				updateText();
			}
		});
	}

}
