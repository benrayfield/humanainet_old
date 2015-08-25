/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.rbmparams;
import humanainet.statsysinterface.Statsys;

public interface StatsysMeasure{
	
	/** Returns a fraction. 0 is worst and 1 is best.
	Measuring usually changes the statsys by it learning things.
	If you dont want a measured statsys to be changed,
	copy it (as a CopyCost) then call this on the copy.
	*/
	public double measure(Statsys s/*, boolean copyFirst*/);
	
	/** If measure(Statsys) changes that statsys, returns true.
	It may change the statsys to measure how well it learns things.
	*/
	public boolean changesStatsys();
	
	/** min Statsys.size() that can be measured */
	public int minSize();
	
	/** max Statsys.size() that can be measured */
	public int maxSize();

}