/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.console;

/** TODO replace any manually coded funcs with a map of string to func,
or even better use xorlisp.Acyc to represent those strings
so any bitstring can be a func. For now I'll allow manually coding some
of the funcs, and gradually integrate them into xorlisp and mindmap.
*/
public class HumanainetConsole implements Console{
	
	protected int commandsRun;

	public String command(String in){
		commandsRun++;
		return "You said "+in.trim()+" and now commandsRun="+commandsRun;
	}

}
