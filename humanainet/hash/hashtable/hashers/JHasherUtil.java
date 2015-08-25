/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash.hashtable.hashers;
import java.security.SecureRandom;
import humanainet.common.CoreUtil;
import humanainet.hash.hashtable.JHasher;

public class JHasherUtil{
	
	public static JHasher[] hashers(SecureRandom rand, int quantity){
		JHasher h[] = new JHasher[quantity];
		for(int i=0; i<quantity; i++){
			//h[i] = new DiffOfMultsOfXorsJHasher(rand);
			
			
			//h[i] = new ChainTwoJHashers(new DiffOfMultsOfXorsJHasher(rand), new Remainder(rand));
			//h[i] = new SumTwoJHashers(new DiffOfMultsOfXorsJHasher(rand), new DiffOfMultsOfXorsJHasher(rand));
			//PlusConstantAsHash runs 10 million JIMap.put per second, faster than any other I know of so far 2015-7-27,
			//but it doesnt make the low bits depend on the high bits, so I'm going with SumOfSelfAndDownshiftHalfThenAddConstant.
			h[i] = new PlusConstantAsHash(rand); 
			h[i] = new ChainTwoJHashers(new PlusConstantAsHash(rand), new Remainder(rand));
			
			
			//h[i] = new SumOfSelfAndDownshiftHalfThenAddConstant(rand);
			
			
			//High quality but slow: h[i] = new ChainTwoJHashers(new PlusConstantAsHash(CoreUtil.strongRand), new First64OfSHA256());
		}
		return h;
	}

}