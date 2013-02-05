package prosevis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Word {

    private final Map<Integer, Integer> labels2types = new HashMap<Integer, Integer>();
    private final ArrayList<Syllable> syllables = new ArrayList<Syllable>();
    private final boolean isPunctuation;
    private boolean isSearchResult = false;
    private Word next = null;
    private final String word;
    private final long[] ids = new long[BreakLinesBy.kNumIndices];
    private final int[] lineNumbers = new int[BreakLinesBy.kNumIndices];
    private final boolean isOpeningQuote;
    private final boolean isMetaWord;
    private String act;
    private String speaker;
    private String stage;

    // in the most eggregious violation of object orientation of all time, a meta
    // word is like a normal word, except that everything about it is
    // uninitialized except for the bare minimum of fields
    public Word(String word) {
        this.isMetaWord = true;
        this.word = word;
        this.isPunctuation = false;
        this.isOpeningQuote = false;
    }

    public Word(String word, Syllable s, long[] idTuple, boolean isOpeningQuote) {
        syllables.add(s);
        isPunctuation = !ParsingTools.notPunct(word);
        this.word = word;
        for (int i = 0; i < ids.length; i++) {
            this.ids[i] = idTuple[i];
        }
        this.isOpeningQuote = isOpeningQuote;
        this.isMetaWord = false;
    }

    public void setNext(Word next) {
        this.next = next;
    }

    public Word next() {
        return this.next;
    }

    public void addLabelTypePair(int idx, int typeIdx) {
        this.labels2types.put(idx, typeIdx);
    }

    public int getTypeIdxForLabelIdx(int labelIdx) {
        return labels2types.get(labelIdx);
    }

    public int getTypeIdxForLabelIdx(int labelIdx, int syllableIdx, boolean[] enabledComparisons) {
        for (int labelType : TypeMap.kSyllableTypes) {
            if (labelType == labelIdx) {
                return syllables.get(syllableIdx).getTypeIdxForLabelIdx(labelIdx, enabledComparisons);
            }
        }
        return getTypeIdxForLabelIdx(labelIdx);
    }

    public int getTypeIdxForLabelIdx(int labelIdx, int syllableIdx) {
        return getTypeIdxForLabelIdx(labelIdx, syllableIdx, null);
    }

    public void addSyllable(Syllable s) {
        syllables.add(s);
    }

    public int getSyllableCount() {
        return syllables.size();
    }

    public boolean isPunct() {
        return isPunctuation;
    }

    public void setIsSearchResult(boolean b) {
        this.isSearchResult = b;
    }

    public boolean isSearchResult() {
        return isSearchResult;
    }

    public String word() {
        return word;
    }

    public long getId(int idx) {
        return ids[idx];
    }

    public void setLineNum(int idx, int num) {
        lineNumbers[idx] = num;
    }

    public int getLineIdx(BreakLinesBy renderType) {
        return lineNumbers[renderType.getIdx()];
    }

    public boolean isOpenQuote() {
        return isOpeningQuote;
    }

    public boolean idsMatch(Word lastWord) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] != lastWord.ids[i]) {
                return false;
            }
        }
        return true;
    }

    public void setProseLine(int line) {
        this.ids[BreakLinesBy.Line.getIdx()] = line;
    }

    public String getWord() {
        return this.word;
    }

    public boolean isMetaNode() {
        return isMetaWord;
    }

    // this massive hack is purely for Shakespeare displaying purposes
    // wiley 2012-04-23
    public void setShakespeareInfo(String lastAct, String lastStage, String lastSpeaker) {
        this.act = lastAct;
        this.stage = lastStage;
        this.speaker = lastSpeaker;
    }

    public String getShakespeareAct() {
        return act;
    }

    public String getShakespeareSpeaker() {
        return speaker;
    }

    public String getShakespeareStage() {
        return stage;
    }

    public float getComparisonValue(int syllableIdx, int syllableTypeIdx, int selfIdx) {
        return this.syllables.get(syllableIdx).getComparisonValue(syllableTypeIdx, selfIdx);
    }

    public static void normalizeReset(Word head) {

        final int numComparisons = head.syllables.get(0).getComparisonCount();

        if (numComparisons < 1) {
            return;
        }

        for (Word itr = head; itr != null; itr = itr.next) {
            for (Syllable s : itr.syllables) {
                s.getComparison().resetNormalize();
            }
        }
    }

    public static void normalizeDataAcrossEntireSpace(Word head, int SelfIdx) {

        final int numComparisons = head.syllables.get(0).getComparisonCount();

        if (numComparisons < 1) {
            return;
        }

        ComparisonData min = new ComparisonData(numComparisons,Float.MAX_VALUE);
        ComparisonData max = new ComparisonData(numComparisons,Float.MIN_VALUE);

        for (Word itr = head; itr != null; itr = itr.next) {
            for (Syllable s : itr.syllables) {
                s.getComparison().rangeValues(min, max);
            }
        }

        for (Word itr = head; itr != null; itr = itr.next) {
            for (Syllable s : itr.syllables) {
                s.getComparison().normalizeInInterval(min.getMin(), max.getMax());
            }
        }

    }

    public static void normalizeDataComparison(Word head, int SelfIdx) {

        final int numComparisons = head.syllables.get(0).getComparisonCount();

        if (numComparisons < 1) {
            return;
        }

        for (Word itr = head; itr != null; itr = itr.next) {
            for (Syllable s : itr.syllables) {
                s.getComparison().normalizeInInterval(s.getComparison().getMin(), s.getComparison().getMax());
            }
        }

//        for (Word itr = head; itr != null; itr = itr.next) {
//            for (Syllable s : itr.syllables) {
//                s.getComparison().normalizeSum(SelfIdx);
//            }
//        }
    }

    private static double[] generateGauss(int _Size) {
        //   Allocate window buffer
        double[] Weights = new double[_Size];
        //   Check allocation
        //   Set window size
        int Size = _Size;
        //   Window half
        int Half = Size >> 1;
        //   Central weight
        Weights[Half] = 1.;
        //   The rest of weights
        for (int Weight = 1; Weight < Half + 1; ++Weight) {
            //   Support point
            double x = 3. * (double) Weight / (double) Half;
            //   Corresponding symmetric weights
            Weights[Half - Weight] = Weights[Half + Weight] = Math.exp(-x * x / 2.);
        }
        //   Weight sum
        double k = 0.;
        for (int Weight = 0; Weight < Size; ++Weight) {
            k += Weights[Weight];
        }
        //   Weight scaling
        for (int Weight = 0; Weight < Size; ++Weight) {
            Weights[Weight] /= k;
        }
        //   Succeeded
        return Weights;
    }

    public static void smoothData(final int window, Word head, boolean useGauss, boolean useNormEntDom, boolean useNormComparison, int SelfIdx) {
        for (Word itr = head; itr != null; itr = itr.next) {
            for (Syllable s : itr.syllables) {
                s.reset();
            }
        }
        
        
        normalizeDataAcrossEntireSpace(head, SelfIdx);
        normalizeDataComparison(head, SelfIdx);
        
        if (window == 1) {
            return;
        }

        final int prefixSize = window / 2;
        final int numComparisons = head.syllables.get(0).getComparisonCount();

        if (numComparisons < 1) {
            return;
        }



        ArrayList<ComparisonData> allValues = new ArrayList<ComparisonData>();

        for (Word itr = head; itr != null; itr = itr.next) {
            for (Syllable s : itr.syllables) {
                allValues.add(s.getComparison());
            }
        }

        double[] W = generateGauss(window);

        for (int i = prefixSize; i < allValues.size() - (prefixSize + 1); i++) {

            ComparisonData sum = new ComparisonData(numComparisons);

            for (int j = -prefixSize; j < prefixSize + 1; j++) {
                if (useGauss) {
                    allValues.get(i + j).addToScaled(sum, W[j + prefixSize]);
                } else {
                    allValues.get(i + j).addToScaled(sum, 1.f / (float) window);
                }
            }
            allValues.get(i).setSmooth(sum);;
        }

//        Syllable curr = null;
//        
//        LinkedList<Syllable> before = new LinkedList<Syllable>();
//        ComparisonData runningSum = new ComparisonData(numComparisons);
//        LinkedList<Syllable> after = new LinkedList<Syllable>();
//        
//        for (Word itr = head; itr != null; itr = itr.next) {
//            for (Syllable s : itr.syllables) {
//                if (before.size() < prefixSize) {
//                    before.add(s);
//                    s.addTo(runningSum);
//                    continue;
//                } else if (curr == null) {
//                    curr = s;
//                    s.addTo(runningSum);
//                    continue;
//                } else if (after.size() < prefixSize) {
//                    after.add(s);
//                    s.addTo(runningSum);
//                    continue;
//                }
//        
//                curr.smooth(runningSum, window);
//                
//                before.pollFirst().subtractFrom(runningSum);
//                curr = after.pollFirst();
//                after.add(s);
//                s.addTo(runningSum);
//            }
//        }

    }
}
