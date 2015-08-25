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
import humanainet.jselfmodify.Mount;
import humanainet.jselfmodify.User;


public class ViewOfSubpath extends AbstractMount{
	
	private final Mount parent;
	private final String myPathInParent;
	
	public ViewOfSubpath(Mount parent, String myPathInParent){
		this.parent = parent;
		this.myPathInParent = myPathInParent;
	}

	public boolean exist(User u, String path) throws Exception{
		return parent.exist(u, path.length()==1 ? myPathInParent : myPathInParent+path);
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return exist(u, JSelfModify.joinPathParts(pathParts));
	}

	public Object get(User u, String path) throws Exception{
		return parent.get(u, path.length()==1 ? myPathInParent : myPathInParent+path);
	}

	public Object get(User u, String pathParts[]) throws Exception{
		return get(u, JSelfModify.joinPathParts(pathParts));
	}

	public InputStream getInStream(User u, String path, long from, long to) throws Exception{
		return parent.getInStream(u, path.length()==1 ? myPathInParent : myPathInParent+path, false, from, to);
	}

	public InputStream getInStream(User u, String pathParts[], long from, long to) throws Exception{
		return getInStream(u, JSelfModify.joinPathParts(pathParts), false, from, to);
	}
	
	public OutputStream getOutStream(User u, String path, long from, long to) throws Exception{
		return parent.getOutStream(u, path.length()==1 ? myPathInParent : myPathInParent+path, false, from, to);
	}

	public OutputStream getOutStream(User u, String pathParts[], long from, long to) throws Exception{
		return getOutStream(u, JSelfModify.joinPathParts(pathParts), false, from, to);
	}

	public void put(User u, String path, Object value) throws Exception{
		parent.put(u, path.length()==1 ? myPathInParent : myPathInParent+path, value);
	}

	public void put(User u, String pathParts[], Object value) throws Exception{
		put(u, JSelfModify.joinPathParts(pathParts), value);
	}
	
	public Mount dir(User u, String path) throws Exception{
		return parent.dir(u, path.length()==1 ? myPathInParent : myPathInParent+path);
	}

	public Mount dir(User u, String pathParts[]) throws Exception{
		return dir(u, JSelfModify.joinPathParts(pathParts));
	}

	public void append(User u, String path, Object valueToAppend) throws Exception{
		parent.append(u, path.length()==1 ? myPathInParent : myPathInParent+path, valueToAppend);
	}

	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		append(u, JSelfModify.joinPathParts(pathParts), valueToAppend);
	}

	public void delete(User u, String path) throws Exception{
		parent.delete(u, path.length()==1 ? myPathInParent : myPathInParent+path);
	}

	public void delete(User u, String pathParts[]) throws Exception{
		delete(u, JSelfModify.joinPathParts(pathParts));
	}

	public String[] listEachAsString(User u, String path) throws Exception{
		String paths[] = parent.listEachAsString(u, path.length()==1 ? myPathInParent : myPathInParent+path);
		int charsToRemove = myPathInParent.length();
		for(int i=0; i<paths.length; i++){
			paths[i] = paths[i].substring(charsToRemove);
		}
		return paths;
	}

	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		//return listEachAsString(u, JSelfModify.joinPathParts(pathParts));
		throw new Exception("TODO after redesigning list functions");
	}
	
	public String toString(){
		return "[Subpath "+myPathInParent+" in "+parent+"]";
	}

}
