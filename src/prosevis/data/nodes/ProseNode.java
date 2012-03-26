package prosevis.data.nodes;

public abstract class ProseNode {
  boolean levelBreak = false;
  ProseNode next = null;
  private final ProseNode parent;

  public ProseNode(ProseNode parent) {
    this.parent = parent;
  }

  public void addBreak() {
    levelBreak = true;
  }

  public boolean getBreak() {
    return levelBreak;
  }

  public void setNext(ProseNode next) {
    this.next = next;
  }

  public ProseNode getNext() {
    return next;
  }

  public ProseNode getParent() {
    return parent;
  }

  public abstract ProseNode getFirstChild();
}
