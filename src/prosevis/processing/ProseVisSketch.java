package prosevis.processing;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PFont;
import prosevis.data.HierNode;
import prosevis.data.NodeIterator;
import prosevis.data.WordNode;
import prosevis.processing.controller.ControllerGUI;
import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.DataTreeView;
import prosevis.processing.model.ProseModelIF;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Slider;

public class ProseVisSketch extends PApplet {
  private static final long serialVersionUID = 1L;
  private static final int VIEW_WIDTH = 1440;
  private static final int VIEW_HEIGHT = 900;
  private static final double SLIDER_FRACTION = 0.01;
  private static final double DScrollInertia = 0.1;
  
  private ControlP5 controlP5;
  private final ProseModelIF theModel;
  private final ArrayList<Slider> sliders;
  private DataTreeView[] lastViews;
  private final HashMap<Integer, PFont> fonts;
  private int curFontSize;
  private int lastY;
  private int lastX;
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
    curFontSize = 14;
    lastViewScrollIdx = -1;
  }

  public void keyReleased() {
  }
  
  public void setup() {
    // size call must be first, Processing is possibly the worst library ever written
    size(VIEW_WIDTH, VIEW_HEIGHT, OPENGL);
    // can't do this in the constructor
    controlP5 = new ControlP5(this);
    background(255, 255, 255);
    frameRate(25);
    fill(0, 0, 0);
    fonts.put(curFontSize, createFont("Monospaced.plain", curFontSize));
    textFont(fonts.get(14), 14); 

    EventQueue.invokeLater(new Runnable() {
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
  
  public void draw() {
    DataTreeView[] views = theModel.getRenderingData();
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
        renderView(views[i], i * viewWidth, 0, viewWidth - sliderWidth, viewHeight);
      }
    } else {
      if (this.scrollInertia != 0.0 && this.inertialScrollIdx >= 0 && inertialScrollIdx < views.length) {
        long now = System.currentTimeMillis();
        long dT = now - lastUpdate;
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
          renderView(views[i], i * viewWidth, 0, viewWidth - sliderWidth, viewHeight);
        }
      }
    }
  }
  
  @Override
  public void mouseDragged() {
    if (mouseButton == LEFT && focused && lastViewScrollIdx >= 0) {
      int x = emouseX;
      int y = emouseY;
      int dy = emouseY - lastY;
      lastX = x;
      lastY = y;
      lastDt = mouseEvent.getWhen() - lastUpdate;
      lastDy = dy;
      lastUpdate = mouseEvent.getWhen();
      scrollInertia = 0.0;
      inertialScrollIdx = -1;
      double newScroll = lastViews[lastViewScrollIdx].addScrollOffset(dy);
      sliders.get(lastViewScrollIdx).setValue((float)newScroll);
    }
  }
  
  @Override
  public void mousePressed() {
    if (mouseButton == LEFT && focused && lastViews != null && lastViews.length > 0) {
      int x = emouseX;
      int y = emouseY;
      if (x >= 0 && x < width && y > 0 && y < height) {
        lastX = x;
        lastY = y;
        lastUpdate = mouseEvent.getWhen();
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
    }
  }

  @Override
  public void mouseReleased() {
    if (lastViewScrollIdx >= 0) {
      int dy = emouseY - lastY;
      if (dy != 0) {
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
  
  private void renderView(DataTreeView dataTreeView, int minX, int minY,
      int viewWidth, int viewHeight) {
    fill(255);
    rect(minX, minY, viewWidth, viewHeight);
    fill(0);
    HierNode lineNode = dataTreeView.getStartingLine();
    int renderedHeight = 0;
    final int lineHeight = curFontSize; // hope this works in general
    final int charWidth = curFontSize / 2 + 1; // hope this works in general
    int renderedWidth;
    String word;

    while (renderedHeight + lineHeight < viewHeight) {
      // we still have space, render another line
      NodeIterator words = new NodeIterator(lineNode);
      WordNode wordNode = words.next();
      if (wordNode == null) {
        // we're out of lines, not even one word in this one
        break;
      }
      renderedWidth = 0;
      while (wordNode != null) {
        word = wordNode.getWord();
        if (renderedWidth + word.length() * charWidth > viewWidth) {
          // garbage collect the page breaks
          words.clearDisplayBreak();
          break;
        }
        text(word, renderedWidth + minX,  renderedHeight + lineHeight + minY);
        renderedWidth += (word.length() + 1) * charWidth;     
        wordNode = (WordNode)words.next();
      }
      
      lineNode = (HierNode)lineNode.getNext();
      renderedHeight += lineHeight;
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
//    PApplet.main(new String[] { "--present", "prosevis.processing.ProseVisSketch" });
    PApplet.main(new String[] {"prosevis.processing.ProseVisSketch"});
  }
}
