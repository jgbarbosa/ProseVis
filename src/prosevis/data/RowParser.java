package prosevis.data;

import java.util.ArrayList;
import java.util.HashMap;

public class RowParser {
  private static final String[] kKnownFields = {
    "tei_section_id",
    "tei_node_id",
    "tei_node_type",  
    "sentence_id",
    "phrase_id",    
    "word",
    "part_of_speech",  
    "accent",
    "phoneme", 
    "stress",
    "tone",
    "break_index",    
  };
  
  private static final String[] kExtraFields = {
    "paragraph_id",
  };

  public static RowParser buildParser(String firstRow) throws TransformationException {
    RowParser rp = new RowParser(firstRow);
    if (!rp.isValid()) {
      throw new TransformationException();
    }
    return rp;
  }

  private boolean isValid = true;

  private int headerLength;

  private final HashMap<Integer, Integer> canon2Orig = new HashMap<Integer, Integer>();

  private boolean isValid() {
    return isValid;
  }

  private RowParser(String firstRow) {
    String [] fields = firstRow.split("\t");
    if (fields.length < kKnownFields.length) {
      isValid = false;
      return;
    }
    ArrayList<Integer> similaryIndices = new ArrayList<Integer>();
    // map the canonical fields
    for (int i = 0; i < fields.length; i++) {
      int canonicalIdx = getCanonicalIndex(fields[i]);
      if (canonicalIdx < 0) {
        if (!isDiscardedField(fields[i])) {
          similaryIndices.add(i);
        }
        continue;
      }
      if (canon2Orig.containsKey(canonicalIdx)) {
        isValid = false;
        return;
      }
      canon2Orig.put(canonicalIdx, i);
    }
    // now add mapping for similarities
    for (int i = 0; i < similaryIndices.size(); i++) {
      canon2Orig.put(i + kKnownFields.length, similaryIndices.get(i));
    }
    headerLength = fields.length;
  }
 
  private boolean isDiscardedField(String str) {
    for (int i = 0; i < kExtraFields.length; i++) {
      if (kExtraFields[i].equals(str)) {
        return true;
      }
    }
    return false;
  }

  private int getCanonicalIndex(String str) {
    for (int i = 0; i < kKnownFields.length; i++) {
      if (kKnownFields[i].equals(str)) {
        return i;
      }
    }
    return -1;
  }

  public String[] getColumns(String line) throws TransformationException {
    String [] row = line.split("\t");
    if (row.length != headerLength) {
      throw new TransformationException();
    }
    String [] result = new String[canon2Orig.size()];
    
    for (int i = 0; i < result.length; i++) {
      if (!canon2Orig.containsKey(i)) {
        // we discard some keys, like paragraph id
        continue;
      }
      result[i] = row[canon2Orig.get(i)];
    }
    return result;
  }
}
