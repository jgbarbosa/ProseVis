package prosevis.data.nodes;

public abstract class ProseNode {
  ProseNode next = null;
  private final ProseNode parent;

  public ProseNode(ProseNode parent) {
    this.parent = parent;
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

  public int getHeight() {
    ProseNode child = getFirstChild();
    if (child == null) {
      return 0;
    }
    return child.getHeight() + 1;
  }


  public abstract ProseNode getFirstChild();
}
