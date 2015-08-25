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
import java.lang.reflect.Method;

import humanainet.jselfmodify.*;
import humanainet.jselfmodify.mounts.AbstractMount;
import humanainet.jselfmodify.util.*;

/** Runs a Java function. First parameter is the package, class, and function name
separated by periods. All remaining parameters are escaped JSelfModify paths
to run as GET commands. If its an instance function instead of static,
then the second parameter returns the instance object.
All remaining parameters are the parameters of the function.
If multiple functions have the same quantity of parameters,
the types of the objects returned by the "escaped JSelfModify paths"
determine which Java function to call. That's how Beanshell does it,
but this is done using only a few lines of java.lang.reflect code instead.
<br><br>
Example that runs this code: java.lang.System.getProperty("java.class.path"):
/jcall/"java.lang.System.getProperty"/"/new/string/java.class.path"
<br><br>
Example that runs this code: jselfmodify.JSelfModify.root().toString():
/jcall/"java.lang.Object.toString"/"/jfuncall/\"jselfmodify.JSelfModify.root\""
*/
public class JavaFuncCommand extends AbstractMount{
	
	public final boolean instanceFuncs;
	
	public JavaFuncCommand(boolean instanceFuncs){
		this.instanceFuncs = instanceFuncs;
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) return this;
		String packageClassAndFunctionName = pathParts[0];
		String s[] = StringUtil.split(packageClassAndFunctionName, "\\.");
		String className = StringUtil.join(JSelfModify.copyAndRemoveLast(s), ".");
		Class c = JSelfModify.loader.loadClass(className);
		//If its a static function, there is 1 more parameter than if it was an instance function.
		//Which function can affect the types of the parameters,
		//but I do not know the types of the parameters until I know how many parameters.
		//I do not know how many parameters until I know if the function is static.
		//It's a cycle and I could end it by using 2 types of JavaFuncCommand,
		//1 for static and 1 for instance. TODO Think is there a simpler way...
		if(instanceFuncs){
			throw new Exception("TODO instance funcs");
		}else{ //static funcs
			Object param[] = new Object[pathParts.length-1];
			for(int i=0; i<param.length; i++){
				param[i] = JSelfModify.root.get(u, pathParts[i+1]); //pathParts[i+1] is an escaped path
			}
			String funcName = s[s.length-1];
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
			Method javaFunc = c.getMethod(funcName, paramTypes);
			return javaFunc.invoke(null, param);
		}
	}
	
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) return StringUtil.EMPTYSQUARED;
		throw new Exception("TODO Should I use WrapMount here (and maybe in get function too)?");
	}
	
}
