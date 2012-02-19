package prosevis.processing.model;

import prosevis.data.DataTree;
import prosevis.data.HierNode;
import prosevis.data.ICon;

public class DataTreeView {
  private DataTree data;
  private double scrollFraction;
  private boolean needsRender = true;
  private int currentFontSize = 14;
  
  public static final double SCROLL_TOP = 1.0;
  public static final double SCROLL_BOTTOM = 0.0;
  private static final double SCROLL_MULTIPLIER = 1.0;
  public enum RenderBy {
    CHAPTER,
    SECTION,
    PARAGRAPH,
    SENTENCE,
    PHRASE,    
  };
  private static RenderBy renderType = RenderBy.PHRASE;
  
  public synchronized void setRenderingBy(RenderBy type) {
    renderType = type;
    needsRender = true;
  }
  
  public DataTreeView(DataTree data, double scroll) {
    this.data = data;
    this.scrollFraction = scroll;
  }

  public DataTreeView(DataTree data) {
    this(data, SCROLL_TOP);
  }
  
  public synchronized DataTree getData() {
    return this.data;
  }
  
  public synchronized double getScroll() {
    return this.scrollFraction;
  }
  
  public synchronized void setScroll(double scroll) {
    this.scrollFraction = scroll;
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

  public synchronized HierNode getStartingLine() {
    HierNode parent = null;
    // 1 - scroll because top of file is 1.0 and bottom is 0.0
    int index = -1;
    switch (renderType) {
    case CHAPTER:
      index = ICon.CHAPTER_IND;
      break;
    case SECTION:
      index = ICon.SECTION_IND;
      break;
    case PARAGRAPH:
      index = ICon.PARAGRAPH_IND;
      break;
    case SENTENCE:
      index = ICon.SENTENCE_IND;
      break;
    case PHRASE:
      index = ICon.PHRASE_IND;
      break;
    default:
      throw new RuntimeException("Can't search for starting line at this hierarchy level");
    }
    int lineNum = (int)(data.getNumNodes(index) * (1 - this.scrollFraction));
    lineNum = Math.min(lineNum, data.getNumNodes(index) - 1);
    return data.findNode(index, lineNum);

  }
  
  public synchronized boolean getAndClearNeedsRender() {
    boolean ret = needsRender;
    needsRender = false;
    return ret;
  }

  public synchronized double addScrollOffset(int dy) {
    switch (renderType) {
    case CHAPTER:
      this.scrollFraction += (SCROLL_MULTIPLIER * dy) / (data.getNumNodes(ICon.CHAPTER_IND) * this.currentFontSize );
      break;
    case SECTION:
      this.scrollFraction += (SCROLL_MULTIPLIER * dy) / (data.getNumNodes(ICon.SECTION_IND) * this.currentFontSize);
      break;
    case PARAGRAPH:
      this.scrollFraction += (SCROLL_MULTIPLIER * dy) / (data.getNumNodes(ICon.PARAGRAPH_IND) * this.currentFontSize);
      break;
    case SENTENCE:
      this.scrollFraction += (SCROLL_MULTIPLIER * dy) / (data.getNumNodes(ICon.SENTENCE_IND) * this.currentFontSize);
      break;
    case PHRASE:
      this.scrollFraction += (SCROLL_MULTIPLIER * dy) / (data.getNumNodes(ICon.PHRASE_IND) * this.currentFontSize);
      break;
    default:
      throw new RuntimeException("Can't search for starting line at this hierarchy level");
    }
    this.scrollFraction = Math.max(0.0, Math.min(1.0, scrollFraction));
    this.needsRender = true;
    return this.scrollFraction;
  }
}
