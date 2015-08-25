/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32.event;
import java.util.HashMap;
import java.util.Map;

import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.acyc.ptr32.RefCounter32;
import humanainet.mindmap.MindmapUtil;
import humanainet.mindmap.io.OldDataFormatYear2015;
import humanainet.xob.XobTypes;
import humanainet.xob.XobUtil;

public class SimpleVarMap32 implements VarMap32{
	
	//TODO remove SimpleVar32 from map when it has no VarListener32 and its value is 0/()/nil,
	//since all vars are mapped to 0/()/nil when they're not in the map.
	
	public final RefCounter32 acyc;
	public RefCounter32 acyc(){ return acyc; }
	
	protected Map<Integer,SimpleVar32> pointerToVar = new HashMap();
	
	/** Each next immutable head of a listPow2 of typVal (See XobTypes). Starts as 0 which is empty listPow2.
	Props are just normal keyVal whose name happens to have that type, so no special processing at this level.
	This can be cached by scanning a file or in memory of value changes for each var or small groups of vars
	maybe sorted somehow, so many VarMap could each hold versions of only 1 (or some group of) vars at a time.
	*/
	protected int eventStream;
	
	public SimpleVarMap32(RefCounter32 acyc){
		this.acyc = acyc;
		acyc.oneMoreRefFromOutside(eventStream);
	}
	
	/** TODO A var is deleted when its value is 0 and it has no VarListener (which can be for direct or prop) */
	public void set(int ptrName, int ptrNewValue){
		int oldValue = get(ptrName);
		if(oldValue == ptrNewValue) return; //TODO merge duplicate code with 
		addToEventStream(ptrName, ptrNewValue);
		
		System.out.println("SimpleVarMap setting "+ptrName+" to value "+ptrNewValue);
		if(ptrNewValue == 0){ //deleting var
			SimpleVar32 var = pointerToVar.get(ptrName);
			//If var == null it doesnt exist and there are no listeners, so nothing to do
			if(var != null){
				//int oldValue = get(ptrName);
				//If it was already 0/()/nil, no change. TODO move this out of if statement since it applies to any value
				//if(oldValue != 0){ //changing value from nonnil to nil
					var.setValue(ptrNewValue); //does nonprop events
					int nameType = Acyc32Util.typeOf(acyc, ptrName);
					if(nameType == XobTypes.typeProp){
						int ob = XobUtil.getObjectOfPropName(acyc, ptrName);
						SimpleVar32 obVar = pointerToVar.get(ob);
						if(obVar != null){
							obVar.firePropChangeEvent(this, ptrName);
						}
					}
				//}
			}
			
			//throw new RuntimeException("TODO code for deleting var, including telling any listeners about its new value of 0/()/nil. var="+ptrName);
			//TODO Count all acyc.oneLessRefFromOutside for prop and nonprop views of it, where relevant.
		}else{
			SimpleVar32 var = pointerToVar.get(ptrName);
			if(var == null){
				var = new SimpleVar32(this, ptrName, ptrNewValue);
				pointerToVar.put(ptrName, var);
			}
			var.setValue(ptrNewValue); //does nonprop events
			RefCounter32 acyc = acyc();
			int type = Acyc32Util.typeOf(acyc, ptrNewValue);
			if(type == XobTypes.typeProp){
				int ob = XobUtil.getObjectOfPropName(acyc, ptrName);
				SimpleVar32 obVar = pointerToVar.get(ob);
				if(!obVar.props.contains(ptrName)){
					acyc.oneMoreRefFromOutside(ptrName);
				}
				obVar.props.add(ptrName);
				obVar.firePropChangeEvent(this, ptrName);
			}
		}
		
		//"Also, prilist and def should be props of any var, and a mindmap item's var doesnt need a value at all, just its props"
		
		//"Also, prilist prop must be bidirectional and updated in realtime automatically by adding whats missing at the end."

		/*System.out.println("varMap set key="+ptrName+" value="+ptrNewValue);
		SimpleVar32 var = pointerToVar.get(ptrName);
		if(var == null){
			if(ptrName == 0) throw new RuntimeException("Cant set a value for key=nil. Tried to set nil's value to: "+ptrNewValue);
			var = new SimpleVar32(this, ptrName, ptrNewValue);
			pointerToVar.put(ptrName, var);
			int typeOfPtrName = Acyc32Util.typeOf(acyc, ptrName);
			if(typeOfPtrName == XobTypes.typeProp){
				int untypedProp = Acyc32Util.valueOf(acyc, ptrName); //(object propName)
				int propOfWhat = acyc.left(untypedProp);
				SimpleVar32 propOfWhatVar = pointerToVar.get(propOfWhat);
				if(propOfWhatVar != null) propOfWhatVar.afterPropCreate(ptrName);
			}
		}else{
			if(ptrNewValue == 0) throw new RuntimeException("Setting a var's value to 0 in VarMap means delete. TODO write deletion code, considering VarListeners and PropListeners. Also consider if any listeners need to be removed because the SimpleVar may be removed during this.");
			var.setValue(ptrNewValue);
		}*/
	}
	
