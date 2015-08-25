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
import java.util.jar.*;
import java.util.zip.*;

import humanainet.jselfmodify.JSelfModify;
import humanainet.jselfmodify.User;
import humanainet.jselfmodify.util.StringUtil;

public class Zip extends AbstractMount{
	/** TODO use unzip function in this class (which returns a TreeOfFilesInMemory) instead of using instances of this class.
	Later, add a function to this class to read a TreeOfFilesInMemory (or any Mount containing byte arrays and Mounts as tree branches?) and return a byte array, a new zip.
	*/
	private Zip(){}

	//TODO in all classes implementing Mount, normalize paths for things like using quotes when it was not necessary.
	
	//TODO Simpler than that, and better, would be to create a tree of Mount that each contain their direct childs,
	//and each direct child is another Mount of the same type or the bytes (byte[], like in this Zip class) of the file.
	
	private InputStream inStream;
	
	/** Value in mapPathInZipToBytes that means the key is a folder instead of file.
	This is needed to represent empty folders, and its a convenient way to
	know if a folder exists when you only have its name.
	*/
	private static final byte emptyByteArrayThatMeansDir[] = new byte[0];

	/** TODO change this to jselfmodify.Mount path, and convert to zip path when necessary.
	Would that interfere with knowing which things are folders and which are files? May need a separate Map to store that info.
	*/
	private Map<String,byte[]> mapPathInZipToBytes = new HashMap<String,byte[]>();

	/** same as mapPathInZipToBytes but different folder/file path syntax */
	private Map<String,byte[]> mapThisPathSyntaxToBytes = new HashMap<String,byte[]>();

	/** TODO use unzip function in this class (which returns a TreeOfFilesInMemory) instead of using instances of this class.
	Later, add a function to this class to read a TreeOfFilesInMemory (or any Mount containing byte arrays and Mounts as tree branches?) and return a byte array, a new zip.
	*/
	private Zip(InputStream zipBytes){
		inStream = zipBytes;
	}

	public boolean exist(User u, String path) throws Exception{
		unzipAll();
		path = JSelfModify.normalizePath(path);
		return mapThisPathSyntaxToBytes.containsKey(path); //TODO Should this mean a file exists or a file/folder exists? This is file only.
	}

	public boolean exist(User u, String pathParts[]) throws Exception{
		return exist(u, JSelfModify.joinPathParts(pathParts));
	}

	public Object get(User u, String path) throws Exception{
		if(path.equals("/")) return get(u, new String[0]); //TODO Should MountHome do this?
		return get(u, JSelfModify.cachedParsePath(path));
	}
	
	//private boolean didPrintMapToStdErrOnce;
	
	/** returns bytes from unzipped file in the zip, TODO: or returns bytes of the zip if array.length==0? */
	public Object get(User u, String pathParts[]) throws Exception{
		//do not modify pathParts[]
		unzipAll();
		//TODO if(pathParts.length==0) return bytes.clone();
		//String path = "";
		//for(String s : pathParts) path += "/"+s;
		//path = path.substring(1); //remove first "/"
		String path = JSelfModify.joinPathParts(pathParts);
		byte b[] = mapThisPathSyntaxToBytes.get(path); //TODO optimize
		if(b == null){
			//if(!didPrintMapToStdErrOnce) System.err.println("Zip file contains: "+mapPathInZipToBytes);
			//didPrintMapToStdErrOnce = true;
			throw new Exception("Not found in zip: "+path+" pathParts="+Arrays.asList(pathParts));
		}
		if(b == emptyByteArrayThatMeansDir){
			return new ViewOfSubpath(this, path); //TODO return same object for repeated calls
		}
		return b;
	}
	
	/*public String[] list() throws Exception{
		//TODO return a view of subfolders. This function is only for direct childs.
		//There must be a way to get all files recursively.
		unzipAll();
		Set<String> firstPathParts = new HashSet<String>();
		for(String s : mapPathInZipToBytes.keySet()){
			firstPathParts.add(MountHome.cachedParsePath(s)[0]);
		}
		String s[] = firstPathParts.toArray(new String[0]);
		Arrays.sort(s);
		return s;
	}*/

