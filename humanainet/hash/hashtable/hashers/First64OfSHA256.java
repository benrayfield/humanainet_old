/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import humanainet.common.MathUtil;
import humanainet.hash.hashtable.JHasher;

/** For extreme cases when your hash functions arent working and you dont know why,
this is the first 64 bits of a secureHash algorithm applied to less than secure
input and output size of 64 bits.
*/
public class First64OfSHA256 implements JHasher{

	public long hash(long j){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("SHA-256");
			for(int i=56; i>=0; i-=8){
				md.update((byte)(j>>i));
			}
			byte hash[] = md.digest();
			long out = 0;
			for(int i=7; i>=0; i--){
				out = (out<<8) | (hash[i]&0xff);
			}
			return out;
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}
	
	/** Test the randomness of this function by seeing how long it takes to repeat the first n bits
	of an output, like it would be used in a hashtable if its capacity was a power of 2.
	If its first repeat is approx sqrt (a little different because of triangle instead of square)
	of capacity, then its a good hash function.
	*/
	public static void main(String args[]){
		double sum = 0;
		for(int rounds=0; rounds<1000; rounds++){
			JHasher hasher = new First64OfSHA256();
			hasher = new ChainTwoJHashers(new PlusConstantAsHash(MathUtil.strongRand), hasher);
			int nBits = 30;
			long capacity = 1<<nBits;
			long mask = capacity-1;
			Set<Long> wrappedOutputs = new HashSet<Long>();
			long firstRepeatedAt = 0;
			for(long in=0; in<capacity; in++){
				long out = hasher.hash(in);
				long wrappedOut = out&mask;
				if(wrappedOutputs.contains(wrappedOut)){
					System.out.println("First repeated a wrapped output from in="+in+" (expected this around sqrt(capacity)="+Math.sqrt(capacity)+" but a little different because of triangle vs square) and that wrappedOut="+wrappedOut);
					firstRepeatedAt = in;
					break;
				}else{
					wrappedOutputs.add(wrappedOut);
				}
			}
			sum += firstRepeatedAt;
			int roundsDone = rounds+1;
			System.out.println("Ave firstRepeatedAt = "+sum/roundsDone);
		}
	}
	
	

}
