package prosevis.data;

import java.util.ArrayList;

public class HierNode extends ProseNode {

  int phonemeCount;
  int wordCount;
  double textWidth;
  double phonemeWidth;
  double posWidth;

  boolean preWord;
  ArrayList<ProseNode> children;
  // 0 means the first node on this hierarchy level, 1 the second, etc
  private final int nodeNumber;

  public HierNode(ProseNode parent, boolean preWord, int ordinal) {
    super(parent);
    this.preWord = preWord;

    children = new ArrayList<ProseNode>();
    wordCount = 0;
    phonemeCount = 0;
    textWidth = 0.0;
    phonemeWidth = 0.0;
    posWidth = 0.0;
    nodeNumber = ordinal - 1;
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
    children.add(child);
  }

  public boolean isPreWord() {
    return preWord;
  }

  public ArrayList<ProseNode> getChildren() {
    return children;
  }

  @Override
  public ProseNode getFirstChild() {
    return children.get(0);
  }

  public ProseNode getLastChild() {
    int size = numChildren();
    return children.get(size - 1);
  }

  public int numChildren() {
    return children.size();
  }

  public void findNode(int currLevel, int reqLevel, int ordinal,
      HierNodeWrapper result) {
    if (currLevel == reqLevel) {
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
    if (this.getMinAtLevel(currLevel, reqLevel) > ordinal) {
      result.resultType = HierNodeWrapper.ResultType.TOO_BIG;
      return;
    }
    if (this.getMaxAtLevel(currLevel, reqLevel) < ordinal) {
      result.resultType = HierNodeWrapper.ResultType.TOO_SMALL;
      return;
    }
    int mid;
    for (int min = 0, max = children.size() - 1; min <= max;) {
      mid = (min + max + 1) / 2;
      HierNode child = (HierNode) children.get(mid);
      child.findNode(currLevel + 1, reqLevel, ordinal, result);
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

  private int getMinAtLevel(int currLevel, int reqLevel) {
    if (currLevel < reqLevel) {
      return ((HierNode) children.get(0))
          .getMinAtLevel(currLevel + 1, reqLevel);
    }
    return this.nodeNumber;
  }

  private int getMaxAtLevel(int currLevel, int reqLevel) {
    if (currLevel < reqLevel) {
      return ((HierNode) children.get(children.size() - 1)).getMaxAtLevel(
          currLevel + 1, reqLevel);
    }
    return this.nodeNumber;
  }

  public int getNodeNumber() {
    return nodeNumber;
  }
}
