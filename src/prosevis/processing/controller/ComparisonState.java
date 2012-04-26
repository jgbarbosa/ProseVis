package prosevis.processing.controller;

import java.awt.Color;

public class ComparisonState {

  private boolean isEnabled;
  private final String name;
  private Color color;

  public ComparisonState(String name, Color c) {
    isEnabled = true;
    this.name = name;
    color = c;
  }

  public synchronized void setColor(Color c) {
    color = c;
  }

  public synchronized String getName() {
    return name;
  }

  public synchronized boolean getEnabled() {
    return isEnabled;
  }

  public synchronized void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public synchronized Color getColor() {
    return this.color;
  }
}
