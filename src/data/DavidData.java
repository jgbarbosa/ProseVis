package data;

import java.util.ArrayList;

public class DavidData {
	private ArrayList<Float> probabilities;
	
	
	public DavidData(float[] prob) {
		probabilities = new ArrayList<Float>();
		
		if (prob == null)
			return;
		
		for (float value: prob) {
			probabilities.add(value);
		}
	}
	
	public float getRelProb(boolean[] actFiles) {
		float maxProb = -1;   // Max Prob among active files
		float maxProbFull = -1;  // Max Prob among all probabilities
		
		for (int itr = 0; itr < probabilities.size(); itr++) {
			
			if (maxProbFull < probabilities.get(itr))
				maxProbFull = probabilities.get(itr);
			
			if (actFiles[itr]) {
				if (maxProb < probabilities.get(itr))
					maxProb = probabilities.get(itr);
			}
		}
		
		return maxProb/maxProbFull;
	}
	
	public int getMaxProbIdx(boolean[] actFiles) {
		float maxProb = -1;
		int maxProbIdx = -1;
		
		for (int itr = 0; itr < probabilities.size(); itr++) {
			if (actFiles[itr]) {
				if ( maxProb < probabilities.get(itr)) {
					maxProb = probabilities.get(itr);
					maxProbIdx = itr;
				}
			}
		}
		return maxProbIdx;
	}
		
	public void display() {
		System.out.print("\n");
		for (float value: probabilities) {
			System.out.print(value + ", ");
		}
	}		
}
