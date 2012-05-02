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

  public void addSyllable(Syllable s){
    syllables.add(s);
  }

  public int getSyllableCount(){
      return syllables.size();
  }

  public boolean isPunct() {
    return isPunctuation;
  }

  public void setIsSearchResult(boolean b) {
    this.isSearchResult  = b;
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

  public float getComparisonValue(int syllableIdx, int syllableTypeIdx) {
    return this.syllables.get(syllableIdx).getComparisonValue(syllableTypeIdx);
  }

  public static void smoothData(final int window, Word head) {
    if (window == 1) {
      for (Word itr = head; itr != null; itr = itr.next) {
        for (Syllable s: itr.syllables) {
          s.resetSmoothing();
        }
      }
      return;
    }

    final int prefixSize = window / 2;
    final int numComparisons = head.syllables.get(0).getComparisonCount();
    if (numComparisons < 1) {
      return;
    }
    Syllable curr = null;
    LinkedList<Syllable> before = new LinkedList<Syllable>();
    double beforeSum = 0.0;
    ComparisonData runningSum = new ComparisonData(numComparisons);
    LinkedList<Syllable> after = new LinkedList<Syllable>();
    double afterSum = 0.0;
    for (Word itr = head; itr != null; itr = itr.next) {
      for (Syllable s: itr.syllables) {
        if (before.size() < prefixSize) {
          before.add(s);
          s.addTo(runningSum);
          continue;
        } else if (curr == null) {
          curr = s;
          s.addTo(runningSum);
          continue;
        } else if (after.size() < prefixSize) {
          after.add(s);
          s.addTo(runningSum);
          continue;
        }
        curr.smooth(runningSum, window);
        before.pollFirst().subtractFrom(runningSum);
        curr = after.pollFirst();
        after.add(s);
        s.addTo(runningSum);
      }
    }
  }
}
