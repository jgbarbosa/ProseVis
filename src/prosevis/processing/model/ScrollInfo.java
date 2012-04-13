package prosevis.processing.model;

import java.util.List;

import prosevis.data.BreakLinesBy;
import prosevis.data.DocWord;

public class ScrollInfo {
  public final List<DocWord> lines;
  public final List<Boolean> firstLines;
  // [0, 1.0] of a line height to adjust the window start by
  public final double lineFrac;
  public final int lineIdx;
  public final BreakLinesBy breakLinesBy;

  public ScrollInfo(List<DocWord> lines, List<Boolean> firsts, int lineNum, double lineFrac, BreakLinesBy renderType) {
    this.lines = lines;
    this.firstLines = firsts;
    this.lineIdx = lineNum;
    this.lineFrac = lineFrac;
    this.breakLinesBy = renderType;
  }
}
