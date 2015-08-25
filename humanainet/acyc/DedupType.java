/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc;

/** De-Duplication Type of an Acyc.
<br><br>
Any of these can be used with small data size near eachother in memory to make use of hardware caching
which makes a big difference when using lots of random access.
<br><br>
benfrayfieldResearch.mainKindsOfAcycOrganizedBySpeed 2015-7-29 says:
Based on smallAcycWithoutDedupIsManyTimesFaster and related discoveries, the main kinds of acyc I'll use,
from slowest to fastest (described by speed on my 4 APUs 1.6 ghz laptop) are, all in terms of random
access: sha256HashCons (estimated 200k pair creations per second), acyc32 (2.3 million per second),
acyc32WithoutDedup allocating pairs in a large array and reading randomly in that array (estimated
20 million per second), 64k of pairs reused many times (130 million per second because of cache).
By combining these 4 general kinds, and variations of them, I'll get my system based on acyc to run
at practical speed. Not everything needs dedup right away, and many parts can be created used and deleted
without ever using it.
*/
public enum DedupType{

	/** Global names without duplicates.
	Addresses are immutable and never have duplicates (unless the secureHash algorithm is cracked).
	Normally uses EconAcyc.
	https://en.wikipedia.org/wiki/Hash_consing
	*/
	sha256HashCons,
	
	/** Like JIMap usedin EconDAcycI. Addresses are mutable because the space is so small.
	Normally uses EconAcyc.
	*/
	localMap,
	
	/** Create lispPairs in the array without ever or only sometimes checking for duplicates,
	pointing lower into the array or if the pointer is in certain negative ranges
	then it means in other kinds of acyc (localMap, sha256HashCons, etc).
	Normally does not use EconAcyc or allow deletions.
	This kind of may use no garbageCollection or use continuous garbageCollection with econacyc
	or anywhere between. Its defining feature is it allows duplicates at least sometimes.
	*/
	fastArrayWithDup
	

}
