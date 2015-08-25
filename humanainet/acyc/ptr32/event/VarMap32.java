/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32.event;

import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.RefCounter32;

/** For events and listeners of value changes per var name. Name and value are both in acyc.
You can have a sortedMap of vars entirely in acycnet without this class,
but acyc doesnt do events by itself (at least not in subtypes of it I've made so far).
<br><br> 
TODO limits on what values the vars can have, based on Const.typVal?
<br><br>
TODO This causes a need to lock var addresses of the names (but not the values).
Is it possible to do this witout locking, organizing it some other way?
I cant think of a way, but if its possible that should be done instead.
Or maybe its time for a redesign of the var address locking system
to allow for unlocking andOr automatic unlock after a certain expire time per lock.
*/
public interface VarMap32{
	
	/** All var names and values which events are about must exist here */
	public RefCounter32 acyc();
	
	/** Gets the current value, a pointer into the Acyc, of the varName/key which is in varNames(...).
	If its not one of those var names, returns 0/()/nil.
	<br><br>
	To avoid the need, in most cases, to lock values that var names map to, it is recommended to get the
	value of the name every time from this func instead of caching it and to listen for changes.
	Its the responsibility of a VarMap to lock the pointer to name (so you should cache that)
	and to give updated value from this get(int name) function even just after
	the Acyc moves the pointer which can happen during garbageCollection.
	TODO You can at that time continue to explore into the childs recursively of the value
	returned by this get, but eventually I think this is only going to be thread safe
	if we count readLocks (incoming pointers) to every var,
	probably 2 counts for each var: pointers from inside the Acyc and pointers from outside. 
	<br><br>
	TODO:
	"TODO I want to avoid locking the pointers of values, so the acyc is free to move those pointers, because it would create too many locked addresses as mindmapItem are created with each small change. I'm ok with mindmapItemName being locked pointers since theres far fewer names than changed values the names map to. Is this possible to do without locking the pointers to values?"
	"Maybe if you can only read a value while listening to it, and events are connected to the changing of pointer values in Acyc, then it could be done. Theres no avoiding the need to store pointer values in java classes such as EditPrilist."
	*/
	public int get(int varName);
	
	/** TODO somehow the mapped var values need to become part of the Acyc.
	Maybe an Acyc should have 1 mutable root node which is set to varMapState(),
	and all the mindmapItem would be linked recursively from it in an avlTree
	(which doesnt guarantee uniqueness of the whole map but does guarantee no duplicates per item).
	*/
	public void set(int ptrName, int ptrNewValue);
	
	/** TODO like PropListener, should VarListener give the current value of a var when start listening?
	Difference with PropListener is there can be many props for the same var.
	*/
	public void startListen(VarListener32 listener, int ptrVarName);
	
	public void stopListen(VarListener32 listener, int ptrVarName);
	
	/** Listen to the object in the typed version of (object propName). Type is MindmapUtil.typeProp.
	Listens for creation and deletion of props of that object, which happens when a prop
	is set(int,int) in this VarMap and happens when startListen and have missed props created earlier
	without considering if this PropListener has ever listened before so every time it starts
	it will hear about all the relevant props and listen for changes.
	*/
	public void startPropListen(VarListener32 listener, int propOfWhat);
	
	public void stopPropListen(VarListener32 listener, int propOfWhat);
	
	public VarListener32[] listeners(int ptrVarName);
	
	/** To get them all, use 0 and acyc.size() */
	public int[] varNames(int fromInclusive, int toExclusive);
	
	/** Returns an immutable sortedMap view of the current state of this VarMap.
	TODO I havent decided on a data format for sortedMap yet.
	*/
	public int varMapState();

}
