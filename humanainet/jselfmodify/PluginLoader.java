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
import java.util.*;

import humanainet.jselfmodify.mounts.*;
import humanainet.jselfmodify.mounts.command.*;
import humanainet.jselfmodify.util.StringUtil;

import java.io.*;

/** Each plugin is a set of files with paths named
a certain way, containing some string transform of the plugin name.
Each plugin has a boot location and 0 or more other locations.
The boot location is decided the first time the plugin is loaded.
A plugin may have many locations it could be loaded from,
including Jar files inside Jar files, but all others except
the boot location will be ignored by PluginLoader.
The version of a plugin in the boot location is not modified.
The versions copied or partially copied from the boot location
to some other location can be modified, and later zipped
into 1 or more Jar files to create modified versions of those
plugins, but that would happen after PluginLoader finishes loading
those specific plugins. PluginLoader is never finished loading
all the plugins because a new plugin can be added at any time.
<br><br>
When searching for plugins in that order, only the first location
of each plugin is used, identified by its relative path.
That first location of a plugin is its "boot location" or "boot mount".
Plugin locations are searched in this order when booting:
<br><br>
Classpath is first, which contains folders and/or Jar files.
Folders are always before Jar files.
Files other than Jars inside a Jar are always before Jar files in a Jar.
All parts of the classpath and all Jars are searched for /data and /JAR
folders, but they are not required unless specific plugins have data or JARs. 
<br><br>
* In all /data folders parallel to Jar files.
If there are no folders on the classpath, then there must be
at least 1 Jar file on the classpath, but do this
regardless of how many folders there are.
For each of those Jar files, in the order they are in the classpath,
look for a /data folder parallel to the Jar file,
and find plugins as subfolders of each /data folder.
For example, HumanAINet is a Jar file that you double-click to run,
and if its on the desktop when you do that, and there is
a /data/org/apache/http folder on the desktop, then the org_apache_http
plugin is loaded from that desktop location instead of the Jar file.
The desktop should also contain a /org/apache/http folder containing its
class files and source-code, as the Jar file would have, because a
plugin is files in a few places, and that is true regardless of where
the plugin is found.
*/
public class PluginLoader extends ClassLoadFromMount{
	
	/** Plugins that are in memory but may not be loaded yet. */
	private final Map<String,Plugin> mapNameToPlugin = new HashMap<String,Plugin>();
	
	/** Add plugin names here after their code is run to plug them into the system. */
	private final Set<String> setOfLoadedPluginNames = new HashSet<String>();
	
	/** If a plugin is in mapNameToPlugin but not setOfLoadedPluginNames after try
	to finish loading it then JSelfModify.logToUser about it once.
	Use this Set to avoid repeating. 
	*/
	private final Set<String> setOfPluginsToldUserWhyTheyFailedToLoad = new HashSet<String>();
	
	private static volatile boolean loadedFirstPlugins;
	
