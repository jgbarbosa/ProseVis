package prosevis.data;


public enum BreakLinesBy {
  Section(0),
  Paragraph(1),
  Sentence(2),
  Phrase(3),
  // these are in the alternate tree hierarchy
  Line(4),
  LineGroup(1);

  private final int idx;
  public final static int kNumIndices = 5;

  private BreakLinesBy(int idIdx) {
    this.idx = idIdx;
    if (idx >= BreakLinesBy.kNumIndices) {
      throw new RuntimeException("Idiot developers shouldn't touch this.");
    }
  }
  public int getIdx() {
    return this.idx;
  }
}