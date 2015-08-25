/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

/** TODO since this has the same funcs, except for their names, as an ordinary mutable var,
should VarMap32 have 1 var we arbitrarily name as the root? Probably so.
*/
@Deprecated //at least until decide if VarMap should do this
public interface MutableRoot32{
	
	/** See comment in setRootNode(int) */
	public int currentRootNode();
	
	/** TODO rewrite this text since I moved these rootNode funcs from Acyc to MutableRoot,
	which some Acycs may implement, because Acyc is defined as having no mutable data except
	the limited space the immutable binary forest nodes occupy so it can become full.
	<br><br>
	Each Acyc has 1 mutable var to hold the root of an avlTree which contains data we certainly want to keep
	and is normally the key/value pairs in in a VarMap. VarMap's most common use is name to mindmapItem
	but can also be used automatically. Any other data may or may not be kept depending on garbageCollection
	algorithms such as EconAcyc. EconAcyc would delete the root node the same as any other if its not
	funded (in units of lispPair) enough for how much memory it uses, so make sure to do that
	on any nodes you want to keep and automate this process where possible, but at a high level
	the users of the system will want to choose what data they interpret as valuable enough to
	keep compared to how much memory it uses including shared lispPairs which the cost is
	shared between those who point at them.
	*/
	public void setRootNode(int pointer);

}
