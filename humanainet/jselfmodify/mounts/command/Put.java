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
package humanainet.jselfmodify.mounts.command;
import java.io.*;

import humanainet.jselfmodify.JSelfModify;
import humanainet.jselfmodify.User;
import humanainet.jselfmodify.mounts.AbstractMount;
import humanainet.jselfmodify.util.StringUtil;

/** The GET function does an action. It does more than GET.
It GETs from the escaped path at pathParts[1] and PUTs that Object to the escaped path at pathParts[0].
You can think of it this way: pathParts[0] = pathParts[1].
In all cases, the GET function returns pathParts[0] which is the destination.
If pathParts.length is 1, it does not modify that destination.
*/
public class Put extends AbstractMount{

	public boolean exist(User u, String pathParts[]) throws Exception{
		return pathParts.length == 0;
	}

	public Object get(User u, String pathParts[]) throws Exception{
		switch(pathParts.length){
			case 2:
				Object ob = JSelfModify.root.get(u, pathParts[1]);
				JSelfModify.root.put(u, pathParts[0], ob);
			case 1:
				return pathParts[0];
			case 0:
				throw new Exception("Empty path");
		}
		throw new Exception("Path has more than 2 parts. Can not PUT(from,to). path="+JSelfModify.joinPathParts(pathParts));
	}

	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		return StringUtil.EMPTYSQUARED;
	}

}
