package prosevis.processing.model;

import java.util.List;

import prosevis.data.BreakLinesBy;
import prosevis.data.DocWord;

public class ScrollInfo {
  public final List<DocWord> lines;
  // [0, 1.0] of a line height to adjust the window start by
  public final double lineFrac;
  public final int lineIdx;
  public final BreakLinesBy breakLinesBy;

  public ScrollInfo(List<DocWord> lines, int lineNum, double lineFrac, BreakLinesBy renderType) {
    this.lines = lines;
    this.lineIdx = lineNum;
    this.lineFrac = lineFrac;
    this.breakLinesBy = renderType;
  }
}