	/** returns 0/()/nil if var doesnt exist in this VarMap */
	public int get(int varName){
		SimpleVar32 var = pointerToVar.get(varName);
		//System.out.println("varMap get key="+varName+" value="+(var==null ? 0 : var.getValue()));
		return var==null ? 0 : var.getValue();
	}
	
	/** Call this whenever value changes, not including setting the var to its current value */
	protected void addToEventStream(int ptrName, int ptrNewValue){
		int keyVal = XobUtil.keyValGlobal(ptrName, ptrNewValue);
		acyc.oneLessRefFromOutside(eventStream);
		eventStream = XobUtil.addPrefixToListPow2(Glo.econacyc, keyVal, eventStream);
		acyc.oneMoreRefFromOutside(eventStream);
	}

	/** Counts RefCounter32.oneMoreReferenceFromOutside in SimpleVar32 */
	public void startListen(VarListener32 listener, int ptrVarName){
		if(ptrVarName == 0) throw new RuntimeException("Cant listen to nil. listener="+listener);
		//System.out.println("varMap startListen listener="+listener+" target="+ptrVarName);
		SimpleVar32 var = pointerToVar.get(ptrVarName);
		if(var == null){
			int ptrVarValue = 0; //Set vars value to nil
			//When start listening to a var that has not been given a value in this VarMap,
			//set its value to 0. 2015-8-1 this confused me in EditPrilist.onChange when
			//it kept crashing because a mindmapItemName that I had just typed had value 0.
			//My 2 error were not setting its value to an empty mindmapItem when adding it and
			//assuming that all values are mindmapItems.
			pointerToVar.put(ptrVarName, var = new SimpleVar32(this, ptrVarName, ptrVarValue));
		}
		var.startListen(listener);
	}

	/** Counts RefCounter32.oneLessReferenceFromOutside in SimpleVar32.
	TODO delete var when its value is 0 and it has no VarListener (including direct and prop listening).
	*/
	public void stopListen(VarListener32 listener, int ptrVarName) {
		//System.out.println("varMap stopListen listener="+listener+" target="+ptrVarName);
		SimpleVar32 var = pointerToVar.get(ptrVarName);
		if(var != null){
			var.stopListen(listener);
			//System.out.println("TODO need storage of the key/value pairs somewhere if having no listeners causes the mapping to be deleted. Also, there are no listeners when "+OldDataFormatYear2015.class.getName()+" first adds them to "+VarMap32.class.getName()+" so should they be deleted right away? I dont want to create a varMapState every time this happens, but there may be no way around it. Think about it. See the commented code below this, and uncomment when figure this out.");
			/*TOOD if(var.howManyListeners() == 0){
				pointerToVar.remove(ptrVarName);
				//Old text before "Counts RefCounter32.oneLessReferenceFromOutside in SimpleVar32" and oneMore*.
				//TODO unlock address if its the last, but that actually needs reference counting including references outside the system
				//and if its an EconAcyc, the listening to vars should require payment (in units of lispPair) the same as any other pointer.
				//TODO create new referenceCounter subtype of Acyc thats between Acyc and EconAcyc,
				//but how to calculate the payment from external references?
				//Maybe an internal object needs to be created for each external reference.
			}*/
		}
	}

	public VarListener32[] listeners(int ptrVarName){
		SimpleVar32 var = pointerToVar.get(ptrVarName);
		if(var == null) return new VarListener32[0];
		return var.listeners();
	}
	
	public void startPropListen(VarListener32 listener, int propOfWhat){
		SimpleVar32 var = pointerToVar.get(propOfWhat);
		if(var == null){
			if(propOfWhat == 0) throw new RuntimeException("Cant set a value for key=nil. Tried to set nil's value to: "+propOfWhat);
			var = new SimpleVar32(this, propOfWhat, 0);
		}
		pointerToVar.put(propOfWhat, var);
		var.startPropListen(listener);
	}
	
	/** TODO delete var when its value is 0 and it has no VarListener (including direct and prop listening) */
	public void stopPropListen(VarListener32 listener, int propOfWhat){
		SimpleVar32 var = pointerToVar.get(propOfWhat);
		if(var != null) var.stopPropListen(listener);
		//TODO when to delete a Var? When nothing listens to it?
		//Thats not right if VarMap is used as primary storage for mindmapItems, but I need
		//to find somewhere else to store those mappings since this is an event system.
	}

	public int[] varNames(int fromInclusive, int toExclusive) {
		throw new RuntimeException("TODO maybe use TreeMap<Integer,SimpleVar32>?");
	}

	/** A pointer into Acyc. Only the new parts need to be appended to file if Acyc is in a RandomAccessFile */
	public int varMapState(){ return eventStream; }

}
