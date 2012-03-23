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

    HierNode goingDown = lineStart;
    while (goingDown.getFirstChild() != null && goingDown.getFirstChild().getFirstChild() != null) {
      goingDown = (HierNode)goingDown.getFirstChild();
    }

    ImplicitWordNode itr = (ImplicitWordNode)goingDown.getFirstChild();

    if (lineStart == lastResultLine) {
      itr = (ImplicitWordNode)lastResult.getNext();
    }

    int i;
    boolean keepGoing = true;
    for ( ; itr != null && keepGoing; itr = (ImplicitWordNode)itr.getNext()) {
      for (i = 0; i < itr.getSyllableCount(); i++) {
        if (itr.getTypeIdxForLabelIdx(labelIdx, i) == typeIdx) {
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
