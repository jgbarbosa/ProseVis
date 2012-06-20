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

  public float getValue(int idx, int selfIdx) {
    if (idx < 0 || idx >= smoothedComparisons.length) {
      return 0.0f;
    }
    float absMaxV = Float.MIN_NORMAL;
    for (int i = 0; i < smoothedComparisons.length; i++) {
      if (smoothedComparisons[i] > absMaxV && i != selfIdx) {
        absMaxV = smoothedComparisons[i];
      }
    }
    return smoothedComparisons[idx] / absMaxV;
  }

  public int getCount() {
    return this.rawComparisons.length;
  }

  public void addTo(ComparisonData runningSum) {
    for (int i = 0; i < rawComparisons.length; i++) {
      runningSum.smoothedComparisons[i] += rawComparisons[i];
    }
  }

  public void subtractFrom(ComparisonData runningSum) {
    for (int i = 0; i < rawComparisons.length; i++) {
      runningSum.smoothedComparisons[i] -= rawComparisons[i];
    }
  }

  public void smooth(ComparisonData runningSum, int window) {
    for (int i = 0; i < rawComparisons.length; i++) {
      smoothedComparisons[i] = runningSum.smoothedComparisons[i] / window;
    }
  }

  public void resetSmoothing() {
    for (int i = 0; i < rawComparisons.length; i++) {
      smoothedComparisons[i] = rawComparisons[i];
    }
  }
}