	/** Optionally when the program starts, call this once to load this program's files
	into the JSelfModify file system. If you do not call this, you will not be able
	to access those files except through things like java.io.File and will not
	be able to use most parts of the JSelfModify system. The standard commands
	will still be there, like in the /do and /const folders, but the /files folder
	will be empty.
	*/
	public static void loadFirstPlugins() throws Exception{
		if(loadedFirstPlugins) return;
		loadedFirstPlugins = true;
		try{
			User u = JSelfModify.rootUser;
			
			//Start at list of things on classpath and find plugins in them recursively.
			List<Mount> classpathMounts =
				Arrays.asList(JSelfModify.getClasspathMounts());
			List<LocalDir> dirsOnClasspath = new ArrayList<LocalDir>();
			List<Mount> nondirsOnClasspath = new ArrayList<Mount>(); //Usually jar files (Zip class)
			for(Mount cpMount : classpathMounts){
				if(cpMount instanceof LocalDir){
					dirsOnClasspath.add((LocalDir)cpMount);
				}else{
					nondirsOnClasspath.add(cpMount);
				}
			}
			//Verify no LocalDir is inside an other.
			for(LocalDir dir : dirsOnClasspath){
				for(LocalDir dir2 : dirsOnClasspath){
					if(dir2 == dir) continue;
					if(dir.containsFile(dir2.dir)) throw new Exception(
						"Dir on classpath is inside other dir on classpath."
						+" outerDir="+dir.dir+" innerDir="+dir2.dir);
				}
			}
			List<Mount> dirsAboveJarMounts = 
				Arrays.asList(JSelfModify.getDirMountsAboveJarsOnClasspath());
			dirsAboveJarMounts.removeAll(dirsOnClasspath);
			
			
			List<Mount> mountsToSearchForPlugins = new LinkedList<Mount>(dirsOnClasspath);
			mountsToSearchForPlugins.addAll(dirsAboveJarMounts);
			mountsToSearchForPlugins.addAll(nondirsOnClasspath);
			
			
			while(!mountsToSearchForPlugins.isEmpty()){
				Mount pluginRoot = mountsToSearchForPlugins.get(0);
				mountsToSearchForPlugins.remove(0);
				JSelfModify.log("Looking for plugins in pluginRoot="+pluginRoot);
				List<String> pluginNamesInDir = new ArrayList<String>();
				if(!dirsAboveJarMounts.contains(pluginRoot)){
					//If you run the program by double-clicking its Jar file,
					//and its on the desktop for example, then this should
					//not search all the folders on the desktop.
					//Only the /data and /JAR folders should be searched.
					pluginNamesInDir.addAll(pluginNames(u, pluginRoot));
				}
				if(pluginRoot.exist(u, "/data")){
					pluginNamesInDir.addAll(
						pluginNames(u, (Mount) pluginRoot.get(u, "/data")));
				}
				if(pluginRoot.exist(u, "/jar")){
					for(String jarPath : pluginRoot.listEachAsString(u, "/jar")){
						if(jarPath.toLowerCase().endsWith(".jar")){
							JSelfModify.log("Looking for plugins in pluginRoot="+pluginRoot
								+" and found Jar file in a /jar path: "+jarPath);
							Mount jar = Zip.unzip(pluginRoot.getInStream(u, jarPath, false, 0L, -1L));
							mountsToSearchForPlugins.add(jar);
						}
					}
				}
				for(String pluginName : pluginNamesInDir){
					if(!JSelfModify.loader.mapNameToPlugin.containsKey(pluginName)){
						Plugin p = new Plugin(pluginRoot, pluginName);
						JSelfModify.loader.startLoadingPlugin(u, p);
					}
				}
			}
			JSelfModify.loader.runCodeInPlugins(u);
		}catch(Exception e){
			throw new RuntimeException("Failed to boot and/or load plugins.", e);
		}
		verifySourceCodeIsHere();
	}
	
	public static void verifySourceCodeIsHere() throws Exception{
		List<String> pathsThatMustBeHere = Arrays.asList(
			"/files/humanainet/jselfmodify/JSelfModify.java",
			"/files/humanainet/jselfmodify/mounts/LocalDir.java");
		User u = JSelfModify.rootUser;
		for(String path : pathsThatMustBeHere){
			try{
				JSelfModify.root.get(u, path);
			}catch(Exception e){
				throw new Exception("While verifying some source code files are here since this"
					+" is a potentially very self referencing software, not found: "+path
					+" If in Eclipse, for example, this often happens when first setting up the software,"
					+" when by default src and bin dirs are different and you need to check the"
					+"\"Allow output folders for source folders\" checkbox in \"Java Build Path\" of the project,"
					+" set them to the same dir, and clean the project to start a full recompile."
					+" It may also be caused by possible bugs in JSelfModify merging its view of local dirs and jar files.",
					e);
			}
		}
	}
	
	/** copies its files into /files but does not run its code.
	You may want to call this on many plugins at once.
	Order does not matter here. Order does matter
	when you call finishLoadingPlugin on each plugin.
	*/
	public void startLoadingPlugin(User u, Plugin p) throws Exception{
		if(mapNameToPlugin.containsKey(p.name)) throw new Exception(
			"Already started loading plugin: "+p.name);
		JSelfModify.log("Creating plugin object in memory: "+p.name
			+" but still need to run its code to finish loading it.");
		JSelfModify.loader.mapNameToPlugin.put(p.name, p);
		JSelfModify.log("Plugin: "+p+" Deep copying plugin's files"
			+" to shared file area, but only as deep as overlapping folders are found...");
		//Copy the Plugin instead of pluginRoot because the Plugin is
		//a wrapper for pluginRoot that can only see files containing
		//the plugin name in certain parts of their path.
		JSelfModify.deepCopyUntilTheresNoOverlapOrOverwriteAtLeaf(
			u, (Mount)JSelfModify.root.get(u,"/files"), p);
		JSelfModify.log("Plugin: "+p+" Done copying.");
	}
	
