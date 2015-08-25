/** Ben F Rayfield offers acyc, the main datastruct of xorlisp, opensource GNU LGPL */
package humanainet.acyc.ptr32;

import humanainet.xorlisp.Nav;
import humanainet.xorlisp.XorlispVM;
import humanainet.xorlisp.XorlispVM32;

public class Nav32 implements Nav{
	
	public final XorlispVM32 vm;
	
	//"TODO for fork and general navigation, how to represent the path from ptrHighestPair to the current ( or )?"
	
	/** Pointer to a pair of
	(left) stack from where recursion started, and bit0 or bit1
	(right) stack of objects queued, paired onto the stack as pair(prevQueueState,objectToQueue).
	<br><br>
	TODO on left stack: T and F funcs from church encoding of lambda choose first or second
	in a pair, so use those)
	<br><br>
	Since the stack is paired in reverse of normal order, any Nav exploring it will reach the
	objects in order they were queued, the reverse of the order of the pairs reached in the acyclicNet,
	which means it recurses all the way to nil which is first in the queue then reaches
	each next item in the queue higher in the acyclicNet and lower in the stack.
	<br><br>
	This allows Nav to be entirely stateless, so other Navs could explore the data which represents
	this Nav's exploring of some other data, in the acyclicNet, recursively without limit
	and without duplication of any of these acyclicNet nodes nomatter what combinations they occur.
	*/
	protected int state;
	
	public final int end, pairOfEnd, bit0, bit1;
	
	/** If ptrIsToObjectYouWantToNavigate then ptrState is created
	as a new pair with ptr the first on its stack
	and its queue (right side of the pair) is ()/nil.
	When ptrIsToObjectYouWantToNavigate is false, thats used for forking a copy of this Nav
	or restoring a saved moment of computing.
	*/
	public Nav32(XorlispVM32 vm, int ptr, boolean ptrIsToObjectYouWantToNavigate){
		this.vm = vm;
		end = 0;
		pairOfEnd = vm.pair(end, end);
		bit0 = vm.pair(end, pairOfEnd);
		bit1 = vm.pair(pairOfEnd, end);
		if(ptrIsToObjectYouWantToNavigate){
			int leftParenOfPtr = vm.pair(bit1, ptr);
			int stack = leftParenOfPtr;
			int queue = end;
			state = vm.pair(stack, queue);
		}else{
			this.state = ptr;
		}
	}

	
	/** Go up to next (,
	which may be in left or right of current pair depending on if we've already
	passed the left child,
	or go down to next ) if we just passed a right child,
	since theres nowhere to immediately go up.
	Its a binary tree viewed as a linear sequence of ( and ) as push and pop
	to navigate the entire acyclicNet. This is the linear way of exploring it,
	which is only practical to use in some parts between uses of treeForward().
	*/
	public void linearForward(){
		throw new RuntimeException("TODO similar to treeForward but changes different part of state");
	}

	/** If at (, move to the matching ). If at (, do linearForward instead.
	If isHighest(), does nothing since are at end of view of acyclicNet.
	*/
	public void treeForward(){
		int stackTop = vm.left(state);
		//TODO what if stackTop == end? Still want to move forward to its rightparen and push that
		int parenobject = vm.left(stackTop); //linked list with nil at right of last pair
		int bit = vm.left(parenobject);
		boolean leftparen = bit==bit1;
		if(leftparen){
			int objectThatWasPushed = vm.right(parenobject);
			int newBit = bit0; //rightparen
			int newParenobject = vm.pair(newBit, objectThatWasPushed);
			int nextLowerListNodeInStack = vm.right(stackTop);
			int newStackTop = vm.pair(newParenobject, nextLowerListNodeInStack);
			int sameQueueState = vm.right(state);
			state = vm.pair(newStackTop, sameQueueState);
		}else{
			linearForward();
		}
	}

	public Nav fork(){
		return new Nav32(vm, state, false);
	}

	public boolean isLparen(){
		int stackTop = vm.left(state);
		int parenobject = vm.left(stackTop); //linked list with nil at right of last pair
		int bit = vm.left(parenobject);
		//church encoding of lambda says T chooses first in pair.
		//TODO Is bit1 the lambda called T/true?
		return bit == bit1;
	}

	public boolean isLowest(){
		int stackTop = vm.left(state);
		int parenobject = vm.left(stackTop); //linked list with nil at right of last pair
		int objectInStackTop = vm.right(parenobject);
		return objectInStackTop == end;
	}

	public boolean isHighest(){
		int stackTop = vm.left(state);
		//int objectInStackTop = vm.right(stackTop);
		//return objectInStackTop == end;
		int nextLowerInStack = vm.right(stackTop);
		return nextLowerInStack == end;
	}
	
	

}
