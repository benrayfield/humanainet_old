/** Ben F Rayfield offers Xob (Xorlisp Objects) opensource GNU LGPL */
package humanainet.xob;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SizeRequirements;

import humanainet.acyc.Const;
import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.Acyc32;
import humanainet.acyc.ptr32.CacheAcyc32;
import humanainet.acyc.ptr32.RefCounter32;
import humanainet.acyc.ptr32.TextUtil32;
import humanainet.acyc.ptr32.alwaysDedup.SimpleEconDAcycI;
import humanainet.common.TestedIn;
import humanainet.acyc.ptr32.Acyc32Util;
import humanainet.xorlisp.XorlispVM32;
import humanainet.xorlisp.old.DebugUtil;

public class XobUtil{
	
	/** Uses typVal and the standard prefix that means word to reuse (if exists) or create a token object
	for use as a mindmap name or any word or phraseInCamelCase, any UTF16 bits.
	Normally the text is either completely whitespace or completely visible chars and not whitespace.
	*/
	public static int token(CacheAcyc32 cAcyc, int tokenType, String token){
		int tokenBits = XobUtil.stringToListOfPowerOf2(cAcyc, token);
		return Glo.econacyc.pair(Const.typVal, cAcyc.acyc.pair(tokenType, tokenBits));
	}
	
	public static int tokenGlobal(String token){
		return token(Glo.cacheacyc, XobTypes.typeToken, token);
	}
	
	/** See typeProp */
	public static int propName(Acyc32 acyc, int object, int prop){
		return Acyc32Util.wrapObjectInType(acyc, XobTypes.typeProp, acyc.pair(object,prop));
	}
	
	public static boolean isPropName(Acyc32 acyc, int object){
		return Acyc32Util.typeOf(acyc, object) == XobTypes.typeProp;
	}
	
	/** gets object in the XobTypes.typeProp typed form of (object prop) */
	public static int getObjectOfPropName(Acyc32 acyc, int propName){
		if(!isPropName(acyc, propName)) throw new RuntimeException("Not a prop name: "+propName);
		int objectThenProp = Acyc32Util.valueOf(acyc, propName);
		return acyc.left(objectThenProp);
	}
	
	/** gets prop in the XobTypes.typeProp typed form of (object prop) */
	public static int getPropOfPropName(Acyc32 acyc, int propName){
		if(!isPropName(acyc, propName)) throw new RuntimeException("Not a prop name: "+propName);
		int objectThenProp = Acyc32Util.valueOf(acyc, propName);
		return acyc.right(objectThenProp);
	}
	
	//TODO...
	//"qnAcycnetStringAsCharOrByte has been answered as char"
	//"TODO test this code with strings"
	//"TODO new simpler mindmap using unifiedAcycnet"

	/** List of powerOf2Item of bits is the standard way a stateless stack of bits is represented. */
	public static String listOfPowerOf2ToString(CacheAcyc32 x, int list){
		int size = sizeOfListOfPowerOf2Items(x.acyc, list);
		if((size&15) == 0){
			//can be viewed as a string, but is it a short enough string and without whitespace?
			int sizeInChars = size>>4;
			StringBuilder sb = new StringBuilder();
			int listNode = list;
			int whichPowerOf2 = 0;
			while(listNode != 0){
				int powerOf2Item = x.acyc.left(listNode); //The first 4 are certainly nil
				if(powerOf2Item != 0) getChars(x, sb, powerOf2Item, whichPowerOf2);
				listNode = x.acyc.right(listNode);
				whichPowerOf2++;
			}
			return sb.toString();
		}
		throw new RuntimeException("Size is not a multiple of 16 bits");
	}