	public void finishLoadingPlugin(User u, Plugin p) throws Exception{
		if(!mapNameToPlugin.containsKey(p.name)) throw new Exception(
			"Did not start loading plugin: "+p.name);
		//TODO merge this function with runCodeInPlugins
		runCodeInPlugins(u);
	}
	
	/** Example: Plugin org_apache_http has at least 1 of these 2 folders:
	pluginRootOrDataDir/org/apache/http
	pluginRootOrDataDir/data/org/apache/http
	but to get the second path you have to call this again
	with pluginRootOrDataDir equal to pluginRootOrDataDir/data.
	*/
	public static List<String> pluginNames(User u, Mount pluginRootOrDataDir) throws Exception{
		return pluginNames(u, pluginRootOrDataDir, "");
	}
	
	/** Returns a sorted immutable list of plugin names */
	private static List<String> pluginNames(User u, Mount pluginRootOrDataDir,
			String prefix) throws Exception{
		List<String> pluginNames = new ArrayList<String>();
		boolean nameEndsHere = false;
		//Name can not end before it has at least 1 path part.
		if(!prefix.equals("")) for(String path : pluginRootOrDataDir.listEachAsString(u, "/")){
			//TODO Define the rules for what a plugin name can be and
			//put it in a function instead of duplicating it here.
			//TODO Allow most unicode chars instead of just letters.
			if(path.equals("/data") || path.equals("/jar")
				|| path.equals("/META-INF")) continue;
			//A plugin name ends at the first folder that contains files
			//or anything other than a Mount.
			//Example: org_apache_http is a plugin name
			//because /org/apache contains no files but /org/apache/http does.
			if(!(pluginRootOrDataDir.get(u, path) instanceof Mount)){
				nameEndsHere = true;
				break;
			}
		}
		if(nameEndsHere) return Collections.singletonList(prefix);
		for(String path : pluginRootOrDataDir.listEachAsString(u, "/")){
			//TODO Define the rules for what a plugin name can be and
			//put it in a function instead of duplicating it here.
			//TODO Allow most unicode chars instead of just letters.
			if(path.equals("/data") || path.equals("/jar")
				|| path.equals("/META-INF") || !path.matches("/[A-Za-z0-9_-]+")) continue;
			Object child = pluginRootOrDataDir.get(u, path);
			if(!(child instanceof Mount)) continue;
			String newPrefix = JSelfModify.cachedParsePath(path)[0];
			if(!prefix.equals("")) newPrefix = prefix+"_"+newPrefix;
			pluginNames.addAll(pluginNames(u, (Mount)child, newPrefix));
		}
		return Collections.unmodifiableList(pluginNames);
	}

	public PluginLoader(ClassLoader parent, Mount classpath){
		super(parent, classpath);
	}
	
	public static void main(String args[]) throws Exception{
		JSelfModify.log("Boot PluginLoader: "+JSelfModify.loader+" Loading first plugins...");
		loadFirstPlugins();
		JSelfModify.log("JSelfModify finished loading.");
	}
	
	/** Returns an immutable List of String plugin names.
	TODO Also return the names of plugins inside the plugins (recursive jars).
	Should I allow recursive plugins (if Jar has a JAR folder inside it)?
	TODO optimize. This should not do so many GET commands if called repeatedly.
	*/
	public List<String> pluginNames(Mount jarFile) throws Exception{
		User u = JSelfModify.rootUser;
		Set<String> pluginNames = new HashSet<String>();
		pluginNames.addAll(pluginNames(u, (Mount) jarFile.get(u, "/data"), ""));
		pluginNames.addAll(pluginNames(u, (Mount) jarFile, ""));
		List<String> pluginNamesList = new ArrayList<String>(pluginNames);
		Collections.sort(pluginNamesList);
		return Collections.unmodifiableList(pluginNamesList);
	}
	
	/*private List<String> pluginNames(User u, Mount z, String prefixForReturnedPaths) throws Exception{
		String paths[] = z.list(u, "/");
		List<String> names = new ArrayList<String>();
		for(String path : paths){
			System.out.println("pluginNames getting relative path: "+path);
			if(path.equals("/plugin.jselfmodify")){
				names.add(prefixForReturnedPaths.replace('/',' ').trim().replace(' ','_'));
			}else{
				Object ob = z.get(u, path);
				if(ob instanceof Mount){
					names.addAll(pluginNames(u, (Mount)ob, prefixForReturnedPaths+path));
				}
			}
		}
		//TODO Why are there duplicate plugin names? Is it the OverlapMounts?
		List<String> namesUnique = new ArrayList<String>();
		Set<String> namesSet = new HashSet<String>();
		for(String name : names){
			if(namesSet.add(name)) namesUnique.add(name);
		}
		return Collections.unmodifiableList(namesUnique);
	}*/
	
