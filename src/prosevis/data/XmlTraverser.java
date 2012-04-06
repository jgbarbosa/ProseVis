package prosevis.data;

import java.util.LinkedList;

import nu.xom.Element;

public class XmlTraverser {
  private final LinkedList<Element> remaining;

  public XmlTraverser(Element root) {
    this.remaining = new LinkedList<Element>();
    this.remaining.add(root);
  }

  // this function iterates through the given element BFS and looks for sp
  // and ab nodes, returning them in the order they are found
  public Element getNextTextNode() {
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
}
