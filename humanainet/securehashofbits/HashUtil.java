/** Ben F Rayfield offers this extension of gnu crypto SHA256 as GNU GPL 2+ with classpath exception */
package humanainet.securehashofbits;
import java.util.List;

import humanainet.securehashofbits.gnu_crypto_noencryption_securehashonly_todoTestCodeAgain.Sha256;
import humanainet.wavetree.bit.BitFunc;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.BitsUtil;
import humanainet.wavetree.bit.Fast0To16Bits;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;

public class HashUtil{
	private HashUtil(){}

	/** Chooses a bit aligned (slower, more complete) or byte aligned sha256 implementation depending on bit length */
	public static final BitFunc sha256 = new BitFunc(){
		public Bits bitfunc(Bits in){
			if(!everCalledHash){
				//I'm ok with thread errors here (traded for efficiency) in reading old values of everCalledHash because
				//testing multiple times wont hurt anything.
				everCalledHash = true;
				//TODO log("Skipping tests because bit aligned fails...")
				//testSHA256();
			}
			if((in.siz()%8) != 0){ //bit aligned, accepts all possible bitstrings
				return bitAlignedSha256Generators.get(0).bitfunc(in);
			}else{ //byte aligned, faster
				return byteAlignedSha256Generators.get(0).bitfunc(in);
			}
		}
	};
	
	private static boolean everCalledHash;
	
	//TODO Does littleEndian of Bitstring cause SHA256 as bytes (in all funcs which calculate it here) to give wrong hash as if 8 bits at a time were moved within the byte?
	
	protected static final BitFunc systemSHA256Generator = new BitFunc(){
		public Bits bitfunc(Bits in){
			if((in.siz()%8) != 0) throw new RuntimeException("TODO bitstring size not a multiple of 8 (TODO modify gnucryptoSHA256 code to allow any bit length): "+in.siz());
			if(in.siz() > Integer.MAX_VALUE) throw new RuntimeException("TODO large bitstring: "+in.siz());
			try{
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				for(long i=0; i<in.siz(); i+=8){
					md.update(in.byteAt(i));
				}
				byte hash[] = md.digest();
				return BitsUtil.bytesToBits(hash); //TODO bytesToBits func could use some optimizing. See its comment.
			}catch(NoSuchAlgorithmException e){
				throw new RuntimeException(e);			
			}
		}
	};
	
	/** Despite its name being "gnu.crypto", I have removed the encryption from it, keeping an unregulated secureHash */
	protected static final BitFunc gnuCrytoNoencryptionSHA256Generator = new BitFunc(){
		public Bits bitfunc(Bits in){
			if((in.siz()%8) != 0) throw new RuntimeException("TODO bitstring size not a multiple of 8: "+in.siz());
			if(in.siz() > Integer.MAX_VALUE) throw new RuntimeException("TODO large bitstring: "+in.siz());
			Sha256 md = new Sha256();
			for(long i=0; i<in.siz(); i+=8){
				//TODO update many bytes at once
				md.update(in.byteAt(i));
			}
			byte hash[] = md.digest();
			return BitsUtil.bytesToBits(hash); //TODO bytesToBits func could use some optimizing. See its comment.
		}
	};
	
	protected static final BitFunc gnuCrytoNoencryptionSHA256GeneratorModifiedForBits = new BitFunc(){
		public Bits bitfunc(Bits in){
			long siz = in.siz();
			boolean remainBits[] = new boolean[(int)(siz%8)];
			BitSha256 md = new BitSha256();
			for(long i=0; i<siz-7; i+=8){
				//TODO update many bytes at once
				md.update(in.byteAt(i));
			}
			long bitStart = siz-remainBits.length;
			if(remainBits.length != 0){
				for(int i=0; i<remainBits.length; i++){
					remainBits[i] = in.bitAt(bitStart+i);
				}
				md.updateLastBits(remainBits);
			}
			byte hash[] = md.digest();
			if(1<2) throw new RuntimeException("TODO unknown if this is working. 2 tests say its not but I dont know if they gave the right answer to test against. Find reliable test cases and get this SHA256 bit alignment working or prove its already working.");
			return BitsUtil.bytesToBits(hash); //TODO bytesToBits func could use some optimizing. See its comment.
		}
	};
	
	protected static final List<BitFunc> bitAlignedSha256Generators = Collections.<BitFunc>unmodifiableList(Arrays.<BitFunc>asList(
		gnuCrytoNoencryptionSHA256GeneratorModifiedForBits
	));
		