	/*private void copyZipContentsToSharedFiles(User u, Zip z) throws Exception{
		System.out.println("copyZipContentsToSharedFiles Zip="+z);
		Mount treeToPut = deepCopy_mountFrom_startPathInThatMount(u, z, "/");
		System.out.println("copyZipContentsToSharedFiles Zip="+z+" treeToPut="+treeToPut);
		JSelfModify.root.put(u, "/", treeToPut);
	}*/

	private void deepCopy_pathToParts_mountFrom(User u, String pathToParts[], Mount from) throws Exception{
		//deepCopy_pathTo_mountFrom_stringSubpath(u, pathTo, from, "/");
		//TODO optimize
		Object ob = JSelfModify.root.exist(u, pathToParts);
		if(ob instanceof Mount){
			JSelfModify.deepCopyUntilTheresNoOverlapOrOverwriteAtLeaf(
				u, (Mount)ob, from);
		}else{ //create or overwrite
			JSelfModify.root.put(u, pathToParts, from);
		}
	}
	
	private MountMapOfStringToObject deepCopy_mountFrom(User u, Mount from) throws Exception{
		return deepCopy_mountFrom_startPathInThatMount(u, from, "/");
	}
	
	/** TODO Make TreeOfFilesInMemory do the work of copying: f.put(u, "/", z);
	And don't waste memory doing it this way. Or is it using the same array that the Zip
	is using. Its bad design either way. JSelfModify's types of Mounts are too tangled
	with eachother.
	*/
	private MountMapOfStringToObject deepCopy_mountFrom_startPathInThatMount(User u, Mount z, String startPath) throws Exception{
		MountMapOfStringToObject tree = new MountMapOfStringToObject();
		for(String path : z.listEachAsString(u, startPath)){
			String  relPath = startPath.equals("/") ? path : path.substring(startPath.length());
			Object ob = z.get(u, path);
			//Zip does not have Mount objects at subpaths. It only has leafs. TODO fix that.
			if(ob instanceof byte[]){
				tree.put(u, relPath, ob);
			}else{
				tree.put(u, relPath, deepCopy_mountFrom_startPathInThatMount(u,z,path));
			}
		}
		return tree;
	}
	
	/*
	private void lookForNewPlugins(User u) throws Exception{
		System.out.println("Looking for new plugins. TODO optimize this process or dont do it so often.");
		String paths[] = JSelfModify.root.list(u, "/JAR");
		Mount copyPluginsTo = (Mount) JSelfModify.root.get(u, "/files_nonclasspath");
		//Mount filesMount = (Mount) J.root.get(u, "/files");
		Plugin rootPlugin = new Plugin(JSelfModify.root, "jselfmodify");
		mapNameToPlugin.put("jselfmodify", rootPlugin);
		for(String path : paths){
			String fileName = path.substring("/JAR/".length());
			if(!fileName.toLowerCase().endsWith(".jar")) continue;
			System.out.println("Found path: "+path);
			if(!mapFileNameToListOfPluginName.containsKey(fileName)){
				//Found a new file to add. It contains 0 or more plugins.
				//Do not add the file more than once.
				InputStream streamOfWholeZipFileAsBytes =
					JSelfModify.root.getInStream(u, path, 0L, -1L);
				System.out.println("streamOfWholeZipFileAsBytes="+streamOfWholeZipFileAsBytes);
				//ZipInputStream is only the bytes of 1 ZipEntry at a time,
				//so that would be the wrong kind of InputStream.
				//The Zip class uses a ZipInputStream on this normal InputStream.
				//Zip z = new Zip(streamOfWholeZipFileAsBytes);
				Mount z = Zip.unzip(streamOfWholeZipFileAsBytes);
				List<String> pluginNames = pluginNames(z);
				System.out.println("In that file, pluginNames="+pluginNames);
				for(String pluginName : pluginNames){
					mapPluginNameToUnzipped.put(pluginName, z);
					if(mapNameToPlugin.containsKey(pluginName)) throw new Exception(
						"Duplicate plugin name: "+pluginName+" path=/JAR/"+fileName);
					Plugin p = new Plugin(z, pluginName);
					mapNameToPlugin.put(pluginName, p);
				}
				mapFileNameToListOfPluginName.put(fileName, pluginNames);
				
				//TODO Get it to work before using multiple classloaders.
				//Until then, the instance var classLoader loads from /files
				//ClassLoader parentLoader = getClass().getClassLoader();
				//ClassLoadFromMount loader = new ClassLoadFromMount(parentLoader, z);
				//mapFileNameToClassLoader.put(fileName, loader);
				
				//The Zip must not contain the same files as any other Zip
				//because it would overwrite them. The only exception is build.xml
				//which every plugin is expected to have. I may think of a way
				//to avoid such duplication later, like separate build files or
				//removing Apache Ant from the build process completely by using
				//Javassist to compile and JSelfModify to access files and zip them.
				//Until then, build.xml is allowed to be duplicated and overwritten.
				
				//copyZipContentsToSharedFiles(u, z);
				//deepCopy_pathToParts_mountFrom(u, new String[]{"files"}, z);
				//deepCopy(u, J.root, z);
				deepCopy(u, copyPluginsTo, z);
				//for(String pathInPlugin : z.list(u, "/")){
				//	if(pathInPlugin.equals("/data") || pathInPlugin.equals("/JAR")){
				//	}
				//}
				
				//TODO Use Plugin.from (the Zip) and Plugin.to (/files) vars
				//instead of referencing them directly in copyZipContentsToSharedFiles(u, z).
			}
		}
		runCodeInPlugins();
	}
	*/
	
