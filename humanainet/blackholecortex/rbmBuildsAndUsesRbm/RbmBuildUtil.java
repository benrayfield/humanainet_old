package humanainet.blackholecortex.rbmBuildsAndUsesRbm;

/** TODO this *.rbmBuildsAndUsesRbm javapackage is for rbms that control,
through bitvars connected to their rbmParams and datasets,
the building and using of other rbms.
The ui will be something like each row or column has 1 bitvar thats on for
a choice between a few options of each type,
and scalar values or sum of bitvars in some range choose the scalar options.
There will also be places to hook in measurements of how well it learns,
permutations and other variations of learning, strategies on
how to choose which trainingData vectors to relearn occasionally
as online/continuous learning occurs, as long as such relearning
does not have to be done more than log number of times for any
specific data vector.
<br><br>
TODO what kind of ui should this have?
I'd like to have text connected to each bitvar, maybe when mouse is over it
that text would be displayed in a shared textfield above or below the bitvars area.
Or if more space on screen is available, all the text could be displayed at once,
and some of it would light up (maybe font color changes between gray and white)
as scalarvars (of which bitvars are weightedBitObserve).
How would scalarvars be displayed?
How are datasets generated (of those designed for balance like all possible
positions of a certain rectangle in a 2d wrapped space)?
How are empirical datasets chosen?
How should it visually represent what its doing when its taking a long time
like in some part of learning or learn/predict alternating process
for a certain dataset?
Should there be a bitvar for each dataset which may be used?
What happens when you click that bitvar? Is it like the "paint" vars in
one of my ways of using FlatPYX?
*/
public class RbmBuildUtil{
	private RbmBuildUtil(){}
	
	//TODO

}