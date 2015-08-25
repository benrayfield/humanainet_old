/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams.actions.simple;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.rbmparams.RbmSimpleAction;

/** Does nothing, in case your code has a place you must call a RbmAction,
like norming and you dont want to norm.
*/
public class RbmSimpleActionNothing implements RbmSimpleAction{
	public void rbmAction(RbmData rbm){}
}