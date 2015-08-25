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

/** TODO rewrite all text to include User objects.
All commands will require a User object, which may be the root User object to allow anything.
If that User is not allowed to do that command, an Exception must be thrown.
If any user is allowed to do that command,
then its easy and efficient to not use the User parameter in the function body.
<br><br>
Mounts and trees of them are not synchronized,
and the quantity of childs (and which childs they are) in a Mount may change at any time.
<br><br>
TODO rewrite all this text because this software started in Audivolv and CodeSimian and is now called JSelfModify,
and because some parts of it are not included in JSelfModify because they were specific to Audivolv or CodeSimian.
Rewrite text...
<br><br>
In software talk, the word "mount" means to leave something at its original location
but make it appear to be in some other hierarchy, and for its existing subhierarchy (below it)
to be usable as a subhierarchy where it is mounted into the new hierarchy.
<br><br>
audivolv.Mount does not access your low-level file system like a normal mount command does.
<br><br>
All functions throw if anything fails.
Example: The del command in windows (with default options) often chooses not to delete a file
but gives no error message. That is bad design, and Audivolv must throw an error if x exists
after delete(x). If Audivolv's options say x should not be deleted, throw an error
because its an error to try to delete something that you are not allowed to delete.
<br><br>
Example: Audivolv should not be able to delete files that are not in a Mount,
and the Mount should only be put on files and dirs that the user allows Audivolv to use
(including the 1 dir created when Audivolv starts).
TODO Put audivolv.Mounts on other dirs and do not allow delete/modify,
as the "Privacy and Safety policy" says.
<br><br>
This Mount interface is similar to a Unix filesystem in these ways:
* Paths are separated by "/".
* Anything can be mounted to any path. Example: sound-card, hard-drive,
folders on the internet, the number of red pixels near the mouse on screen. Anything.
Of course, the more advanced mounts require writing code, which requires open-source.
<br><br>
Remember to check Audivolv's privacy-and-safety-policy before
writing code that implements Mount and connects to these data sources.
<br><br>
Example: mount the hard-drive folder c:/temp/music to /musicfiles
Example: mount the contents of this Audivolv*.jar to /jarfile/audivolv5
Example: mount http://fsf.org to /website/fsf.org and read from it (writing would fail)
<br><br>
This Mount interface has this unique feature:
* Text in the hierarchy names can use any of the 1.1 million
Unicode symbols (not just those on your keyboard), including the forward slash "/" delimiter.
Example: /sentences/"©an/you\r\n save \"a\" file here?"/"you can in audivolv.txt"
All files must be UTF-8 for this to work.
*/
public interface Mount{
	
	//TODO add functions to Mount for datastruct.virtualaddress.Vad similar to InputStream and OutputStream functions

	public boolean exist(User u, String path) throws Exception;

	public boolean exist(User u, String pathParts[]) throws Exception;

	public Object get(User u, String path) throws Exception;
	
	public Object get(User u, String pathParts[]) throws Exception;
	
