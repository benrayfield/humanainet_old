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

import humanainet.jselfmodify.Mount;
import humanainet.jselfmodify.User;

/** A Mount inside a Mount. Replace the inner Mount at any time. This lets you give a Mount that you can change later.
Be careful not to create a cycle of Mounts inside Mounts, because it would infinite-loop.
*/
public class WrapMount extends AbstractMount{

	private Mount innerMount;

	public WrapMount(Mount m){
		setInnerMount(m);
	}

	public void setInnerMount(Mount m){
		innerMount = m;
	}

	public boolean exist(User u, String path) throws Exception{
		return innerMount.exist(u, path);
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return innerMount.exist(u, pathParts);
	}
	
	public Object get(User u, String path) throws Exception{
		return innerMount.get(u, path);
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		return innerMount.get(u, pathParts);
	}
	
	public InputStream getInStream(User u, String path, long from, long to) throws Exception{
		return innerMount.getInStream(u, path, false, from, to);
	}
	
	public InputStream getInStream(User u, String pathParts[], long from, long to) throws Exception{
		return innerMount.getInStream(u, pathParts, false, from, to);
	}
	
	public OutputStream getOutStream(User u, String path, boolean bigEndian, long from, long to) throws Exception{
		return innerMount.getOutStream(u, path, bigEndian, from, to);
	}
	
	public OutputStream getOutStream(User u, String pathParts[], boolean bigEndian, long from, long to) throws Exception{
		return innerMount.getOutStream(u, pathParts, bigEndian, from, to);
	}
	
	public void put(User u, String path, Object value) throws Exception{
		innerMount.put(u, path, value);
	}
	
	public void put(User u, String pathParts[], Object value) throws Exception{
		innerMount.put(u, pathParts, value);
	}
	
	public Mount dir(User u, String path) throws Exception{
		return innerMount.dir(u, path);
	}
	
	public Mount dir(User u, String pathParts[]) throws Exception{
		return innerMount.dir(u, pathParts);
	}
	
	public void append(User u, String path, Object valueToAppend) throws Exception{
		innerMount.append(u, path, valueToAppend);
	}
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		innerMount.append(u, pathParts, valueToAppend);
	}
	
	public void delete(User u, String path) throws Exception{
		innerMount.delete(u, path);
	}
	
	public void delete(User u, String pathParts[]) throws Exception{
		innerMount.delete(u, pathParts);
	}

	public String[] listEachAsString(User u, String path) throws Exception{
		return innerMount.listEachAsString(u, path);
	}
	
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		return innerMount.listEachAsParts(u, pathParts);
	}

}
