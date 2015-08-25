/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.console;

/** Stateful. All Console does is take each string command and give
string response. List of previous commands may be stored by caller.
The display of the in and out strings are also for caller to do. 
*/
public interface Console{
	
	public String command(String in);

}