package prosevis.processing.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PFont;
import prosevis.ArgumentHack;
import prosevis.data.TypeMap;
import prosevis.data.Word;
import prosevis.processing.controller.ControllerGUI;
import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.DataTreeView;
import prosevis.processing.model.ScrollInfo;
import prosevis.processing.model.color.ColorSchemeUtil;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.Slider;

public class ProseVisSketch extends PApplet {
  private static final long serialVersionUID = 1L;
  private static final double DScrollInertia = 0.3;
  private static final int kTitleBarHeight = 16;
  private static final int kTitleBarBackground = 200;
  private static final int kMetaTextBackgroundColor = 220;
  protected static final int kScrollWheelSensitivity = 10;
  private static final int kToolTipTextSize = 14;

  private ControlP5 controlP5;
  private final ApplicationModel theModel;
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
  private int lastSliderWidth;
  private int lastViewWidth;
  private final ArrayList<CoordinateWordMap> wordMaps = new ArrayList<CoordinateWordMap>();
  private final ToolTipContext toolTipContext = new ToolTipContext();
  private ControlListener sliderListener = new ControlListener() {
    @Override
    // In theory, receive events from sliders when they get clicked
    // In practice, this method is called frequently and without any user input
    // I suspect it is being called on every frame render
    // Why is this happening?  I cannot divine the cause from the documentation
    // Processing is the worst library I have ever had the misfortune to have
    // used.
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
  };

  public ProseVisSketch() {
    theModel = new ApplicationModel(ArgumentHack.getViewArea().width,
        ArgumentHack.getViewArea().height);
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
    // size call must be first, Processing is possibly the worst library ever
    // written
    if (theModel.isLassoMode()) {
      System.out.println("Entering big screen mode, using OpenGL components");
      size(theModel.getScreenX(), theModel.getScreenY(), OPENGL);
    } else {
      size(theModel.getScreenX(), theModel.getScreenY(), JAVA2D);
    }
    // can't do this in the constructor, again, worst library ever
    controlP5 = new ControlP5(this);

    background(255, 255, 255);
    frameRate(25);
    fill(0, 0, 0);
    setFont(14);
    // Fun fact, ApplicationModel will refuse to add documents until this baby
    // gets called from here. However, if you try and make this call before
    // setFont, there is an internal NullPointerException from, you guessed it,
    // Processing.
    WidthCalculator.setWidthCalculator(new WidthCalculator(this,
        ApplicationModel.kMinFontSz, ApplicationModel.kMaxFontSz));
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
    
    addMouseWheelListener(new MouseWheelListener() { 
      public void mouseWheelMoved(MouseWheelEvent mwe) {
        final int x = mwe.getX();
        final int viewWidth = lastViewWidth;
        int viewIdx = -1;
        for (int i = 0; i < lastViews.length; i++) {
          if (x < (i + 1) * viewWidth && x >= i * viewWidth) {
            viewIdx = i;
            break;
          }
        }
        if (viewIdx < 0) {
          return;
        }
        lastViews[viewIdx].addScrollOffset(-mwe.getUnitsToScroll() * ProseVisSketch.kScrollWheelSensitivity);
    }}); 
  }

  void setFont(int size) {
    if (curFontSize != size) {
      if (!fonts.containsKey(size)) {
        fonts.put(size, createFont(GetFontName(), size));
      }
      textFont(fonts.get(size), size);
      curFontSize = size;
    }
  }

  private static String GetFontName() {
//    for (String s: PFont.list()) {
//      System.out.println(s);
//    }
    return "Courier New";
  }
  
  private int getFontDescent() {
    return (int) (fonts.get(curFontSize).descent() * curFontSize);
  }

