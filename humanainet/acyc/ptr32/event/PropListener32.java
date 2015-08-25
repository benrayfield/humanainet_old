package humanainet.acyc.ptr32.event;

/** A prop is the name: (typVal (MindmapUtil.typeProp (object propName)).
Listens for creation or deletion of props of a certain lispPair
but not in general changes in value to those props because if you want
further updates you need to start listening to the prop after you are
told its created. Props use the same VarMap as things that arent props.
You can also have props of props recursively.
*/
@Deprecated //merging with VarListener32
public interface PropListener32{
	
	/** This is called when a prop first gets a value and when start listening
	and have missed the creation of older props, so theres no need to get a list of
	existing props before starting to listen for creations/deletions.
	<br><br>
	Deleting something in VarMap (which may be a prop) means to remove its value,
	not to remove the key from the Acyc. Prop is such a key. Its removed when its value is set to 0/()/nil.
	*/
	public void propChanged(VarMap32 varMap, int prop);

}
