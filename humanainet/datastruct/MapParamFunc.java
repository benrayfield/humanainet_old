/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.datastruct;
import java.util.Map;
import java.util.Set;

/** A very general kind of function, a little slower and more memory than usual
because you need to create a Map to call it.*/
public interface MapParamFunc<ReturnType,KeyType,ValueType>{
	
	public ReturnType mapParamFunc(Map<KeyType,ValueType> params);
	
	/** These are the keys which are understood for use in the Map in newStatsys func.
	Other keys will be ignored.
	*/
	public Set<KeyType> keysUnderstood();
	
	/** key is any in the Set returned by keysUnderstood().
	Returns 1 or more example values which can be literally used or
	interpreted as examples to help you choose other values for that key.
	*/
	public Set<ValueType> exampleValuesForKey(KeyType key);

}
