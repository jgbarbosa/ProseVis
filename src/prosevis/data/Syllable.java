package prosevis.data;

import java.util.HashMap;
import java.util.Map;

public class Syllable {
  private final Map<Integer, Integer> label2type = new HashMap<Integer, Integer>();
  DavidData dData;

  public Syllable(int[] sAttributes) {
    this(sAttributes, null);
  }

  public Syllable(int[] sAttributes, float[] prob) {
    this.label2type.put(TypeMap.kStressIdx, sAttributes[0]);
    this.label2type.put(TypeMap.kPhonemeIdx, sAttributes[1]);
    this.label2type.put(TypeMap.kPhonemeC1Idx, sAttributes[2]);
    this.label2type.put(TypeMap.kPhonemeVIdx, sAttributes[3]);
    this.label2type.put(TypeMap.kPhonemeC2Idx, sAttributes[4]);

    /* Initialize David's Data */
    dData = new DavidData(prob);
  }

  public int getPhoneme() {
    return label2type.get(TypeMap.kPhonemeIdx);
  }

  public int getStress() {
    return label2type.get(TypeMap.kStressIdx);
  }

  public int getComponent(int i) {
    switch (i) {
    case 0:
      return label2type.get(TypeMap.kPhonemeC1Idx);
    case 1:
      return label2type.get(TypeMap.kPhonemeVIdx);
    case 2:
      return label2type.get(TypeMap.kPhonemeC2Idx);
    }
    return -1;
  }

  public void displayDavidData() {
    dData.display();
  }

  public float getRelProb(boolean[] actFiles) {
    return dData.getRelProb(actFiles);
  }

  public int getMaxProbIdx(boolean[] actFiles) {
    return dData.getMaxProbIdx(actFiles);
  }

  public int getTypeIdxForLabelIdx(int labelIdx) {
    return label2type.get(labelIdx);
  }
}