	public static int stringToListOfPowerOf2(CacheAcyc32 cAcyc, String s){
		//System.out.println("TODO finish this extreme optimization to create far less objects");
		//this way uses too many objects return stringToListOfPowerOf2_oneBitAtATime(cAcyc.acyc, s);
		//return stringToListOfPowerOf2_oneBitAtATime(cAcyc.acyc, s);
		
		int listOfPowerOf2Item = 0;
		int prefixCharsRemaining = s.length();
		int powerOf2 = lastPowerOf2NotExceeding(prefixCharsRemaining);
		while(0 <= powerOf2){
			int howManyCharsThisBlock = 1<<powerOf2;
			int powerOf2ItemOrNil;
			if(howManyCharsThisBlock <= prefixCharsRemaining){
				int fromInclusive = prefixCharsRemaining-howManyCharsThisBlock;
				int toExclusive = prefixCharsRemaining;
				powerOf2ItemOrNil = completeBinaryTreeOfBits(cAcyc, s, fromInclusive, toExclusive);
				prefixCharsRemaining -= howManyCharsThisBlock;
			}else{
				powerOf2ItemOrNil = 0;
			}
			listOfPowerOf2Item = cAcyc.acyc.pair(powerOf2ItemOrNil, listOfPowerOf2Item);
			powerOf2--;
		}
		for(int i=0; i<4; i++){ //char is 2^4 bits
			listOfPowerOf2Item = cAcyc.acyc.pair(0, listOfPowerOf2Item);
		}
		return listOfPowerOf2Item;
		
	}
	
	public static int completeBinaryTreeOfBits(CacheAcyc32 cAcyc, String s, int fromInclusive, int toExclusive){
		int howManyChars = toExclusive-fromInclusive;
		if(howManyChars < 1) throw new RuntimeException("fromInclusive="+fromInclusive+" toExclusive="+toExclusive);
		if(howManyChars == 1) return cAcyc.charToPointer(s.charAt(fromInclusive));
		if((howManyChars&(howManyChars-1)) != 0) throw new RuntimeException(
			"Not a power of 2 number of chars: fromInclusive="+fromInclusive+" toExclusive="+toExclusive);
		int midIndex = fromInclusive+(howManyChars>>1);
		int left = completeBinaryTreeOfBits(cAcyc, s, fromInclusive, midIndex);
		int right = completeBinaryTreeOfBits(cAcyc, s, midIndex, toExclusive);
		return cAcyc.acyc.pair(left, right);
	}
	
	public static int lastPowerOf2NotExceeding(int i){
		int j = 1;
		int powerOf2 = 0;
		while(j <= i){
			powerOf2++;
			j <<= 1;
		}
		return powerOf2-1;
	}
	
	/** old code creates too many lispPair objects */
	@Deprecated //use stringToListOfPowerOf2 instead
	public static int stringToListOfPowerOf2_oneBitAtATime(Acyc32 acyc, String s){
		//TODO optimize by using LispCache32's char funcs, but for now addPrefixToListOfPowerOf2Items will work
		//and at least it guarantees no duplicate lispPairs (except that all of the bit sizes up to the
		//number of bits in the string will exist, and can be garbageCollected in new XorlispVM later)
		int listOfPowerOf2OfBits = 0;
		for(int i=s.length()-1; i>=0; i--){
			char c = s.charAt(i);
			//System.out.println("char = ["+c+"]");
			for(int b=0; b<16; b++){
				boolean bit = ((c>>b)&1)!=0;
				int pointerToBit = bit ? Const.bit1 : Const.bit0;
				//System.out.println("bit="+(bit?1:0)+" bitptr="+pointerToBit);
				listOfPowerOf2OfBits = addPrefixToListPow2(acyc, pointerToBit, listOfPowerOf2OfBits);
			}
		}
		return listOfPowerOf2OfBits;
	}
	
	public static String toShortString(Acyc32 x, int pointer){
		return ""+pointer; //TODO something a little more descriptive
	}
	
	public static int[] pointersInListPow2(Acyc32 acyc, int listPow2){
		int pointers[] = new int[sizeOfListOfPowerOf2Items(acyc, listPow2)];
		int sizeSoFar = 0;
		int listNode = listPow2;
		int whichPowerOf2 = 0;
		while(listNode != 0){
			int powerOf2Item = acyc.left(listNode);
			if(powerOf2Item != 0){
				//put 2^whichPowerOf2 items in pointers[size and higher]
				pointersInListPow2(acyc, powerOf2Item, pointers, sizeSoFar, whichPowerOf2);
				sizeSoFar += (1 << whichPowerOf2);
			}
			listNode = acyc.right(listNode);
			whichPowerOf2++;
		}
		if(sizeSoFar != pointers.length) throw new RuntimeException("Not a listPow2: "+listPow2);
		return pointers;
	}
	
