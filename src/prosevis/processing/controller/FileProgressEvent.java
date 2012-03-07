package prosevis.processing.controller;

import java.awt.Window;
import java.awt.event.WindowEvent;

import prosevis.data.DataTree;
import prosevis.data.TypeMap;

public class FileProgressEvent extends WindowEvent {
  private static final long serialVersionUID = 8864974185787431900L;

  public enum ProgressType {
    PROGRESS,
    FINISHED_SUCC,
    FINISHED_FAIL,
  }

  private final ProgressType status;
  private final double progress;
  private final DataTree result;
  private final TypeMap resultingTypeMap;

  public FileProgressEvent(Window src, DataTree tree, TypeMap map) {
    super(src, WindowEvent.WINDOW_STATE_CHANGED);
    this.status = (tree != null) ? ProgressType.FINISHED_SUCC : ProgressType.FINISHED_FAIL;
    this.progress = 1.0;
    this.result = tree;
    this.resultingTypeMap = map;
  }

  public FileProgressEvent(Window src, double progressFrac) {
    super(src, WindowEvent.WINDOW_STATE_CHANGED);
    this.status = ProgressType.PROGRESS;
    this.progress = progressFrac;
    this.result = null;
    this.resultingTypeMap = null;
  }

  public ProgressType getStatus() {
    return this.status;
  }

  public DataTree getResult() {
    return result;
  }

  public TypeMap getResultingTypeMap() {
    return resultingTypeMap;
  }

  public double getProgress() {
    return progress;
  }
}
