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
package humanainet.jselfmodify;
import java.io.*;
import java.util.*;

import humanainet.jselfmodify.mounts.LocalDir;
import humanainet.jselfmodify.mounts.LogStream;
import humanainet.jselfmodify.mounts.MountMapOfStringToObject;
import humanainet.jselfmodify.mounts.OverlapMounts;
import humanainet.jselfmodify.mounts.Zip;
import humanainet.jselfmodify.mounts.command.NewInstanceCommand;
import humanainet.jselfmodify.mounts.command.Put;
import humanainet.jselfmodify.util.StringUtil;

import java.net.MalformedURLException;

public class JSelfModify{
	
	//TODO clean up this log code. Should be using AppendableTree only for both of /do/log and /do/logToUser
	
	/** Same as /do/log/"whateverIsInTheLineVar" */
	public static void log(String line){
		//TODO Until get JSelfModify's plugins system and logging working again (after various system wide redesigns), use System.out
		System.out.println("(TODOJsmLog): "+line);
		/*
		try{
			//TODO optimize by remembering which Mount /do/log is,
			//but find a way to update that cache if /do/log changes.
			//It would be generally useful for Mount to have
			//listeners which could be registered with each Mount
			//for certain events, but it may slow down and complicate
			//this software more than its worth. Think about it.
			//It would also be useful for updating JSelfModifyWindow
			//when parts of the tree being viewed change.
			root.get(rootUser, new String[]{"do", "log", line});
		}catch(Exception e){
			throw new RuntimeException("Could not log: "+line, e);
		}
		*/
	}
	
	public static void log(Object toStringAsLine){
		log(toStringAsLine.toString());
	}
	
	public static void logToUser(String line){
		logToUserOnSameLine("\r\n"+line);
	}
	
	public static void logToUser(Object toStringAsLine){
		logToUser(toStringAsLine.toString());
	}
	
	public static void logToUserOnSameLine(String text){
		/*System.out.println("TODO put something the user will see at /do/logToUser then remove this line");
		log(text); //TODO on same line
		for(Appendable log : logsToUser){
			try{
				log.append(text);
			}catch(IOException e){
				throw new RuntimeException("Could not log: "+text, e);
			}
		}*/
		try{
			logTreeToUser.append(text);
		}catch(IOException e){
			throw new RuntimeException("Could not log: "+text, e);
		}
	}
	
	public static void registerLog(Appendable log){
		logs.add(log);
	}
	
	public static void unregisterLog(Appendable log){
		logTree.remove(log);
	}
	
	public static void registerLogForDisplayingToUser(Appendable log){
		logTreeToUser.add(log);
		registerLog(log);
	}
	
	public static void unregisterLogForDisplayingToUser(Appendable log){
		logTreeToUser.remove(log);
		unregisterLog(log);
	}
	
	/** Log level. Lowest is 0. There is no upper limit,
	but only small ints are used. Example: Its common to use
	log levels 0, 1, and 2.
	Use code like this to log:
	if(JSelfModify.log>1)JSelfModify.log("log this if level is more than 1");
	JSelfModify.log("Or use this code for level 0 (always log it).");
	Higher log level is for more detailed things.
	Lower log level is for less frequent things that are more important.
	This is a public static var for efficiency.
	It is more efficient to use an if statement to check the log level
	before creating the string to log because toString() functions
	and string concat functions are slow. Using if statements
	that way avoids those slow functions if the logging is not done.
	The log(String) function and log var are not the only way
	logging can be done, but they are a convenient way to
	use the main log which is at path /log.
	*
	public static int log = 0;
	public static int getLogLevel(){ return log; }
	public static void setLogLevel(int newLogLevel){
		if(newLogLevel < 0) throw new RuntimeException("newLogLevel="+newLogLevel);
		log = newLogLevel;
	}
	*/
	
	private static List<Appendable> logs = new ArrayList<Appendable>();
	
	private static List<Appendable> logsToUser = new ArrayList<Appendable>();
	
