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

import humanainet.jselfmodify.JSelfModify;
import humanainet.jselfmodify.Mount;

public class ClassLoadFromMount extends ClassLoader{
	
	private Mount classpathMount;
	
	/** If classpathMount is null, use this instead.
	Its the absolute JSelfModify path to find bytes of Java classes in,
	and that location is searched again every time new class bytes are read here.
	*/ 
	private String classpathMountLocation;
	
	//TODO What is parent ClassLoader being used for?
	//findClass isnt using it, as the javadoc in ClassLoader says it should.
	
	/*
	public ClassLoadFromMount(ClassLoader parent, String classpathMountLocation){
		super(parent);
		this.classpathMountLocation = classpathMountLocation;
	}*/
	
	public ClassLoadFromMount(ClassLoader parent, Mount classpathMount){
		super(parent);
		this.classpathMount = classpathMount;
	}
	
	protected Class findClass(String name) throws ClassNotFoundException{
		//System.out.println(getClass().getName()+" finding class: "+name);
		try{
			return super.findClass(name);
		}catch(ClassNotFoundException e){}
		try{
			//System.out.println("Not found in parent ClassLoader. Looking in Mount for: "+name);
			
			//TODO Return same class if asked for same name
			
			String mountPath = '/'+name.replace('.','/')+".class";
			if(classpathMount == null) mountPath = classpathMountLocation+mountPath;
			//Mount mount = classpathMount==null ? Audivolv.root : classpathMount;
			Mount mount = classpathMount;
			//if(Audivolv.log>0) Audivolv.log("Looking for bytes of class "+name
			//	+" at audivolv path "+mountPath+" in "+mount);
			Object shouldBeByteArray = mount.get(JSelfModify.rootUser, mountPath);
			//if(Audivolv.log>0) Audivolv.log("Got a "+shouldBeByteArray.getClass().getName());
			byte bytecode[] = (byte[]) shouldBeByteArray;
			if(bytecode.length < 100 || 1000000 < bytecode.length) throw new Exception(
				"Bytecode is too big or small: "+bytecode.length);
			//if(Audivolv.log>0) Audivolv.log("Got "+bytecode.length+" bytes. Defining class "+name);
			Class newClass = defineClass(name, bytecode, 0, bytecode.length);
			//if(Audivolv.log>0) Audivolv.log("Defined class "+name
			//	+" but did not connect it to other classes yet (link it).");
			return newClass;
		}catch(Exception e){
			//if(Audivolv.log>0) Audivolv.log("Could not load class: "+name+" Exception="+e);
			throw new ClassNotFoundException(name, e);
		}
	}
}
