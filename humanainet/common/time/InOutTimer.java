/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common.time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import humanainet.common.CoreUtil;

/** Remembers average time on code that runs between in() and out().  */
public class InOutTimer{
	
	protected long count;
	
	protected double timeSum;
	
	protected double lastStarted = Double.NaN;
	
	public final String unit;
	
	protected static final Map<String,InOutTimer> unitToCounter = new HashMap();
	
	public InOutTimer(){
		this("UNDEFINEDUNIT");
	}
	
	public InOutTimer(String unit){
		this.unit = unit;
	}
	
	public boolean isIn(){
		return lastStarted==lastStarted; //else is NaN
	}

	public void in(){
		if(isIn()) throw new RuntimeException(
			"Already in. now="+CoreUtil.time()+" lastStarted="+lastStarted);
		lastStarted = CoreUtil.time();
	}
	
	public void out(){
		if(!isIn()) throw new RuntimeException("Already out. now="+CoreUtil.time());
		double duration = CoreUtil.time()-lastStarted;
		timeSum += duration;
		count++;
		lastStarted = Double.NaN;
	}
	
	/** average seconds between in() and out(), or NaN if never called in() then out(). */
	public double ave(){
		return timeSum/count;
	}

	public String toString(){
		return "["+getClass().getSimpleName()+" ave "+ave()+" seconds per "+unit+"]";
	}
	
	//FIXME should these static funcs be synchronized?
	//Probably so (depending how they're used),
	//but would that change the timing they are meant to measure?
	
	public static final InOutTimer forUnit(String unit){
		InOutTimer t = unitToCounter.get(unit);
		if(t == null){
			t = new InOutTimer(unit);
			unitToCounter.put(unit, t);
		}
		return t;
	}
	
	public static final void removeForUnit(String unit){
		unitToCounter.remove(unit);
	}
	
	public static String[] units(){
		String u[] = unitToCounter.keySet().toArray(new String[0]);
		Arrays.sort(u);
		return u;
	}

}
