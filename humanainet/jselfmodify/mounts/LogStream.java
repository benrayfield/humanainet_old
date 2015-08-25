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
package humanainet.jselfmodify.mounts;
import java.io.*;

import humanainet.jselfmodify.JSelfModify;
import humanainet.jselfmodify.User;
import humanainet.jselfmodify.util.StringUtil;

/** Call the GET function to add a string to the end of the log.
Example: MountHome.root.get("/log/\"testing the log. Print this to log.\"")
Or use the APPEND function.
Example: MountHome.root.append("/log", "\ntesting the log. Print this to log");
*/
public class LogStream extends AbstractMount{

	private Appendable p;
	
	/** Example Appendable: System.out */
	public LogStream(Appendable p){
		this.p = p;
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return pathParts.length == 0;
	}

	public Object get(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) throw new Exception("Empty path");
		if(pathParts.length > 1)  throw new Exception("Too many pathParts. Only 1 is allowed. path="+JSelfModify.joinPathParts(pathParts));
		p.append("\r\n").append(pathParts[0]);
		return pathParts[0];
	}
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		if(pathParts.length != 0) throw new Exception("Can only append to this log. Path must be empty but is: "+JSelfModify.joinPathParts(pathParts));
		p.append(pathParts[0]);
	}

	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		return StringUtil.EMPTYSQUARED;
	}

}
