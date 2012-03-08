package prosevis.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TypeMap {
  public static final int kSoundexIdx = 0;
  public static final int kNoLabelIdx = -1;
  public static final int kPOSLabelIdx = 6;
  public static final int kWordLabelIdx = 5;
  // not reall immutable, but nothing lasts forever
  public static final String[] kPossibleColorByLabels = {"none", "word", "pos"};
  public static final String[] kPossibleTextByLabels = {"none", "word", "pos"};
  public static String kNoLabelLabel = "none";
  // maps from labels to labelIdx's
  private final Map<String, Integer> label2labelIdx = new HashMap<String, Integer>();
  // maps from labelIdx to individual mappings from types to typeIdx
  private final Map<Integer, Map<String, Integer>> type2typeIdx = new HashMap<Integer, Map<String, Integer>>();
  // maps from labelIdx to Map<TypeIdx, Type>
  private final Map<Integer, Map<Integer, String>> typeIdx2type = new HashMap<Integer, Map<Integer, String>>();
  public TypeMap() {
    addLabel("soundex", 0);
    addLabel(kNoLabelLabel, kNoLabelIdx);
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

  public int getTypeIdx(int labelIdx, String type) {
    Map<String, Integer> t2tI = type2typeIdx.get(labelIdx);
    if (!t2tI.containsKey(type)) {
      int typeIdx = t2tI.size();
      t2tI.put(type, typeIdx);
      Map<Integer, String> tI2t = typeIdx2type.get(labelIdx);
      tI2t.put(typeIdx, type);
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
    return changed;
  }

  public int getLabelIdx(String label) {
    return label2labelIdx.get(label);
  }

  public boolean hasLabel(String label) {
    return label2labelIdx.containsKey(label);
  }

  public Collection<Integer> getLabelIdxs() {
    return this.label2labelIdx.values();
  }

  public Collection<Integer> getTypeIdxsForLabel(int labelIdx) {
    return this.typeIdx2type.get(labelIdx).keySet();
  }
}
