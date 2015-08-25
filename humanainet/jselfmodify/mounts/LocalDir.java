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

/** All paths use forward slash, not backslash.
Paths must start with slash and not end with slash.
Paths can contain any Unicode symbols if they are escaped.
Example: /path/"that ends with"/a/forward-slash/in/"quotes /"
<br><br>
Example windows path: c:/temp dir/file.txt
As an audivolv path, it could be: /hardDrive/c:/"temp dir"/file.txt
This is an error because of its space: /hardDrive/c:/temp dir/file.txt
Absolute path: /hardDrive/c:/temp
Error because it does not start with slash: dir/file.txt
*/
public class LocalDir extends AbstractMount{
	
	//RuntimeException use newline-related functions in audivolv.S class
	//to use local newline strings on hard-drive and '\n' in memory.
	//RuntimeException Use local newline strings in UI objects that may need them,
	//but their functions should return strings transformed to use '\n'.
	
	public final File dir;
	private LocalDir parentLocalDir;
	
	/** I don't know why File.getParentFile() would return null for a
	nonroot parent, but I added this code and it fixed it.
	*/
	private static File parentOf(File fileOrDir){
		File parentFile = fileOrDir.getParentFile();
		if(parentFile != null) return parentFile;
		//TODO handle escaped slashes, which are allowed on Linux and some other operating systems.
		return new File(fileOrDir.getAbsolutePath().replaceFirst("(/|\\\\)[^/\\\\]+$",""));
	}

	/** Returns null if this is a root folder */
	public LocalDir parentLocalDir() throws Exception{
		if(parentLocalDir == null){
			parentLocalDir = new LocalDir(parentOf(dir));
		}
		return parentLocalDir;
	}
	
	/** Returns true if this LocalDir wraps fileOrDir or if
	fileOrDir is a descendant of the wrapped File.
	*/ 
	public boolean containsFile(File fileOrDir){
		fileOrDir = fileOrDir.getAbsoluteFile();
		while(fileOrDir != null){
			if(dir.equals(fileOrDir)) return true;
			fileOrDir = parentOf(fileOrDir);
		}
		return false;
	}
	

	public LocalDir(File absoluteDir) throws Exception{
		if(absoluteDir == null) throw new Exception("Null dir");
		this.dir = absoluteDir.getAbsoluteFile();
		if(!absoluteDir.isDirectory()) throw new Exception("Not a dir: "+absoluteDir);
	}
	
	private long maxFileSizeToRead = 100*1000*1000; //TODO make this an option. TODO use streams.

	public boolean exist(User u, String path) throws Exception{
		return file(path).exists(); //TODO Should this mean a file exists or a file/folder exists?
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return exist(u, JSelfModify.joinPathParts(pathParts));
	}

	/** Throws FileNotFoundException if not exist.
	Returns another instance of this class for a dir or returns byte array for a file.
	*/
	public Object get(User u, String path) throws Exception{
		//if(Audivolv.log>0) Audivolv.log("Get path: "+path);
		File f = file(path);
		if(f.isFile()){
			long len = f.length();
			if(len > maxFileSizeToRead) throw new Exception("File "+f+" is too big: "+len+" bytes");
			if(len != (int)len) throw new RuntimeException("Stream big file ("+len+" bytes)");
			byte b[] = new byte[(int)len];
			InputStream in = new FileInputStream(f);
			int bytesRead;
			try{
				bytesRead = in.read(b);
			}finally{
				in.close();
			}
			if(bytesRead != b.length) throw new Exception("Did not read all bytes from file "+f);
			return b;
		}else if(f.isDirectory()){
			return new LocalDir(f); //RuntimeException return existing LocalDir if it has been returned before
		}else{
			throw new FileNotFoundException(f.getAbsolutePath());
		}
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		return get(u, JSelfModify.joinPathParts(pathParts));
	}
	
	public InputStream getInStream(User u, String path, boolean bigEndian, long bitFrom, long bitTo) throws Exception{
		if(bitFrom != 0 || bitTo != -1) throw new Exception(
			"TODO bitFrom other than 0 (start) and bitTo other than -1 (end).");
		File f = file(path);
		if(f.isFile()){
			//TODO Should all InputStreams be remembered in this class to verify they are closed later,
			//or to close them if delete() is called?
			return new FileInputStream(f);
		}else if(f.isDirectory()){
			throw new Exception("Is a dir, not a file. Can not get InputStream. JSelfModify path: "+path+" is local path: "+f.getAbsolutePath());
		}else{
			throw new FileNotFoundException("JSelfModify path: "+path+" is local path: "+f.getAbsolutePath());
		}
	}
	
