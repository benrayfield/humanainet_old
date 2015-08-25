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

public abstract class AbstractMount implements Mount{
	
	public boolean exist(User u, String path) throws Exception{
		return exist(u, JSelfModify.cachedParsePath(path));
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}

	public Object get(User u, String path) throws Exception{
		return get(u, JSelfModify.cachedParsePath(path));
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public InputStream getInStream(User u, String path, boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		return getInStream(u, JSelfModify.cachedParsePath(path), bigEndian, bitIndexFrom, bitIndexTo);
	}
	
	public InputStream getInStream(User u, String pathParts[], boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public OutputStream getOutStream(User u, String path, boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		return getOutStream(u, JSelfModify.cachedParsePath(path), bigEndian, bitIndexFrom, bitIndexTo);
	}
	
	public OutputStream getOutStream(User u, String pathParts[], boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public long bitSize(User u, String path) throws Exception{
		return bitSize(u, JSelfModify.cachedParsePath(path));
	}

	public long bitSize(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public void put(User u, String path, Object value) throws Exception{
		put(u, JSelfModify.cachedParsePath(path), value);
	}
	
	public void put(User u, String pathParts[], Object value) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public void move(User u, String path, String newPath) throws Exception{
		move(u, JSelfModify.cachedParsePath(path), JSelfModify.cachedParsePath(newPath));
	}
	
	public void move(User u, String pathParts[], String newPathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public Mount dir(User u, String path) throws Exception{
		return dir(u, JSelfModify.cachedParsePath(path));
	}
	
	public Mount dir(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public void append(User u, String path, Object valueToAppend) throws Exception{
		append(u, JSelfModify.cachedParsePath(path), valueToAppend);
	}
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public void delete(User u, String path) throws Exception{
		delete(u, JSelfModify.cachedParsePath(path));
	}
	
	public void delete(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public String[] listEachAsString(User u, String path) throws Exception{
		String s[][] = listEachAsParts(u, JSelfModify.cachedParsePath(path));
		//String onePathPart[] = new String[1];
		String paths[] = new String[s.length];
		for(int i=0; i<s.length; i++){
			//onePathPart[0] = s[i];
			//s[i] = JSelfModify.joinPathParts(onePathPart);
			paths[i] = JSelfModify.joinPathParts(s[i]);
		}
		return paths;
	}
	
	public String[] listEachAsString(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}
	
	public String[][] listEachAsParts(User u, String path) throws Exception{
		return listEachAsParts(u, JSelfModify.cachedParsePath(path));
	}
	
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO (my class is "+getClass().getName()+")");
	}

}