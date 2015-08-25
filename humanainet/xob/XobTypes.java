/** Ben F Rayfield offers Xob (Xorlisp Objects) opensource GNU LGPL */
package humanainet.xob;
import humanainet.acyc.Glo;

/** You can get these types by deriving from 0/()/nil, so this class is not a dependency.
Its a convenience and optimization to precalculate these in Glo.econacyc namespace.
*/
public class XobTypes{
	private XobTypes(){}
	
	/** listOfPowerOf2Item of bits of UTF16 text, including text views of bitstring or any lispPair in base64 of SHA256 form */
	public static final int typeToken = t("token");
	
	public static final int typeListPow2OfToken = t("listPow2OfToken");
	
	public static final int typeListPow2OfListPow2OfToken = t("listPow2OfListPow2OfToken");
	
	/** list of bitstrings */
	public static final int typeListPow2OfListPow2OfBit = t("listPow2OfListPow2OfBit");
	
	/** Unlike typeListOfBitstring, this is a list of any lispPair */
	public static final int typeListPow2 = t("listPow2");
	
	/** Using typVal and this specific type of course (not written below as its a common datastruct)...
	Object and property name, the object.propertyName in "o.p = propertyValue".
	Use (object propertyName) as key in VarMap.
	Can be used recursively like ((object propertyName) propertyOfThatPropertyName).
	TODO need basic search ability or datastruct to find 
	*/
	public static final int typeProp = t("prop");
	
	/** untyped form is (key value), where each of key and value can be any lispPair.
	Can be used in map or any namespace. I'm first using it for typeEventStream.
	*/
	public static final int typeKeyVal = t("keyVal");
	
	/** Data format is NumberUtil.typeVarSizeScalar and its value is seconds since year 1970.
	That number type has variable precision so it can technically represent planck times and BigDecimal.
	Half its base2 digits are integer part and half are fraction part, as a twosComplement fixedpoint scalar.
	<br><br>
	java.lang.System.currentTimeMillis() and nanoTime() can be used together to create time numbers
	that are accurate to microseconds relative to when nanoTime started,
	but for communication across different computers or runs of the program on same computer,
	the theoretical limit is plus/minus 1 millisecond, and in practice its plus/minus 15 milliseconds.
	In other systems, time may be more accurate. We all should use the same data format.
	*/
	public static final int typeGlobalTime = t("time");
	
	/** Renamed from eventStack to eventStream. Still a listPow2 of keyVal.
	<br><br>
	Normally only pushed onto instead of pop, and sometimes remove many in a randomAccess way
	all at once similar to garbageCollection. An eventStack is a listPow2 of objects which each
	have a time, key, and value. See XobTypes javaclass for details on that datastruct which
	may be done using 2 keyVal, 1 for time as key and the next keyVal as its value. That next
	keyVal is the var's actual name and value at that time. An eventStack can be for any set
	of var. Normally each computer has 1 main eventStack for all changes to vars and 1
	eventStack for each var. The eventStacks for individual vars can be derived from the main
	eventStack since it contains everything they contain, so its a cache instead of a part of
	the data. It can be created again by reading the main eventStack.
	<br><br>
	keyboard and mouse
	events, and any other game controllers, and maybe combined with usernames, can all be events.
	For example, the event of pushing the "j" (by char) key or by a javaVkKeycode, or the event
	of mouse being a certain postion at a certain time. Events can be for any time, key,
	and value in the system, any lispPairs, not just these simple things.
	<br><br>Be careful of creating lispPairs that point at any part of the eventStack
	(including its root which parts of are often reused in the listPow2), because that would
	prevent the deleting of old versions, which needs to be done sometimes to save space as
	every event in the system can get very large.
	<br><br>
	DATA FORMAT:
	<br><br>
	untyped form is a listPow2 of some kind of object with a time, key, and value,
	a data format I'm still considering if I should use 2 keyVal or just lispPairs directly
	and in what combinations.
	It will be something like (timeObject (key value)).
	I'm considering using keyVal to wrap each of those pairs so time could be used as a key in sortedMaps.
	It would be something like...
	(typVal (keyVal (time (typVal (keyVal (key value))))))
	The simpler way is...
	(typVal (timeKeyVal (timeObject (key value))))
	That simpler way could be further simplified by using time as untyped.
	While this software uses var size numbers for time, today it fits in 64 bits,
	and in year 2038 it will automatically expand to 128 bits, and eventually to 256 and so on.
	Since data is sparse you mostly pay for the nonzero digits.
	I'll go with this data format, at least for now. It can be wrapped in the implied types
	for use in other datastructs outside this code if you want to use it that way too.
	<br><br>
	(typVal (timeKeyVal (untypedTime (typedKey, typedValue)))),
	where typedKey and typedValue are any object.
	<br><br>
	TODO I need a system of knowing what type untyped objects should be wrapped in
	as part of the definition of other types, like typeEventStack will have various untyped objects
	start at certain places in the lispPair forest.
	*/
	public static final int typeEventStream = t("eventStream");
	
	/** Use typeKeyVal with typeGlobalTime as key and the object as val.
	OLD:
	untyped form is (untypedTime object), where untypedTime is an untyped form of typeGlobalTime.
	It means at that time, the object existed. The object could be anything.
	I'm planning to use it for typeKeyval objects in a versioning system for history of some of the vars.
	*
	public static final int typeAtTimeExistObject = t("atTimeExistObject");
	*/
	
	private static int t(String type){
		int ptr = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, type);
		Glo.econacyc.oneMoreRefFromOutside(ptr);
		return ptr;
	}

}
