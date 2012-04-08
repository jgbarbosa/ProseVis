package prosevis.data.nodes;

import java.util.ArrayList;

public class HierNode extends ProseNode {

  int phonemeCount;
  int wordCount;
  double textWidth;
  double phonemeWidth;
  double posWidth;

  boolean preWord;
  ArrayList<ProseNode> children = new ArrayList<ProseNode>();
  // 0 means the first node on this hierarchy level, 1 the second, etc
  private final int nodeNumber;
  // this is the unique number per file representing this hierarchy group
  private final long nodeId;

  public HierNode(ProseNode parent, int ordinal, long id) {
    super(parent);

    wordCount = 0;
    phonemeCount = 0;
    textWidth = 0.0;
    phonemeWidth = 0.0;
    posWidth = 0.0;
    nodeNumber = ordinal;
    nodeId = id;
  }

  public void incTextWidth(double incWidth) {
    textWidth += incWidth;
  }

  public double getTextWidth() {
    return textWidth;
  }

  public void incPhonemeWidth(double incWidth) {
    phonemeWidth += incWidth;
  }

  public double getPhonemeWidth() {
    return phonemeWidth;
  }

  public void incPOSWidth(double incWidth) {
    posWidth += incWidth;
  }

  public double getPOSWidth() {
    return posWidth;
  }

  public void incWordCount() {
    wordCount++;
  }

  public int getWordCount() {
    return wordCount;
  }

  public void incPhonemeCount() {
    phonemeCount++;
  }

  public int getPhonemeCount() {
    return phonemeCount;
  }
  public void addChild(ProseNode child) {
    addChild(child, false);
  }

  public void addChild(ProseNode child, boolean bypassSetNext) {
    if (bypassSetNext) {
      // for instance, if you're a hiernode in the XML tree, these next fields
      // are already initialized by the TSV tree
      children.add(child);
      return;
    }
    if (children.size() > 0) {
      children.get(children.size() - 1).setNext(child);
    } else {
      // seek our predecessor in this tree
      HierNode cur = this;
      HierNode up = (HierNode)this.getParent();
      int levelsUp = 0;
      while (up != null && up.getFirstChild() == cur) {
        cur = up;
        up = (HierNode)up.getParent();
        levelsUp++;
      }
      if (up != null) {
        // now go all the way right on this level, under this parent
        // then follow those nodes all the way back down
        HierNode goRight = (HierNode)up.getFirstChild();
        while (goRight.getNext().getParent() == up && goRight.getNext() != cur) {
          goRight = (HierNode)goRight.getNext();
        }
        while (levelsUp > 0) {
          goRight = (HierNode)goRight.getLastChild();
          levelsUp--;
        }
        goRight.getLastChild().setNext(child);
      }
    }
    children.add(child);
  }

  public boolean isPreWord() {
    return children.size() > 0 && getFirstChild() instanceof WordNode;
  }

  public ArrayList<ProseNode> getChildren() {
    return children;
  }

  @Override
  public ProseNode getFirstChild() {
    if (children.size() > 0) {
      return children.get(0);
    } else {
      return null;
    }
  }

  public ProseNode getLastChild() {
    int size = numChildren();
    return children.get(size - 1);
  }

  public int numChildren() {
    return children.size();
  }

  public void findNode(int curHeight, int reqHeight, int ordinal,
      HierNodeWrapper result) {
    if (curHeight == reqHeight) {
      if (ordinal < this.nodeNumber) {
        result.resultType = HierNodeWrapper.ResultType.TOO_BIG;
      } else if (ordinal > this.nodeNumber) {
        result.resultType = HierNodeWrapper.ResultType.TOO_SMALL;
      } else {
        result.resultType = HierNodeWrapper.ResultType.VALID;
      }
      result.result = this;
      return;
    }
    if (this.getMinAtHeight(curHeight, reqHeight) > ordinal) {
      result.resultType = HierNodeWrapper.ResultType.TOO_BIG;
      return;
    }
    if (this.getMaxAtHeight(curHeight, reqHeight) < ordinal) {
      result.resultType = HierNodeWrapper.ResultType.TOO_SMALL;
      return;
    }
    for (int min = 0, max = children.size() - 1; min <= max;) {
      int mid = (min + max + 1) / 2;
      HierNode child = (HierNode) children.get(mid);
      child.findNode(curHeight - 1, reqHeight, ordinal, result);
      if (min == max) {
        return;
      }
      switch (result.resultType) {
      case TOO_BIG:
        max = mid - 1;
        break; // switch
      case TOO_SMALL:
        min = mid + 1;
        break; // switch
      default:
        return; // function
      }
    }
    throw new RuntimeException(
        "Tried to find node that doesn't exist in this tree");
  }

  private int getMinAtHeight(int curHeight, int reqHeight) {
    if (curHeight > reqHeight) {
      return ((HierNode) children.get(0))
          .getMinAtHeight(curHeight - 1, reqHeight);
    }
    return this.nodeNumber;
  }

  private int getMaxAtHeight(int curHeight, int reqHeight) {
    if (curHeight > reqHeight) {
      return ((HierNode) children.get(children.size() - 1)).getMaxAtHeight(
          curHeight - 1, reqHeight);
    }
    return this.nodeNumber;
  }

  public int getNodeNumber() {
    return nodeNumber;
  }

  public long getId() {
    return nodeId;
  }
}
