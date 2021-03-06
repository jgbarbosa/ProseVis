package prosevis.data;

import java.util.HashMap;
import java.util.Map;


public class Syllable {
  private final Map<Integer, Integer> label2type = new HashMap<Integer, Integer>();
  private final ComparisonData comparisons;

  public Syllable(int[] sAttributes, ComparisonData cData) {
    this.label2type.put(TypeMap.kStressIdx, sAttributes[0]);
    this.label2type.put(TypeMap.kPhonemeIdx, sAttributes[1]);
    this.label2type.put(TypeMap.kPhonemeC1Idx, sAttributes[2]);
    this.label2type.put(TypeMap.kPhonemeVIdx, sAttributes[3]);
    this.label2type.put(TypeMap.kPhonemeC2Idx, sAttributes[4]);
    this.comparisons = cData;
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

  public int getTypeIdxForLabelIdx(int labelIdx, boolean[] enabledComparisons) {
    if (labelIdx == TypeMap.kColorByComparisonIdx) {
      if (comparisons == null) {
        return TypeMap.kNoTypeIdx;
      }
      return comparisons.getMaxIdx(enabledComparisons);
    }
    return label2type.get(labelIdx);
  }

  public float getComparisonValue(int idx, int selfIdx) {
    if (comparisons == null) {
      return 0.0f;
    }
    return comparisons.getValue(idx, selfIdx);
  }

  public int getComparisonCount() {
    if (comparisons == null) {
      return -1;
    }
    return comparisons.getCount();
  }
  
   public ComparisonData getComparison() {
    return comparisons;
  }

  public void addTo(ComparisonData runningSum) {
    if (comparisons == null) {
      return;
    }
    comparisons.addTo(runningSum);
  }

  public void subtractFrom(ComparisonData runningSum) {
    if (comparisons == null) {
      return;
    }
    comparisons.subtractFrom(runningSum);
  }

  public void smooth(ComparisonData runningSum, int window) {
    if (comparisons == null) {
      return;
    }
    comparisons.smooth(runningSum, window);
  }

  public void resetSmoothing() {
    if (comparisons != null) {
      comparisons.resetSmoothing();
    }
  }
  
  public void reset() {
    if (comparisons != null) {
      comparisons.reset();
    }
  }
}