	public InputStream getInStream(User u, String pathParts[], boolean bigEndian, long bitFrom, long bitTo) throws Exception{
		return getInStream(u, JSelfModify.joinPathParts(pathParts), bigEndian, bitFrom, bitTo);
	}

	public String[] list() throws Exception{
		String s[] = dir.list();
		Arrays.sort(s);
		//if(Audivolv.log>0) Audivolv.log("Listing "+s.length+" direct childs of dir: "+Arrays.asList(s));
		return s;
	}

	/** Creates file if not exist */
	public void put(User u, String path, Object value) throws Exception{
		if("/".equals(path)){
			if(value instanceof Mount){
				Mount mountToPut = (Mount) value;
				for(String childPathToPut : mountToPut.listEachAsString(u, "/")){
					File existingChild = file(childPathToPut);
					if(existingChild.isFile()) throw new Exception("TODO Overwrite file and create folder. path="+path);
					Object putAtChild = mountToPut.get(u, childPathToPut);
					put(u, childPathToPut, putAtChild);
				}
				String pathParts[] = JSelfModify.cachedParsePath(path);
				
			}else{
				throw new Exception("Can't replace self. Path has no parts."
					+" If value was a Mount then could merge the nonoverlapping parts"
					+" like copying a folder over a folder with the same name does not"
					+" delete nonoverlapping files in the first folder.");
			}
		}
		//if(Audivolv.log>0) Audivolv.log("Put "+value.getClass().getName()+" to path: "+path);
		File f = file(path);
		//RuntimeException create permission options to choose which files can be read/write/modify/delete etc
		//For now, allow everything, and only create these LocalDir Mounts for dirs you give permission for
		if(f.isFile()){
			//throw new RuntimeException("Replace file: "+f);
			JSelfModify.logToUser("Replacing file: "+f);
		//}else if(f.isDirectory()){
		//	throw new RuntimeException("Replace dir: "+f);
		}{//else{
			//Audivolv.log("Create file: "+f);
			byte b[];
			if(value instanceof byte[]){
				b = (byte[]) value;
			}else if(value instanceof String){
				//b = S.strToBytes((String)value);
				b = ((String)value).getBytes("UTF-8");
			}else if(value instanceof Mount){
				//TODO allow f.isDirectory() and create the contents of the Mount in it.
				Mount m = (Mount) value;
				String childs[] = m.listEachAsString(u, "/");
				for(int i=0; i<childs.length; i++){
					//String pathPart = MountHome.cachedParsePath(childs[i])[0]; //They are all relative paths size 1
					put(u, path+childs[i], m.get(u, childs[i]));
				}
				//TODO What if its a MountMapOfStringToObject trying to create a new dir?
				//The dir(User,String) function was added to Mount to use instead of doing that,
				//but should an Exception be thrown here if its obvious this is being called to do that?
				return;
			}else{
				throw new RuntimeException("Save to file something other than byte[] or String: "+value.getClass().getName());
			}
			f.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(f);
			try{
				out.write(b);
			}finally{
				out.close();
			}
			//if(Audivolv.log>0) Audivolv.log("Done creating file "+f+" Saved "+b.length+" bytes.");
		}
	}
	
	public void put(User u, String pathParts[], Object value) throws Exception{
		put(u, JSelfModify.joinPathParts(pathParts), value);
	}
	
	public void move(User u, String pathParts[], String newPathParts[]) throws Exception{
		if(pathParts.length == 0 || newPathParts.length == 0) throw new Exception(
			"Cant move from or to self: From: "+JSelfModify.joinPathParts(pathParts)
			+" To: "+JSelfModify.joinPathParts(newPathParts));
		File fileFrom = file(JSelfModify.joinPathParts(pathParts));
		if(!fileFrom.exists()) throw new Exception("From of move not exist: "+JSelfModify.joinPathParts(pathParts));
		File fileTo = file(JSelfModify.joinPathParts(newPathParts));
		if(fileTo.exists()) throw new Exception("To of move already exists: "+JSelfModify.joinPathParts(newPathParts));
		fileFrom.renameTo(fileTo);
		/*
		if(pathParts.length == 1 && newPathParts.length == 1){
			...
		}
		if(pathParts.length == 0 || newPathParts.length == 0) throw new Exception(
			"TODO move when 1 or more paths are deeper than 1. From: "+JSelfModify.joinPathParts(pathParts)
			+" To: "+JSelfModify.joinPathParts(newPathParts));
		*/
	}
	
