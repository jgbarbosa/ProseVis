package prosevis.processing.model;

import prosevis.data.BreakLinesBy;
import prosevis.data.DocWord;
import prosevis.data.TypeMap;

public class Searcher {
  private DocWord lastResult;
  private int lastLabelIdx = TypeMap.kNoLabelIdx;
  private int lastTypeIdx = TypeMap.kNoLabelIdx;

  public DocWord search(DocWord firstWord, DocWord lineStart, int labelIdx, int typeIdx, BreakLinesBy renderType) {
    if (lastLabelIdx == labelIdx && lastTypeIdx == typeIdx && lastResult != null) {
      // we're doing the same search again, so just go forward from the current
      // first word, and look for a match
      // if we see our last result first thing, skip that one
      boolean waitForLast = false;
      if (lineStart.getLineIdx(renderType) == lastResult.getLineIdx(renderType)) {
        waitForLast = true;
      }
      for (DocWord cur = lineStart; cur != null; cur = cur.next()) {
        if (waitForLast) {
          if (cur == lastResult) {
            waitForLast = false;
          }
          continue;
        }
        if (cur.isSearchResult()) {
          lastResult = cur;
          return lastResult;
        }
      }
      return null;
    }

    // new search!
    // highlite everything, starting from the beginning, return the first word
    // after we cross the line we currently have visible
    lastLabelIdx = labelIdx;
    lastTypeIdx = typeIdx;
    DocWord result = null;
    boolean areAfterCurrentLine = false;
    for (DocWord cur = firstWord; cur != null; cur = cur.next()) {
      if (cur == lineStart) {
        areAfterCurrentLine = true;
      }
      for (int i = 0; i < cur.getSyllableCount(); i++) {
        cur.setIsSearchResult(false);
        if (cur.getTypeIdxForLabelIdx(labelIdx, i) == typeIdx) {
          cur.setIsSearchResult(true);
          break;
        }
      }
      if (areAfterCurrentLine &&
          cur.isSearchResult() &&
          result == null) {
        lastResult = result = cur;
      }
    }
    return result;
  }
}