	private static String zipPathToThisPathSyntax(String zipPath){
		if(zipPath.startsWith("/")) zipPath = zipPath.substring(1);
		if(zipPath.endsWith("/")) zipPath = zipPath.substring(0,zipPath.length()-1);
		String pathParts[] = zipPath.split("/");
		return JSelfModify.joinPathParts(pathParts);
	}
	
	public String[] listEachAsString(User u, String path) throws Exception{
		unzipAll();
		
		
		//TODO optimize this function by storing the files in the zip (and their bytes) as a tree.
		//This is done inefficiently in mapPathInZipToBytes now, without a tree.

		/*
		//TODO handle audivolv path escape codes
		unzipAll();
		Set<String> paths = new HashSet<String>();
		//TODO rename all "Audivolv" to "JSelfModify". This software started in CodeSimian,
		//and branched to Audivolv and HumanAINet, but is becoming a separate software called JSelfModify.
		if(!path.startsWith("/")) throw new Exception("Audivolv paths must start with forward-slash. Path="+path);
		String prefix = path.substring(1); //Remove starting slash
		for(String pathInZip : mapPathInZipToBytes.keySet()){
			//TODO Are there any case-sensitive related bugs here?
			if(pathInZip.startsWith("/")) throw new Exception(
				"Zip paths do not start with slash: pathInZip="+pathInZip);
			if(pathInZip.endsWith("/")){ //is a dir
				pathInZip = pathInZip.substring(0,pathInZip.length()-1);
				if("".equals(pathInZip)) continue; //is dir the zip is in
			}
			//if(Audivolv.log>1)Audivolv.log("pathInZip="+pathInZip);
			if(pathInZip.startsWith(prefix+"/")){
				String relZipPathNotStartWithSlash = pathInZip.substring(prefix.length()+1);
				if(relZipPathNotStartWithSlash.indexOf('/') == -1){
					//TODO use something like this code, which I copy/pasted from LocalDir.java					
					//if(!relUrl.startsWith("/")) throw new MalformedURLException("In this software, a relative URL must start with slash. relUrl="+relUrl);
					//if(!relUrl.contains("%")) throw new RuntimeException("TODO write code to unescape %s in URLs. relUrl is: "+relUrl);
					//if(relUrl.length() > 1 && relUrl.endsWith("/")) relUrl = relUrl.substring(0,relUrl.length()-1);
					//relUrl = relUrl.substring(1); //remove first /
					//StringBuilder sb = new StringBuilder();
					//for(String urlPart : relUrl.split("/")){
					//	sb.append('/').append(escapePathPart(urlPart));
					//}
					//return sb.toString();

					String pathParts[] = pathInZip.split("/");
					StringBuilder sb = new StringBuilder();
					for(String p : pathParts) sb.append('/').append(MountHome.escapePathPart(p));
					paths.add(sb.toString()); //only 1 level deep, no recursive folders/files
				}
			}
		}
		String s[] = paths.toArray(S.EMPTY);
		Arrays.sort(s);
		//if(Audivolv.log>1)Audivolv.log("Zip returning paths (in "+path+"): "+Arrays.asList(s));
		return s;
		*/
		if(!path.startsWith("/")) throw new Exception("Paths must start with slash. Path="+path);
		if(path.endsWith("/") && !path.equals("/")) throw new Exception("Paths must not end with slash unless the path equals slash. Path="+path);
		path = JSelfModify.normalizePath(path);
		Set<String> paths = new HashSet<String>();
		String pathEndsWithSlash = path.equals("/") ? "/" : path+"/";
		synchronized(mapThisPathSyntaxToBytes){
			for(String p : mapThisPathSyntaxToBytes.keySet()){
				//Paths returned must be relative to this Mount object that list was called on.
				if(p.startsWith(pathEndsWithSlash)) paths.add(p);
			}
		}
		String s[] = paths.toArray(StringUtil.EMPTY);
		Arrays.sort(s);
		//for(String x : s) System.out.println("Zip is returning path: "+x);
		return s;
	}
	
	/*public String[] listEachAsParts(User u, String[] pathParts) throws Exception{
		return listEachAsString(u, JSelfModify.joinPathParts(pathParts));
	}*/
	
