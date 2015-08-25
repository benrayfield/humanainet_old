/** Ben F Rayfield offers xorlisp opensource GNU LGPL */
package humanainet.stream;

public interface InStream{
	
	public boolean readBit();
	
	public byte readByte();
	
	//TODO Dont bring in wavetree.bit.Bits at this core level,
	//but consider what functions are needed to effectively use it in plugins.

}
