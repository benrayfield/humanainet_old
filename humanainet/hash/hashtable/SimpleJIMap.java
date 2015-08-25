/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import humanainet.common.MathUtil;
import humanainet.hash.hashtable.hashers.JHasherUtil;

/** Use as much final and private as possible as optimization. JIMap needs to be extremely fast. */
public final class SimpleJIMap implements JIMap{
	
	//TODO OPTIMIZE require capacity to be a power of 2 and use bit masking to avoid % and Math.abs. Would be faster
	
	/** this mask wraps into range of longs[] and rounds last bit down to even.
	This reduces quality of hashing compared to a capacity thats a prime number,
	but we can balance that by using better hash functions.
	This is an optimization to avoid calculating remainder.
	*/
	public final int mask;
	
	/** A slot is an even/odd pair of longs.
	This is more efficient than 2 separate arrays because the memory is near.
	As a map from long to int, this wastes 1/4 of its memory,
	but whats needed from this class is primarily speed.
	<br><br>
	OLD TEXT:
	Groups of 3 ints: long key is 2 ints. Next is the int value it maps to.
	This array contains int value -1 everywhere thats not used.
	<br><br>
	TODO final? Maybe subclass can create bigger array during a rehash.
	*
	private int ints[];
	*/
	private final long longs[];
	
	//"TODO use tombstones for deletion and clear them when rehash. TODO which int value means tombstone? The first 2 ints must be the key, and the value is the tombstone."
	
	//"TODO use interface for hash functions that depend on long key. maybe keep long state the whole way."
	
	//"TODO use long array instead of int, and make it a long to long map, casting down to int when needed? I choose to save memory as 3 ints at a time instead of 2 longs."
	
	/** For repeated hashing, start with the long key, get the long it hashes to, wrap that around slot array,
	and repeat using the same long (not the wrapped value which has less bits) until find the slot needed.
	*/
	private final JHasher hashers[];
	
	/** Number of key/value pairs, each occupyingints used divided by 3 */
	private int size;
	
	private int tombstones;
	
	public static final int valueEmpty = -1;
	
	public static final int valueTombstone = -2;
	
	public SimpleJIMap(SecureRandom forCreatingHashers, int logBase2OfCapacity){
		this(
			JHasherUtil.hashers( forCreatingHashers, howManyHashersForCapacity(1<<logBase2OfCapacity) ),
			logBase2OfCapacity
		);
	}
	
	//int maxSlotForKey = -1;
	
	/** As an optimization, capacity cant change, but can still rehash into the same size.
	This fits well with Acyc having constant size. Create a new hashtable when rehash.
	<br><br>
	Make sure to give enough hashers that more will never be needed, not even once.
	Key goes into each if what was returned by the last didnt work.
	<br><br>
	Its recommended to use howManyHashersForCapacity() hashers, or just use 64 if you want a constant.
	If hash functions are good, an average of 2 hashers should be used if you use half capacity including tombstones,
	but I see it using much more than that. Maybe I need to redesign SimpleJHasher from (j^a)*(j^b) - (j^c)*(j^d)
	but it looks like a good hash function if a b c and d are randomly chosen.
	Maybe the problem is using a capacity thats a power of 2 instead of a prime number.
	*/
	public SimpleJIMap(JHasher hashers[], int logBase2OfCapacity){
		this.hashers = hashers.clone();
		int capacity = 1 << logBase2OfCapacity;
		longs = new long[capacity*2];
		mask = longs.length-2; //-1 would wrap into longs[]. -1 more than that rounds last bit down to even.
		clear();
	}
	
	public int size(){ return size; }
	
	public int tombstones(){ return tombstones; }
	
	private long gets = 0;
	private long cyclesInGet = 0;
	public long gets(){ return gets; }
	public double aveCyclesPerGet(){ return (double)cyclesInGet/puts; }
	
	private long puts = 0;
	private long cyclesInPut = 0;
	public long puts(){ return puts; }
	public double aveCyclesPerPut(){ return (double)cyclesInPut/puts; }
	
	private long hybridgetputs = 0;
	private long cyclesInHybridgetputs = 0;
	public long hybridgetputs(){ return puts; }
	public double aveCyclesPerHybridgetput(){ return (double)hybridgetputs/cyclesInHybridgetputs; }
	
