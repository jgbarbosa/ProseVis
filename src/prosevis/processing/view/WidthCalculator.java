package prosevis.processing.view;


// This class assumes that we're using a fixed width font to render strings
// if this were not the case, it would be necessary to think carefully about
// threading accesses to the sketch for per string text widths
public class WidthCalculator {
  // only get at this through the static getter
  private static WidthCalculator wcSingleton;


  private final int minSz;
  private final int charWidths[];
  public WidthCalculator(ProseVisSketch sketch, int minSz, int maxSz) {
    this.minSz = minSz;
    this.charWidths = new int[maxSz - minSz + 1];
    for (int i = minSz; i <= maxSz; i++) {
      charWidths[i - minSz] = (int)(sketch.textWidth('W') + 0.5f);
    }
  }

  // returns the width of the given String-like object in pixels at size fontSz
  public int width(CharSequence str, int fontSz) {
    return str.length() * charWidths[fontSz - minSz];
  }

  public synchronized static WidthCalculator getWidthCalculator() {
    return wcSingleton;
  }

  // It is unfortunate that I need the ProseVisSketch to finish initializing
  // before I can make this object, which has led me to this difficult place
  public synchronized static void setWidthCalculator(WidthCalculator wc) {
    wcSingleton = wc;
  }

  public int getTabWidth(int fontSz) {
    return 5 * charWidths[fontSz - minSz];
  }
}
