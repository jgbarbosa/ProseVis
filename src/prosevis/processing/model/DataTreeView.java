package prosevis.processing.model;

import prosevis.data.DataTree;

public class DataTreeView {
  private DataTree data;
  private double scrollPercentage;
  
  public static final double SCROLL_TOP = 100.0;
  public static final double SCROLL_BOTTOM = 0.0;
  
  public DataTreeView(DataTree data, double scroll) {
    this.data = data;
    this.scrollPercentage = scroll;
  }

  public DataTreeView(DataTree data) {
    this(data, SCROLL_TOP);
  }
  
  public synchronized DataTree getData() {
    return this.data;
  }
  
  public synchronized double getScroll() {
    return this.scrollPercentage;
  }
  
  public synchronized void setScroll(double scroll) {
    this.scrollPercentage = scroll;
  }
}
