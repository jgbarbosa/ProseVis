package prosevis.data;

public class NodeIterator {
  private WordNode nextWord;
  private HierNode parentNode;

  public NodeIterator(HierNode parent) {
    this.parentNode = parent;
    if (parentNode != null) {
      HierNode itr = parentNode;
      while (!itr.isPreWord()) {
        itr = (HierNode)itr.getFirstChild();
      }

      nextWord = (WordNode)itr.getFirstChild();

      setDisplayBreak();
    }
  }

  private void setDisplayBreak() {
    HierNode itr = parentNode;
    while (!itr.isPreWord()) {
      itr = (HierNode)itr.getLastChild();
    }

    WordNode lastWord = (WordNode) itr.getLastChild();
    lastWord.setDisplayBreak(true);
  }

  public WordNode next() {
    WordNode ret = nextWord;
    if (nextWord != null) {
      if (nextWord.getDisplayBreak()) {
        nextWord.setDisplayBreak(false);
        nextWord = null;
      } else {
        nextWord = (WordNode)nextWord.getNext();
      }
    }

    return ret;
  }

  public void clearDisplayBreak() {
    if (parentNode == null) return;
    HierNode itr = parentNode;
    while (!itr.isPreWord()) {
      itr = (HierNode)itr.getLastChild();
    }

    WordNode lastWord = (WordNode) itr.getLastChild();
    lastWord.setDisplayBreak(false);
  }
}