	protected void unzipAll() throws Exception{
		if(!mapPathInZipToBytes.isEmpty()) return; //already unzipped
		if(inStream instanceof JarInputStream){ //extends ZipInputStream
			throw new Exception("TODO Manifest.mf may have to be unzipped separately. Test if thats true, and if so, write code to handle it.");
			/*
			Manifest m = ((JarInputStream)zipIn).getManifest();
			ByteArrayOutputStream mOut = new ByteArrayOutputStream();
			m.write(mOut);
			byte mBytes[] = mOut.toByteArray();
			list.add(new Jars.ZipEntryAndByteArray(new ZipEntry("Manifest.mf"),mBytes));
			*/
		}
		ZipInputStream stream;
		if(inStream instanceof ZipInputStream){
			stream = (ZipInputStream) inStream;
		}else{
			stream = new ZipInputStream(inStream);
		}
		inStream = null;
		synchronized(mapThisPathSyntaxToBytes){
			while(true){
				ZipEntry entry = stream.getNextEntry(); //the stream now contains only the bytes for this entry
				if(entry==null) break; //no more entries
				String name = entry.getName();
				byte b[];
				if(entry.isDirectory()) b = emptyByteArrayThatMeansDir;
				else b = readBytesForCurrentEntry(entry, stream);
				mapPathInZipToBytes.put(name, b);
				mapThisPathSyntaxToBytes.put(zipPathToThisPathSyntax(name), b);
			}
		}
	}

	public static MountMapOfStringToObject unzip(InputStream inStreamOrZipInStream) throws Exception{
		MountMapOfStringToObject root = new MountMapOfStringToObject();
		synchronized(ZipInputStream.class){ //TODO verify 
			if(inStreamOrZipInStream instanceof JarInputStream){ //extends ZipInputStream
				throw new Exception("TODO Manifest.mf may have to be unzipped separately. Test if thats true, and if so, write code to handle it.");
				/*
				Manifest m = ((JarInputStream)zipIn).getManifest();
				ByteArrayOutputStream mOut = new ByteArrayOutputStream();
				m.write(mOut);
				byte mBytes[] = mOut.toByteArray();
				list.add(new Jars.ZipEntryAndByteArray(new ZipEntry("Manifest.mf"),mBytes));
				*/
			}
			ZipInputStream stream;
			if(inStreamOrZipInStream instanceof ZipInputStream){
				stream = (ZipInputStream) inStreamOrZipInStream;
			}else{
				stream = new ZipInputStream(inStreamOrZipInStream);
			}
			inStreamOrZipInStream = null;
			while(true){
				ZipEntry entry = stream.getNextEntry(); //the stream now contains only the bytes for this entry
				if(entry==null) break; //no more entries
				String pathInZip = entry.getName();
				//if(pathInZip.endsWith("/")) continue; //folder, not file
				String path = zipPathToThisPathSyntax(pathInZip);
				if(entry.isDirectory()){
					root.put(JSelfModify.rootUser, path, new MountMapOfStringToObject());
				}else{
					byte b[] = readBytesForCurrentEntry(entry, stream);
					//System.out.println("unzipping. pathInZip="+pathInZip+" path="+path);
					root.put(JSelfModify.rootUser, path, b);
				}
			}
		}
		return root;
	}
	
	private static byte[] readBytesForCurrentEntry(ZipEntry ze, ZipInputStream zi) throws IOException{
		//if(Audivolv.log>0) Audivolv.log("Reading ZipEntry: "+ze);
		byte b[] = new byte[1];
		int totalBytesRead = 0;
		while(true){
			if(b.length == totalBytesRead){ //double array size
				byte b2[] = new byte[b.length*2];
				System.arraycopy(b, 0, b2, 0, b.length);
				b = b2;
			}
			int bytesToRead = b.length - totalBytesRead;
			int bytesRead = zi.read(b, totalBytesRead, bytesToRead);
			if(bytesRead == 0) Thread.yield(); //could be an internet stream
			if(bytesRead == -1){ //end of stream
				byte bDone[] = new byte[totalBytesRead];
				System.arraycopy(b, 0, bDone, 0, bDone.length);
				return bDone;
			}
			totalBytesRead += bytesRead;
		}
	}
	
	public String toString(){
		return getClass().getName()+"["+mapPathInZipToBytes.size()+" compressed files]";
	}

}
