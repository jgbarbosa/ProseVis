package prosevis.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TypeMap {
  // this hack is very different from the other indices
  // we catch it by special case and do something very different at render time
  public static final int kColorByComparisonIdx = -5;
  // second consonant of the phoneme
  public static final int kPhonemeC2Idx = -4;
  // vowel of the phoneme
  public static final int kPhonemeVIdx = -3;
  // first consonant of the phoneme
  public static final int kPhonemeC1Idx = -2;
  public static final int kNoLabelIdx = -1;
  // your guess is as good as mine
  public static final int kSoundexIdx = 0;
  public static final int kWordIdx = 5;
  public static final int kPOSLabelIdx = 6;
  public static final int kAccentIdx = 7;
  // idx for the whole phoneme
  public static final int kPhonemeIdx = 8;
  public static final int kStressIdx = 9;
  public static final int kToneIdx = 10;
  public static final String kNoLabelLabel = "none";
  public static final String kWordLabel = "word";
  public static final String kPosLabel = "pos";
  public static final String kPhonemeAllLabel = "sound-full";
  public static final String kPhonemeStartLabel = "sound-initial";
  public static final String kPhonemeVowelLabel = "sound-vowel";
  public static final String kPhonemeFinalLabel = "sound-final";
  public static final String kSoundexLabel = "soundex";
  public static final String kColorByComparison = "comparison";

  // not reall immutable, but nothing lasts forever
  public static final String[] kPossibleColorByLabels = {"none", "stress", "pos", "tone", "accent", "soundex", "word", kPhonemeAllLabel, kPhonemeStartLabel, kPhonemeVowelLabel, kPhonemeFinalLabel, kColorByComparison};
  public static final String[] kPossibleTextByLabels = {"none", "pos", kPhonemeAllLabel, "word"};
  public static final int[] kSyllableTypes = {kPhonemeC2Idx, kPhonemeVIdx, kPhonemeC1Idx, kPhonemeIdx, kStressIdx, kColorByComparisonIdx};
  public static final int kMaxFields = 12;
  public static final int kNoTypeIdx = -1;

  // maps from labels to labelIdx's
  private final Map<String, Integer> label2labelIdx = new HashMap<String, Integer>();
  // maps from labelIdx to individual mappings from types to typeIdx
  private final Map<Integer, Map<String, Integer>> type2typeIdx = new HashMap<Integer, Map<String, Integer>>();
  // maps from labelIdx to Map<TypeIdx, Type>
  private final Map<Integer, Map<Integer, String>> typeIdx2type = new HashMap<Integer, Map<Integer, String>>();
  private String[] comparisonDataHeaders = null;

  public TypeMap() {
    addLabel("soundex", kSoundexIdx);
    addLabel(kNoLabelLabel, kNoLabelIdx);
    addLabel(kPhonemeAllLabel, kPhonemeIdx);
    addLabel(kPhonemeStartLabel, kPhonemeC1Idx);
    addLabel(kPhonemeVowelLabel, kPhonemeVIdx);
    addLabel(kPhonemeFinalLabel, kPhonemeC2Idx);
  }

  public TypeMap(TypeMap other) {
    this();
    label2labelIdx.putAll(other.label2labelIdx);
    for (Entry<Integer, Map<String, Integer>> entry: other.type2typeIdx.entrySet()) {
      Map<String, Integer> t2tidx = new HashMap<String, Integer>();
      t2tidx.putAll(entry.getValue());
      this.type2typeIdx.put(entry.getKey(), t2tidx);
    }
    for (Entry<Integer, Map<Integer, String>> entry: other.typeIdx2type.entrySet()) {
      Map<Integer, String> tidx2t = new HashMap<Integer, String>();
      tidx2t.putAll(entry.getValue());
      this.typeIdx2type.put(entry.getKey(), tidx2t);
    }

    this.comparisonDataHeaders = other.comparisonDataHeaders;
  }

  public void addLabel(String label, int labelIdx) {
      // if label is special, do something different
      if (label2labelIdx.containsKey(label)) {
        if (labelIdx != label2labelIdx.get(label)) {
          throw new RuntimeException("Can't add duplicate label with differrent labelIdx");
        }
      } else {
        label2labelIdx.put(label, labelIdx);
        type2typeIdx.put(labelIdx, new HashMap<String, Integer>());
        typeIdx2type.put(labelIdx, new HashMap<Integer, String>());
      }
  }

  public int getOrAddTypeIdx(int labelIdx, String type) {
    Map<String, Integer> t2tI = type2typeIdx.get(labelIdx);
    if (!t2tI.containsKey(type)) {
      int typeIdx = t2tI.size();
      t2tI.put(type, typeIdx);
      Map<Integer, String> tI2t = typeIdx2type.get(labelIdx);
      tI2t.put(typeIdx, type);
    }
    return t2tI.get(type);
  }

  public int maybeGetTypeIdx(int labelIdx, String type) {
    Map<String, Integer> t2tI = type2typeIdx.get(labelIdx);
    if (!t2tI.containsKey(type)) {
      return -1;
    }
    return t2tI.get(type);
  }

  public String getTypeForIdx(int labelIdx, int typeIdx) {
    return this.typeIdx2type.get(labelIdx).get(typeIdx);
  }

  public boolean mergeTypeMap(TypeMap other) {
    boolean changed = false;
    // first reconcile the labels, looking for conflicts
    for (Entry<String, Integer> s: other.label2labelIdx.entrySet()) {
      if (!label2labelIdx.containsKey(s.getKey())) {
        label2labelIdx.put(s.getKey(), s.getValue());
        changed = true;
      } else if (label2labelIdx.get(s.getKey()) != s.getValue()) {
        throw new RuntimeException("Error, somehow our type maps got out of sync with each other");
      }
    }

    for (Entry<Integer, Map<String, Integer>> s: other.type2typeIdx.entrySet()) {
      Map<String, Integer> theirs = s.getValue();
      if (!type2typeIdx.containsKey(s.getKey())) {
        type2typeIdx.put(s.getKey(), new HashMap<String, Integer>());
        changed = true;
      }
      Map<String, Integer> mine = type2typeIdx.get(s.getKey());
      for (Entry<String, Integer> t: theirs.entrySet()) {
        if (!mine.containsKey(t.getKey())) {
          mine.put(t.getKey(), t.getValue());
          changed = true;
        } else if (t.getValue() != mine.get(t.getKey())) {
          throw new RuntimeException("Error, somehow our type maps got out of sync with each other!");
        }
      }
    }

    for (Entry<Integer, Map<Integer, String>> s: other.typeIdx2type.entrySet()) {
      Map<Integer, String> theirs = s.getValue();
      if (!typeIdx2type.containsKey(s.getKey())) {
        typeIdx2type.put(s.getKey(), new HashMap<Integer, String>());
        changed = true;
      }
      Map<Integer, String> mine = typeIdx2type.get(s.getKey());
      for (Entry<Integer, String> t: theirs.entrySet()) {
        if (!mine.containsKey(t.getKey())) {
          mine.put(t.getKey(), t.getValue());
          changed = true;
        } else if (t.getValue() != mine.get(t.getKey())) {
          throw new RuntimeException("Error, somehow our type maps got out of sync with each other!!");
        }
      }
    }

    if (comparisonDataHeaders == null) {
      this.comparisonDataHeaders = other.comparisonDataHeaders;
      changed = changed || other.comparisonDataHeaders != null;
    }
    return changed;
  }

  public Integer getLabelIdx(String label) {
    if (label.equals(TypeMap.kColorByComparison)) {
      return TypeMap.kColorByComparisonIdx;
    }
    return label2labelIdx.get(label);
  }

  public boolean hasLabel(String label) {
    return label2labelIdx.containsKey(label);
  }

  public Collection<Integer> getLabelIdxs() {
    return this.label2labelIdx.values();
  }

  public Collection<Integer> getTypeIdxsForLabel(int labelIdx) {
    Map<Integer, String> forLabel = typeIdx2type.get(labelIdx);
    if (forLabel == null) {
      return Collections.emptyList();
    }
    return forLabel.keySet();
  }

  public boolean hasComparisonDataHeaders() {
    return comparisonDataHeaders != null;
  }

  public String [] getComparisonDataHeaders() {
    return comparisonDataHeaders;
  }

  public void addComparisonDataHeaders(String [] newHeaders) {
    if (comparisonDataHeaders != null) {
      return;
    }
    this.comparisonDataHeaders = new String[newHeaders.length];
    this.addLabel(kColorByComparison, kColorByComparisonIdx);
    for (int i = 0; i < newHeaders.length; i++) {
      comparisonDataHeaders[i] = newHeaders[i];
      // put the headers in so that the random colorset has something to reason about
      this.getOrAddTypeIdx(kColorByComparisonIdx, newHeaders[i]);
    }
  }

  public void clearComparisonData() {
    this.comparisonDataHeaders = null;
  }
}
