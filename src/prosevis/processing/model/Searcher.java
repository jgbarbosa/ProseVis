package prosevis.processing.model;

import prosevis.data.BreakLinesBy;
import prosevis.data.TypeMap;
import prosevis.data.Word;

public class Searcher {
  private Word lastResult;
  private int lastLabelIdx = TypeMap.kNoLabelIdx;
  private int lastTypeIdx = TypeMap.kNoLabelIdx;

  public Word search(Word firstWord, Word lineStart, int labelIdx, int typeIdx, BreakLinesBy renderType) {
    if (lastLabelIdx == labelIdx && lastTypeIdx == typeIdx && lastResult != null) {
      // we're doing the same search again, so just go forward from the current
      // first word, and look for a match
      // if we see our last result first thing, skip that one
      boolean waitForLast = false;
      if (lineStart.getLineIdx(renderType) == lastResult.getLineIdx(renderType)) {
        waitForLast = true;
      }
      for (Word cur = lineStart; cur != null; cur = cur.next()) {
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
    Word result = null;
    boolean areAfterCurrentLine = false;
    for (Word cur = firstWord; cur != null; cur = cur.next()) {
      if (cur == lineStart) {
        areAfterCurrentLine = true;
      }
      for (int i = 0; i < cur.getSyllableCount(); i++) {
        cur.setIsSearchResult(false);
        // we disallow looking at comparison data so we can pass null :(
        if (cur.getTypeIdxForLabelIdx(labelIdx, i, null) == typeIdx) {
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