	/** This software started when? Number of seconds since year 1970, with decimal point. */
	public static final double timeStarted;
	
	private static final double nanoTimeRelativeTo;
	
	/** Number of seconds since year 1970, with decimal point, accurate to approximately .000001 second, depending on the computer and operating system, using the System.nanoTime() function and timeStarted var. */
	public static double time(){
		//faster but less accurate: return System.currentTimeMillis()*.001;
		return (System.nanoTime() - nanoTimeRelativeTo)*.000000001 + timeStarted;
	}
	
	static{
		//TODO I would use Nanotimer but I dont want jselfmodify to depend on other software
		timeStarted = System.currentTimeMillis()*.001;
		nanoTimeRelativeTo = System.nanoTime();
		/*
		double t = time();
		double tLessAccurate = System.currentTimeMillis()*.001;
		double diff = Math.abs(t - tLessAccurate);
		//double requiredAccuracy = .00005;
		//double requiredAccuracy = .0001;
		double requiredAccuracy = .0003; //Its usually more accurate, but when system starts its slower
		String timeTest = "time()="+t+" timeLessAccurateByMillis="+tLessAccurate+" diff="+diff+" requiredAccuracy="+requiredAccuracy;
		System.out.println(timeTest);
		if(diff > requiredAccuracy) throw new RuntimeException(timeTest);
		System.out.println("Verified accurate timer is working. (TODO how accurate does it have to be? I've seen more than .00001 seconds if enough code is added for a 2.5 megabyte jar file. requiredAccuracy="+requiredAccuracy);
		*/
	}

	/** This is the root of the main tree of Mounts. It starts as containing an empty TreeOfFilesInMemory.
	<br><br>
	Most softwares that use JSelfModify will call MountHome.root.setInnerMount(their preferred root Mount) once when the software starts.
	An example of changing the inner Mount later is if the user changes file privacy options while the program runs
	which should call MountHome.root.setInnerMount(a Mount that can only access the files the privacy options allow).
	The most common way to do that is to call fillRootWithClasspath(false).
	*
	public static final WrapMount root = new MountHome();
	*/

	/** The most common Mounts to put in this are:
	a tree of all revant files/folders at "/ls", and a "log file" at "/log", and things the server/website does at "/server",
	and threads could be stored at "/threads".
	Example: /files/anyfiles/nlmi.html is the bytes of the Natural Language Mouse Interface javascript software.
	Example: /server/nlmi/"x y" calls a function called "nlmi" with parameter "x y". nlmi.html calls that function and expects an Ajax response.
	Example: /threads/"/processIncomingRequestToServer/nlmi/\"x y\""
		would run /processIncomingRequestToServer/nlmi/"x y" in a new Thread and display it as a child of /threads while the Thread is running.
	All threads should be code running inside some Mount.get function, which means new Mount classes should be created
		where the "get" function is similar to Runnable.run function.
	*/
	public static final Mount root;
	
	/** for reflection. Returns root. */
	public static final Mount root(){
		return root;
	}
	
	/** for reflection. Returns rootUser. */
	public static final User rootUser(){
		return rootUser;
	}
	
	/** for reflection. Returns anonymousUser. */
	public static final User anonymousUser(){
		return anonymousUser;
	}
	
	public static final PluginLoader loader;
	
