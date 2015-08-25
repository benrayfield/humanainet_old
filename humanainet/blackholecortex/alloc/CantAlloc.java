/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex.alloc;

public class CantAlloc extends RuntimeException{
	
	public CantAlloc(){}
	
	public CantAlloc(String message){
		super(message);
	}
	
	public CantAlloc(String message, Throwable cause){
		super(message, cause);
	}

}
