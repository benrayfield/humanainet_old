/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

import java.util.HashMap;
import java.util.Map;

import humanainet.acyc.Const;
import humanainet.acyc.ptr32.alwaysDedup.SimpleEconDAcycI;
import humanainet.common.CoreUtil;
import humanainet.hash.hashtable.JIMap;
import humanainet.hash.hashtable.SimpleJIMap;
import humanainet.xorlisp.XorlispVM32;
import humanainet.xorlispui.EconDAcycIUi;

/** Common objects with easy functions to get them.
All funcs return a tree of powerOf2Item with bit0 or bit1 at each leaf.
All such possible values are cached up to 2^16-1.
*/ 
public class CacheAcyc32{
	
	public final RefCounter32 acyc;
	
	public final int end, pairOfEnd, bit0, bit1, typVal;
	
	public int bitToPointer(boolean b){ return b ? bit1 : bit0; }
	
	public int twoBitsToPointer(int zeroToThree){ return twoBits[zeroToThree&3]; }
	public int twoBitsToPointer(boolean two, boolean one){
		if(two) return twoBits[one ? 3 : 2];
		return twoBits[one ? 1 : 0];
	}
	protected final int twoBits[] = new int[4];
	
	public int fourBitsToPointer(int zeroToFifteen){ return fourBits[zeroToFifteen&15]; }
	public int fourBitsToPointer(boolean eight, boolean four, boolean two, boolean one){
		int i = 0;
		if(eight) i |= 8;
		if(four) i |= 4;
		if(two) i |= 2;
		if(one) i |= 1;
		return fourBits[i];
	}
	protected final int fourBits[] = new int[0x10];
	
	public int byteToPointer(byte b){ return eightBits[b&0xff]; }
	protected final int eightBits[] = new int[0x100];
	
	protected final Map<Integer,Byte> pointerToByte = new HashMap();
	public boolean isByte(int pointer){ return pointerToByte.containsKey(pointer); }
	/** Throws if its not a byte */
	public byte pointerToByte(int pointer){
		Byte b = pointerToByte.get(pointer);
		if(b == null) throw new RuntimeException("Not a byte at pointer="+pointer);
		return b;
	}
	
	public int charToPointer(char c){ return sixteenBits[c]; }
	public int shortToPointer(short s){ return sixteenBits[s&0xffff]; }
	protected final int sixteenBits[] = new int[0x10000];
	
	//TODO OPTIMIZE use a JIMap for this
	protected final Map<Integer,Short> pointerToShort = new HashMap();
	//protected final JIMap pointerToShort = new SimpleJIMap(CoreUtil.strongRand, 18);
	public boolean isShortOrChar(int pointer){ return pointerToShort.containsKey(pointer); }
	/** Throws if its not a short or char */
	public short pointerToShort(int pointer){
		Short s = pointerToShort.get(pointer);
		if(s == null) throw new RuntimeException("Not a short or char at pointer="+pointer);
		return s;
		
		//int i = pointerToShort.get(pointer);
		//if(i == -1) throw new RuntimeException("Not a short or char at pointer="+pointer);
	}
	/** Throws if its not a short or char */
	public char pointerToChar(int pointer){
		return (char)pointerToShort(pointer);
	}
	
