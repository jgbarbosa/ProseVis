package prosevis.processing.controller;

import java.awt.Window;
import java.awt.event.WindowEvent;

import prosevis.data.DataTree;

public class FileProgressEvent extends WindowEvent {
  private static final long serialVersionUID = 8864974185787431900L;

  public enum ProgressType {
    PROGRESS,
    FINISHED_SUCC,
    FINISHED_FAIL,
  }

  private ProgressType status;
  private double progress;
  private DataTree result;
  
  public FileProgressEvent(Window src, DataTree tree) {
    super(src, WindowEvent.WINDOW_STATE_CHANGED);
    this.status = (tree != null) ? ProgressType.FINISHED_SUCC : ProgressType.FINISHED_FAIL;
    this.progress = 1.0;
    this.result = tree;
  }

  public FileProgressEvent(Window src, double progressFrac) {
    super(src, WindowEvent.WINDOW_STATE_CHANGED);
    this.status = ProgressType.PROGRESS;
    this.progress = progressFrac;
    this.result = null;
  }
  
  public ProgressType getStatus() {
    return this.status;
  }

  public DataTree getResult() {
    return result;
  }

  public double getProgress() {
    return progress;
  }
}
