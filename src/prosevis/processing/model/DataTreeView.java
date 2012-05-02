package prosevis.processing.model;

import prosevis.data.BreakLinesBy;
import prosevis.data.Document;
import prosevis.data.TypeMap;
import prosevis.data.Word;
import prosevis.processing.view.GeometryModel;
import prosevis.processing.view.WidthCalculator;

public class DataTreeView {
  private final Document data;
  private double scrollFraction;
  private boolean needsRender = true;
  private int currentFontSize = 14;
  private int colorByLabelIdx = TypeMap.kNoLabelIdx;
  private int textByLabelIdx = TypeMap.kWordIdx;
  private final Searcher searcher = new Searcher();
  private ScrollInfo lastScrollInfo;
  private final LineWrapper lineWrapper;
  public static final double kScrollTop = 1.0;
  public static final double kScrollBottom = 0.0;
  private static final double kScrollMultiplier = 1.0;
  private BreakLinesBy renderType = BreakLinesBy.Phrase;
  private int smoothingWindow;

  public DataTreeView(Document data, int fontSz, GeometryModel geoModel, WidthCalculator wc) {
    this.data = data;
    this.scrollFraction = kScrollTop;
    this.currentFontSize = fontSz;
    lineWrapper = new LineWrapper(data.getFirstWord(), geoModel, wc);
    lineWrapper.setFontSize(currentFontSize);
    // init lastScrollInfo
    getScrollInfo();
  }


  public synchronized void setRenderingBy(BreakLinesBy type) {
    if (!data.hasXml()) {
      if (type == BreakLinesBy.Line) {
        type = BreakLinesBy.Phrase;
      } else if (type == BreakLinesBy.LineGroup) {
        type = BreakLinesBy.Paragraph;
      }
    }
    if (renderType != type) {
      needsRender = true;
      renderType = type;
    }
  }

  public synchronized Document getData() {
    return this.data;
  }

  public synchronized double getScroll() {
    return this.scrollFraction;
  }

  public synchronized void setScroll(double scroll) {
    this.scrollFraction = Math.max(0.0, Math.min(1.0, scroll));
    this.needsRender = true;
  }

  public static boolean sameFiles(
      DataTreeView[] views, DataTreeView[] lastViews) {
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

  public synchronized ScrollInfo getScrollInfo() {
    lastScrollInfo = lineWrapper.getScrollInfo(renderType, 1.0  - scrollFraction);
    return lastScrollInfo;
  }

  public synchronized boolean getAndClearNeedsRender() {
    boolean ret = needsRender;
    needsRender = false;
    return ret;
  }

  public synchronized double addScrollOffset(int dy) {
    this.scrollFraction +=
        (kScrollMultiplier * dy) /
        (lineWrapper.getNumLines(renderType) * currentFontSize);
    this.scrollFraction = Math.max(0.0, Math.min(1.0, scrollFraction));
    this.needsRender = true;
    return this.scrollFraction;
  }

  public synchronized void setSize(int newSize) {
    if (currentFontSize != newSize) {
      this.needsRender = true;
      this.currentFontSize = newSize;
      this.lineWrapper.setFontSize(newSize);
    }
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
    int lineIdx = lastScrollInfo.lineIdx;
    if (lastScrollInfo.lineFrac > 0.5) {
      lineIdx++;
    }
    Word lineStart = lastScrollInfo.lines.get(lineIdx);
    if (lineStart == null) {
      return;
    }

    Word result = searcher.search(
        data.getFirstWord(), lineStart, labelIdx, typeIdx, renderType);
    if (result == null) {
      return;
    }

    // now we know a word with the desired property, adjust the scroll
    final int lineNum = result.getLineIdx(renderType);

    this.scrollFraction = 1.0 -
        (lineNum / (double) lineWrapper.getNumLines(renderType));
    this.needsRender = true;
  }


  public synchronized boolean canRenderByProseLines() {
    return data.hasXml();
  }


  public synchronized void setSmoothingWindow(int smoothingWindow) {
    if (smoothingWindow % 2 == 0) {
      // we center each window around the word in question, so each window much
      // be an odd size
      smoothingWindow++;
    }
    if (smoothingWindow != this.smoothingWindow) {
      this.smoothingWindow = smoothingWindow;
      Word.smoothData(this.smoothingWindow, data.getFirstWord());
      this.needsRender = true;
    }
  }
}
