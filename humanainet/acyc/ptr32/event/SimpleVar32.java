/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32.event;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.acyc.ptr32.RefCounter32;
import humanainet.mindmap.MindmapUtil;

public class SimpleVar32{
	
	protected final VarMap32 varMap;
	
	protected final Set<VarListener32> listeners = new HashSet();
	
	/** Starts null. Created when first listener is added. In most Vars this wont be created
	because there are more props than vars they are props of, and no all vars have any props.
	*/
	protected Set<VarListener32> propListeners;
	
	protected int ptrName, ptrValue;
	
	/** Starts null. Created when first prop is added. Key is (ptrName propName),
	where ptrName is in this SimpleVar.
	*
	protected Map<Integer,SimpleVar32> props;
	*/
	protected Set<Integer> props;
	
	public SimpleVar32(VarMap32 varMap, int ptrName, int ptrValue){
		this.varMap = varMap;
		this.ptrName = ptrName;
		this.ptrValue = ptrValue;
		RefCounter32 acyc = varMap.acyc();
		acyc.oneMoreRefFromOutside(ptrName);
		acyc.oneMoreRefFromOutside(ptrValue);
	}
	
	public void setValue(int newPtrValue){
		varMap.acyc().oneLessRefFromOutside(ptrValue);
		ptrValue = newPtrValue;
		varMap.acyc().oneMoreRefFromOutside(ptrValue);
		fireChangeEvent(varMap, ptrName);
	}
	
	public int getValue(){ return ptrValue; }
	
	public int getName(){ return ptrName; }
	
	/** prop is (object propName) where object is my getValue() and propName is any lispPair *
	public SimpleVar32 getPropVar(int prop, boolean createIfNotExist){
		if(props == null) props = new HashMap();
		SimpleVar32 propVar = props.get(prop);
		if(createIfNotExist && propVar == null){
			int type = Acyc32Util.typeOf(varMap.acyc(), prop);
			if(type != MindmapUtil.typeProp) throw new RuntimeException("Not a prop: "+prop);
			propVar = varMap.get
		}
	}*/
	
	public int[] getProps(){
		if(props == null) return new int[0];
		int p[] = new int[props.size()];
		int pSize = 0;
		for(Integer i : props) p[pSize++] = i;
		return p;
	}
	
	/*public void afterPropCreate(int prop){
		//If it doesnt exist in VarMap, its value is 0/()/nil and if so the listeners will find that if they look
		if(propListeners != null) for(VarListener32 listener : propListeners){
			listener.afterVarChange(varMap, prop);
		}
		if(props == null) props = new HashSet();
		if(!props.contains(prop)){
			props.add(prop);
			varMap.acyc().oneMoreRefFromOutside(prop);
		}
	}
	
	public void afterPropRemove(int prop){
		varMap.set(prop, 0); //removing it again wont hurt anything. Just in case this is called before prop remove.
		props.remove(prop);
		if(propListeners != null) for(VarListener32 listener : propListeners){
			listener.afterVarChange(varMap, prop);
		}
		varMap.acyc().oneLessRefFromOutside(prop);
	}*/
	
	public void startListen(VarListener32 listener){
		varMap.acyc().oneMoreRefFromOutside(ptrName);
		listeners.add(listener);
	}
	
	public void stopListen(VarListener32 listener){
		varMap.acyc().oneLessRefFromOutside(ptrName);
		listeners.remove(listener);
	}
	
	public void startPropListen(VarListener32 listener){
		if(propListeners == null) propListeners = new HashSet();
		if(listeners.contains(listener)) return;
		propListeners.add(listener);
		if(props != null) for(Integer i : props) listener.afterVarChange(varMap, i);
	}
	
	public void stopPropListen(VarListener32 listener){
		if(propListeners == null || !propListeners.contains(listener)) return;
		propListeners.remove(listener);
	}
	
	public VarListener32[] listeners(){
		return listeners.toArray(new VarListener32[0]);
	}
	
	public int howManyListeners(){
		return listeners.size();
	}
	
	protected void fireChangeEvent(VarMap32 varMap, int name){
		for(VarListener32 listener : listeners){
			listener.afterVarChange(varMap, name);
		}
	}
	
	/** propName is XobTypes.typeProp typed form of (object name) */
	protected void firePropChangeEvent(VarMap32 varMap, int propName){
		if(propListeners != null) for(VarListener32 listener : propListeners){
			listener.afterVarChange(varMap, propName);
		}
	}
	
	public String toString(){
		return "[SimpleVar "+ptrName+" -> "+ptrValue+"]";
	}

}