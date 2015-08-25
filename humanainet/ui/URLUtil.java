/** Ben F Rayfield offers this software under GNU GPL 2+ open source license(s) */
package humanainet.ui;
import static humanainet.common.CommonFuncs.*;
import javax.swing.JComponent;
import javax.swing.JLabel;
import humanainet.mindmap.ParseUtil;
import humanainet.mindmap.ui.ColorUtil;
//import jscreenpixels.dynarect.Dynarect;
//import jscreenpixels.javacomponents.ResizableStretchDynarect;
import humanainet.realtimeschedulerTodoThreadpool.Task;

/** URLs include things like javaclass://the.package.ClassName
which use the default constructor.
See NsNodeUtil.getFirstURLInDefOf(NsNode) for details.
In later versions, more kinds of URL will be handled,
maybe a web browser viewing http URLs,
and certainly sha256:len://2353465435...:len urls
which refer to things in humanainetBigdataSection
*/
public class URLUtil{
	
	/** To avoid the danger of allowing just any text to run code, wraps in a TaskPlayerUi which
	does not start until user sees class name and chooses to start it, so it is their responsibility
	to choose if they trust their local java class of that name and any system they may have
	consciously opted into which may download new code. 
	*/
	public static JComponent urlToJComponent(String url){
		if(!ParseUtil.isURL(url)) return new JLabel("Not a url: "+url);
		//return new JLabel("testing... URL is "+url);
		//return new JTextArea("testing... URL is "+url);
		String tokens[] = url.split("://");
		String type = tokens[0]; //Examples: http, javaclass
		if(type.equals("javaclass")){
			String className = tokens[1];
			try{
				Class c = Class.forName(className);
				if(JComponent.class.isAssignableFrom(c)){
					log("File is running code as JComponent, using constructor of javaclass "+c.getName());
					//return (JComponent) c.newInstance();
					return new TaskPlayerUi(c);
				}//else if(Dynarect.class.isAssignableFrom(c)){
				//	log("File is running code as Dynarect, using constructor of javaclass "+c.getName());
				//	return new ResizableStretchDynarect((Dynarect)c.newInstance());
				//}
				return label("<html>javaclass "+className+" is not a "+JComponent.class.getName()/*+" or "+Dynarect.class.getName()*/+"</html>");
			}catch(Exception e){
				return label("<html>Not a javaclass or could not create one: "+e+" error="+e.getMessage()+"</html>");
			}
		}
		return label("<html>Dont know what to do with URL "+url+"<br>Try javaclass://the.package.ClassName of some subclass of "+JComponent.class.getName()+" and if you need it to run on an interval it can implement "+Task.class.getName()+" and will automatically be started and stopped.</html>");
	}
	
	/** Same color as PrilistsPanel so you can see it */
	public static JLabel label(String text){
		JLabel j = new JLabel(text);
		j.setBackground(ColorUtil.background);
		j.setForeground(ColorUtil.foreground);
		return j;
	}
	
	//public static <T> T newInstance(Class<>)
	

}
