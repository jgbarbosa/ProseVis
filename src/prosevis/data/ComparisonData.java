package prosevis.data;

public class ComparisonData {
  public final float[] rawComparisons;
  public final float[] smoothedComparisons;

  public ComparisonData(int length) {
    rawComparisons = new float[length];
    smoothedComparisons = new float[length];
  }

  public int getMaxIdx(boolean[] enabledComparisons) {
    float maxV = 0;
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
    float absMaxV = Float.MIN_NORMAL;
    for (int i = 0; i < smoothedComparisons.length; i++) {
      if (smoothedComparisons[i] > absMaxV) {
        absMaxV = smoothedComparisons[i];
      }
    }
    return smoothedComparisons[idx] / absMaxV;
  }
}