	/** Test once if they all equal. Then uses first in the list. TODO sort them by speed over time. */
	protected static final List<BitFunc> byteAlignedSha256Generators = Collections.<BitFunc>unmodifiableList(Arrays.<BitFunc>asList(
		systemSHA256Generator,
		gnuCrytoNoencryptionSHA256Generator
	));
	
	protected static Bits bitsFromHex(String hex){
		Bits b = Fast0To16Bits.EMPTY;
		for(int i=0; i<hex.length(); i++){
			int digit = Integer.parseInt(""+hex.charAt(i), 16);
			b = b.cat(Fast0To16Bits.get(4, digit));
		}
		return b;
	}
	
	protected static Bits bitsFromUTF8(String text){
		Bits b = Fast0To16Bits.EMPTY;
		byte bytes[];
		try{
			bytes = text.getBytes("UTF8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
		for(int i=0; i<bytes.length; i++){
			b = b.cat(Fast0To16Bits.get(bytes[i]));
		}
		return b;
	}
	
	protected static Bits zeros(int howManyZeros){
		Bits b = Fast0To16Bits.EMPTY;
		for(int i=0; i<howManyZeros; i++){
			b = Fast0To16Bits.FALSE.cat(b);
		}
		return b;
	}
	
	protected static boolean equals(Bits x, Bits y){
		if(x.siz() != y.siz()) return false;
		for(long i=0; i<x.siz(); i++){
			if(x.bitAt(i) != y.bitAt(i)) return false;
		}
		return true;
	}
	
	protected static String toBinaryString(Bits x){
		StringBuilder sb = new StringBuilder();
		for(long i=0; i<x.siz(); i++){
			sb.append(x.bitAt(i) ? '1' : '0');
		}
		return sb.toString();
	}
	
	protected static void verify(Bits in, Bits correctOut, Bits observedOut, String testName){
		if(!equals(correctOut, observedOut)){
			throw new RuntimeException(
				"Test ["+testName+"] failed."
				+"\r\nin=          "+toBinaryString(in)
				+"\r\ncorrectOut=  "+toBinaryString(correctOut)
				+"\r\nobservedOut= "+toBinaryString(observedOut));
		}
	}
	
	public static void testSHA256(){
		log("Testing SHA256 in both bit and byte aligned modes...");
		// https://www.cosic.esat.kuleuven.be/nessie/testvectors/hash/sha/Sha-2-256.unverified.test-vectors
		//lists some sha256 test inputs that are not multiples of 8 bits, and their outputs,
		//but it appears they are all different lengths of bit0.
		//Including these:
		//message=5 zero bits hash=B873D5D6263FC6539CD8BF430D13ED28B2FA3BF62F06A3C23411BB3A60BC70F1
		//message=19 zero bits hash=032402854554BC5AFCB7350FA61BB35C42128B7BA800D95BD37D2A12EAE2617B
		Bits in, correctOut, observedOut;
		
		in = bitsFromUTF8("abc");
		correctOut = bitsFromHex("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
		observedOut = gnuCrytoNoencryptionSHA256GeneratorModifiedForBits.bitfunc(in);
		verify(in,correctOut,observedOut, "gnuCrytoNoencryptionSHA256GeneratorModifiedForBits abc");
		observedOut = systemSHA256Generator.bitfunc(in);
		verify(in,correctOut,observedOut, "systemSHA256Generator abc");
		
		in = zeros(5);
		correctOut = bitsFromHex("B873D5D6263FC6539CD8BF430D13ED28B2FA3BF62F06A3C23411BB3A60BC70F1");
		observedOut = gnuCrytoNoencryptionSHA256GeneratorModifiedForBits.bitfunc(in);
		verify(in,correctOut,observedOut, "gnuCrytoNoencryptionSHA256GeneratorModifiedForBits 5 zeros");
		
		in = zeros(19);
		correctOut = bitsFromHex("032402854554BC5AFCB7350FA61BB35C42128B7BA800D95BD37D2A12EAE2617B");
		observedOut = gnuCrytoNoencryptionSHA256GeneratorModifiedForBits.bitfunc(in);
		verify(in,correctOut,observedOut, "gnuCrytoNoencryptionSHA256GeneratorModifiedForBits 19 zeros");
		
		//TODO test with 1 bits in the extra bits like https://www.reddit.com/r/crypto/comments/3dn1ab/im_adding_an_updatebit_array_function_to_gnu/
	
		log("SHA256 tests pass (TODO more tests bit aligned with 1s in the extra bits).");
	}
	
	public static void log(String line){
		//TODO hook into commonfuncs.CommonFuncs.log(String) if it exists, by reflection
		System.out.println(line);
	}
	
	public static void main(String args[]){
		testSHA256();
	}

}