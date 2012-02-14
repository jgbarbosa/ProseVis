package prosevis.processing;

import java.awt.EventQueue;

import processing.core.PApplet;
import processing.core.PFont;

public class ProseVisSketch extends PApplet {
  private static final long serialVersionUID = 1L;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          ControllerGUI window = new ControllerGUI();
          window.go();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

//    PApplet.main(new String[] { "--present", "prosevis.processing.ProseVisSketch" });
    PApplet.main(new String[] {"prosevis.processing.ProseVisSketch" });
  }
  
  public void keyReleased() {
  }
  
  public void setup() {
//    size(800,800);
    size(1440, 900);
    PFont f = loadFont("Monospaced.plain-12.vlw");
    textFont(f);
    fill(255, 0, 0);
    // 5 fps
    frameRate(30);
    background(255);
    text("Hello Strings!",10,100);
  }

  public void draw() {
    stroke(0);
    if (mousePressed) {
      line(mouseX,mouseY,pmouseX,pmouseY);
    }
   
  }
}