	/** Call this after plugins exist in memory but before they are finished loading.
	This observes their "requires" attribute
	(which is a comma separated list of required plugin names)
	and obeys that dependency network while finding a way to run the code
	in each plugin then mark that plugin as loaded so those that depend
	on it can be loaded next.
	*/
	protected void runCodeInPlugins(User u) throws Exception{
		//Run the JSelfModify code in plugins to finish loading them,
		//but only if their required plugins are loaded.
		//Run this loop until nothing changes.
		//If plugins are left with required plugins that do not exist (yet?)
		//then they will have to wait until a later call to be loaded, if ever.
		int prevPluginsLoaded = countPluginsLoaded();
		while(true){
			for(Plugin p : mapNameToPlugin.values()){
				//if(p.loaded()) continue;
				if(setOfLoadedPluginNames.contains(p.name)) continue;
				boolean canLoad = true;
				List<String> req = p.depends();
				JSelfModify.log("Plugin "+p.name+" requires: "+req);
				for(String requiredPluginName : req){
					Plugin reqPlug = mapNameToPlugin.get(requiredPluginName);
					
					//Some Jar files will not have files expected in a plugin,
					//but they do have the Java packages and classes needed
					//to be that plugin. If an other plugin requires a plugin
					//that is not found but its package exists,
					//the requirement is satisfied, but no Plugin object is created.
					//In that way, a plugin can be equal to a Java package
					//of the same name string transformed with different delimiter char.
					//String pkgName = pluginNameToPackageName(requiredPluginName);
					//UPDATE: Instead of checking Package, which usually only works
					//when at least 1 class has been classloaded in that package,
					//check if that path exists in /files, which always works
					//for any Jar files on the classpath or in the /files/JAR folder
					//because when this software starts they are copied to /files.
										
					if(reqPlug == null){
						String pluginRelPath = pluginNameToRelPath(requiredPluginName);
						String packagePath = pluginRelPath;
						String dataPath = "/files/data"+pluginRelPath;
						if(!JSelfModify.root.exist(u, packagePath) && !JSelfModify.root.exist(u, dataPath)){
							canLoad = false;
							break;
						}
					}else{
						//if(!reqPlug.loaded){
						if(!setOfLoadedPluginNames.contains(reqPlug.name)){
							canLoad = false;
							break;
						}
					}
				}
				if(canLoad){
					JSelfModify.log("Loading plugin: "+p);
					String code = p.jselfmodifyCode();
					for(String jselfmodifyPath : StringUtil.lines(code)){
						jselfmodifyPath = jselfmodifyPath.trim();
						if(jselfmodifyPath.length() == 0) continue;
						JSelfModify.log("START: Run JSelfModify code: "+jselfmodifyPath);
						//All JSelfModify code is run as a GET command on a path
						JSelfModify.root.get(u, jselfmodifyPath);
						JSelfModify.log("DONE: Run JSelfModify code: "+jselfmodifyPath);
					}
					//p.loaded = true;
					setOfLoadedPluginNames.add(p.name);
					JSelfModify.log("Loaded plugin: "+p);
				}
			}
			int pluginsLoaded = countPluginsLoaded();
			if(prevPluginsLoaded == pluginsLoaded){
				for(String path : new String[]{"/", "/META-INF", "/jar", "/data"}){
					for(String importantPath : JSelfModify.root.listEachAsString(u, path)){
						JSelfModify.log("Important path: "+importantPath);
					}
				}
				JSelfModify.log("Done loading plugins for now. Total loaded: "+pluginsLoaded
					+" ("+namesOfLoadedPlugins()+
					") Total waiting on plugins that were not found (yet?): "
					+(mapNameToPlugin.size()-pluginsLoaded)+" ("+namesOfKnownPluginsNotLoaded()+")");
				break;
			}
			prevPluginsLoaded = pluginsLoaded;
		}
		for(String pluginName : mapNameToPlugin.keySet()){
			if(!setOfLoadedPluginNames.contains(pluginName)){
				if(!setOfPluginsToldUserWhyTheyFailedToLoad.contains(pluginName)){
					JSelfModify.logToUser(whyDidPluginFailToLoad(pluginName));
					setOfPluginsToldUserWhyTheyFailedToLoad.add(pluginName);
				}
			}
		}
		JSelfModify.log("Done loading plugins. totalPlugins="+mapNameToPlugin.size()
			+" loadedPlugins="+setOfLoadedPluginNames.size());
	}
	
