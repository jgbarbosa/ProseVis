package prosevis.data;

public class Syllable {
	int phoneme;
	int stress;
	DavidData dData;
	
	int[] phoComponents = new int[3];
	
	public Syllable(int[] sAttributes, float[] prob) {
		this.stress = sAttributes[0];
		this.phoneme = sAttributes[1];
		this.phoComponents[0] = sAttributes[2];
		this.phoComponents[1] = sAttributes[3];
		this.phoComponents[2] = sAttributes[4];
		
		/* Initialize David's Data */
		dData = new DavidData(prob);
	}
	
	public int getPhoneme(){
		return phoneme;
	}

	public int getStress(){
		return stress;
	}
	
	public int getComponent(int i){
		return phoComponents[i];
	}
	
	public void displayDavidData() {
		dData.display();
	}
	public float getRelProb(boolean[] actFiles) {
		return dData.getRelProb(actFiles);
	}
	
	public int getMaxProbIdx(boolean[] actFiles){
		return dData.getMaxProbIdx(actFiles);
	}
}
