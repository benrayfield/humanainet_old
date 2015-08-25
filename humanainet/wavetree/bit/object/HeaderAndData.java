package humanainet.wavetree.bit.object;
import humanainet.wavetree.bit.Bits;

public interface HeaderAndData{
	
	public Bits data();

	/** may be empty */
	public Bits header();
	
	public Bits headerThenData();

}
