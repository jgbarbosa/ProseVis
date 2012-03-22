package prosevis.processing.model;

import prosevis.data.HierNode;
import prosevis.data.ImplicitWordNode;

public class Searcher {
  private ImplicitWordNode lastResult;
  private HierNode lastResultLine = null;

  public ImplicitWordNode search(int breakLevels, HierNode lineStart, int labelIdx, int typeIdx) {
    if (lastResult != null) {
      lastResult.setIsSearchResult(false);
    }
    boolean skipLastResult = false;
    if (lineStart == lastResultLine) {
      skipLastResult = true;
    }
    while (lineStart.getFirstChild() != null && lineStart.getFirstChild().getFirstChild() != null) {
      lineStart = (HierNode)lineStart.getFirstChild();
    }

    ImplicitWordNode itr = (ImplicitWordNode)lineStart.getFirstChild();

    int i;
    boolean keepGoing = true;
    for ( ; itr != null && keepGoing; itr = (ImplicitWordNode)itr.getNext()) {
      for (i = 0; i < itr.getSyllableCount(); i++) {
        if (itr.getTypeIdxForLabelIdx(labelIdx, i) == typeIdx) {
          if (skipLastResult && itr == lastResult) {
            // this is not the result you're looking for :)
            break;
          }
          keepGoing = false;
          break;
        }
      }
      if (!keepGoing) {
        break;
      }
    }

    lastResult = itr;
    if (itr != null) {
      // notate that this word is our search result
      itr.setIsSearchResult(true);
      // find the hiernode corresponding to the line, remember it
      // if we search again and find that they haven't moved the display, then
      // we'll skip this result
      lastResultLine = (HierNode)itr.getParent();
      for (int j = 1; j < breakLevels; j++) {
        lastResultLine = (HierNode)lastResultLine.getParent();
      }
    } else {
      lastResultLine = null;
    }

    return lastResult;
  }
}
