package prosevis.data;

public abstract class AbstractWordNode extends ProseNode {
  public AbstractWordNode(ProseNode parent) {
    super(parent);
  }
  public abstract void setDisplayBreak(boolean isBreak);
  public abstract boolean getDisplayBreak();
}
