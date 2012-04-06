package prosevis.data.nodes;

public class TreeSelector {
  public enum WhichTree {
    XML,
    TSV,
  }
  private WhichTree whichTree = WhichTree.TSV;
  public WhichTree whichTree() {
    return whichTree;
  }

  public void setWhichTree(WhichTree whichTree) {
    this.whichTree = whichTree;
  }
}
