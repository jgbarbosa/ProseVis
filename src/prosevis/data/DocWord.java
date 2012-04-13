package prosevis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DocWord {
  private final Map<Integer, Integer> labels2types = new HashMap<Integer, Integer>();
  private final ArrayList<Syllable> syllables = new ArrayList<Syllable>();
  private final boolean isPunctuation;
  private boolean isSearchResult = false;
  private DocWord next = null;
  private final String word;
  private final long[] ids = new long[BreakLinesBy.kNumIndices];
  private final int[] lineNumbers = new int[BreakLinesBy.kNumIndices];
  private final boolean isOpeningQuote;

  public DocWord(String word, Syllable s, long[] idTuple, boolean isOpeningQuote) {
    syllables.add(s);
    isPunctuation = !ParsingTools.notPunct(word);
    this.word = word;
    for (int i = 0; i < ids.length; i++) {
      this.ids[i] = idTuple[i];
    }
    this.isOpeningQuote = isOpeningQuote;
  }

  public void setNext(DocWord next) {
    this.next = next;
  }

  public DocWord next() {
    return this.next;
  }

  public void addLabelTypePair(int idx, int typeIdx) {
    this.labels2types.put(idx, typeIdx);
  }

  public int getTypeIdxForLabelIdx(int labelIdx) {
    return labels2types.get(labelIdx);
  }

  public int getTypeIdxForLabelIdx(int labelIdx, int syllableIdx) {
    for (int labelType : TypeMap.kSyllableTypes) {
      if (labelType == labelIdx) {
        return syllables.get(syllableIdx).getTypeIdxForLabelIdx(labelIdx);
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

  public boolean idsMatch(DocWord lastWord) {
    for (int i = 0; i < ids.length; i++) {
      if (ids[i] != lastWord.ids[i]) {
        return false;
      }
    }
    return true;
  }
}