	public static final User rootUser;
	public static final User anonymousUser;
	static{
		Appendable bootLog = System.out;
		try{
			logTree = new AppendableTree();
			logTreeToUser = new AppendableTree();
			registerLogForDisplayingToUser(bootLog);
			mapNameToUser = new HashMap<String,User>();
			//TODO Use better authentication than IP address,
			//but do not add any setup or installation to the process
			//because it would be better to have no user names and login system
			//than to complicate this software. Find a simple way or don't do it at all.
			//For now, the only simple way I know of is log in through URL
			//and a little extra security by requiring IP address be 1 of some
			//allowed set of IP addresses for that user (currently a set of size 1).
			mapInternetAddressToUsers = new HashMap<String,Set<User>>();
			parsePathCache = new WeakHashMap<String,String[]>();
			rootUser = getOrCreateUser(
				"root",
				hashPassword("[TODO better root password than this "+StringUtil.rand.nextLong()+StringUtil.rand.nextLong()+StringUtil.rand.nextLong()+"]"),
				"localhost"
			);
			anonymousUser = getOrCreateUser(
				"anonymoususer",
				hashPassword("anonymouspass"),
				"anonymousUnknownAddress"
			);
			root = new MountMapOfStringToObject();
			deepCopyUntilTheresNoOverlapOrOverwriteAtLeaf(
				rootUser, root, coreCommandsAndConstants());
			ClassLoader parentLoader = JSelfModify.class.getClassLoader();
			loader = new PluginLoader(parentLoader, (Mount) root.get(rootUser, "/files"));
		}catch(Exception e){
			throw new RuntimeException("Failed to boot JSelfModify", e);
		}
		unregisterLogForDisplayingToUser(bootLog);
	}
	
	/** for general logging, /do/log (not just /do/logToUser),
	branches multiple logs that can be registered in this class.
	*/
	protected static final AppendableTree logTree;
	
	/** for logging to user that they will see in window or whatever the user interface,
	branching into a tree in case multiple places to see it, branches which can be registered in this class.
	*/
	protected static final AppendableTree logTreeToUser;
	
	/** When booting this software, copy this into the root Mount. */
	private static Mount coreCommandsAndConstants() throws Exception{
		User u = JSelfModify.rootUser;
		Mount core = new MountMapOfStringToObject();
		//Create folders as writable Mounts so when plugins are copied,
		//the first plugin copied does not create these as read-only
		//or whatever other limits the plugin's files/folders have.
		core.put(u, "/files", new MountMapOfStringToObject());
		core.put(u, "/files/data", new MountMapOfStringToObject());
		core.put(u, "/files/jar", new MountMapOfStringToObject());
		core.put(u, "/files/META-INF", new MountMapOfStringToObject());
		//Constants:
		core.put(u, "/const/true", true);
		core.put(u, "/const/false", false);
		//Commands:
		core.put(u, "/do/=", new Put());
		core.put(u, "/do/jnew", new NewInstanceCommand());
		//TODO Create something like /jstatic but for vars instead of functions
		//so I can get System.out from plugin.jselfmodify file.
		//core.put(u, "/do/log", new LogStream(System.out));
		core.put(u, "/do/log", new LogStream(logTree));
		/*Appendable appendableLogToUser = new Appendable(){
			public Appendable append(char c) throws IOException{
				JSelfModify.logToUserOnSameLine(""+c);
				return this;
			}
			public Appendable append(CharSequence csq) throws IOException{
				JSelfModify.logToUserOnSameLine(csq.toString());
				return this;
			}
			public Appendable append(CharSequence csq, int start, int end) throws IOException{
				JSelfModify.logToUserOnSameLine(csq.subSequence(start, end).toString());
				return this;
			}
		};*/
		//core.put(u, "/do/logToUser", new LogStream(appendableLogToUser));
		core.put(u, "/do/logToUser", new LogStream(logTreeToUser));
		core.put(u, "/do/unzip", "TODO put an /unzip/toUnzippedPath/fromZipPath command here.");
		core.put(u, "/do/zip", "TODO put a /zip/toZipPath/fromUnzippedPath command here.");
		return core;
	}
	
	public static boolean doesUserExist(String name){
		return mapNameToUser.containsKey(name);
	}
	