	public int getIfExistElsePut(long key, int value){
		hybridgetputs++; //TODO remove this
		//if(value < 0) throw new IllegalArgumentException( //TODO is it worth the speed loss to check this?
		//	"Values must be nonnegative. key="+key+" value="+value);
		int i = 0;
		long hash = key;
		//final JHasher hasher = hashers[0];
		while(true){ //For speed, trust hasher not to cause infinite loop and the array not to get too full
			cyclesInHybridgetputs++; //TODO remove this
			//long hash = hashers[i].hash(key);
			//hash = hasher.hash(hash);
			hash = hashers[i].hash(key+hash);
			//hash = hash*31+key+3;
			//TODO is there a way to avoid Math.abs here while JHasher still uses all 64 bits?
			//int slotForKey = (int)(Math.abs(hash)%longs.length) & 0x7ffffffe; //absVal sign bit and round down last bit
			int slotForKey = ((int)hash)&mask;
			//maxSlotForKey = Math.max(maxSlotForKey, slotForKey);
			int slotForValue = slotForKey+1;
			//countInArrayHowManyEmptySlots() is very slow...
			//System.out.println("(size="+size+" emp="+countInArrayHowManyEmptySlots()
			//	+" maxSlotForKey="+maxSlotForKey+") slotForKey="+slotForKey+" put key="+key+" valueToPut="+value
			//	+" i="+i+" slotForKey="+slotForKey+" obsKey="+longs[slotForKey]+" obsVal="+longs[slotForValue]);
			if(longs[slotForValue] == valueEmpty){ //It doesnt exist. Put it here.
				longs[slotForKey] = key;
				longs[slotForValue] = value;
				size++;
				//System.out.println("Done put at slotForKey="+slotForKey);
				return value;
			}else if(longs[slotForKey] == key){ //Either found it or its tombstone.
				if(longs[slotForValue] == valueTombstone){ //It doesnt exist. Put it here replacing its tombstone.
					longs[slotForValue] = value;
					tombstones--;
					return value;
				}else{ //Found it. Return existing value, ignoring value parameter.
					return (int)longs[slotForValue];
				}
			}
			i++;
		}
	}
	
	public int get(long key){
		gets++;
		int i = 0;
		long hash = key;
		//final JHasher hasher = hashers[0];
		while(true){ //For speed, trust hasher not to cause infinite loop and the array not to get too full
			cyclesInGet++;
			//long hash = hashers[i].hash(key);
			//hash = hasher.hash(hash);
			hash = hashers[i].hash(key+hash);
			//hash = hash*31+key+3;
			//TODO is there a way to avoid Math.abs here while JHasher still uses all 64 bits?
			//int slotForKey = (int)(Math.abs(hash)%longs.length) & 0x7ffffffe; //absVal sign bit and round down last bit
			int slotForKey = ((int)hash)&mask;
			int slotForValue = slotForKey+1;
			//System.out.println("(size="+size+") get key="+key+" i="+i
			//	+" slotForKey="+slotForKey+" obsKey="+longs[slotForKey]+" obsVal="+longs[slotForValue]);
			if(longs[slotForValue] == valueEmpty) return valueEmpty;
			if(longs[slotForKey] == key){ //found its value or tombstone
				long value = longs[slotForValue];
				if(value == valueTombstone) return valueEmpty;
				return (int)value;
			}
			i++;
		}
	}
	
	//"TODO use a different hash function for each next hash. Generate many such functions using Xor and Plus of the JHasher classes (existing ways to combine them) and new SimpleJHasher created with different run of SecureRandom"
	
	public void put(long key, int value){
		puts++; //TODO remove this
		//if(value < 0) throw new IllegalArgumentException( //TODO is it worth the speed loss to check this?
		//	"Values must be nonnegative. key="+key+" value="+value);
		int i = 0;
		long hash = key;
		//final JHasher hasher = hashers[0];
		while(true){ //For speed, trust hasher not to cause infinite loop and the array not to get too full
			cyclesInPut++; //TODO remove this
			//long hash = hashers[i].hash(key);
			//hash = hasher.hash(hash);
			hash = hashers[i].hash(key+hash);
			//hash = hash*31+key+3;
			//TODO is there a way to avoid Math.abs here while JHasher still uses all 64 bits?
			//int slotForKey = (int)(Math.abs(hash)%longs.length) & 0x7ffffffe; //absVal sign bit and round down last bit
			int slotForKey = ((int)hash)&mask;
			//maxSlotForKey = Math.max(maxSlotForKey, slotForKey);
			int slotForValue = slotForKey+1;
			//countInArrayHowManyEmptySlots() is very slow...
			//System.out.println("(size="+size+" emp="+countInArrayHowManyEmptySlots()
			//	+" maxSlotForKey="+maxSlotForKey+") slotForKey="+slotForKey+" put key="+key+" valueToPut="+value
			//	+" i="+i+" slotForKey="+slotForKey+" obsKey="+longs[slotForKey]+" obsVal="+longs[slotForValue]);
			if(longs[slotForValue] == valueEmpty){ //It doesnt exist. Put it here.
				longs[slotForKey] = key;
				longs[slotForValue] = value;
				size++;
				//System.out.println("Done put at slotForKey="+slotForKey);
				return;
			}else if(longs[slotForKey] == key){ //Found it. Update value.
				if(longs[slotForValue] == valueTombstone) tombstones--;
				longs[slotForValue] = value;
				return;
			}
			i++;
		}
	}
	
