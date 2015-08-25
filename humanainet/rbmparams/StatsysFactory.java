/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams;
import humanainet.datastruct.MapParamFunc;
import humanainet.statsysinterface.Statsys;

/** See benfrayfieldResearch.rbmParamGroups
and benfrayfieldResearch.parametersToExploreWithContrastiveDivergence
Basically theres many kinds of params (hyperparameters they're sometimes called)
to explore them manually, so an automated way of assembling learning algorithms
and testing them on datasets is needed, to find better learning algorithms
which may each be optimized for a different thing like compute time or memory
or sparseness or balance after sequential vs all at once learning etc.
<br><br>
TODO StatsysFactory is more general than rbm. It could return bayesian network
or any other AI datastruct and algorithm.
<br><br>
TODO I know I need a StatsysFactory interface or something like it,
but its such a complex problem I dont know how to start. I'll think about it...
*/
public interface StatsysFactory extends MapParamFunc<Statsys,String,Object>{}