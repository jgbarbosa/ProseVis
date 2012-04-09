package prosevis.processing.model;

import prosevis.data.BreakLinesBy;
import prosevis.data.DataTree;
import prosevis.data.TypeMap;
import prosevis.data.nodes.HierNode;
import prosevis.data.nodes.TreeSelector;
import prosevis.data.nodes.WordNode;

public class DataTreeView {
  private final DataTree data;
  private double scrollFraction;
  private boolean needsRender = true;
  private int currentFontSize = 14;
  private int colorByLabelIdx = TypeMap.kNoLabelIdx;
  private int textByLabelIdx = TypeMap.kWordIdx;
  private final Searcher searcher = new Searcher();
  public static final double SCROLL_TOP = 1.0;
  public static final double SCROLL_BOTTOM = 0.0;
  private static final double SCROLL_MULTIPLIER = 1.0;
  private static BreakLinesBy renderType = BreakLinesBy.Phrase;

  public synchronized void setRenderingBy(BreakLinesBy type) {
    if (renderType != type) {
      needsRender = true;
      renderType = type;
    }
    if (type.equals(BreakLinesBy.Line)) {
      data.setWhichTree(TreeSelector.WhichTree.XML);
    } else {
      data.setWhichTree(TreeSelector.WhichTree.TSV);
    }
  }

  public DataTreeView(DataTree data, int fontSz) {
    this.data = data;
    this.scrollFraction = SCROLL_TOP;
    if (renderType.equals(BreakLinesBy.Line)) {
      data.setWhichTree(TreeSelector.WhichTree.XML);
    } else {
      data.setWhichTree(TreeSelector.WhichTree.TSV);
    }
  }

  public synchronized DataTree getData() {
    return this.data;
  }

  public synchronized double getScroll() {
    return this.scrollFraction;
  }

  public synchronized void setScroll(double scroll) {
    this.scrollFraction = Math.max(0.0, Math.min(1.0, scroll));
    this.needsRender = true;
  }

  public static boolean sameFiles(DataTreeView[] views, DataTreeView[] lastViews) {
    if (views == null || lastViews == null) {
      return false;
    }
    if (views.length != lastViews.length) {
      return false;
    }
    for (int i = 0; i < views.length; i++) {
      if (views[i].data.getPath() != lastViews[i].data.getPath()) {
        return false;
      }
    }
    return true;
  }

  public synchronized ScrollInfo getScrollRenderInfo() {
    // 1 - scroll because top of file is 1.0 and bottom is 0.0
    double fracLines = data.getNumNodes(renderType) * (1 - this.scrollFraction);
    int lineNum = (int)fracLines;
    double lineFrac = fracLines - lineNum;
    lineNum = Math.min(lineNum, data.getNumNodes(renderType) - 1);
    HierNode node = data.findNode(renderType, lineNum);
    return new ScrollInfo(node, lineFrac);
  }

  public synchronized boolean getAndClearNeedsRender() {
    boolean ret = needsRender;
    needsRender = false;
    return ret;
  }

  public synchronized double addScrollOffset(int dy) {
    this.scrollFraction += (SCROLL_MULTIPLIER * dy) / (data.getNumNodes(renderType) * this.currentFontSize );
    this.scrollFraction = Math.max(0.0, Math.min(1.0, scrollFraction));
    this.needsRender = true;
    return this.scrollFraction;
  }

  public synchronized void setSize(int newSize) {
    if (currentFontSize != newSize) {
      this.needsRender = true;
    }
    this.currentFontSize = newSize;
  }

  public synchronized int getFontSize() {
    return currentFontSize;
  }

  public synchronized void setTextBy(int labelIdx) {
    textByLabelIdx = labelIdx;
    this.needsRender = true;
  }

  public synchronized int getTextBy() {
    return textByLabelIdx;
  }

  public synchronized void setColorBy(int labelIdx) {
    colorByLabelIdx = labelIdx;
    this.needsRender = true;
  }
  public synchronized int getColorBy() {
    return colorByLabelIdx;
  }

  public synchronized void searchForTerm(int typeIdx, int labelIdx) {
    int breakLevels = renderType.getHeight();
    ScrollInfo scrollInfo = getScrollRenderInfo();
    HierNode lineStartInHierarchy = scrollInfo.lineNode;
    if (scrollInfo.lineFrac > 0.2) {
      lineStartInHierarchy = (HierNode)lineStartInHierarchy.getNext();
    }
    if (lineStartInHierarchy == null) {
      return;
    }

    WordNode result = searcher.search(breakLevels, lineStartInHierarchy, labelIdx, typeIdx);
    if (result == null) {
      return;
    }
    // now we know a word with the desired property, adjust the scroll until we get there
    // find the appropriate hiernode
    lineStartInHierarchy = (HierNode)result.getParent();
    for (int i = 1; i < breakLevels; i++) {
      lineStartInHierarchy = (HierNode)lineStartInHierarchy.getParent();
    }

    this.scrollFraction = 1.0 - (lineStartInHierarchy.getNodeNumber() / (double) data.getNumNodes(renderType));
    this.needsRender = true;
  }
}
