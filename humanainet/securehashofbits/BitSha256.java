/** Ben F Rayfield offers this extension of gnu crypto SHA256 as GNU GPL 2+ with classpath exception */
package humanainet.securehashofbits;
import humanainet.securehashofbits.gnu_crypto_noencryption_securehashonly_todoTestCodeAgain.*;

public class BitSha256 extends Sha256{
	
	/** Starts as -1.
	 * TODO increase when count increases, but allow decrease by 0-7 bits by setBitLength(long) */
	protected long bitLength = -1;
	
	/** As in the gnuCrypto (secure hash only) padBuffer code, this is normally 0x80
	because its byte aligned. Its commented "padding is always binary 1 followed by binary 0s".
	To extend that to sha256 of a number of bits that does not have to be a multiple of 8,
	when there are such bits, I'm movnig that bit1 toward lower bits (bigEndian)
	and putting the last bits of the content to be hashed there.
	If update(boolean...) is called with 1-7 bits, it puts those bits in this byte.
	*/
	protected boolean upToSevenBits[] = new boolean[0];
	//protected byte lastByteWhichEndsWithBit1 = (byte) 0x80;
	
	public void updateLastBits(boolean... oneToSevenBits){
		if(oneToSevenBits.length == 0 || oneToSevenBits.length > 7){
			throw new IllegalArgumentException("oneToSevenBits.length=="+oneToSevenBits.length);
		}
		this.upToSevenBits = oneToSevenBits;
	}
	
	protected byte lastByteWhichEndsWithBit1(){
		int i = 0x80; //the bit1 after the content to be hashed
		for(boolean bit : upToSevenBits){
			i = bit ? 0x80|(i>>1) : (i>>1);
		}
		return (byte) i;
	}
	
	protected byte[] padBuffer(){
		if(bitLength == -1){ //multiple of 8 bits
			return super.padBuffer();
		}else{
			if(1<2) throw new RuntimeException("TODO update and padBuffer funcs need to be changed, so transform is not called when update last byte past bit length before have time to reduce its length. It doesnt always allow backing up. The last byte needs to have 0s padded in before updating, so I created update(boolean...) func.");
			System.err.println("TODO padding doesnt align with bytes. It sometimes needs to modify last byte output to add a 1, and then from there 0 bits are padded until the last 64 bits completes a set of 512 bits, so length will always be byte aligned.");
			System.err.println("TODO modify SHA256 code, padBuffer, to allow number of bits thats not a multiple of 8.");
			//count is in bytes
			int n = (int) (count % 64);
			int padding = (n < 56) ? (56 - n) : (120 - n);
			byte[] result = new byte[padding + 8];
			//padding is always binary 1 followed by binary 0s
			//result[0] = (byte) 0x80;
			result[0] = lastByteWhichEndsWithBit1();
			//save number of bits, casting the long to an array of 8 bytes
			//long bits = count << 3;
			long bits = (count << 3) + upToSevenBits.length;
			result[padding++] = (byte)(bits >>> 56);
			result[padding++] = (byte)(bits >>> 48);
			result[padding++] = (byte)(bits >>> 40);
			result[padding++] = (byte)(bits >>> 32);
			result[padding++] = (byte)(bits >>> 24);
			result[padding++] = (byte)(bits >>> 16);
			result[padding++] = (byte)(bits >>> 8);
			result[padding  ] = (byte) bits;
			return result;
		}
	}

}