	public CacheAcyc32(RefCounter32 acyc){
		this.acyc = acyc;
		end = 0;
		pairOfEnd = acyc.pair(end, end);
		bit0 = acyc.pair(end, pairOfEnd);
		bit1 = acyc.pair(pairOfEnd, end);
		typVal = acyc.pair(pairOfEnd, pairOfEnd);
		for(int i=0; i<4; i++){ //2 bits
			twoBits[i] = acyc.pair( bitToPointer((i&2)!=0), bitToPointer((i&1)!=0) );
			acyc.oneMoreRefFromOutside(twoBits[i]);
		}
		for(int i=0; i<0x10; i++){ //4 bits
			fourBits[i] = acyc.pair( twoBitsToPointer(i/4), twoBitsToPointer(i%4) );
			acyc.oneMoreRefFromOutside(fourBits[i]);
		}
		for(int i=0; i<0x100; i++){ //8 bits
			eightBits[i] = acyc.pair( fourBitsToPointer(i/16), fourBitsToPointer(i%16) );
			pointerToByte.put(eightBits[i], (byte)i);
			acyc.oneMoreRefFromOutside(eightBits[i]);
		}
		for(int i=0; i<0x10000; i++){ //16 bits
			sixteenBits[i] = acyc.pair( byteToPointer((byte)(i/0x100)), byteToPointer((byte)i) );
			pointerToShort.put(sixteenBits[i], (short)i);
			acyc.oneMoreRefFromOutside(sixteenBits[i]);
		}
	}
	
	/** Returns a pointer for use in the Acyc32, not the literal int value.
	Complete binary forest depth 5 since 2^5=32 bits in an int.
	*/
	public int untypedInt(int i){
		int high = shortToPointer((short)(i>>16));
		int low = shortToPointer((short)i);
		return acyc.pair(high, low);
	}
	
	/** complete binary forest depth 6 since 2^6=64 bits in a long */
	public int untypedLong(long j){
		//TODO cache blocks of all 0s or all 1s in CacheAcyc32, but does checking for parts like that slow it too much?
		int high = untypedInt((int)(j>>32));
		int low = untypedInt((int)j);
		return acyc.pair(high, low);
	}
	
	public int untypedInt128(long high, long low){
		//TODO cache blocks of all 0s or all 1s in CacheAcyc32, but does checking for parts like that slow it too much?
		return acyc.pair(untypedLong(high), untypedLong(low));
	}
	
	public int untypedInt256(long bits192, long bits128, long bits64, long bits0){
		//TODO cache blocks of all 0s or all 1s in CacheAcyc32, but does checking for parts like that slow it too much?
		int high = untypedInt128(bits192,bits128);
		int low = untypedInt128(bits64,bits0);
		return acyc.pair(high, low);
	}
	
	public static void main(String args[]){
		Map<Integer,String> names = new HashMap();
		names.put(Const.end, ".");
		names.put(Const.bit0, "<0>");
		names.put(Const.bit1, "<1>");
		names.put(Const.typVal, "<typVal>");
		//SimpleLisp32 x = new SimpleLisp32((byte)22);
		RefCounter32 x = new SimpleEconDAcycI((byte)22);
		CacheAcyc32 cache = new CacheAcyc32(x);
		
		System.out.println("end = "+TextUtil32.toString(x, Const.end, names));
		System.out.println("pairOfEnd = "+TextUtil32.toString(x, Const.pairOfEnd, names));
		System.out.println("bit0 = "+TextUtil32.toString(x, Const.bit0, names));
		System.out.println("bit1 = "+TextUtil32.toString(x, Const.bit1, names));
		System.out.println("typVal = "+TextUtil32.toString(x, Const.typVal, names));
		
		for(int i=0; i<cache.twoBits.length; i++){
			System.out.println("twoBits["+i+"] = "+TextUtil32.toString(x, cache.twoBits[i], names));
		}
		for(int i=0; i<cache.fourBits.length; i++){
			System.out.println("fourBits["+i+"] = "+TextUtil32.toString(x, cache.fourBits[i], names));
		}
		for(int i=0; i<cache.eightBits.length; i++){
			System.out.println("eightBits["+i+"] = "+TextUtil32.toString(x, cache.eightBits[i], names));
		}
		for(int i=0; i<cache.sixteenBits.length; i++){
			System.out.println("sixteenBits["+i+"] = "+TextUtil32.toString(x, cache.sixteenBits[i], names));
		}
		
		int charZ = cache.charToPointer('z');
		System.out.println("z = "+TextUtil32.toString(x, charZ, names));
	}

}