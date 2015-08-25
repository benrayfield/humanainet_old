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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import humanainet.jselfmodify.*;
import humanainet.jselfmodify.util.StringUtil;

/** Plugin is a data-structure loaded from a Jar file.
A Jar file can contain 0 or more plugins.
Instances of Plugin should only be created by PluginLoader.
A plugin is general Java code and any other files.
The only specific code a plugin normally connects to
is the JSelfModify language, and only as a string of JSelfModify code
to start running or setup whatever the plugin does.
Its all done through reflection. Its general enough that this
would be a good plugin system for any Java software.
See PluginLoader for details.
*/
public final class Plugin extends AbstractMount{
	
	/** The name of a plugin is its java packages up to the maximum depth
	that contains all the plugin's classes, with periods replaced with underscores.
	Example: Armed Bear Common Lisp is in package org.armedbear.lisp,
	and its plugin name is org_armedbear_lisp. That is the same way packages
	are renamed in the Java files generated from JSP files.
	The name is not org_armedbear because the lisp folder in org/armedbear
	contains all files in Armed Bear Common Lisp.
	Other plugin name examples: org_apache_http, gigalinecompile, wikipediatext.
	A plugin does not have to contain any Java code.
	The wikipediatext plugin is in a Jar file that contains a large text file
	that the nlmi plugin uses. Therefore the nlmi plugin must be loaded after
	the wikipediatext plugin, and that is specified in the "require" plugin attribute.
	*/
	public final String name;
	
	private final String[] nameParts;

	/** Plugin should not know if its loaded or not. PluginLoader should.
	If a plugin is loaded, that means all plugins it requires are also loaded.
	Loaded means the Jar file (or other source of bytes) is unzipped into /files
	*
	public boolean loaded(){ return loaded; }
	boolean loaded;
	*/

	/** Example: "/JAR/gigalinecompile_0.1_useAsJar_or_unzipToGetSource.jar" *
	public final String zippedPath;
	*/

	/** Example: "/".
	Many plugins share the same unzipped path, which is why they are
	not allowed to contain the same files as any other plugin,
	except (until this is redesigned) the build.xml file which all plugins can contain.
	*
	public final String unzippedPath;
	*/
	
	private Mount where;

	public Plugin(Mount where, String name){
		this.where = where;
		this.name = name;
		nameParts = StringUtil.split(name, "_");
	}
	
	public static String attributeRelPath(String pluginName, String attributeName){
		return "/data/"+pluginName.replace('_','/')+"/plugin."+attributeName;
	}

	/** Returns empty string if that attribute does not exist.
	A plugin's attribute is a pluginname.attributename file
	in the plugin's data folder.
	*/
	public String getAttribute(String attributeName) throws Exception{
		String path = attributeRelPath(name, attributeName);
		
		if(!where.exist(JSelfModify.rootUser, path)) return "";
		return StringUtil.bytesToStr((byte[]) where.get(JSelfModify.rootUser, path));
	}
	
	public String version() throws Exception{
		return getAttribute("version");
	}
	
	/** Returns an immutable List of required plugin names */
	public List<String> depends() throws Exception{
		String s = getAttribute("depends");
		s = s.trim();
		if(s.length() == 0) return Collections.EMPTY_LIST;
		return Collections.unmodifiableList(Arrays.asList(StringUtil.split(s, "\\,")));
	}
	
	/** Returns the JSelfModify code in the plugin that plugs it into the rest of the software.
	May return empty string, like if the plugin is only data but no code.
	*/
	public String jselfmodifyCode() throws Exception{
		return getAttribute("jselfmodify");
	}
	
	public String toString(){
		return "[Plugin: "+name+" where="+where+"]";
	}
	
