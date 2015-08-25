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
import humanainet.jselfmodify.util.StringUtil;

import java.io.*;

public class OverlapMounts extends AbstractMount{
	
	private Mount mounts[];
	
	public OverlapMounts(Mount mounts[]){
		this.mounts = mounts.clone();
		if(this.mounts.length == 0) throw new RuntimeException("No "+Mount.class.getName()+"s to overlap. This is usually caused by Eclipse/Netbeans/etc (if you're running from a folder instead of a Jar file) not putting all the files on the classpath. If you're in such a program, try unzipping the Jar file onto that classpath and run the program again.");
	}

	/** Returns true if any inner Mount finds it before any of them throw. Throws if any of them throw before finding it.
	Returns false if none of them find it or throw.
	*/
	public boolean exist(User u, String path) throws Exception{
		StringBuilder sb = null;
		for(Mount m : mounts){
			try{
				if(m.exist(null, path)) return true;
			}catch(Exception e){
				if(sb == null) sb = new StringBuilder();
				sb.append('\n').append(StringUtil.errToString(e));
			}
		}
		if(sb == null) return false;
		throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not call exist "+path+'\n'+sb.toString());
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return exist(u, JSelfModify.joinPathParts(pathParts)); //TODO duplicate code to possibly get optimizations from calling this function instead of the other, in recursive Mounts.
	}

	public Object get(User u, String pathParts[]) throws Exception{
		StringBuilder sb = null;
		for(Mount m : mounts){
			try{
				return m.get(u, pathParts);
			}catch(Exception e){
				if(sb == null) sb = new StringBuilder();
				sb.append('\n').append(StringUtil.errToString(e));
			}
		}
		throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not get "
			+JSelfModify.joinPathParts(pathParts)+'\n'+sb.toString());
	}
	
	public InputStream getInStream(User u, String pathParts[], boolean bigEndian, long from, long to) throws Exception{
		StringBuilder sb = null;
		for(Mount m : mounts){
			try{
				return m.getInStream(u, pathParts, bigEndian, from, to);
			}catch(Exception e){
				if(sb == null) sb = new StringBuilder();
				sb.append('\n').append(StringUtil.errToString(e));
			}
		}
		throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not getInStream from="
			+from+" to="+to+JSelfModify.joinPathParts(pathParts)+'\n'+sb.toString());
	}
	
	/** TODO Should this delete in the first Mount it deletes correctly? Or in all?
	If all, it could be a problem because some Mount are in an OverlapMount to avoid modifying
	an other Mount in the OverlapMount, like the classpath Mounts should not be modified.
	*/
	public void delete(User u, String pathParts[]) throws Exception{
		StringBuilder sb = null;
		for(Mount m : mounts){
			try{
				m.delete(u, pathParts);
				return;
			}catch(Exception e){
				if(sb == null) sb = new StringBuilder();
				sb.append('\n').append(StringUtil.errToString(e));
			}
		}
		if(sb == null) return;
		throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not delete "
			+JSelfModify.joinPathParts(pathParts)+'\n'+sb.toString());
	}

	public String[] listEachAsString(User u, String parentPath) throws Exception{
		/*Set<String> listAll = new HashSet<String>();
		//TODO if operlap, should it throw?
		for(Mount m : mounts) for(String relPath : m.list(parentPath)) listAll.add(relPath);
		String s[] = listAll.toArray(new String[0]);
		Arrays.sort(s);
		return s;
		*/
		Set<String> allPaths = new HashSet<String>();
		StringBuilder sb = null;
		int errors = 0;
		for(Mount m : mounts){
			try{
				for(String path : m.listEachAsString(u, parentPath)) allPaths.add(path);
			}catch(Exception e){ 
				if(sb == null) sb = new StringBuilder();
				sb.append("\nIN list("+parentPath+"), ERROR ("+(++errors)+" of "+mounts.length+") FROM "+getClass().getName()+":\n").append(StringUtil.errToString(e));
			}
		}
		if(errors == mounts.length) throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not get "+parentPath+sb.toString());
		String s[] = allPaths.toArray(StringUtil.EMPTY);
		Arrays.sort(s);
		return s;
	}
	
	/*public String[] listEachAsParts(User u, String pathParts[]) throws Exception{
		return listEachAsString(u, JSelfModify.joinPathParts(pathParts));
	}*/

	public void put(User u, String[] pathParts, Object value) throws Exception{
		StringBuilder sb = null;
		for(Mount m : mounts){
			try{
				m.put(u, pathParts, value);
				return;
			}catch(Exception e){
				if(sb == null) sb = new StringBuilder();
				sb.append('\n').append(StringUtil.errToString(e));
			}
		}
		throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not call put on path "+
				JSelfModify.joinPathParts(pathParts)+"with value"+value+" and the ERRORS ARE:\n"+sb.toString());
	}
	
	public Mount dir(User u, String[] pathParts) throws Exception{
		StringBuilder sb = null;
		for(Mount m : mounts){
			try{
				return m.dir(u, pathParts);
			}catch(Exception e){
				if(sb == null) sb = new StringBuilder();
				sb.append('\n').append(StringUtil.errToString(e));
			}
		}
		throw new Exception("In "+mounts.length+" "+Mount.class.getName()+"s, could not call dir on path "+
			JSelfModify.joinPathParts(pathParts)+" and the ERRORS ARE:\n"+sb.toString());
	}
	
	public String toString(){
		return getClass().getName()+Arrays.asList(mounts);
	}
	
	public OverlapMounts withPrefix(Mount prefix){
		Mount m[] = new Mount[mounts.length+1];
		m[0] = prefix;
		System.arraycopy(mounts, 0, m, 1, mounts.length);
		return new OverlapMounts(m);
	}
	
	public OverlapMounts withSuffix(Mount suffix){
		Mount m[] = new Mount[mounts.length+1];
		System.arraycopy(mounts, 0, m, 0, mounts.length);
		m[mounts.length] = suffix;
		return new OverlapMounts(m);
	}

}
