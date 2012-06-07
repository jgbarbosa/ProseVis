package prosevis.processing.model;

public class ToolTipFields {
  private boolean simiEnabled = true;
  private boolean soundEnabled = true;
  private boolean posEnabled = true;
  private boolean wordEnabled = true;

  public synchronized void setTooltipFieldsEnabled(boolean wordEnabled, boolean posEnabled,
      boolean soundEnabled, boolean simiEnabled) {
    this.wordEnabled = wordEnabled;
    this.posEnabled = posEnabled;
    this.soundEnabled = soundEnabled;
    this.simiEnabled = simiEnabled;
  }
  
  public synchronized boolean isWordEnabled() {
    return wordEnabled;
  }
  public synchronized boolean isPOSEnabled() {
    return posEnabled;
  }
  public synchronized boolean isSoundEnabled() {
    return soundEnabled;
  }
  public synchronized boolean isSimilarityEnabled() {
    return simiEnabled;
  }
}
