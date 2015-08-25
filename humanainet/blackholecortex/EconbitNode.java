/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.blackholecortex;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
//import recog.RecogNeuralOn;
import humanainet.common.CoreUtil;

/** TODO need to rewrite this...
just do the picture with every pixel being a currency type that people draw on
at any time (not need to be there continuously) to gamble like wikipedia that
it will stay that way, while others who left it that way gambled it would stay
the same at least for some time ... Allow branching the main picture or any
local part in network so you and your friends or whoever you're connected
to can play the same econbits gambling on who can predict best
... to solve getting started problem, add random painter (maybe funded by the
difference in gambles over time which was otherwise not being used). didnt
realize it at the time econbits uses velocity = velocity*derivative(velocity)
- friction (or was it plus friction?). Allow people to change their gamble
(between 2 choices up or down, per subject) any time and keep that amount
gambled (taking from their gameMoney) or put into their gameMoney depending
if they win (minority) or lose (majority). If equal amounts gambled on the 2
choices, then if its an even number the gambled-on-up's win, or if its an
odd number, the gambled-on-downs win. It will rarely equal. An important part
is to include a subject (a string, naming anything) which everyone sees as they
decide to spend their time predicting that subject or not. The other important
thing is to keep track of, not just the players current gameMoney as usual,
but also the total value of the subject which is a function only of those
gambles that were minority (got the least gambles) at the time since that
is who is predicting the others better. Also, people can gamble on the
opposite of what they want to train others, using the gameMoney they lose
by being likely in majority, ... train others to predict (or gamble on)
later, which is just how I think people would react to winning (being in
minority gamble that time and trying to learn how to keep doing that).
In that way, the system has value as a way to pay people to predict a
varying number about a subject, in trade for previous good predictions
about that same subject. I dont know if I want to suggest people trade
these prediction roles since somebody else cant predict a thing just
because they bought your stock shares after you predicted it well.
... todo add mindmap voting later, as pairs of subject-and-item-in-list
(and maybe sentence-in-definition), which are each gambled on same as
pixel, in this ui by dragging up and down in list. ... important -
econbits must be in javascript and call to server. must work immediately.
just start painting with thousands of other people. - important show
people after each paintbrush stroke something that means "feel good
success, predicted better than others" or "bad prediction, you're in
majority". - important have gradual colors for each pixel which are
summed from the up/down differences, but when somebody paints there
push it all the way up or down (and how much of their gameMoney for
that pixel should they spend? Maybe a standard of 3%?) - use gradual
interest rate, they win or lose that certain amount per millisecond
(continuously in 1000 parts that total per second) in the game,
paying/losing it while they're in majority (who says that pixel
should be what?), winning it when in minority. Even a new player
cant mess it up much since minority wins and spammers or new players
not paying attention or painting things others find interesting will
not predict in minority (did their own best thing and others went
with it) and will lose gameMoney and soon be ignored.
*/
public class EconbitNode extends WeightsNode{
	
	/** same indexs as weightFrom[] and nodeFrom[] */
	public double gambleFrom[];
	
	public EconbitNode(long address){
		super(address);
	}
	
	protected void changeArraysSize(int newCapacity){
		super.changeArraysSize(newCapacity);
		double gambleFrom2[] = new double[newCapacity];
		System.arraycopy(gambleFrom, 0, gambleFrom2, 0, size);
		gambleFrom = gambleFrom2;
	}
	
	protected void swapIndexs(int x, int y){
		super.swapIndexs(x, y);
		double tempGamble = gambleFrom[x];
		gambleFrom[x] = gambleFrom[y];
		gambleFrom[y] = tempGamble;
	}
	
}