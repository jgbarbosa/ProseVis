package prosevis.data;

public enum BreakLinesBy {
  Section(4),
  Paragraph(3),
  Sentence(2),
  Phrase(1),
  // these are in the alternate tree hierarchy
  Line(1);

  private int height;
  private BreakLinesBy(int height) {
    this.height = height;
  }
  public int getHeight() {
    return height;
  }
}