	public static User getOrCreateUser(String name, String hashOfPassword, String ipAddress) throws Exception{
		User u = mapNameToUser.get(name);
		if(u == null){
			u = new User(name, hashOfPassword, ipAddress);
			mapNameToUser.put(name, u);
			Set<User> usersAtAddress = mapInternetAddressToUsers.get(ipAddress);
			if(usersAtAddress == null){
				usersAtAddress = new HashSet<User>();
				usersAtAddress.add(u);
				mapInternetAddressToUsers.put(ipAddress, usersAtAddress);
			}else{
				usersAtAddress.add(u);
			}
			System.out.println("Created user: "+name+" hashedPass="+hashOfPassword+" address="+ipAddress);
		}else{
			u.verifyHashedPassword(hashOfPassword);
			u.verifyIpAddress(ipAddress);
			System.out.println("TODO Verified user: "+name+" hashedPass="+hashOfPassword+" address="+ipAddress);
		}
		return u;
	}
	
	public static User[] usersAtAddress(String ipAddress){
		Set<User> usersAtAddress = mapInternetAddressToUsers.get(ipAddress);
		if(usersAtAddress == null) return new User[0];
		return usersAtAddress.toArray(new User[0]);
	}

	/** Sets the inner Mount of MountHome.root to 1 of 2 standard Mounts which contain
	files on the classpath (this Jar file etc) and files parallel to it (files/folders in the same folder as this Jar file).
	TODO There may have been a bug where the users home folder on their operating system was used instead of the folder this Jar file is in. Fix that.
	<br><br>
	To protect the user's privacy, alsoIncludeFilesAndFoldersParallelToClasspath should be false unless the software explains
	to the user that certain files/folders on their hard-drive will be included. That is important because some uses of JSelfModify are
	in a web server that makes all files in MountHome.root available on the internet. It does not violate the users's privacy
	to serve the files inside the Jar file to the internet, because its simply a software they downloaded and will only contain
	the users's files if the user tried to put them into the software as a Jar/Zip file.
	<br><br>
	If alsoIncludeFilesAndFoldersParallelToClasspath, those are first and the classpath is second, in the OverlapMounts that is created to contain them all.
	That means if the same file exists in both places, the one on the hard-drive will be used instead of the one in this Jar file,
	which is useful for modifying the software while it runs to change its behavior quickly. You don't have to change the Jar file to do that.
	*
	public static void fillRootWithClasspath(boolean alsoIncludeFilesAndFoldersParallelToClasspath) throws Exception{
		List<Mount> list = new ArrayList<Mount>();
		if(alsoIncludeFilesAndFoldersParallelToClasspath) for(Mount m : getDirMountsAboveClasspath()) list.add(m);
		for(Mount m : getClasspathMounts()) list.add(m);
		root.setInnerMount(new OverlapMounts(list.toArray(new Mount[0])));
	}*/

	public static OverlapMounts getClasspathAndParallelFiles(boolean alsoIncludeFilesAndFoldersParallelToClasspath) throws Exception{
		List<Mount> list = new ArrayList<Mount>();
		if(alsoIncludeFilesAndFoldersParallelToClasspath) for(Mount m : getDirMountsAboveJarsOnClasspath()) list.add(m);
		for(Mount m : getClasspathMounts()) list.add(m);
		return new OverlapMounts(list.toArray(new Mount[0]));
	}

	private JSelfModify(){
		//super(new TreeOfFilesInMemory()); //replace with any other Mount later
	}
	
	public static void deepCopyUntilTheresNoOverlapOrOverwriteAtLeaf(
			User u, Mount to, Mount from) throws Exception{
		if(from.equals(to)) return; //"until theres no overlap"
		for(String fromPath : from.listEachAsString(u, "/")){
			Object toChild = to.exist(u, fromPath)
				? to.get(u, fromPath)
				: null;
			Object fromChild = from.get(u, fromPath);
			boolean put = false;
			if(toChild instanceof Mount){ //merge
				if(fromChild instanceof Mount){
					deepCopyUntilTheresNoOverlapOrOverwriteAtLeaf(
						u, (Mount)toChild, (Mount)fromChild);
				}else{ //overwrite
					put = true;
					//to.put(u, fromPath, fromChild);
				}
			}else{ //create or overwrite
				put = true;
				//to.put(u, fromPath, fromChild);
			}
			//TODO Why compare with != instead of !fromChild.equals(toChild)?
			//Its compared with equals above.
			if(put && fromChild != toChild){
				//"until theres no overlap"
				to.put(u, fromPath, fromChild);
			}
		}
	}

