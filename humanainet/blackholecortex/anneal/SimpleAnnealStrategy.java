package humanainet.blackholecortex.anneal;

public class SimpleAnnealStrategy implements AnnealStrategy{
	
	public final int cyclesPerLearnMany, cyclesPerLearnOne, cyclesPerPredict;
	
	public SimpleAnnealStrategy(int cyclesPerLearnMany, int cyclesPerLearnOne, int cyclesPerPredict){
		this.cyclesPerLearnMany = cyclesPerLearnMany;
		this.cyclesPerLearnOne = cyclesPerLearnOne;
		this.cyclesPerPredict = cyclesPerPredict;
	}

	public int cyclesPerLearnMany(){ return cyclesPerLearnMany; }
	
	public int cyclesPerLearnOne(){ return cyclesPerLearnOne; }

	public int cyclesPerPredict(){ return cyclesPerPredict; }
	
	public double temperature(int thisCycle, int totalCycles){
		if(thisCycle < 0 || totalCycles <= thisCycle || totalCycles <= 0) throw new IllegalArgumentException(
			"thisCycle="+thisCycle+" totalCycles="+totalCycles);
		return 1-(double)thisCycle/totalCycles;
	}

}