	public Mount dir(User u, String path) throws Exception{
		File f = file(path);
		if(!f.isDirectory()){
			if(f.isFile()) throw new Exception("Can't create dir because its already a file. path="+path);
			if(!file(path).mkdirs()) throw new Exception("Could not create dir. path="+path);
		}
		return (Mount) get(u, path); //TODO optimize by copying some code from the get function here.
	}
	
	public Mount dir(User u, String pathParts[]) throws Exception{
		return dir(u, JSelfModify.joinPathParts(pathParts));
	}
	
	public void delete(User u, String path) throws Exception{
		//if(Audivolv.log>0) Audivolv.log("Delete path: "+path);
		File f = file(path);
		if(f.isFile()){
			throw new RuntimeException("Verify Audivolv options allow delete file. path="+path+" file="+f);
		}else if(f.isDirectory()){
			//Move the dir first. If it can not be moved, it probably can not be deleted.
			throw new RuntimeException("Verify Audivolv options allow delete it, and move dir,"
				+" then recursively delete it. path="+path+" dir="+f);
		}else{
			//if(Audivolv.log>0) Audivolv.log("Tried to delete but already not exist. path="+path+" file="+f);
		}
	}
	
	private File file(String path) throws Exception{
		//if(path.charAt(0) != '/') throw new Exception("Path must start with forward slash: "+path);
		String pathParts[] = JSelfModify.cachedParsePath(path);
		File f = dir;
		for(String pathPart : pathParts) f = new File(f, pathPart);
		return f.getAbsoluteFile();
	}

	/** CHANGING DESIGN BECAUSE...
	<br><br>
	needs to be relative to this mount, but may require redesign of list functions
	so they return String[][] so they can return all the path parts,
	or it could be the single string form of each path.
	I changed this function so it would return a path part, but I didnt consider
	that others would call it from deeper in parents so there would be multiple path parts.
	<br><br>
	Loading mindmap from dir: /files/data/mindmap
	Loading mindmap defs from: /prilistBuild.txt
	Exception in thread "main" java.lang.Exception: First part of path not found: prilistBuild.txt in path: /prilistBuild.txt
	at jselfmodify.mounts.MountMapOfStringToObject.get(MountMapOfStringToObject.java:49)
	at jselfmodify.mounts.AbstractMount.get(AbstractMount.java:31)
	at mindmap.MindMapLoader.loadDefsFromFile(MindMapLoader.java:38)
	at mindmap.MindMapLoader.loadMindMap(MindMapLoader.java:30)
	at mindmap.MindMapLoader.main(MindMapLoader.java:137)
	 */
	public String[][] listEachAsParts(User u, String parentPathParts[]) throws Exception{
		if(parentPathParts.length == 0){ //return names of direct childs
			String s[] = dir.list();
			//for(int i=0; i<s.length; i++) s[i] = "/"+JSelfModify.escapePathPart(s[i]);
			String eachsParts[][] = new String[s.length][1];
			for(int i=0; i<s.length; i++) eachsParts[i][0] = s[i];
			return eachsParts;
		}else{
			//TODO optimize. This can be done faster if not recursive.
			String tailPath[] = JSelfModify.copyAndRemoveFirst(parentPathParts);
			//Does not escape: Object ob = get('/'+parentPathParts[0]);
			Object ob = get(u, new String[]{parentPathParts[0]});
			if(!(ob instanceof LocalDir)) throw new Exception("Getting path "
				+Arrays.asList(parentPathParts)+", got wrong type: "+ob.getClass().getName());
			Mount m = (LocalDir) ob;
			String s[][] = m.listEachAsParts(u, tailPath); //already sorted, as required by list function.
			//String prefix = "/"+JSelfModify.escapePathPart(parentPathParts[0]);
			//for(int i=0; i<s.length; i++) s[i] = prefix+s[i];
			//return s;
			if(s.length == 0) return StringUtil.EMPTYSQUARED;
			String eachsParts[][] = new String[s.length][s[0].length+1];
			for(int i=0; i<s.length; i++){
				eachsParts[i][0] = parentPathParts[0];
				System.arraycopy(s[i], 0, eachsParts[i], 1, s[0].length);
			}
			return eachsParts;
			//throw new Exception("TODO what of s[][] should be returned? Is it the right parts?");
		}
	}
	
	public String toString(){
		return dir.getAbsolutePath();
	}
	