	public String toString(){
		return getClass().getSimpleName();
	}
	
	private static Mount classpathMounts[];

	private static Mount dirsAboveClasspathMounts[];
	
	/** TODO not allow Users to be created too fast. Loading 1 URL can create a new User so its easy for someone to fill this with many HTTP calls if there are no limits.
	TODO not public. Made it public so ServerBody could mount it at path /users
	*/
	public static final Map<String,User> mapNameToUser;
	
	/** Don't allow any 1 IP address to have too many user accounts. */
	public static final Map<String,Set<User>> mapInternetAddressToUsers;
	
	private static final WeakHashMap<String,String[]> parsePathCache;

	/** Returns the root of the main tree of Mounts. If setRoot(Mount) has not been called, then
	public static Mount root(){
	}

	/** You must not modify the returned String array. It may be returned again */
	public static String[] cachedParsePath(String path) throws Exception{
		String s[] = parsePathCache.get(path);
		if(s != null) return s;
		s = parsePath(path);
		parsePathCache.put(path, s);
		return s;
	}
	
	/** Joins each path part with / and escapes them if necessary. Returned string starts with /
	TODO create a cache function for this, similar to cachedParsePath caches parsePath.*/
	public static String joinPathParts(String pathParts[]){
		if(pathParts.length == 0) return "/";
		StringBuilder sb = new StringBuilder();
		for(String s : pathParts) sb.append("/").append(escapePathPart(s));
		return sb.toString();
	}

	//TODO some "TODO" were changed to "RuntimeException". Change them back. Only the ToDo objects were supposed to be that.
	
	/** Returns the same String if it does not need to be escaped.
	Things that must be escaped:
		whitespace,
		forward slash,
		double quote,
		nonprintable ascii
	*/
	public static String escapePathPart(String s){
		//TODO add more escape codes
		StringBuilder sb = new StringBuilder("\"");
		char c[] = s.toCharArray();
		boolean convertToString = false; //if there is any whitespace or chars that must be escaped
		for(int i=0; i<c.length; i++){
			switch(c[i]){
				case '/':
					sb.append('/');
					convertToString = true;
				break;
				case '\r':
					sb.append("\\r");
					convertToString = true;
				break;
				case '\n':
					sb.append("\\n");
					convertToString = true;
				break;
				case '"':
					sb.append("\\\"");
					convertToString = true;
				break;
				case '\\':
					sb.append("\\\\");
					convertToString = true;
					//TODO Is this paranoid? Technicly the following path does not need quotes: /abc/def/gh\ijk
					//It does not need quotes because only / is a path delimiter and backslash is not.
					//Probably best to require quotes if there are any backslashes because they are for escaping.
				break;
				default:
					boolean whitespace = c[i] <= ' ' || c[i] == (char)127; //TODO Should all chars below ' ' be called whitespace? Should they be escaped in the string?
					if(whitespace) convertToString = true;
					sb.append(c[i]);
			}
		}
		if(convertToString) return sb.append('"').toString(); //use first "
		return sb.toString().substring(1); //remove first "
	}
	
