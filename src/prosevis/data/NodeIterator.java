package prosevis.data;

public class NodeIterator<T extends AbstractWordNode> {
  private T nextWord;
  private final HierNode parentNode;

  public NodeIterator(HierNode parent) {
    this.parentNode = parent;
    if (parentNode != null) {
      HierNode itr = parentNode;
      while (!itr.isPreWord()) {
        itr = (HierNode)itr.getFirstChild();
      }

      nextWord = (T)itr.getFirstChild();

      setDisplayBreak();
    }
  }

  private void setDisplayBreak() {
    HierNode itr = parentNode;
    while (!itr.isPreWord()) {
      itr = (HierNode)itr.getLastChild();
    }

    T lastWord = (T)itr.getLastChild();
    lastWord.setDisplayBreak(true);
  }

  public T next() {
    T ret = nextWord;
    if (nextWord != null) {
      if (nextWord.getDisplayBreak()) {
        nextWord.setDisplayBreak(false);
        nextWord = null;
      } else {
        nextWord = (T)nextWord.getNext();
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