	/** Separates a list of paths. Should work with Windows and Unix-like paths.
	RuntimeException test that.
	RuntimeException handle all path types on all operating-systems.
	*/
	public static String[] separatePaths(String paths) throws Exception{
		return separatePaths(paths,.08);
	}
	
	/** interprets if the path is Windows or Unix with some accuracy and separates, else throws.
	requiredAccuracyFraction increases as chance of correctness increases, but its not proportional.
	*/
	private static String[] separatePaths(String paths, double requiredAccuracyFraction) throws Exception{
		if(requiredAccuracyFraction < 0 || requiredAccuracyFraction > 1)
			throw new RuntimeException(requiredAccuracyFraction+" is not a fraction");
		double chanceUnixInsteadWindows = chancePathListIsUnixInsteadOfWindows(paths);
		double accuracy = Math.abs(.5-chanceUnixInsteadWindows)*2;
		if(chanceUnixInsteadWindows >= .5+requiredAccuracyFraction/2) return separateUnixPaths(paths);
		else if(chanceUnixInsteadWindows <= .5-requiredAccuracyFraction/2) return separateWindowsPaths(paths);
		else{
			double chanceSize1 = chancePathListIsAtMostSize1(paths, chanceUnixInsteadWindows);
			if(chanceSize1 >= requiredAccuracyFraction) return new String[]{paths};
			throw new Exception("Not sure (accuracy is "+accuracy
				+" but required accuracy is "+requiredAccuracyFraction
				+") what operating system these paths are for: "+paths);
		}
	}
	
	private static String[] separateUnixPaths(String paths) throws Exception{
		if("".equals(paths)) throw new Exception("Empty path");
		if(paths.startsWith(":") || paths.endsWith(":")) throw new Exception(
			"Unix path starts or ends with colon delimiter: "+paths);
		return paths.split(":");
	}
	
	private static String[] separateWindowsPaths(String paths) throws Exception{
		if("".equals(paths)) throw new Exception("Empty path");
		if(paths.startsWith(";") || paths.endsWith(";")) throw new Exception(
			"Windows path starts or ends with semicolon delimiter: "+paths);
		return paths.split(";");
	}
	
	private static double chancePathListIsAtMostSize1(String paths, double chancePathListIsUnixInsteadOfWindows){
		if(paths.length()==0) return 1;
		if(chancePathListIsUnixInsteadOfWindows < 0 || chancePathListIsUnixInsteadOfWindows > 1)
			throw new RuntimeException(chancePathListIsUnixInsteadOfWindows+" is not a fraction");
		int colons = paths.split(":").length-1;
		int semicolons = paths.split(";").length-1;
		double chanceIfUnix = colons==0 ? 1 : 0;
		double chanceIfWindows = semicolons==0 ? 1 : 0;
		return chanceIfUnix*chancePathListIsUnixInsteadOfWindows
			+ (1-chancePathListIsUnixInsteadOfWindows)*chanceIfWindows;
	}
	
	/** Returns 1 if its certainly a Unix path list, or 0 if its certainly a Windows path list,
	but probably returns somewhere in the middle. The farther from .5, the more certain.
	Colon is Unix path delimiter and Windows drive letter suffix.
	Unix uses mostly forward slashes in paths and Windows uses mostly backward slashes.
	*/
	private static double chancePathListIsUnixInsteadOfWindows(String paths){
		if(paths.length()==0) return .5;
		String colonForwardSlashesArray[] = paths.split(":/");
		String colonBackSlashesArray[] = paths.split(":\\\\");
		String colonArray[] = paths.split(":");
		double startsWith = .5;
		if(paths.length() > 1){
			//RuntimeException: for each path, if only colon is at index 1 and a letter is at index 0, probably windows
			//RuntimeException: for each path, if it starts with 2 backslashes, its probably windows
			if((Character.isLetter(paths.charAt(0)) && paths.charAt(1)==':') || paths.startsWith("\\\\")){
				startsWith = .5*startsWith + .5*.1; //Windows > Linux
			}
			//RuntimeException: for each path, if it starts with 1 forward slash, its probably linux
			if(paths.startsWith("/")) startsWith = .5*startsWith + .5*.9; //Windows < Linux
			//RuntimeException: for each path instead of only the first path
		}
		int colons = colonArray.length-1; //Windows = Unix
		if(colons > 0 && colonArray[0].length() > 1) startsWith = .5*startsWith + .5*.9; //Windows < Linux
		int colonForwardSlashes = colonForwardSlashesArray.length-1; //Windows < Unix
		int colonBackSlashes = colonBackSlashesArray.length-1; //Windows > Unix
		int slashedColons = colonForwardSlashes-colonBackSlashes; //Windows = Unix
		//int nonSlashedColons = colons-slashedColons; //Windows < Unix
		int semicolons = paths.split(";").length-1; //Windows > Unix
		int forwardSlashes = paths.split("/").length-1; //Windows < Unix
		int backSlashes = paths.split("\\\\").length-1; //Windows > Unix
		int slashes = forwardSlashes+backSlashes;
		if(colons > semicolons+1) return 1; //Example C:\a\b;C:\c;C:\d\e colons==semicolons+1
		double plainSlashFraction = slashes==0 ? .5 : (double)forwardSlashes / slashes;
		double colFraction = slashedColons==0 ? .5 : (double)colonForwardSlashes/slashedColons;
		double colonAndSemicolonFraction = semicolons==0 ? .55 : .3;
		double weightPlainSlash=.18, weightCol=.15, weightColAndSemi=.25, weightStartsWith=.42;
		return plainSlashFraction*weightPlainSlash
			+ colFraction*weightCol
			+ colonAndSemicolonFraction*weightColAndSemi
			+ startsWith*weightStartsWith;
		//RuntimeException fix comments. Some are linux and some are unix. I do not think I got separate info for those.
	}
	