	public void remove(long key){
		int i = 0;
		long hash = key;
		//final JHasher hasher = hashers[0];
		while(true){ //For speed, trust hasher not to cause infinite loop and the array not to get too full
			//long hash = hashers[i].hash(key);
			//hash = hasher.hash(hash);
			hash = hashers[i].hash(key+hash);
			//hash = hash*31+key+3;
			//TODO is there a way to avoid Math.abs here while JHasher still uses all 64 bits?
			//int slotForKey = (int)(Math.abs(hash)%longs.length) & 0x7ffffffe; //absVal sign bit and round down last bit
			int slotForKey = ((int)hash)&mask;
			int slotForValue = slotForKey+1;
			if(slotForValue == valueEmpty) return; //didnt exist
			if(longs[slotForKey] == key){ //found its value or tombstone
				if(longs[slotForValue] != valueTombstone){
					longs[slotForValue] = valueTombstone;
					tombstones++;
					size--;
				}
				return;
			}
			i++;
		}
	}
	
	/** unsorted */
	public long[] keys(){
		long keys[] = new long[size];
		int keysFound = 0;
		for(int i=0; i<longs.length; i+=2){
			long value = longs[i+1];
			if(0 <= value) keys[keysFound++] = longs[i];
		}
		return keys;
	}
	
	public void clear(){
		Arrays.fill(longs, valueEmpty);
		size = 0;
	}
	
	/** Instead of trusting size, count how many slots have value valueEmpty */
	public int countInArrayHowManyEmptySlots(){
		int count = 0;
		for(int i=1; i<longs.length; i+=2){
			if(longs[i] == valueEmpty) count++;
		}
		return count;
	}
	
	/** newSizeIfWasFull should be about twice the actual expected size since it gets really slow when near full.
	Its in units of key/value pairs including tombstones, not the specific units the array is made of.
	*
	public void rehash(int newSizeIfWasFull){
		throw new RuntimeException("TODO");
	}*/
	
	
	public static int howManyHashersForCapacity(int capacity){
		return 16 + (int)(4*Math.log(capacity));
		//return 256;
	}
	
	/** test this class */
	public static void main(String args[]){
		int logBase2OfCapacity = 16;
		int capacity = 1<<logBase2OfCapacity;
		SimpleJIMap map = new SimpleJIMap(MathUtil.strongRand, logBase2OfCapacity);
		Map<Long,Integer> data = new HashMap(); //verify JIMap stores and gives back this data
		while(data.size() < capacity/2){
			long key = Math.abs(MathUtil.strongRand.nextLong());
			int value = Math.abs(MathUtil.strongRand.nextInt());
			data.put(key, value);
			map.put(key, value);
			if(map.size() == 31000){
				System.out.println("Which slots are occupied:");
				for(int i=0; i<map.longs.length; i+=2){
					long slotKey = map.longs[i];
					long slotValue = map.longs[i+1];
					if(slotValue == valueEmpty && slotKey != valueEmpty) throw new RuntimeException("key="+slotKey+" value="+slotValue+" at i="+i);
					System.out.print(0<=slotValue ? '1' : '.');
				}
				//return;
			}
		}
		for(Map.Entry<Long,Integer> entry : data.entrySet()){
			long key = entry.getKey();
			int correctValue = entry.getValue();
			int observedValue = map.get(key);
			if(observedValue != correctValue) throw new RuntimeException(
				"key="+key+" observedValue="+observedValue+" correctValue="+correctValue);
			System.out.println("Got correct value for key="+key+" value="+observedValue);
		}
		map.put(55L, 56);
		int fiftySix = map.get(55L);
		if(fiftySix != 56) throw new RuntimeException("Didnt map 55L to 56 correctly.");
		map.remove(55L);
		fiftySix = map.get(55L);
		if(fiftySix != -1) throw new RuntimeException("Didnt unmap 55L correctly");
		map.put(55L, 57);
		int fiftySeven = map.get(55L);
		if(fiftySeven != 57) throw new RuntimeException("Didnt remap 55L to 57 correctly");
		throw new RuntimeException("TODO remove a random half of the key/value pairs, verify they are gone, then add more, then check its contents again.");
	}

}
