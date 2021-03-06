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

  public static boolean usesLengthBasedLineBreaks(int i) {
    return i != Phrase.idx;
//    return i == Section.idx || i == Paragraph.idx;
  }
  public static boolean insertWhiteSpace(int i) {
    return true;
//    return i == Section.idx || i == Paragraph.idx;
  }
}