	/** tests path separator functions */
	public static void main(String s[]) throws Exception{
		double requiredAccuracy = .08, requiredCompareAccuracy = .11;
		testCompareUnixAndWindowsPath("/C/a/b/c.txt", 1, "C:\\a\\b\\c.txt", 1, requiredAccuracy, requiredCompareAccuracy);
		testCompareUnixAndWindowsPath("/C/a/b/c.txt:/D/e/f.gif", 2, "C:\\a\\b\\c.txt;D:\\e\\f.gif", 2, requiredAccuracy, requiredCompareAccuracy);
		testCompareUnixAndWindowsPath("/C/a/b/c.txt:/D/e/f.gif:/C", 3, "C:\\a\\b\\c.txt;D:\\e\\f.gif;C:\\", 3, requiredAccuracy, requiredCompareAccuracy);
		testCompareUnixAndWindowsPath("/", 1, "C:\\", 1, requiredAccuracy, requiredCompareAccuracy);
		testCompareUnixAndWindowsPath("abc.txt:def.jpg", 2, "abc.txt;def.jpg", 2, requiredAccuracy, requiredCompareAccuracy); //fake paths but good test
		System.out.println("All "+LocalDir.class.getName()+" tests pass.");
	}
	
	private static void testCompareUnixAndWindowsPath(
			String unixPaths, int unixPathQuantity, String windowsPaths, int windowsPathQuantity,
			double requiredAccuracy, double requiredCompareAccuracy) throws Exception{
		double chanceUnixIsUnix = chancePathListIsUnixInsteadOfWindows(unixPaths);
		double chanceWindowsIsUnix = chancePathListIsUnixInsteadOfWindows(windowsPaths);
		if(chanceUnixIsUnix <= requiredAccuracy/2) throw new Exception(
			"The chance "+unixPaths+" is a unix path is only "+chanceUnixIsUnix);
		if(chanceWindowsIsUnix >= 1-requiredAccuracy/2) throw new Exception(
			"The chance "+unixPaths+" is a windows path is only "+(1-chanceWindowsIsUnix));
		if(chanceUnixIsUnix < chanceWindowsIsUnix+requiredCompareAccuracy) throw new Exception(
			unixPaths+" <-- Unix path ("+chanceUnixIsUnix+" chance is Unix) compared to Windows path ("
			+chanceWindowsIsUnix+" chance is Unix) is too close --> "+windowsPaths);
		String unixPathsArray[] = separatePaths(unixPaths, requiredAccuracy);
		String windowsPathsArray[] = separatePaths(windowsPaths, requiredAccuracy);
		if(unixPathsArray.length != unixPathQuantity) throw new Exception("You said there are "+unixPathQuantity
			+" Unix paths in "+unixPaths+" but I see "+unixPathsArray.length+" paths.");
		if(windowsPathsArray.length != windowsPathQuantity) throw new Exception("You said there are "+windowsPathQuantity
			+" Windows paths in "+unixPaths+" but I see "+unixPathsArray.length+" paths.");
	}
	
	public boolean equals(Object ob){
		if(!(ob instanceof LocalDir)) return false;
		return ((LocalDir)ob).dir.equals(dir);
	}
	
	public int hashCode(){
		return dir.hashCode();
	}

}