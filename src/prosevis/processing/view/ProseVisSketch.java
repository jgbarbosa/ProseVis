package prosevis.processing.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PFont;
import prosevis.data.HierNode;
import prosevis.data.ImplicitWordNode;
import prosevis.data.NodeIterator;
import prosevis.data.TypeMap;
import prosevis.processing.controller.ControllerGUI;
import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.ColorView;
import prosevis.processing.model.DataTreeView;
import prosevis.processing.model.ProseModelIF;
import prosevis.processing.model.ScrollInfo;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Slider;

public class ProseVisSketch extends PApplet {
  private static final long serialVersionUID = 1L;
  private static final int VIEW_WIDTH = 1440;
  private static final int VIEW_HEIGHT = 800;
  private static final double SLIDER_FRACTION = 0.01;
  private static final double DScrollInertia = 0.3;

  private ControlP5 controlP5;
  private final ProseModelIF theModel;
  private final ArrayList<Slider> sliders;
  private DataTreeView[] lastViews;
  private final HashMap<Integer, PFont> fonts;
  private int curFontSize;
  private int lastY;
  private int lastViewScrollIdx;
  private long lastUpdate;
  private double scrollInertia;
  private int inertialScrollIdx;
  private long lastDt;
  private int lastDy;

  public ProseVisSketch() {
    theModel = new ApplicationModel();
    sliders = new ArrayList<Slider>();
    lastViews = null;
    fonts = new HashMap<Integer, PFont>();
    curFontSize = -1;
    lastViewScrollIdx = -1;
  }

  @Override
  public void keyReleased() {
  }

  @Override
  public void setup() {
    // size call must be first, Processing is possibly the worst library ever written
    size(VIEW_WIDTH, VIEW_HEIGHT, OPENGL);
    // can't do this in the constructor
    controlP5 = new ControlP5(this);
    background(255, 255, 255);
    frameRate(25);
    fill(0, 0, 0);
    setFont(14);

    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          ControllerGUI window = new ControllerGUI(theModel);
          window.go();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    }

  private void setFont(int size) {
    if (curFontSize != size) {
      if (!fonts.containsKey(size)) {
        fonts.put(size, createFont("Monospaced.plain", size));
      }
      textFont(fonts.get(size), size);
      curFontSize = size;
    }
  }

  private int getFontDescent() {
    return (int)(fonts.get(curFontSize).descent()  * curFontSize);
  }

  @Override
  public void draw() {
    DataTreeView[] views = theModel.getRenderingData();
    ColorView colorView = theModel.getColorView();
    final int viewHeight = VIEW_HEIGHT;
    final int viewWidth = (views.length < 1)
        ? VIEW_WIDTH
        : VIEW_WIDTH / views.length;
    final int sliderWidth = max((int)(viewWidth * SLIDER_FRACTION), 10);

    if (!DataTreeView.sameFiles(views, lastViews)) {
      lastViewScrollIdx = -1;
      background(255, 255, 255);
      // crap, new data or layout, remove all the sliders
      lastViews = views;
      for (Slider s: sliders) {
        controlP5.remove(s.name());
      }
      sliders.clear();
      for (int i = 0; i < views.length; i++) {
        // add a slider for this slice of the screen
        controlP5.addSlider("slider" + sliders.size(), 0.0f, 1.0f, (float)views[sliders.size()].getScroll(),
            (i + 1) * viewWidth - sliderWidth,
            0,
            sliderWidth, viewHeight);
        Slider slider = (Slider)controlP5.controller("slider" + sliders.size());
        slider.setLabelVisible(false);
        slider.setId(sliders.size());
        slider.setMoveable(false);

        // because they don't implement listeners, we'll need to keep a reference for ourselves
        sliders.add(slider);
        renderView(views[i], colorView, i * viewWidth, 0, viewWidth - sliderWidth, viewHeight);
      }
    } else {
      long now = System.currentTimeMillis();
      long dT = now - lastUpdate;
      if (this.scrollInertia != 0.0 && this.inertialScrollIdx >= 0 && inertialScrollIdx < views.length && dT > 0) {
        lastUpdate = now;
        double scroll = views[inertialScrollIdx].addScrollOffset((int)(scrollInertia * dT));
        sliders.get(inertialScrollIdx).setValue((float)scroll);
        if (scrollInertia > 0) {
          scrollInertia = Math.max(0.0, scrollInertia - DScrollInertia);
        } else {
          scrollInertia = Math.min(0.0, scrollInertia + DScrollInertia);
        }

      }
      for (int i = 0 ; i < views.length; i++) {
        if (views[i].getAndClearNeedsRender()) {
          renderView(views[i], colorView, i * viewWidth, 0, viewWidth - sliderWidth, viewHeight);
        }
      }
    }
  }

  @Override
  public void mouseDragged() {
    if (focused) {
      int y = emouseY;
      int dy = emouseY - lastY;
      lastY = y;
      lastDt = mouseEvent.getWhen() - lastUpdate;
      lastDy = dy;
      lastUpdate = mouseEvent.getWhen();
      if (mouseButton == LEFT && lastViewScrollIdx >= 0) {
        scrollInertia = 0.0;
        inertialScrollIdx = -1;
        double newScroll = lastViews[lastViewScrollIdx].addScrollOffset(dy);
        sliders.get(lastViewScrollIdx).setValue((float)newScroll);
      } else if (mouseButton == RIGHT) {
        theModel.updateZoom(lastDy);
      }
    }
  }