	public static final String[] parsePath(String path) throws Exception{
		//TODO optimize
		//if(Audivolv.log>0) Audivolv.log("Parsing path: "+path);
		char c[] = path.toCharArray();
		if(c[0] != '/'){
			throw new Exception("Path must start with slash: "+path);
		}
		if(c.length == 1) return new String[0]; //If relative, the current path. Else the root path.
		if(c[c.length-1] == '/') throw new Exception("Path ends with slash: "+path);
		List<String> pathParts = new ArrayList<String>();
		boolean stringLiteral = false;
		//If the last N chars were backslash, is N odd? If odd, its an escape.
		boolean oddAdjacentBackslashCount = false;
		int i = 1; //Path starts with slash. Start after it.
		StringBuilder sb = new StringBuilder(); //empty this after each path part
		while(i < c.length){
			if(stringLiteral){
				if(oddAdjacentBackslashCount){ //In string literal and escaping
					switch(c[i]){
						case '"': case '\\':
							sb.append(c[i]);
						break;
						case 'r':
							sb.append('\r');
						break;
						case 'n':
							sb.append('\n');
						break;
						case 't':
							sb.append('\t');
						break;
						case 'u':
							throw new RuntimeException("Unicode escape codes");
						default:
							throw new Exception("Not an escape code, at index "+i+" of "+path);
					}
					oddAdjacentBackslashCount = false;
				}else{ //In string literal and not escaping
					switch(c[i]){
						case '\\': //start escaping
							oddAdjacentBackslashCount = true;
						break;
						case '"': //end string literal
							stringLiteral = false;
							if(i+1 < c.length && c[i+1] != '/') throw new Exception(
								"Path contains string literal that ended before a char that is not forward-slash: "+path);
						break;
						default:
							sb.append(c[i]);
					}
				}
			}else{ //not stringLiteral
				switch(c[i]){
					case '"': //start string literal
						if(c[i-1] != '/') throw new Exception(
							"String literal in path can only start after a forward slash: "+path);
						stringLiteral = true;
					break;
					case '\\':
						throw new Exception(
							"Path has backslash but its not in a string literal in the path: "+path);
					case '/': //path part ends
						if(sb.length() == 0) throw new Exception(
							"Empty path part (2 adjacent slashes) in path: "+path);
						pathParts.add(sb.toString());
						sb.setLength(0);
					break;
					default:
						boolean whitespace = c[i] <= ' ' || c[i] == (char)127;
						if(whitespace) throw new Exception(
							"Path has whitespace thats not in a string literal in the path: "+path);
						sb.append(c[i]);
				}
			}
			i++;
		}
		if(stringLiteral) throw new Exception("Path ends with unclosed string literal: "+path);
		if(sb.length() > 0) pathParts.add(sb.toString());
		//if(Audivolv.log>0) Audivolv.log("Parsed path: "+pathParts);
		return pathParts.toArray(new String[0]);
	}
	
	public static String[] copyAndRemoveFirst(String s[]) throws Exception{
		return copyAndRemoveFirstN(s,1);
	}
	
	public static String[] copyAndRemoveFirstN(String s[], int n) throws Exception{
		//TODO optimize this or what calls this
		if(s.length < n) throw new Exception("s.length="+s.length+" n="+n);
		String tail[] = new String[s.length-n];
		for(int i=0; i<tail.length; i++) tail[i] = s[i+n];
		return tail;
	}

	public static String[] copyAndRemoveLast(String s[]) throws Exception{
		//TODO optimize this or what calls this
		String allExceptLastPart[] = new String[s.length-1];
		System.arraycopy(s, 0, allExceptLastPart, 0, allExceptLastPart.length);
		return allExceptLastPart;
	}

