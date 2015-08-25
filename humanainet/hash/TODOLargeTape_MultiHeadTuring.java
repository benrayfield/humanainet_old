/** Ben F Rayfield offers humanainet.hash opensource GNU LGPL. Its uses include Acyc used by Xorlisp. */
package humanainet.hash;

import java.util.Arrays;

public class TODOLargeTape_MultiHeadTuring{
	
	public final int heads[];
	
	public final boolean out[], move[];
	
	public final boolean tape[];
	
	/** out and move use array index of bit read by each head, so its exponential size. */
	public TODOLargeTape_MultiHeadTuring(int heads, boolean out[], boolean move[], int tapeSize){
		this.heads = new int[heads];
		this.out = out;
		this.move = move;
		tape = new boolean[tapeSize];
	}
	
	public void clear(){
		for(int i=0; i<heads.length; i++) heads[i] = i;
		Arrays.fill(tape, false);
	}
	
	

}