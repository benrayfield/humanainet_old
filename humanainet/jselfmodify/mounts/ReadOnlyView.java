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
import java.util.*;

import humanainet.jselfmodify.JSelfModify;
import humanainet.jselfmodify.Mount;
import humanainet.jselfmodify.User;

import java.io.*;

/** Recursively wraps another Mount and allows only read operations,
but be careful of command mounts normally found in the /do section
which use read (get) as actions, like /do/log is the action of logging,
or /do/= copies between 2 paths. This class guarantees read only behavior
if the inner Mount has read only behavior through its read functions
including get, list, bitSize, etc.
Put commands throw an UnsupportedOperationException.
*/
public class ReadOnlyView implements Mount{
	
	protected final Mount inner;
	
	public ReadOnlyView(Mount inner){
		this.inner = inner;
	}
	
	/** Wraps in a ReadOnlyView if it isnt already one */
	public static ReadOnlyView readOnlyView(Mount inner){
		if(inner instanceof ReadOnlyView) return (ReadOnlyView)inner;
		return new ReadOnlyView(inner);
	}
	
	public boolean exist(User u, String path) throws Exception{
		return inner.exist(u,path);
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return inner.exist(u,pathParts);
	}

	public Object get(User u, String path) throws Exception{
		Object o = inner.get(u,path);
		if(o instanceof Mount) return readOnlyView((Mount)o);
		return o;
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		Object o = inner.get(u,pathParts);
		if(o instanceof Mount) return readOnlyView((Mount)o);
		return o;
	}
	
	public InputStream getInStream(User u, String path,
		boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		return inner.getInStream(u,path,bigEndian,bitIndexFrom,bitIndexTo);
	}
	
	public InputStream getInStream(User u, String pathParts[],
		boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		return inner.getInStream(u,pathParts,bigEndian,bitIndexFrom,bitIndexTo);
	}
	
	public OutputStream getOutStream(User u, String path,
			boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+path);
	}
	
	public OutputStream getOutStream(User u, String pathParts[],
			boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+JSelfModify.joinPathParts(pathParts));
	}
	
	public long bitSize(User u, String path) throws Exception{
		return inner.bitSize(u,path);
	}

	public long bitSize(User u, String pathParts[]) throws Exception{
		return inner.bitSize(u,pathParts);
	}
	
	public void put(User u, String path, Object value) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+path);
	}
	
	public void put(User u, String pathParts[], Object value) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+JSelfModify.joinPathParts(pathParts));
	}
	
	public void move(User u, String path, String newPath) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+path);
	}
	
	public void move(User u, String pathParts[], String newPathParts[]) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+JSelfModify.joinPathParts(pathParts)+" newPathParts="+JSelfModify.joinPathParts(newPathParts));
	}
	
	public Mount dir(User u, String path) throws Exception{
		return readOnlyView(inner.dir(u,path));
	}
	
	public Mount dir(User u, String pathParts[]) throws Exception{
		return readOnlyView(inner.dir(u,pathParts));
	}
	
	public void append(User u, String path, Object valueToAppend) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+path);
	}
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+JSelfModify.joinPathParts(pathParts));
	}
	
	public void delete(User u, String path) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+path);
	}
	
	public void delete(User u, String pathParts[]) throws Exception{
		throw new UnsupportedOperationException("because readonly. path="+JSelfModify.joinPathParts(pathParts));
	}

	public String[] listEachAsString(User u, String path) throws Exception{
		return inner.listEachAsString(u,path);
	}
	
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		return inner.listEachAsParts(u,pathParts);
	}
	
	public String toString(){
		return "[ReadOnlyView "+inner+"]";
	}

	public String[] listEachAsString(User u, String pathParts[]) throws Exception{
		return inner.listEachAsString(u, pathParts);
	}

	public String[][] listEachAsParts(User u, String path) throws Exception{
		return inner.listEachAsParts(u, path);
	}

}