	private static String[] getClassPath(){
		try{
			//if(Audivolv.log>0) Audivolv.log("Getting classpath");
			String cpName = "java.class.path";
			String cp = System.getProperty(cpName).trim();
			System.out.println("java.class.path="+cp);
			if(cp == null) throw new Exception(cpName+" is null. Try changing your Java options.");
			if(cp.equals("")) throw new Exception("Empty classpath. Try changing your Java options.");
			String paths[] = LocalDir.separatePaths(cp);
			System.out.println("Got classpath: "+Arrays.asList(paths));
			return paths;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private static void initClasspath() throws Exception{
		time(); //init time
		System.out.println("MountHome.initClasspath()");
		if(classpathMounts == null){
			String paths[] = getClassPath();
			System.out.println("MountHome.initClasspath() paths["+paths.length+"]="+Arrays.asList(paths));
			Mount m[] = new Mount[paths.length];
			List<Mount> list = new ArrayList<Mount>(); //parents of classpath. TODO are there duplicates?
			for(int i=0; i<paths.length; i++){
				File f = new File(paths[i]);
				if(f.isDirectory()){
					System.out.println("path is dir: "+f);
					m[i] = new LocalDir(f);
					//Do not addto dirsAboveClasspathMounts because
					//thats only useful if classpath is all Jar file(s)
				}else if(f.isFile()){
					System.out.println("path is file (must be unzippable): "+f);
					File p = f.getParentFile();
					if(p == null) p = new File(f.getAbsolutePath().replaceFirst("(/|\\\\)[^/\\\\]+$",""));
					LocalDir dParent = new LocalDir(p);
					//Don't duplicate folder if 2 Jars in same folder
					if(!list.contains(dParent)) list.add(dParent);
					boolean isSomeTypeOfZip = paths[i].matches(".+\\.([JjWw][Aa][Rr]|[Zz][Ii][Pp])");
					if(!isSomeTypeOfZip) throw new Exception("File on classpath is not"
						+" any type of zip file (including .jar and .war files): "+f);
					//m[i] = new Zip(new FileInputStream(f));
					m[i] = Zip.unzip(new FileInputStream(f));
				}else{
					throw new Exception("While getting classpath, could not find "+f);
				} 
			}
			classpathMounts = m;
			dirsAboveClasspathMounts = list.toArray(new Mount[0]);
		}
		System.out.println("END: MountHome.initClasspath()\r\nclasspathMounts="
			+Arrays.asList(classpathMounts)+"\r\ndirsAboveClasspathMounts="
			+Arrays.asList(dirsAboveClasspathMounts));
	}

	public static Mount[] getClasspathMounts() throws Exception{
		initClasspath();
		return classpathMounts.clone();
	}

	/** Example: If classpath is this Jar file, returns the LocalDir Mount that contains it.
	This Jar may be in memory only or somewhere else, so this may return an empty array.
	*/
	public static Mount[] getDirMountsAboveJarsOnClasspath() throws Exception{
		initClasspath();
		return dirsAboveClasspathMounts.clone();
	}

	/** path and the returned relative URL both start with slash. The path may have quotes and backslashes for escapes,
	but those things are not allowed in URLs. If it can not be exactly translated to URL with no data loss, throws.
	escapeQuotes is needed if you want to quote a URL, like String htmlText = "<a href=\""+pathToRelUrl(path,true)+"\"></a>".
	*/
	public static String pathToRelUrl(String path, boolean escapeQuotes) throws Exception{
		if(path.contains("\\")) throw new Exception("Path contains backslash and maybe can not be converted to relative URL (and for the few paths that could be converted to URL that way, that code has not been written. TODO write it). Path: "+path);
		//TODO optimize. This can be done by iterating the chars instead of many replace functions.
		String s = path.replace("%","%25") //must do this replacement first to avoid replacing parts of the other replacements
			.replace("!","%21")
			.replace("*","%2A")
			.replace("'","%27")
			.replace("(","%28")
			.replace(")","%29")
			.replace(";","%3B")
			.replace(":","%3A")
			.replace("@","%40")
			.replace("&","%26")
			.replace("=","%3D")
			.replace("+","%2B")
			.replace("$","%24")
			.replace(",","%2C")
			//.replace("/","%2F") Leave / the way it is so its part of the hierarchy in the URL
			.replace("?","%3F")
			.replace("#","%23")
			.replace("[","%5B")
			.replace("]","%5D");
		if(escapeQuotes) s = s.replace("\"","%22");
		return s;
	}

	/** The opposite of pathToRelUrl(...). Relative URLs are a subset of this more flexible path syntax,
	so there are never any Exception for that reason, but it will throw if the URL is invalid syntax.
	*/
	public static String relUrlToPath(String relUrl) throws MalformedURLException{
		relUrl = unescapeUrl(relUrl);
		if(!relUrl.startsWith("/")) throw new MalformedURLException("In this software, a relative URL must start with slash. relUrl="+relUrl);
		if(relUrl.length() > 1 && relUrl.endsWith("/")) relUrl = relUrl.substring(0,relUrl.length()-1);
		/*relUrl = relUrl.substring(1); //remove first /
		StringBuilder sb = new StringBuilder();
		for(String urlPart : relUrl.split("/")){
			sb.append('/').append(escapePathPart(urlPart));
		}
		return sb.toString();
		*/
		return relUrl;
	}

	/** hasUrlEscapeCodes does not have to be a URL. All this does is change things like "%3D" to "=".
	Throws MalformedURLException if % is not followed by exactly 2 hex digits.
	*/
	public static String unescapeUrl(String hasUrlEscapeCodes) throws MalformedURLException{
		int size = hasUrlEscapeCodes.length();
		int i=0;
		StringBuilder sb = new StringBuilder();
		while(i < size){
			char c = hasUrlEscapeCodes.charAt(i);
			if(hasUrlEscapeCodes.charAt(i) == '%'){
				if(i+2 >= size) throw new MalformedURLException("Last PERCENT was not followed by 2 hex digits. hasUrlEscapeCodes="+hasUrlEscapeCodes);
				//TODO optimize
				String hexStr = hasUrlEscapeCodes.substring(i+1,i+3);
				try{
					sb.append((char) Integer.parseInt(hexStr,16));
				}catch(NumberFormatException e){
					throw new MalformedURLException("Invalid URL escape code: %"+hexStr+" in hasUrlEscapeCodes="+hasUrlEscapeCodes);
				}
				i += 3;
			}else{
				sb.append(c);
				i++;
			}
		}
		return sb.toString();
	}

	public static String normalizePath(String path) throws Exception{
		return joinPathParts(cachedParsePath(path));
	}
	
	public static String hashPassword(String password){
		return "hashedPass_TODO_more_secure_"
			+password.charAt(0)+password.hashCode()+password.charAt(password.length()-1);
	}
	
	/** Knows how to create an InputStream for some kinds of objects. Throws if not. */
	public static InputStream newInStreamFor(Object ob, long bitFrom, long bitTo) throws Exception{
		if(bitFrom != 0 || bitTo != -1) throw new Exception(
			"TODO from and to bit indexs other than 0 (start) and -1 (end). from="+bitFrom+" to="+bitTo);
		if(ob instanceof InputStream) return (InputStream) ob;
		if(ob instanceof byte[]) return new ByteArrayInputStream((byte[])ob);
		if(ob instanceof File){
			File f = (File) ob;
			if(f.isFile()) return new FileInputStream(f);
		}
		if(ob instanceof String){
			//TODO optimize
			return new ByteArrayInputStream(StringUtil.strToBytes((String)ob));
		}
		throw new Exception("Do not know how to create InputStream for: "+ob+" type="+ob.getClass());
	}
	
	/** If the user clicks a "load plugins" or "save plugins" button in a window,
	for example, then this is the folder that FileDialog should start in.
	Its the first folder on the classpath or the folder under 1 of the Jar
	files on the classpath, so if the user double-clicked a Jar file
	to start this program, this will be the folder that Jar file is in.
	*/
	public static File userStartsLookingForPluginsInThisFolder() throws Exception{
		for(Mount m : getClasspathMounts()){
			if(m instanceof LocalDir) return ((LocalDir)m).dir;
		}
		for(Mount m : getDirMountsAboveJarsOnClasspath()){
			if(m instanceof LocalDir) return ((LocalDir)m).dir;
		}
		throw new Exception("Classpath is probably empty."
			+" Was looking for dir or dir above jar on classpath.");
	}
	
	public static String childAbsolutePath(String jselfmodifyAbsolutePathOfParent, String childPathPart) throws Exception{
		String parentParts[] = cachedParsePath(jselfmodifyAbsolutePathOfParent);
		String childAbsolutePathParts[] = new String[parentParts.length+1];
		System.arraycopy(parentParts, 0, childAbsolutePathParts, 0, parentParts.length);
		childAbsolutePathParts[parentParts.length] = childPathPart;
		return joinPathParts(childAbsolutePathParts);
	}

}
