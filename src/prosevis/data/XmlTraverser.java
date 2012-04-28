package prosevis.data;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class XmlTraverser {
  private final LinkedList<Element> remaining;
  // some elements have text before and after intermediate nodes
  private Element current = null;
  private int nextChildIdx = -1;

  public XmlTraverser(Element root) {
    this.remaining = new LinkedList<Element>();
    this.remaining.add(root);
  }

  // this function iterates through the given element BFS and looks for sp
  // and ab nodes, returning them in the order they are found
  private Element getNextTextNode() {
    while (!remaining.isEmpty()) {
      Element e = remaining.poll();
      final int childrenCount = e.getChildElements().size();
      for (int i = 0; i < childrenCount; i++) {
        remaining.add(e.getChildElements().get(i));
      }
      if ("ab".equals(e.getQualifiedName()) || "l".equals(e.getQualifiedName())) {
        return e;
      }
    }
    return null;
  }

  public String getNextLineOfText() {
    while (true) {
      if (current == null || nextChildIdx < 0) {
        current = getNextTextNode();
        nextChildIdx = 0;
      }
      if (current == null) {
        return null;
      }
      for ( ; nextChildIdx < current.getChildCount(); nextChildIdx++) {
        Node n = current.getChild(nextChildIdx);
        if (n instanceof Text) {
          nextChildIdx++;
          return n.getValue();
        }
      }
      current = null;
    }
  }

  public String getNextCleanLineOfText() {
    String unclean = getNextLineOfText();
    if (unclean == null) {
      return null;
    }
    return unclean.trim().replaceAll("", "\"")
        .replaceAll("", "\"");
  }
}
