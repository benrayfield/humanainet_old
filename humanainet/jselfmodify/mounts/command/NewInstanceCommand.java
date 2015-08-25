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
import java.lang.reflect.*;

import humanainet.jselfmodify.*;
import humanainet.jselfmodify.mounts.AbstractMount;
import humanainet.jselfmodify.util.StringUtil;

/** All JSelfModify commands are done by a GET command on them,
where the subpaths are the parameters, usually an escaped JSelfModify path.
In this case, the first few parameters are Java packages and a Java class name.
After that, if anything, are escaped JSelfModify paths that evaluate
to the parameters of the constructor of that Java class.
<br><br>
Example:
/new/jselfmodify/LocalDir/"/new/java.io.File/\"/new/string/localFilePath\""
Or the same command could be done using a variable:
/=/"/temp/theFile"/"/new/java.io.File/\"/new/string/localFilePath\""
/new/jselfmodify/LocalDir/"/temp/theFile"
If you use variables like that, be careful to use unique names because
any other plugin or code in the system uses the same namespace.
To get around that problem, maybe I could use the same solution
that Apache Ant uses, which is their "local" tag which makes
a certain name a local variable even if it is already a global variable.
*/
public class NewInstanceCommand extends AbstractMount{
	
	public Object get(User u, String pathParts[]) throws Exception{
		//TODO merge duplicate code. Copied some of this from JavaFuncCommand.
		String className = pathParts[0];
		Class c = JSelfModify.loader.loadClass(className);
		Object param[] = new Object[pathParts.length-1];
		for(int i=0; i<param.length; i++){
			param[i] = JSelfModify.root.get(u, pathParts[i+1]); //pathParts[i+1] is an escaped path
		}
		Class paramTypes[] = new Class[param.length];
		for(int i=0; i<param.length; i++){
			Class cl = param[i].getClass();
			//TODO optimize and combine duplicate code. This code is in JavaFuncCommand and NewInstanceCommand
			if(cl == Boolean.class) paramTypes[i] = boolean.class;
			else if(cl == Byte.class) paramTypes[i] = byte.class;
			else if(cl == Character.class) paramTypes[i] = char.class;
			else if(cl == Short.class) paramTypes[i] = short.class;
			else if(cl == Integer.class) paramTypes[i] = int.class;
			else if(cl == Long.class) paramTypes[i] = long.class;
			else if(cl == Float.class) paramTypes[i] = float.class;
			else if(cl == Double.class) paramTypes[i] = double.class;
			else paramTypes[i] = cl;
		}
		Constructor con = c.getConstructor(paramTypes);
		return con.newInstance(param);
	}
	
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		return StringUtil.EMPTYSQUARED;
	}
	
}