  @Override
  public void draw() {
    RenderingInformation renderInfo = theModel.getRenderingData();
    DataTreeView[] views = renderInfo.views;
    TypeMap typeMap = renderInfo.typeMap;
    boolean[] enabledComparisons = renderInfo.enabled;
    final int sliderWidth = lastSliderWidth = renderInfo.sliderWidth;
    final int viewWidth = lastViewWidth = renderInfo.viewWidth;
    final int viewHeight = renderInfo.viewHeight;

    if (!DataTreeView.sameFiles(views, lastViews)) {
      // crap, new data or layout, remove all sliders, reset scroll inertia, possibly add wordMaps
      lastViewScrollIdx = -1;
      lastViews = views;
      
      
      while (wordMaps.size() < lastViews.length) {
        wordMaps.add(new CoordinateWordMap());
      }
      
      refreshSliders(viewHeight);

      if (lastViews.length < 1) {
        fill(255);
        rect(0,0, renderInfo.viewWidth, viewHeight);
      }
      
      for (int i = 0; i < lastViews.length; i++) {
        enabledComparisons = renderInfo.enabled;
        if (!renderInfo.enableSelfSimilarity) {
          enabledComparisons = Arrays.copyOf(renderInfo.enabled, renderInfo.enabled.length);
          int selfIdx = lastViews[i].getSelfIdx();
          if (selfIdx >= 0) {
            enabledComparisons[selfIdx] = false;
          }
        }
        renderView(lastViews[i],
            typeMap, 
            wordMaps.get(i),
            i * viewWidth, 
            0,
            viewWidth - sliderWidth,
            viewHeight,
            enabledComparisons);
      }
    } else {
      updateScrollWithInertia();

      for (int i = 0; i < views.length; i++) {
        if (views[i].getAndClearNeedsRender()) {
          sliders.get(i).setValue((float) views[i].getScroll());
          enabledComparisons = renderInfo.enabled;
          if (!renderInfo.enableSelfSimilarity) {
            enabledComparisons = Arrays.copyOf(renderInfo.enabled, renderInfo.enabled.length);
            enabledComparisons[i] = false;
          }
          renderView(
              views[i], 
              typeMap, 
              wordMaps.get(i),
              i * viewWidth,
              0, 
              viewWidth - sliderWidth, 
              viewHeight, 
              enabledComparisons
              );
        }
      }
    }
    
    // now that we think all the documents are drawn, add in the tooltip
    if (lastViews.length > 0) {      
      addTooltip(typeMap, enabledComparisons);
    }
  }

  private void refreshSliders(final int viewHeight) {
    for (Slider s : sliders) {
      controlP5.remove(s.name());
      s.removeListener(sliderListener);
    }
    sliders.clear();
    for (int i = 0; i < lastViews.length; i++) {
      // add a slider for this slice of the screen
      controlP5.addSlider("slider" + sliders.size(), 0.0f, 1.0f,
          (float) lastViews[sliders.size()].getScroll(), (i + 1) * lastViewWidth
              - lastSliderWidth, 0, lastSliderWidth, viewHeight);
      Slider slider = (Slider) controlP5
          .controller("slider" + sliders.size());
      slider.setLabelVisible(false);
      slider.setId(sliders.size());
      slider.setMoveable(false);
      slider.addListener(sliderListener );

      // because they don't implement listeners, we'll need to keep a
      // reference for ourselves
      sliders.add(slider);
    }
  }

  private void updateScrollWithInertia() {
    long now = System.currentTimeMillis();
    long dT = now - lastUpdate;
    if (this.scrollInertia != 0.0 && this.inertialScrollIdx >= 0
        && inertialScrollIdx < lastViews.length && dT > 0) {
      lastUpdate = now;
      double scroll = lastViews[inertialScrollIdx]
          .addScrollOffset((int) (scrollInertia * dT));
      sliders.get(inertialScrollIdx).setValue((float) scroll);
      if (scrollInertia > 0) {
        scrollInertia = Math.max(0.0, scrollInertia - DScrollInertia);
      } else {
        scrollInertia = Math.min(0.0, scrollInertia + DScrollInertia);
      }
    }
  }
  