	/** Throws if it did load */
	public String whyDidPluginFailToLoad(String pluginName) throws Exception{
		if(setOfLoadedPluginNames.contains(pluginName)) throw new Exception(
			"Plugin "+pluginName+" did not fail to load. Why are you asking why it failed?");
		if(!mapNameToPlugin.containsKey(pluginName)) throw new Exception(
			"Could not find any files in plugin "+pluginName+" and/or create a "+Plugin.class.getName()
			+" object for it, which are both very early in how plugins are loaded.");
		Plugin p = mapNameToPlugin.get(pluginName);
		List<String> depends;
		try{
			depends = p.depends();
		}catch(Exception e){
			return "Could not get list of plugin names that plugin "+p.name
				+" depends on. err="+StringUtil.errToString(e);
		}
		for(String depend : depends){
			if(!setOfLoadedPluginNames.contains(depend)){
				return "Could not load plugin "+p.name+" because it depends on"
					+" these plugins: "+depends+" and at least this one: "+depend+" was not loaded. "
					+depend+" was not loaded because: "+whyDidPluginFailToLoad(depend);
			}
		}
		return "All the plugins that plugin "+p.name+" depends on are loaded, so probably "
			+p.name+" did not load because some of its code failed."
			+" Look in the /data"+pluginNameToRelPath(pluginName)+"/plugin.* files for that code."
			+" Or maybe it failed for some other reason. It got almost to the end of the loading process.";
	}
	
	public static String pluginNameToPackageName(String pluginName){
		return pluginName.replace('_', '.');
	}
	
	public List<String> namesOfLoadedPlugins(){
		List<String> list = new ArrayList<String>(setOfLoadedPluginNames);
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}
	
	public List<String> namesOfKnownPluginsNotLoaded(){
		List<String> list = new ArrayList<String>();
		for(Plugin p : mapNameToPlugin.values()){
			//if(!p.loaded) list.add(p.name);
			if(!setOfLoadedPluginNames.contains(p.name)) list.add(p.name);
		}
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}
	
	public static String pluginNameToRelPath(String pluginName){
		//Plugin names are not allowed to have chars that need to be escaped.
		//If they did, I would have to use J.escapePathPart(String) here.
		return "/"+pluginName.replace('_', '/');
	}
	
	public int countPluginsLoaded(){
		/*int count = 0;
		for(Plugin p : mapNameToPlugin.values()){
			if(p.loaded) count++;
		}
		return count;
		*/
		return setOfLoadedPluginNames.size();
	}
	
	/** PluginLoader is not a Mount.
	public Object get(User u, String pathParts[]) throws Exception{
		//TODO lookForNewPlugins(u);
		switch(pathParts.length){
		case 0:
			return this;
		case 1:
			String pluginName = pathParts[0];
			Mount z = mapPluginNameToUnzipped.get(pluginName);
			if(z != null) return z;
			throw new Exception("Plugin not found: "+pluginName);
		default:
			throw new Exception("Too many path parts. Only depth 1 is allowed. path="
				+JSelfModify.joinPathParts(pathParts));
		}
	}*/

}
