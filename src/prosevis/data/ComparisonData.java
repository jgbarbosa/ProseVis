package prosevis.data;

public class ComparisonData {
  public final float[] rawComparisons;
  public final float[] smoothedComparisons;

  public ComparisonData(int length) {
    rawComparisons = new float[length];
    smoothedComparisons = new float[length];
  }

  public int getMaxIdx(boolean[] enabledComparisons) {
    float maxV = -Float.MAX_VALUE;
    int maxIdx = -1;
    for (int i = 0; i < smoothedComparisons.length; i++) {
      if (smoothedComparisons[i] > maxV && enabledComparisons[i]) {
        maxIdx = i;
        maxV = smoothedComparisons[i];
      }
    }
    return maxIdx;
  }

  public float getValue(int idx) {
    if (idx < 0 || idx >= smoothedComparisons.length) {
      return 0.0f;
    }
    float absMaxV = smoothedComparisons[0];
    int absMaxIdx = 0;
    for (int i = 0; i < smoothedComparisons.length; i++) {
      if (smoothedComparisons[i] > absMaxV) {
        absMaxIdx = i;
        absMaxV = smoothedComparisons[i];
      }
    }
    return smoothedComparisons[idx] / smoothedComparisons[absMaxIdx];
  }
}