  private void addTooltip(TypeMap typeMap, boolean[] enabledComparisons) {
    toolTipContext.recordPosition(mouseX, mouseY);
    if (!toolTipContext.shouldShow()) {
      return;
    }
    int viewIdx = -1;
    for (int i = 0; i < lastViews.length; i++) {
      if (mouseX >= i * lastViewWidth && mouseX <= (i + 1) * lastViewWidth) {
        viewIdx = i;
        break;
      }
    }
    if (viewIdx < 0) {
      // not sure this can happen
      return;
    }
    Word w = wordMaps.get(viewIdx).translate(mouseX, mouseY);
    if (w == null || w.isMetaNode()) {
      // very possibly we're not even hovering over a word
      return;
    }
    final String pos = typeMap.getTypeForIdx(
        TypeMap.kPOSLabelIdx, 
        w.getTypeIdxForLabelIdx(TypeMap.kPOSLabelIdx));
    StringBuilder sound = new StringBuilder(typeMap.getTypeForIdx(
        TypeMap.kPhonemeIdx,
        w.getTypeIdxForLabelIdx(TypeMap.kPhonemeIdx)));
    // only look at the first syllable, which gets 90% of the use cases
    final int maxSimIdx = w.getTypeIdxForLabelIdx(
        TypeMap.kColorByComparisonIdx, 0, enabledComparisons);
    final String maxSim = typeMap.getTypeForIdx(TypeMap.kColorByComparisonIdx, maxSimIdx);
    for (int i = 1; i < w.getSyllableCount(); i++) {
      sound.append("|");
      sound.append(typeMap.getTypeForIdx(TypeMap.kPhonemeIdx, 
          w.getTypeIdxForLabelIdx(TypeMap.kPhonemeIdx, i)));
    }
    
    String [] lines = {
        "Word: " + w.getWord(),
        "POS: " + pos,
        "Sound: " + sound,
        "Most similar to: " + maxSim,
    };
    int boxDx = 0;
    for (int i = 0; i < lines.length; i++) {
      boxDx = Math.max(boxDx, 3 + (int)textWidth(lines[i]));
    }
    fill(kTitleBarBackground);
    int xOrig = mouseX + 20;
    if (xOrig + boxDx > (viewIdx + 1) * lastViewWidth) {
      xOrig = xOrig - boxDx;
    }
    int yOrig = mouseY + 10;
    final int boxDy = lines.length * kToolTipTextSize + 4;
    rect(xOrig, yOrig, boxDx, boxDy);
    fill(0);
    for (int i = 0; i < lines.length; i++) {
      text(lines[i], xOrig + 1, yOrig + (i + 1) * kToolTipTextSize - 1);      
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
        sliders.get(lastViewScrollIdx).setValue((float) newScroll);
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
      lastDy = 0;
      lastDy = 0;
      lastUpdate = mouseEvent.getWhen();
      if (mouseButton == LEFT) {
        if (x >= 0 && x < width && y > 0 && y < height) {
          scrollInertia = 0.0;
          inertialScrollIdx = -1;
          final int viewWidth = lastViewWidth;
          final int sliderWidth = lastSliderWidth;
          for (int i = 0; i < lastViews.length; i++) {
            if (x < (i + 1) * viewWidth - sliderWidth && x >= i * viewWidth) {
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
        scrollInertia = lastDy / (double) (lastDt);

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

  private void renderView(
      DataTreeView dataTreeView,
      TypeMap typeMap,
      CoordinateWordMap wordMap,
      int minX, int minY, int viewWidth, int viewHeight,
      boolean[] enabledComparisons) {
    
    final int yOrigin = minY;
    final int xOrigin = minX;
    final int maxViewWidth = viewWidth;
    final int colorByLabelIdx = typeMap.getLabelIdx(dataTreeView.getColorByLabel());
    final Map<String, Color> colorMap = dataTreeView.getColorMap();
    wordMap.clear();
    fill(255);
    rect(xOrigin, yOrigin, viewWidth, viewHeight);
    // leave room for the title bar, drawn last
    minY += kTitleBarHeight;
    viewHeight -= kTitleBarHeight;
    // indent a touch so that the text looks more natural
    minX += 3;
    viewWidth -= 3;
    fill(0);
    ScrollInfo scrollInfo = dataTreeView.getScrollInfo();
    int lineIdx = scrollInfo.lineIdx;
    int renderedHeight = 0;
    final int lineHeight = dataTreeView.getFontSize(); // hope this works in
                                                       // general
    setFont(lineHeight);
    final int dLine = getFontDescent();
    final float spaceWidth = textWidth(' ');
    // final double charWidth = curFontSize * 0.618033988; // hope this works in
    // general
    // assume all characters are the same width, guesstimate the widths
    minY -= (int) (scrollInfo.lineFrac * lineHeight);
    String renderedText;
    StringBuilder lineBuffer = new StringBuilder();
    float renderedWidth = 0;
    // for instance, if we're rendering words, we'll get the idx for the word
    // field
    final int renderTextByLabelIdx = dataTreeView.getTextBy();
    final StringBuilder tmp = new StringBuilder();
    while (renderedHeight + lineHeight < viewHeight) {
      // we still have space, render another line
      if (lineIdx >= scrollInfo.lines.size()) {
        // we're out of lines, not even one word in this one
        break;
      }
      Word wordNode = scrollInfo.lines.get(lineIdx);
      if (wordNode == null) {
        // we've hit a blank line
        lineIdx++;
        renderedHeight += lineHeight;
        continue;
      }
      renderedWidth = 0;
      while (wordNode != null
          && wordNode.getLineIdx(scrollInfo.breakLinesBy) == lineIdx) {
        if (wordNode.isMetaNode()) {
          renderedText = wordNode.getWord();
        } else if (renderTextByLabelIdx == TypeMap.kNoLabelIdx) {
          renderedText = "     ";
        } else if (renderTextByLabelIdx == TypeMap.kPhonemeIdx) {
          final int phonemeCount = wordNode.getSyllableCount();
          tmp.setLength(0);
          for (int i = 0; i < phonemeCount; i++) {
            tmp.append(typeMap.getTypeForIdx(renderTextByLabelIdx, wordNode
                .getTypeIdxForLabelIdx(renderTextByLabelIdx, i,
                    enabledComparisons)));
            tmp.append(' ');
          }
          renderedText = tmp.toString();
        } else {
          renderedText = typeMap.getTypeForIdx(renderTextByLabelIdx,
              wordNode.getTypeIdxForLabelIdx(renderTextByLabelIdx));
        }
        final float wordWidth = textWidth(renderedText);
        if (wordWidth + renderedWidth >= viewWidth - spaceWidth) {
          break;
        }

        if (lineBuffer.length() > 0 && !wordNode.isPunct()) {
          lineBuffer.append(' ');
          renderedWidth += spaceWidth;
        }

        final int wordTopX = (int) renderedWidth + minX;
        final int wordTopY = renderedHeight + minY + dLine;
        final int wordDx = (int) wordWidth;
        final int wordDy = lineHeight;
        wordMap.put(wordTopX, wordTopY, wordDx, wordDy, wordNode);
        
        if (wordNode.isSearchResult()) {
          fill(255, 0, 0);
          rect(wordTopX, wordTopY, wordDx, wordDy);
        }
        if (wordNode.isMetaNode()) {
          fill(kMetaTextBackgroundColor);
          rect(wordTopX, wordTopY, wordDx, wordDy);
        } else if (colorByLabelIdx != TypeMap.kNoLabelIdx) {
          colorBackground(colorByLabelIdx, colorMap, typeMap, wordNode, wordTopX,
              wordTopY, wordDx, wordDy, enabledComparisons);
        }
        lineBuffer.append(renderedText);
        renderedWidth += wordWidth;
        wordNode = wordNode.next();
      }
      fill(0);
      text(lineBuffer.toString(), minX, renderedHeight + lineHeight + minY);
      lineBuffer.setLength(0);
      lineIdx++;
      renderedHeight += lineHeight;
    }

    // draw in the title bar last so that nothing overlaps with it
    setFont(kTitleBarHeight);
    fill(kTitleBarBackground);
    rect(xOrigin, yOrigin, maxViewWidth, kTitleBarHeight + 2);
    fill(0);
    text(" " + dataTreeView.getData().getName(),
        xOrigin,
        yOrigin + kTitleBarHeight - 1);
  }

  private void colorBackground(int colorByLabelIdx, Map<String, Color> colorMap,
      TypeMap typeMap,
      Word wordNode, int topX, int topY, int dx, int dy,
      boolean[] enabledComparisons) {
    if (wordNode.isPunct()) {
      return;
    }

    switch (colorByLabelIdx) {
    case TypeMap.kStressIdx:
    case TypeMap.kPhonemeIdx:
    case TypeMap.kPhonemeC1Idx:
    case TypeMap.kPhonemeVIdx:
    case TypeMap.kPhonemeC2Idx:
    case TypeMap.kColorByComparisonIdx:
      final int phonemeCount = wordNode.getSyllableCount();
      final double ddx = dx / (double) phonemeCount;
      double nextX = topX + ddx;
      int lastX = topX;
      for (int i = 0; i < phonemeCount; i++) {
        final int syllableTypeIdx = wordNode.getTypeIdxForLabelIdx(
            colorByLabelIdx, i, enabledComparisons);
        if (syllableTypeIdx == TypeMap.kNoTypeIdx) {
          continue;
        }
        final String type = typeMap.getTypeForIdx(colorByLabelIdx, syllableTypeIdx);
        Color c = colorMap.get(type);
        if (c == null) {
          c = colorMap.get(ColorSchemeUtil.kDefaultLabel);
        }
        if (colorByLabelIdx == TypeMap.kColorByComparisonIdx) {
          final float[] hsb = new float[3];
          Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
          final float similarity = wordNode.getComparisonValue(i,
              syllableTypeIdx);
          c = Color.getHSBColor(hsb[0], hsb[1] * similarity, hsb[2]);
        }
        fill(c.getRed(), c.getGreen(), c.getBlue());
        rect(lastX, topY, (int) (nextX - lastX), dy);
        lastX = (int) nextX;
        nextX += ddx;
      }
      break;
    default:
      final int syllableTypeIdx = 
          wordNode.getTypeIdxForLabelIdx(colorByLabelIdx);
      final String type =
          typeMap.getTypeForIdx(colorByLabelIdx, syllableTypeIdx);
      Color c = colorMap.get(type);
      if (c == null) {
        return;
      }
      fill(c.getRed(), c.getGreen(), c.getBlue());
      rect(topX, topY, dx, dy);
      break;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ArgumentHack.pickSizeForDimension(screenDim);

    if (args.length >= 2 && "fullscreen".equals(args[1])) {
      PApplet.main(new String[] { "--present",
          "prosevis.processing.view.ProseVisSketch" });
    } else {
      PApplet.main(new String[] { "prosevis.processing.view.ProseVisSketch" });
    }
  }
}