	protected static void pointersInListPow2(Acyc32 acyc, int powerOf2Item,
			int getPointers[], int offsetInGetPointers, int recurseMore){
		if(recurseMore == 0){
			//Any object is a powerOf2Item as 2^0 items.
			if(!Acyc32Util.isObject(acyc, powerOf2Item)) throw new RuntimeException(
				"Not a powerOf2Item, in this case (2^0 number of objects, 1 of any object by Xorlisp32Util.isObject: "+powerOf2Item);
			getPointers[offsetInGetPointers] = powerOf2Item;
		}else{
			int sizeOfEachBranch = 1 << (recurseMore-1);
			int left = acyc.left(powerOf2Item);
			int right = acyc.right(powerOf2Item);
			pointersInListPow2(acyc, left, getPointers, offsetInGetPointers, recurseMore-1);
			pointersInListPow2(acyc, right, getPointers, offsetInGetPointers+sizeOfEachBranch, recurseMore-1);
		}
	}
	
	/** Only an object, satisfying isObject function, can be added,
	which does not include powerOf2Item.
	<br><br>
	A powerOf2Item is pair of pair of pair... a complete binary tree with an object at each leaf.
	A listOfPowerOf2Item has either nil or a powerOf2Item at each item in itself as a linkedList.
	Each place deeper in that linkedList is twice as big as the last,
	including doubling potential size where nils are to mark where to skip.
	<br><br>
	Does not verify this is a listOfPowerOf2Item because that would be inefficient.
	If you need to know, use a typVal with any root.
	*/
	public static int addPrefixToListPow2(Acyc32 acyc, int pointerToAdd, int list){
		if(!Acyc32Util.isObject(acyc, pointerToAdd)) throw new RuntimeException(
			"Not an object: "+toShortString(acyc,pointerToAdd));
		return addPrefixToListOfPowerOf2Items_recursive(acyc, pointerToAdd, list);
	}
	
	/** pointerToAdd may be a powerOf2Item. list cant be null.
	pointerToAdd must be the same size of powerOf2Item as the first item
	in list (left of list pair), which may be a single item if its 2^0.
	*/
	protected static int addPrefixToListOfPowerOf2Items_recursive(
			Acyc32 acyc, int pointerToAdd, int list){
		if(list == 0){ //was empty linkedList, return linkedList of 1 item
			return acyc.pair(pointerToAdd, 0);
		}
		int itemInList = acyc.left(list);
		int ahead = acyc.right(list);
		if(itemInList == 0){
			//This is a skipped powerOf2Item like a 0 base2 digit.
			//All listOfPowerOf2Item have at least 1 nonskipped item (is not nil).
			//Replace nil with pointerToAdd, and keep any larger powerOf2Items deeper in list
			return acyc.pair(pointerToAdd, ahead);
		}else{
			//Merge pointerToAdd with first item in list and insert it ahead in the list,
			//recursively merging ahead like carrying base2 digits,
			//but its a powerOf2Item list not a base2 number.
			int newPair = acyc.pair(pointerToAdd,itemInList);
			int mergedNewPairAhead = addPrefixToListOfPowerOf2Items_recursive(acyc, newPair, ahead);
			//like carrying a base2 digit, leaves a 0 so list is same size
			return acyc.pair(0, mergedNewPairAhead);
		}
	}
	
