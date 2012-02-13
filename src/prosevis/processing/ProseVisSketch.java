package prosevis.processing;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import processing.core.PApplet;
import fullscreen.FullScreen;

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

    PApplet.main(new String[] { "--present", "prosevis.processing.ProseVisSketch" });
  }
  
  public void keyReleased() {
  }
  
  public void setup() {
//    size(800,800);
    size(1440, 900);
    
    // 5 fps
    frameRate(30);
    background(255);
  }

  public void draw() {
    stroke(0);
    if (mousePressed) {
      line(mouseX,mouseY,pmouseX,pmouseY);
    }
   
  }
}
