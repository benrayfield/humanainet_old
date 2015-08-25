/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest.ptrHashCons;

public class Sha256PairHasher{
	
	/** The middle 72 bits are xored, which is the least bytes that can be xored/merged
	and still do it in a single SHA256 cycle.
	*/
	public static final int bytesOfMergedSHA256PairBeforeHash = 55;
	
	/** UPDATE: I'm only going to xor and merge the middle 72 bits,
	which is the least number of bytes to xor/merge and still get a single SHA256 cycle.
	This function must return 55 bytes.
	<br><br>
	To deal with the following, uses the first 128 bits of A,
	then the last 128 bits of A xored with the first 128 bits of B,
	then the last 128 bits of B.
	This results in a single SHA256 cycle.
	TODO it would be better to overlap fewer bits,
	since any message of at most 512-65 bits will take only a single cycle.
	This method I've described overlaps 63 too many bits,
	but it will probably still be nearly as hard to crack as SHA256(A concat B).
	<br><br>
	https://en.wikipedia.org/wiki/SHA-2 says:
	Pre-processing:
	append the bit '1' to the message
	append k bits '0', where k is the minimum number >= 0 such that the resulting message
	    length (modulo 512 in bits) is 448.
	append length of message (without the '1' bit or padding), in bits, as 64-bit big-endian integer
	    (this will make the entire post-processed length a multiple of 512 bits)
	*/
	public static byte[] bytesToHash(SHA256 left, SHA256 right){
		throw new RuntimeException("TODO");
	}

}
