/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptrObjectForest.ptrHashCons;
import humanainet.acyc.ptrObjectForest.lazyDedup.Bifor;

public interface HashCons extends Bifor{
	
	//TODO would this increase the length? Consider that SHA256 takes 512 bits input and must append 64 bits length then pad, so there is room for more data in the input without doing more compute cycles, but in the output it would be extra...
	//public String algorithm();
	
	

}
