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
import humanainet.jselfmodify.Mount;
import humanainet.jselfmodify.User;
import humanainet.jselfmodify.util.StringUtil;

public class MountMapOfStringToObject extends AbstractMount{

	public final Map<String,Object> map;
	
	public MountMapOfStringToObject(){
		this(new HashMap<String,Object>());
	}

	public MountMapOfStringToObject(Map<String,Object> map){
		this.map = map;
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) return true;
		if(pathParts.length == 1) return map.containsKey(pathParts[0]);
		Object tail = map.get(pathParts[0]);
		if(tail == null) return false;
		if(tail instanceof Mount){
			return ((Mount)tail).exist(u, JSelfModify.copyAndRemoveFirst(pathParts));
		}
		return false;
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) return this;
		Object tail = map.get(pathParts[0]);
		if(tail == null) throw new Exception("First part of path not found: "
			+pathParts[0]+" in path: "+JSelfModify.joinPathParts(pathParts));
		if(pathParts.length == 1) return tail;
		if(tail instanceof Mount){
			return ((Mount)tail).get(u, JSelfModify.copyAndRemoveFirst(pathParts));
		}
		throw new Exception("Can not recurse path past the first part. path="
			+JSelfModify.joinPathParts(pathParts)
			+" Got this object from first path part: "+tail);
	}
	
	public InputStream getInStream(User u, String pathParts[], boolean bigEndian, long from, long to) throws Exception{
		if(from != 0 || to != -1) throw new Exception("TODO from and to other than start and end (bit indexs).");
		Object ob = get(u, pathParts);
		return JSelfModify.newInStreamFor(ob, from, to);
	}
	
	public void put(User u, String pathParts[], Object value) throws Exception{
		if(pathParts.length == 0) throw new Exception("Can't replace self.  Path has no parts.");
		if(pathParts.length == 1){
			synchronized(map){
				map.put(pathParts[0], value);
			}
		}else{
			Object tail = map.get(pathParts[0]);
			if(tail == null){
				tail = new MountMapOfStringToObject();
				synchronized(map){
					map.put(pathParts[0], tail);
				}
			}
			if(tail instanceof Mount){
				((Mount)tail).put(u, JSelfModify.copyAndRemoveFirst(pathParts), value);
			}else{
				synchronized(map){
					map.put(pathParts[0], value); //replace
				}
			}
		}
	}
	
	public Mount dir(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) return this;
		Object tail = map.get(pathParts[0]);
		if(tail == null) synchronized(map){
			tail = new MountMapOfStringToObject();
			map.put(pathParts[0], tail);
		}
		if(!(tail instanceof Mount)) throw new Exception("Can not create dir because object at"
			+" first part of path is not a Mount. path="+JSelfModify.joinPathParts(pathParts));
		if(pathParts.length > 1){
			return ((Mount)tail).dir(u, JSelfModify.copyAndRemoveFirst(pathParts));
		}
		return (Mount) tail;
	}
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		throw new Exception("TODO");
	}
	
	public void delete(User u, String pathParts[]) throws Exception{
		if(pathParts.length == 0) throw new Exception("Can not delete self. Path has no parts.");
		if(pathParts.length == 1){
			synchronized(map){
				map.put(pathParts[0], null);
			}
		}else{
			Object tail = map.get(pathParts[0]);
			if(tail == null) return; //Nothing to delete. Delete means "after this call it not exist", and that is done.
			if(tail instanceof Mount){
				((Mount)tail).delete(u, JSelfModify.copyAndRemoveFirst(pathParts));
			}
			//Else can not recurse path past the first part to find the parent of the
			//thing to delete. Delete means cause it to not exist. Its already that way. Done.

		}
	}
	
	/** Returns a list of path parts that are not escaped because they are in array form */
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		if(JSelfModify.joinPathParts(pathParts).equals("/files/data/mindmap")){
			System.out.println("Testing /files/data/mindmap");
		}
		/*
		//TODO optimize for if map.size() is large and this is called often without changing much.
		if(pathParts.length == 0){
			String s[];
			synchronized(map){
				s = map.keySet().toArray(StringUtil.EMPTY);
			}
			Arrays.sort(s);
			return s;
		}else{
			Object tail = map.get(pathParts[0]);
			if(tail == null) return StringUtil.EMPTY;
			if(tail instanceof Mount){
				String s[] = ((Mount)tail).listEachAsParts(u, JSelfModify.copyAndRemoveFirst(pathParts));
				return s;
			}
			return StringUtil.EMPTY;
		}*/
		//TODO optimize for if map.size() is large and this is called often without changing much.
		if(pathParts.length == 0){
			String s[];
			synchronized(map){
				s = map.keySet().toArray(StringUtil.EMPTY);
			}
			Arrays.sort(s);
			String eachsPathParts[][] = new String[s.length][1];
			for(int i=0; i<s.length; i++) eachsPathParts[i][0] = s[i];
			return eachsPathParts;
		}else{
			String firstPathPartExploringHere = pathParts[0];
			Object tail = map.get(firstPathPartExploringHere);
			if(tail == null) return StringUtil.EMPTYSQUARED;
			if(tail instanceof Mount){
				String s[][] = ((Mount)tail).listEachAsParts(u, JSelfModify.copyAndRemoveFirst(pathParts));
				if(s.length == 0) return StringUtil.EMPTYSQUARED;
				int eachsNumberOfParts = s[0].length+1;
				//return s;
				String eachsPathParts[][] = new String[s.length][eachsNumberOfParts];
				for(int i=0; i<s.length; i++){
					eachsPathParts[i][0] = firstPathPartExploringHere;
					System.arraycopy(s[i], 0, eachsPathParts[i], 1, eachsNumberOfParts-1);
				}
				return eachsPathParts;
			}
			return StringUtil.EMPTYSQUARED;
		}
	}

}
