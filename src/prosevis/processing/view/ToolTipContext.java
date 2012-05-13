package prosevis.processing.view;

public class ToolTipContext {
  private static final long kDefaultMinHoverTimeMillis = 500;

  long minHoverTime;
  long lastTime;
  private int lastX;
  private int lastY;
  
  public ToolTipContext() {
    this.minHoverTime = kDefaultMinHoverTimeMillis;
  }
  
  public ToolTipContext(long minHoverTimeMillis) {
    this.minHoverTime = minHoverTimeMillis;
  }
  
  public void recordPosition(int mouseX, int mouseY) {
    if (mouseX != lastX || mouseY != lastY) {
      lastTime = System.currentTimeMillis();
      lastX = mouseX;
      lastY = mouseY;
    }
  }
  
  public boolean shouldShow() {
    return System.currentTimeMillis() - lastTime > minHoverTime;
  }

}
