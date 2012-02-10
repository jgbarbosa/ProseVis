package prosevis.processing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import processing.core.PApplet;
import processing.core.PImage;

public class ProseVisSketch extends PApplet {
  private static final long serialVersionUID = 1L;

  public void getFilePath() {
    // set system look and feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {
      e.printStackTrace(); 
    }


    //Create a file chooser
    final JFileChooser fc = new JFileChooser();

    //In response to a button click:
    int returnVal = fc.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
    } else {
    }
  }
  /**
   * @param args
   */
  public static void main(String[] args) {
    PApplet.main(new String[] { "--present", "ProseVisSketch" });
  }
  
  public void keyReleased() {
  }
  
  public void setup() {
    size(800,800);
    background(255);
    final ProseVisSketch myself = this;
    this.addKeyListener(new KeyListener() {
      @Override
      public void keyPressed(KeyEvent arg0) {
      }
      @Override
      public void keyReleased(KeyEvent arg0) {
        if (arg0.getKeyChar() == 'f') {
          myself.getFilePath();
        }
      }
      @Override
      public void keyTyped(KeyEvent arg0) {
      }
    });
  }

  public void draw() {
    stroke(0);
    if (mousePressed) {
      line(mouseX,mouseY,pmouseX,pmouseY);
    }
   
  }
}
