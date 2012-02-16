package prosevis.processing;

import java.awt.EventQueue;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.DataTreeView;
import prosevis.processing.model.ProseModelIF;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.Slider;
import controlP5.Textfield;

public class ProseVisSketch extends PApplet {
  private static final long serialVersionUID = 1L;
  private static final int VIEW_WIDTH = 1440;
  private static final int VIEW_HEIGHT = 900;
  private static final double SLIDER_FRACTION = 0.02;
  
  private ControlP5 controlP5;// = new ControlP5(this);
  private ProseModelIF theModel = new ApplicationModel();
  private ArrayList<Slider> sliders = new ArrayList<Slider>();
  private int lastNumRows = 1;
  private int lastNumViews = 0;

  public void keyReleased() {
  }
  
  public void setup() {
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

    size(VIEW_WIDTH, VIEW_HEIGHT);
    background(255, 255, 255);
    frameRate(25);
    PFont f = loadFont("Monospaced.plain-12.vlw");
    textFont(f);
    fill(0, 0, 0);
    controlP5 = new ControlP5(this);
    
    fill(0, 0, 0);
    
    //controlP5.addNumberbox("numberboxA",rectColor,100,140,100,14).setId(1);
    //controlP5.addNumberbox("numberboxB",backColor,100,180,100,14).setId(2);
    //controlP5.addNumberbox("numberboxC",0,100,220,100,14).setId(3);
    
    //controlP5.addSlider("sliderA",100,200,100,100,260,100,14).setId(4);
    //controlP5.addSlider("sliderMine", 0.0f, 100.0f, 200, 200, 10, 200).setLabelVisible(false);
    //controlP5.addTextfield("textA",100,290,100,20).setId(5);
    
    //controlP5.controller("numberboxA").setMax(255);
    //controlP5.controller("numberboxA").setMin(0);
//  text("Hello Strings!",10,100);

  }

  public void draw() {
    background(255, 255, 255);
    int numRows = theModel.getNumberRows();
    DataTreeView[] views = theModel.getRenderingData();
    if (numRows != lastNumRows || views.length != lastNumViews) {
      System.out.println("adding sliders");
      // crap, new data or layout, remove all the sliders
      lastNumRows = numRows;
      lastNumViews = views.length;
      final int viewHeight = VIEW_HEIGHT / numRows;
      final int viewWidth = (views.length < numRows)
          ? VIEW_WIDTH 
          : VIEW_WIDTH / ((numRows - 1 + views.length) / numRows);
      final int sliderWidth = max((int)(viewWidth * SLIDER_FRACTION), 10);
      for (Slider s: sliders) {
        controlP5.remove(s.name());
      }
      sliders.clear();
      int column = 0;
      int row = 0;
      while (sliders.size() < views.length) {
        // add a slider for this slice of the screen
        Slider slider = new Slider(controlP5, 
            (ControllerGroup)controlP5.controlWindow.tabs().get(1), 
            "slider" + sliders.size(),
            0.0f, 100.0f, (float)views[sliders.size()].getScroll(),
            (column + 1) * viewWidth - sliderWidth,
            row * viewHeight,
            sliderWidth, viewHeight);
        slider.setLabelVisible(false);
        slider.setId(sliders.size() + 1);
        slider.setMoveable(false);

        controlP5.register(slider);
        sliders.add(slider);
        row++;
        if (row % numRows == 0) {
          column++;
          row = 0;
        }
      }
    }
  }
    
  // a slider event will change the value of textfield textA
  public void sliderA(int theValue) {
    ((Textfield)controlP5.controller("textA")).setValue(""+theValue);
  }
  
  public void controlEvent(ControlEvent theEvent) {
    println("got a control event from controller with id "+theEvent.controller().id());
    switch(theEvent.controller().id()) {
      case 1: // numberboxA
      break;
      case 2:  // numberboxB
      break;  
    }
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
//    PApplet.main(new String[] { "--present", "prosevis.processing.ProseVisSketch" });
    PApplet.main(new String[] {"prosevis.processing.ProseVisSketch" });
  }
}
