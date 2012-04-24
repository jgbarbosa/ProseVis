package prosevis.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class XmlSearcher {
  public static String kSEASRNamespace =
      "http://www.seasr.org/ns/services/openmary/tei/1.0";
  private final String namespace;
  private final String attribute;
  private final LinkedList<Element> remaining;
  private final Map<String, Element> foundSoFar;

  public XmlSearcher(Document doc, String namespace, String attribute) {
    this.namespace = namespace;
    this.attribute = attribute;
    this.remaining = new LinkedList<Element>();
    this.foundSoFar = new HashMap<String, Element>();
    remaining.add(doc.getRootElement());
  }

  public XmlSearcher(Document doc) {
    this(doc, kSEASRNamespace, "id");
  }

  // Just use BFS to traverse our document, searching for the right stuff
  public Element findElement(String value) {
    if (foundSoFar.containsKey(value)) {
      return foundSoFar.get(value);
    }
    Attribute v;
    Element curr;
    while (!remaining.isEmpty()) {
      curr = remaining.poll();
      // there must be a good reason for this, because it sucks a lot
      // but I really doubt there was a good reason to completely ignore
      // the collections interface
      final int childCount = curr.getChildElements().size();
      for (int i = 0; i < childCount; i++) {
        remaining.add(curr.getChildElements().get(i));
      }
      if ((v = curr.getAttribute(attribute, namespace)) != null) {
        foundSoFar.put(v.getValue(), curr);
        if (value.equals(v.getValue())) {
          return curr;
        }
      }
    }

    throw new RuntimeException("Searched the entire document looking for " + value +
        " but couldn't find it, you must be crazy.");
  }

  public Element findChildLike(String seasrId, String childTag) {
    Element e = findElement(seasrId);
    // now, look for the first immediate child with the given tag
    final int childCount = e.getChildElements().size();
    for (int i = 0; i < childCount; i++) {
      if (e.getChildElements().get(i).getLocalName().equals(childTag)) {
        return e.getChildElements().get(i);
      }
    }
    throw new RuntimeException("Couldn't find child");
//    return null;
  }

  public String findTextInChildLike(String seasrId, String childTag) {
    Element child = findChildLike(seasrId, childTag);
    if (child == null) { return null; }
    for (int i = 0; i < child.getChildCount(); i++) {
      Node n = child.getChild(i);
      if (n instanceof Text) {
        return n.getValue().trim();
      }
    }
    throw new RuntimeException("Couldn't find child text");
  }
}
