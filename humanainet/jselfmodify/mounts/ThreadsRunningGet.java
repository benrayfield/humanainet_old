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
import java.util.*;

import humanainet.jselfmodify.JSelfModify;
import humanainet.jselfmodify.User;

/** See the Javadoc comment of MountHome.root for details. It describes a path called "/threads".
I'll copy some of that text below...
/threads/"/processIncomingRequestToServer/nlmi/\"x y\""
		would run /processIncomingRequestToServer/nlmi/"x y" in a new Thread and display it as a child of /threads while the Thread is running.
	All threads should be code running inside some Mount.get function, which means new Mount classes should be created
		where the "get" function is similar to Runnable.run function.
*/
public class ThreadsRunningGet extends AbstractMount{

	public static class GettingPath extends Thread{
		public final String path;
		public ThreadsRunningGet parent;
		public GettingPath(ThreadsRunningGet parent, String path){
			this.parent = parent;
			this.path = path;
		}
		public void run(){
			//TODO Is it important to save any Throwable thrown by getting path?
			try{
				JSelfModify.log("TODO Use the user who created the thread instead of rootUser. path="+path);
				JSelfModify.root.get(JSelfModify.rootUser, path);
			}catch(Exception e){
				throw new RuntimeException(e);
			}finally{
				List<GettingPath> list = parent.pathToGet_to_listOfThread.get(path);
				synchronized(list){
					list.remove(this);
				}
			}
		}
	}

	/** The Thread simply runs jselfmodify.Mount.get on the path. That thread is a child of this Mount while its running.
	Then it is removed. The return value of the "get" is not used.
	*/
	private Map<String,List<GettingPath>> pathToGet_to_listOfThread = new HashMap<String,List<GettingPath>>();

	public boolean exist(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) throw new Exception("Empty path");
		if(pathParts.length == 1){
			return pathToGet_to_listOfThread.containsKey(pathParts[0]);
		}else{
			return false;
		}
	}

	public Object get(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) throw new Exception("Empty path");
		if(pathParts.length == 1){
			//Example: /files/anyfiles/nlmi.html is the first pathPart,
			//which means its escaped (a recursive path) in the path to GET.
			String pathToGet = pathParts[0];
			GettingPath thread = new GettingPath(this, pathToGet);
			//pathToGet_to_thread.put(pathToGet, thread);
			synchronized(pathToGet_to_listOfThread){
				List<GettingPath> list = pathToGet_to_listOfThread.get(pathToGet);
				if(list == null){
					list = new ArrayList<GettingPath>();
					pathToGet_to_listOfThread.put(pathToGet, list);
				}
				synchronized(list){
					list.add(thread);
				}
			}
			thread.start();
			return "Thread started: "+pathToGet;
		}else{
			throw new Exception("Only direct childs are allowed. Path has too many parts: "+JSelfModify.joinPathParts(pathParts));
		}
	}

	public void delete(User u, String pathParts[]) throws Exception{
		throw new Exception("TODO end thread if it exists. There would have to be some var to set in the thread to make it voluntarily end. Thread.stop causes errors.");
	}

	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		/*String s[];
		synchronized(pathToGet_to_listOfThread){
			s = pathToGet_to_listOfThread.keySet().toArray(new String[0]);
		 }
		for(int i=0; i<s.length; i++) s[i] = "/"+JSelfModify.escapePathPart(s[i]);
		Arrays.sort(s);
		return s;
		*/
		throw new Exception("TODO after redesigning list functions");
	}

}
