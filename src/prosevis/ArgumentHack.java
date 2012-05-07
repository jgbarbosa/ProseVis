package prosevis;

import java.awt.Dimension;

public class ArgumentHack {
  private static Dimension ViewAreaDim;

  public static void pickSizeForDimension(Dimension screenDim) {
    ViewAreaDim = new Dimension(screenDim.width - 50, screenDim.height - 100);    
  }
  
  public static Dimension getViewArea() {
    return ViewAreaDim;
  }

}