	/** Opposite of addPrefixToListOfPowerOf2Items. Does not return whats removed. Returns the new list.
	If list is empty, returns nil.
	*/
	public static int removeOneFromListOfPowerOf2Items(Acyc32 x, int list){
		System.out.println("removeOneFromListOfPowerOf2Items param = "+TextUtil32.toString(x, list, DebugUtil.defaultNamesForDebugging));
		if(list == 0) return 0; //list was empty, return nil.
		/*int itemInList = x.left(list);
		if(itemInList != 0){
			//Optimization, could have done this below after the loop.
			//Dont split a powerOf2Item. Just remove this leaf.
			int ahead = x.right(list);
			return x.pair(0, ahead);
		}*/
		int countNils = 0;
		int recurseList = list;
		while(x.left(recurseList) == 0){
			countNils++;
			recurseList = x.right(recurseList);
			System.out.println("recurseList = "+TextUtil32.toString(x, recurseList, DebugUtil.defaultNamesForDebugging));
		}
		//We passed something like: nil nil nil nil nil powerOf2Item...
		//Change powerOf2Item to nil and all those nils before it become smaller powerOf2Items.
		//recurseList is now the listNode whose left is the first nonnil powerOf2Item.
		int ahead = x.right(recurseList);
		System.out.println("ahead = "+TextUtil32.toString(x, ahead, DebugUtil.defaultNamesForDebugging));
		int newNilThenAheadOrJustNil = ahead==0 ? ahead : x.pair(0,ahead);
		System.out.println("newNilThenAheadOrJustNil = "+TextUtil32.toString(x, newNilThenAheadOrJustNil, DebugUtil.defaultNamesForDebugging));
		int toSplit = x.left(recurseList); //split everything in here except the smallest leftmost leaf
		System.out.println("toSplit = "+TextUtil32.toString(x, toSplit, DebugUtil.defaultNamesForDebugging));
		int newList = newNilThenAheadOrJustNil;
		System.out.println("newList = "+TextUtil32.toString(x, newList, DebugUtil.defaultNamesForDebugging));
		System.out.println("countNils="+countNils);
		for(int i=0; i<countNils; i++){
			int leaveAhead = x.right(toSplit);
			System.out.println("i="+i+" leaveAhead = "+TextUtil32.toString(x, leaveAhead, DebugUtil.defaultNamesForDebugging));
			newList = x.pair(leaveAhead, newList);
			System.out.println("i="+i+" newList = "+TextUtil32.toString(x, newList, DebugUtil.defaultNamesForDebugging));
			toSplit = x.left(toSplit); //powerOf2Item can be split on either side
			System.out.println("i="+i+" toSplit = "+TextUtil32.toString(x, toSplit, DebugUtil.defaultNamesForDebugging));
		}
		System.out.println("returning newList = "+TextUtil32.toString(x, newList, DebugUtil.defaultNamesForDebugging)+"\r\n\r\n\r\n\r\n");
		//removed the last value of toSplit
		return newList;
	}
	
	/** Returns the first object, not a powerOf2Item (which can contain a power of 2 number of objects) */
	public static int firstInListOfPowerOf2Items(XorlispVM32 x, int list){
		int recurseList = list;
		int powerOf2Item;
		while((powerOf2Item = x.left(recurseList)) == 0){
			recurseList = x.right(recurseList);
		}
		while(!Acyc32Util.isObject(x, powerOf2Item)){
			powerOf2Item = x.left(powerOf2Item);
		}
		return powerOf2Item; //leftmost in the first nonnil powerOf2Item
	}
	
	/** Size in units of items (its leafs), not in units of powerOf2Item */
	public static int sizeOfListOfPowerOf2Items(Acyc32 x, int list){
		if(list == 0) return 0;
		int size = 0;
		int mask = 1;
		int listNode = list;
		while(listNode != 0){
			if(x.left(listNode) != 0) size |= mask;
			listNode = x.right(listNode);
			mask <<= 1;
		}
		return size;
	}
	
	/** 0 <= index < sizeOfListOfPowerOf2Items(acyc,list) */
	@TestedIn(void.class)
	public static int getByIndexInListPow2(Acyc32 acyc, int list, int index){
		int indexRemaining = index;
		int whichPowOf2 = 0;
		int listNode = list;
		while(listNode != 0){
			int completeBinaryTreeOrNil = acyc.left(listNode);
			if(completeBinaryTreeOrNil != 0){
				int sizeOfBranch = 1<<whichPowOf2;
				if(sizeOfBranch/2 <= indexRemaining && indexRemaining < sizeOfBranch){
					return getInCompleteBinaryTree(acyc, completeBinaryTreeOrNil, whichPowOf2, indexRemaining);
				}
				indexRemaining -= sizeOfBranch;
			}
			listNode = acyc.right(listNode);
			whichPowOf2++;
		}
		throw new IndexOutOfBoundsException("index="+index+" list="+list);
	}
	
