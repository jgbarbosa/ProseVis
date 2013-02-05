package prosevis.data;

public class ComparisonData {

    public final float[] rawComparisons;
    public final float[] smoothedComparisons;
    public final float[] normalizedComparisons;

    public ComparisonData(int length) {
        rawComparisons = new float[length];
        smoothedComparisons = new float[length];
        normalizedComparisons = new float[length];
    }
    
    public ComparisonData(int length, float val) {
        rawComparisons = new float[length];
        smoothedComparisons = new float[length];
        normalizedComparisons = new float[length];
        for(int i=0; i <  length; i++) {
            rawComparisons[i] = val;
            smoothedComparisons[i] = val;
            normalizedComparisons[i] = val;
        }
    }

    public int getMaxIdx(boolean[] enabledComparisons) {
        float maxV = Float.MIN_VALUE;
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
        for (int i = 0; i < normalizedComparisons.length; i++) {
            runningSum.smoothedComparisons[i] += normalizedComparisons[i];
        }
    }

    public void addToScaled(ComparisonData runningSum, double ScaledFactor) {
        for (int i = 0; i < normalizedComparisons.length; i++) {
            runningSum.smoothedComparisons[i] += ScaledFactor * normalizedComparisons[i];
        }
    }

    public void subtractFrom(ComparisonData runningSum) {
        for (int i = 0; i < normalizedComparisons.length; i++) {
            runningSum.smoothedComparisons[i] -= normalizedComparisons[i];
        }
    }

    public void smooth(ComparisonData runningSum, int window) {
        for (int i = 0; i < normalizedComparisons.length; i++) {
            smoothedComparisons[i] = runningSum.smoothedComparisons[i] / window;
        }
    }

    public void resetNormalize() {
        for (int i = 0; i < rawComparisons.length; i++) {
            normalizedComparisons[i] = rawComparisons[i];
        }
    }

    public void setSmooth(ComparisonData value) {
        for (int i = 0; i < normalizedComparisons.length; i++) {
            smoothedComparisons[i] = value.smoothedComparisons[i];
        }
    }

    public void resetSmoothing() {
        for (int i = 0; i < rawComparisons.length; i++) {
            smoothedComparisons[i] = normalizedComparisons[i];
        }
    }

    public void reset() {
        for (int i = 0; i < rawComparisons.length; i++) {
            normalizedComparisons[i] = rawComparisons[i];
            smoothedComparisons[i] = normalizedComparisons[i];
        }
    }
    
    public void rangeValues(ComparisonData min, ComparisonData max) {
        for (int i = 0; i < rawComparisons.length; i++) {
                min.rawComparisons[i] = (normalizedComparisons[i] < min.rawComparisons[i]) ? normalizedComparisons[i] : min.rawComparisons[i];
                max.rawComparisons[i] = (normalizedComparisons[i] > max.rawComparisons[i]) ? normalizedComparisons[i] : max.rawComparisons[i];
        }
    }

    public float getMin() {
        float min = normalizedComparisons[0];

        for (int i = 1; i < normalizedComparisons.length; i++) {
                min = (normalizedComparisons[i] < min) ? normalizedComparisons[i] : min;
        }
        return min;
    }

    public float getMax() {
        float max = normalizedComparisons[0];

        for (int i = 1; i < normalizedComparisons.length; i++) {
                max = (normalizedComparisons[i] > max) ? normalizedComparisons[i] : max;
        }
        return max;
    }

    public float getSum() {
        float sum = 0;

        for (int i = 0; i < normalizedComparisons.length; i++) {
                sum += normalizedComparisons[i];
        }
        return sum;
    }

    public void normalizeInInterval(float min, float max) {
        if ((max - min) <= 0) {
            return;
        }
        for (int i = 0; i < normalizedComparisons.length; i++) {
           normalizedComparisons[i] = normalizedComparisons[i] / (max - min);
        }
    }

    public void normalizeSum(int SelfIdx) {
        float sum = getSum();
        for (int i = 0; i < normalizedComparisons.length; i++) {
           normalizedComparisons[i] = normalizedComparisons[i] / sum;
        }
    }
}
