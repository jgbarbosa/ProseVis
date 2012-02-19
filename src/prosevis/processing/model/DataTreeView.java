package prosevis.processing.model;

import prosevis.data.DataTree;
import prosevis.data.HierNode;
import prosevis.data.ICon;

public class DataTreeView {
  private DataTree data;
  private double scrollFraction;
  private boolean needsRender = true;
  
  public static final double SCROLL_TOP = 1.0;
  public static final double SCROLL_BOTTOM = 0.0;
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
    int lineNum = -1;
    // 1 - scroll because top of file is 1.0 and bottom is 0.0
    switch (renderType) {
    case CHAPTER:
      lineNum = (int)(data.getNumNodes(ICon.CHAPTER_IND) * (1 - this.scrollFraction));
      return data.findNode(ICon.CHAPTER_IND, lineNum);
    case SECTION:
      lineNum = (int)(data.getNumNodes(ICon.SECTION_IND) * (1 - this.scrollFraction));
      return data.findNode(ICon.SECTION_IND, lineNum);
    case PARAGRAPH:
      lineNum = (int)(data.getNumNodes(ICon.PARAGRAPH_IND) * (1 - this.scrollFraction));
      return data.findNode(ICon.PARAGRAPH_IND, lineNum);
    case SENTENCE:
      lineNum = (int)(data.getNumNodes(ICon.SENTENCE_IND) * (1 - this.scrollFraction));
      return data.findNode(ICon.SENTENCE_IND, lineNum);
    case PHRASE:
      lineNum = (int)(data.getNumNodes(ICon.PHRASE_IND) * (1 - this.scrollFraction));
      return data.findNode(ICon.PHRASE_IND, lineNum);
    default:
      throw new RuntimeException("Can't search for starting line at this hierarchy level");
    }
  }
  
  public synchronized boolean getAndClearNeedsRender() {
    boolean ret = needsRender;
    needsRender = false;
    return ret;
  }
}