  @Override
  public void mousePressed() {
    int x = emouseX;
    int y = emouseY;
    if (focused && lastViews != null && lastViews.length > 0) {
      lastY = y;
      lastUpdate = mouseEvent.getWhen();
      if (mouseButton == LEFT) {
        if (x >= 0 && x < width && y > 0 && y < height) {
          scrollInertia = 0.0;
          inertialScrollIdx = -1;
          int viewWidth = width / lastViews.length;
          for (int i = 0; i < lastViews.length; i++) {
            if (x < (i + 1) * viewWidth && x >= i * viewWidth) {
              lastViewScrollIdx = i;
              break;
            }
          }
        }
      } else if (mouseButton == RIGHT) {
       // nothing to do here, it suffices to have updated the lastY earlier
      }
    }
  }

  @Override
  public void mouseReleased() {
    if (lastViewScrollIdx >= 0) {
      if (lastDt > 0.0 && lastDy != 0.0) {
        // estimate the velocity in pixels per millisecond
        scrollInertia = lastDy / (double)(lastDt);

        inertialScrollIdx = lastViewScrollIdx;
      }
    }
    lastViewScrollIdx = -1;
  }


  @Override
  public void focusLost() {
    lastViewScrollIdx = -1;
    inertialScrollIdx = -1;
    scrollInertia = 0.0;
  }

  private void renderView(DataTreeView dataTreeView, ColorView colorView, int minX, int minY,
      int viewWidth, int viewHeight) {
    fill(255);
    rect(minX, minY, viewWidth, viewHeight);
    fill(0);
    ScrollInfo scrollInfo = dataTreeView.getScrollRenderInfo();
    HierNode lineNode = scrollInfo.lineNode;
    int renderedHeight = 0;
    final int lineHeight = dataTreeView.getFontSize(); // hope this works in general
    setFont(lineHeight);
    final int dLine = getFontDescent();
    final float spaceWidth = textWidth(' ');
    //final double charWidth = curFontSize * 0.618033988; // hope this works in general
    // assume all characters are the same width, guesstimate the widths
    minY -= (int)(scrollInfo.lineFrac * lineHeight);
    String renderedText;
    StringBuilder lineBuffer = new StringBuilder();
    float renderedWidth = 0;
    // for instance, if we're rendering words, we'll get the idx for the word field
    final int renderTextByLabelIdx = dataTreeView.getTextBy();
    final int colorByLabelIdx = dataTreeView.getColorBy();

    while (renderedHeight + lineHeight < viewHeight) {
      // we still have space, render another line
      NodeIterator<ImplicitWordNode> words = new NodeIterator<ImplicitWordNode>(lineNode);
      ImplicitWordNode wordNode = words.next();
      if (wordNode == null) {
        // we're out of lines, not even one word in this one
        break;
      }
      renderedWidth = 0;
      while (wordNode != null) {
        renderedText = colorView.getType(renderTextByLabelIdx, wordNode.getTypeIdxForLabelIdx(renderTextByLabelIdx));
        final float wordWidth = textWidth(renderedText);
        if (colorByLabelIdx != TypeMap.kNoLabelIdx) {
          colorBackground(colorByLabelIdx, colorView, wordNode, (int)renderedWidth + minX, renderedHeight + minY + dLine, (int)wordWidth, lineHeight - dLine);
        }
        if (wordWidth + renderedWidth > viewWidth) {
          // garbage collect the page breaks
          words.clearDisplayBreak();
          break;
        }
        lineBuffer.append(renderedText);
        lineBuffer.append(' ');
        renderedWidth += wordWidth + spaceWidth;
        wordNode = words.next();
      }
      fill(0);
      text(lineBuffer.toString(), minX,  renderedHeight + lineHeight + minY);
      lineBuffer.setLength(0);
      lineNode = (HierNode)lineNode.getNext();
      renderedHeight += lineHeight;
    }
  }

  private void colorBackground(int colorByLabelIdx, ColorView colorView,
      ImplicitWordNode wordNode, int topX, int topY, int dx, int dy) {
    if (wordNode.isPunct()) {
      return;
    }
    switch (colorByLabelIdx) {
    case TypeMap.kStressIdx:
    case TypeMap.kPhonemeIdx:
    case TypeMap.kPhonemeC1Idx:
    case TypeMap.kPhonemeVIdx:
    case TypeMap.kPhonemeC2Idx:
      final int phonemeCount = wordNode.getSyllableCount();
      final double ddx = dx / (double) phonemeCount;
      double nextX = topX + ddx;
      int lastX = topX;
      for (int i = 0; i < phonemeCount; i++) {
        Color c = colorView.getColor(
            colorByLabelIdx, wordNode.getTypeIdxForLabelIdx(colorByLabelIdx, i));
        fill(c.getRed(), c.getGreen(), c.getBlue());
        rect(lastX, topY, (int)(nextX - lastX), dy);
        lastX = (int)nextX;
        nextX += ddx;
      }
      break;
    default:
      Color c = colorView.getColor(
          colorByLabelIdx, wordNode.getTypeIdxForLabelIdx(colorByLabelIdx));
      fill(c.getRed(), c.getGreen(), c.getBlue());
      rect(topX, topY, dx, dy);
      break;
    }
  }

  public void controlEvent(ControlEvent theEvent) {
    int sliderIdx = theEvent.controller().id();
    if (sliderIdx >= sliders.size() || sliderIdx < 0) {
      System.err.println("Got a bad slider index: " + sliderIdx);
      return;
    }
    Slider updated = sliders.get(theEvent.controller().id());
    DataTreeView updateData = lastViews[theEvent.controller().id()];
    updateData.setScroll(updated.value());
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  //  PApplet.main(new String[] { "--present", "prosevis.processing.view.ProseVisSketch" });
    PApplet.main(new String[] {"prosevis.processing.view.ProseVisSketch"});
  }
}