	//TODO function for zipping the unzipped files (after they are possibly modified)
	//into a new Jar file in /files/JAR to create an updated plugin.
	//Also, use Javassist to compile any modified Java code before zipping it.
	
	
	/** Plugin is a Mount with filters to exclude anything thats
	not part of the plugin, which allows many plugins to have
	the same root path while not sharing any files.
	This is done simply by requiring that the /data folder has
	a subfolder of the plugin name, and the plugin uses only that.
	The plugin also has files/folders in a normal Java
	package/class tree, and the source-code is in that
	same tree. Those are different ways of translating plugin
	name to JSelfModify paths where leaf paths do not overlap.
	*/
	public boolean filterAcceptsPath(String pathParts[]) throws Exception{
		if(pathParts.length == 0) throw new Exception(
			"Do not call this on root plugin path."
			+" It has to be calculated a different way, per file.");
		String first = pathParts[0];
		int startIndex = first.equalsIgnoreCase("data") || first.equalsIgnoreCase("jar") ? 1 : 0;
		//Example path: /data/org/armedbear/lisp/DOC/file.txt
		//Example path: /org/armedbear/lisp/Interpreter.java
		int end = Math.min(pathParts.length, nameParts.length);
		for(int i=startIndex; i<end; i++){
			if(!pathParts[i].equals(nameParts[i-startIndex])) return false;
		}
		return true;
	}
	
	public boolean filterAcceptsPath(String path) throws Exception{
		//TODO optimize
		return filterAcceptsPath(JSelfModify.cachedParsePath(path));
	}
	
	public boolean exist(User u, String pathParts[]) throws Exception{
		if(!filterAcceptsPath(pathParts)) return false;
		return where.exist(u, pathParts);
	}
	
	public Object get(User u, String pathParts[]) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		return where.get(u, pathParts);
	}
	
	public InputStream getInStream(User u, String pathParts[], boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		return where.getInStream(u, pathParts, bigEndian, bitIndexFrom, bitIndexTo);
	}
	
	public OutputStream getOutStream(User u, String pathParts[], boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		return where.getOutStream(u, pathParts, bigEndian, bitIndexFrom, bitIndexTo);
	}
	
	public long bitSize(User u, String pathParts[]) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		return where.bitSize(u, pathParts);
	}
	
	public void put(User u, String pathParts[], Object value) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		where.put(u, pathParts, value);
	}
	
	public Mount dir(User u, String pathParts[]) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		return where.dir(u, pathParts);
	}
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception{
		if(!filterAcceptsPath(pathParts)) throw new Exception(
			"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
		where.append(u, pathParts, valueToAppend);
	}
	
	public void delete(User u, String pathParts[]) throws Exception{
		if(!filterAcceptsPath(pathParts)){
			//Delete means cause to not exist.
			//Since the path is not part of this plugin, nothing to do.
			return;
		}
		where.delete(u, pathParts);
	}

	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception{
		/*if(pathParts.length == 0){
			String firstPathParts[] = where.listEachAsParts(u, pathParts);
			List<String> filteredPaths = new ArrayList<String>();
			for(String firstPathPart : firstPathParts){
				if(filterAcceptsPath(new String[]{firstPathPart})) filteredPaths.add(firstPathPart);
			}
			return filteredPaths.toArray(new String[0]);
		}else{
			if(!filterAcceptsPath(pathParts)) throw new Exception(
				"Plugin path filter does not match: "+JSelfModify.joinPathParts(pathParts));
			return where.listEachAsParts(u, pathParts);
		}*/
		String wheresPathParts[][] = where.listEachAsParts(u, pathParts);
		List<String[]> filteredPaths = new ArrayList<String[]>();
		for(String oneFromWherePathParts[] : wheresPathParts){
			//if(filterAcceptsPath(new String[]{firstPathPart})) filteredPaths.add(firstPathPart);
			if(filterAcceptsPath(oneFromWherePathParts)) filteredPaths.add(oneFromWherePathParts);
		}
		return filteredPaths.toArray(new String[0][]);
	}
	
	
	
}