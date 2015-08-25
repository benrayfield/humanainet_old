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

/** Use MountHome.hashPassword and MountHome.rootUser with this class.
Every function in class Mount takes a User object.
<br><br>
Today 2014/4 this jselfmodify.User class is only here for possible expansion,
and practically every JSelfModify call uses JSelfModify.rootUser without
checking any password. I expect the system will eventually need users,
but I try to design it as much like an anonymous wiki as I can.
*/
public final class User{
	
	public final String name;
	
	public final String hashedPassword;
	
	/** User is allowed to log in from these IP addresses.
	Key is IP address. Value is time last logged in (number of seconds since year 1970).
	This should be limited to some small size to avoid hackers logging in the same User from many places,
	but over some time, allow new IP addresses to be added and old unused IP addresses to be removed.
	*/
	public final Map<String,Double> ipAddressToLastLoginTime = new HashMap<String,Double>();
	
	/** Use "localhost" if there is not an IP address. */
	public User(String name, String hashedPassword, String ipAddress) throws Exception{
		if(!name.matches("^[a-z][a-z0-9_]{1,49}$")) throw new Exception("Can not create user name: "+name+" Try something that starts with a letter and is between 1 and 50 letters/numbers/underscores long. Lowercase only.");
		this.name = name;
		this.hashedPassword = hashedPassword;
		ipAddressToLastLoginTime.put(ipAddress, JSelfModify.time());
	}
	
	public String toString(){
		return "[User: "+name+"]";
	}
	
	public void verifyPassword(String password){
		String hash = JSelfModify.hashPassword(password);
		if(!hashedPassword.equals(hash)) throw new SecurityException(
			"Could not login User: "+name+" Password is wrong: "+password
			+" TODO Use verifyHashedPassword instead of verifyPassword function, for more security.");
	}
	
	public void verifyHashedPassword(String hashedPassword){
		if(!this.hashedPassword.equals(hashedPassword)) throw new SecurityException(
			"Could not login User: "+name+" Hashed password is wrong: "+hashedPassword);
	}
	
	public void verifyIpAddress(String ipAddress){
		if(!ipAddressToLastLoginTime.containsKey(ipAddress)) throw new SecurityException(
			"Could not login User: "+name+" from IP Address: "+ipAddress+" is not in the allowed list of IP addresses for this user.");
	}
	
	public int hashCode(){
		return name.hashCode()+1;
	}
	
	public boolean equals(Object ob){
		if(this == ob) return true;
		if(ob instanceof User && name.equals(((User)ob).name)) throw new RuntimeException(ob+" duplicates this "+this);
		return false;
	}

}