	@TestedIn(void.class)
	public static int getInCompleteBinaryTree(Acyc32 acyc, int tree, int whichPowOf2, int index){
		int treeSize = 1<<whichPowOf2;
		if(index < 0 || treeSize <= index) throw new IndexOutOfBoundsException("index="+index+" treeSize="+treeSize);
		int indexRemaining = index;
		int siz = treeSize;
		while(indexRemaining != 1){
			int branchSize = siz/2;
			if(indexRemaining < branchSize){
				tree = acyc.left(tree);
			}else{
				tree = acyc.right(tree);
				indexRemaining -= branchSize;
			}
		}
		if(Acyc32Util.isObject(acyc, tree)) throw new RuntimeException(
			"Not an object: "+tree+" at index "+index+" of tree="+tree+" which should have whichPowOf2="+whichPowOf2);
		return tree;
	}
	
	
	
	/** Throws if its not a 4 recursion of pair of leafs as bits *
	public static char powerOf2ItemToChar(XorlispVM32 x, int powerOf2Item){
		for()
	}*/
	
	protected static void getChars(CacheAcyc32 x, StringBuilder sb, int powerOf2Item, int whichPowerOf2){
		if(whichPowerOf2 == 4){
			//sb.append(powerOf2ItemToChar(x, powerOf2Item));
			sb.append(x.pointerToChar(powerOf2Item));
		}else if(4 < whichPowerOf2){
			int left = x.acyc.left(powerOf2Item);
			int right = x.acyc.right(powerOf2Item);
			getChars(x, sb, left, whichPowerOf2-1);
			getChars(x, sb, right, whichPowerOf2-1);
		}else throw new RuntimeException(
			"whichPowerOf2 must be at least 4 but is "+whichPowerOf2);
	}
	
	//TODO rename all *Global funcs to *Main or *ThisComp, since global namespace uses HashCons
	
	/** Returns a XobTypes.typeKeyVal */
	public static int keyValGlobal(int key, int value){
		int keyThenValue = Glo.econacyc.pair(key, value);
		return Acyc32Util.wrapObjectInType(Glo.econacyc, XobTypes.typeKeyVal, keyThenValue);
	}
	
	
	public static void main(String args[]){
		//XorlispVM32 x = new SimpleLisp32((byte)22);
		RefCounter32 x = new SimpleEconDAcycI((byte)22);
		CacheAcyc32 cAcyc = new CacheAcyc32(x);
		Map<Integer,String> names = new HashMap();
		names.put(Const.end, ".");
		names.put(Const.bit0, "<0>");
		names.put(Const.bit1, "<1>");
		names.put(Const.typVal, "<typVal>");
		System.out.println("Testing "+XobUtil.class.getName()+" bit funcs.");
		int list = 0;
		for(int i=0; i<5; i++){
			list = addPrefixToListPow2(x, Const.bit0, list);
			System.out.println("list = "+TextUtil32.toString(x, list, names));
		}
		System.out.println("Removing 1 at a time...");
		for(int i=0; i<5; i++){
			list = removeOneFromListOfPowerOf2Items(x, list);
			System.out.println("list = "+TextUtil32.toString(x, list, names));
		}
		
		System.out.println("Tests pass for "+XobUtil.class.getName()+" bit funcs.");
		
		System.out.println("Testing "+XobUtil.class.getName()+" string funcs.");
		CacheAcyc32 cache = new CacheAcyc32(x);
		//String s = "Testing xorlisp";
		String s = "xor";
		int sizeBeforePointer = x.size();
		System.out.println("Size before creating pointer to string: "+sizeBeforePointer);
		int pointerToString = stringToListOfPowerOf2(cAcyc, s);
		int sizeAfterPointer = x.size();
		System.out.println("Size after creating pointer to string: "+sizeAfterPointer);
		String s2 = listOfPowerOf2ToString(cache, pointerToString);
		System.out.println("s  = "+TextUtil32.toString(x, pointerToString, names));
		int sizeAfterBackToString = x.size();
		System.out.println("Size after back to string: "+sizeAfterBackToString);
		if(!s.equals(s2)) throw new RuntimeException(
			"Got different string back: s=["+s+"] s2=["+s2+"]");
		System.out.println("Tests pass for "+XobUtil.class.getName()+" string funcs.");
	}

}