	/** Returns a view of the sequence of bits at path,
	from bitIndexFrom (inclusive) to bitIndexTo (exclusive) or -1.
	<br><br>
	bitIndexFrom 0 and bitIndexTo -1 is the whole thing.
	<br><br>
	bitIndexTo -1 means the upper limit is bitSize(u, path).
	<br><br>
	bigEndian is for the order of bits in each byte and is only relevant if
	the specified bit range does not start and end on multiples of 8.
	Example: If the bit range is 7 to 17 then 2 bytes can be written.
	The first is bits 7 to 14. The second is truncated and is bits 15 to 17.
	If bigEndian, then bit 15 will be the most significant bit of the second byte.
	If not bigEndian, then bit 15 will be the least significant bit of the second byte.
	Either way, only 3 bits of the second byte are used.
	<br><br>
	Because of the complexity and inefficiency, some implementations of this interface
	may throw an Exception if the bit range is not byte aligned.
	In general, any implementation of this interface is allowed to throw
	an Exception from any function at any time for any reason or no reason. 
	*/
	public InputStream getInStream(User u, String path,
		boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception;
	
	public InputStream getInStream(User u, String pathParts[],
		boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception;
	
	/** Returns a view of the sequence of bits at path,
	from bitIndexFrom (inclusive) to bitIndexTo (exclusive) or -1.
	<br><br>
	bitIndexFrom 0 and bitIndexTo -1 is the whole thing.
	<br><br>
	bitIndexTo -1 means no upper limit, and may result in appending
	after the end (bitSize(u, path)) is reached, if appending is supported.
	<br><br>
	bigEndian is for the order of bits in each byte and is only relevant if
	the specified bit range does not start and end on multiples of 8.
	Example: If the bit range is 7 to 17 then 2 bytes can be written.
	The first is bits 7 to 14. The second is truncated and is bits 15 to 17.
	If bigEndian, then bit 15 will be the most significant bit of the second byte.
	If not bigEndian, then bit 15 will be the least significant bit of the second byte.
	Either way, only 3 bits of the second byte are used.
	<br><br>
	Because of the complexity and inefficiency, some implementations of this interface
	may throw an Exception if the bit range is not byte aligned.
	In general, any implementation of this interface is allowed to throw
	an Exception from any function at any time for any reason or no reason.
	<br><br>
	TODO How should the following be specified in the parameters?:
	The OutputStream can write past the end of the file or not,
	in the case when the specified bit range is the whole file (or whatever it is). ???
	*/
	public OutputStream getOutStream(User u, String path,
		boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception;
	
	public OutputStream getOutStream(User u, String pathParts[],
		boolean bigEndian, long bitIndexFrom, long bitIndexTo) throws Exception;
	
	/** Returns the size of path in bits or returns -1 if path can not be used as bits. */
	public long bitSize(User u, String path) throws Exception;

	public long bitSize(User u, String pathParts[]) throws Exception;
	
	/** Copies length number of bytes
	from the byte array (index fromStart to index fromStart+length-1)
	to this Mount (index toStart to index toStart+length).
	Increases bSize(...) if writing the bytes overlaps the end of the existing bytes,
	but throws if the existing byte range and the written byte range do not overlap and are not adjacent.
	Throws if fromStart or toStart is negative.
	<br><br>
	fromStart is a long (64 bit integer) instead of int (32 bit integer) to make it easier to
	upgrade this software to 64 bit Java which allows bigger array sizes.
	*
	public void bWrite(User u, String path, long length, byte from[], long fromStart, long toStart) throws Exception;
	
	public void bWrite(User u, String pathParts[], long length, byte from[], long fromStart, long toStart) throws Exception;
	
	** Opposite of bWrite(...) *
	public void bRead(User u, String path, long length, byte to[], long toStart, long fromStart) throws Exception;
	
	public void bRead(User u, String pathParts[], long length, byte to[], long toStart, long fromStart) throws Exception;
	*/

	public void put(User u, String path, Object value) throws Exception;
	
	public void put(User u, String pathParts[], Object value) throws Exception;
	
	public void move(User u, String path, String newPath) throws Exception;
	
	public void move(User u, String pathParts[], String newPathParts[]) throws Exception;
	
	/** If path is a dir, does nothing. If path does not exist, creates a dir.
	Else throws because path exists and is not a dir. Returns the dir.
	<br><br>
	This is needed because different kinds of Mount use different classes for dir.
	Examples: LocalDir is for paths on a hard-drive.
	MountMapOfStringToObject is for trees in memory.
	Zip uses ZipInputStream and ZipOutputStream.
	<br><br>
	To create a file, use a byte array. They all use byte array.
	*/
	public Mount dir(User u, String path) throws Exception;
	
	public Mount dir(User u, String pathParts[]) throws Exception;
	
	/** Example: append a String to a text file. Encoding is always UTF-8. */
	public void append(User u, String path, Object valueToAppend) throws Exception;
	
	public void append(User u, String pathParts[], Object valueToAppend) throws Exception;
	
	public void delete(User u, String path) throws Exception;
	
	public void delete(User u, String pathParts[]) throws Exception;

	/** Relative to this Mount, returns all direct childs of path. "/" gets my direct childs. */
	public String[] listEachAsString(User u, String path) throws Exception;
	
	/** list(empty String array) is the same as list("/") */
	public String[] listEachAsString(User u, String pathParts[]) throws Exception;
	
	/** same as listEachAsString functions except returns as pathParts */
	public String[][] listEachAsParts(User u, String path) throws Exception;
	
	/** same as listEachAsString functions except returns as pathParts */
	public String[][] listEachAsParts(User u, String pathParts[]) throws Exception;

}
