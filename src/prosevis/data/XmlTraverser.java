package prosevis.data;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class XmlTraverser {
  // Queue of all nodes in document, which we're searching for text in
  private final LinkedList<Element> remaining = new LinkedList<Element>();
  // Queue of all nodes remaining in our current node
  private final LinkedList<Node> possiblyHaveText = new LinkedList<Node>();

  public XmlTraverser(Element root) {
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
      if ("ab".equals(e.getQualifiedName()) || "l".equals(e.getQualifiedName()) || "p".equals(e.getQualifiedName())) {
        return e;
      }
    }
    return null;
  }

  public String getNextLineOfText() {
    while (true) {
      while (possiblyHaveText.isEmpty()) {
        Element next = getNextTextNode();
        if (next == null) {
          return null;
        }
        for (int i = next.getChildCount() - 1; i >= 0; i--) {
          possiblyHaveText.push(next.getChild(i));
        }
      }
      Node curr = possiblyHaveText.pop();
      if (curr == null) {
        continue;
      }
      if (curr instanceof Text) {
        return curr.getValue();
      } else {
        for (int i = curr.getChildCount() - 1; i >= 0; i--) {
          possiblyHaveText.push(curr.getChild(i));
        }
      }
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
