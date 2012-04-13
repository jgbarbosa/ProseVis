package prosevis.processing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import prosevis.data.BreakLinesBy;
import prosevis.data.DocWord;
import prosevis.processing.view.GeometryModel;
import prosevis.processing.view.WidthCalculator;

public class LineWrapper {
  private final Map<BreakLinesBy, List<DocWord>> lineBreaks =
      new EnumMap<BreakLinesBy, List<DocWord>>(BreakLinesBy.class);
  private final Map<BreakLinesBy, List<Boolean>> firstLines =
      new EnumMap<BreakLinesBy, List<Boolean>>(BreakLinesBy.class);
  private final int lastViewWidth;
  private int lastFontSz;
  private final DocWord head;
  private final GeometryModel geom;
  private final WidthCalculator wc;

  public LineWrapper(DocWord data, GeometryModel geoModel, WidthCalculator wc) {
    this.geom = geoModel;
    this.wc = wc;
    lastFontSz = -1;
    lastViewWidth = -1;
    head = data;
  }

  public void setFontSize(int sz) {
    if (sz != lastFontSz) {
      lineBreaks.clear();
      firstLines.clear();
      lastFontSz = sz;
    }
  }

  public int getNumLines(BreakLinesBy renderType) {
    final int viewWidth = geom.getViewTextX();
    if (!isValidFor(renderType, viewWidth)) {
      calculateLineBreaks(renderType, viewWidth, wc);
    }

    return lineBreaks.get(renderType).size();
  }


  public List<DocWord> getLines(BreakLinesBy renderType) {
    final int viewWidth = geom.getViewTextX();
    if (!isValidFor(renderType, viewWidth)) {
      calculateLineBreaks(renderType, viewWidth, wc);
    }
    return Collections.unmodifiableList (lineBreaks.get(renderType));
  }

  private boolean isValidFor(
      BreakLinesBy renderType, int viewWidth) {
    if (lastViewWidth == viewWidth && lineBreaks.containsKey(renderType)) {
      return true;
    }
    return false;
  }

  private void calculateLineBreaks(
      BreakLinesBy renderType, int viewWidth, WidthCalculator wc) {
    final long[] curIds = new long[BreakLinesBy.kNumIndices];
    final int[] widths = new int[BreakLinesBy.kNumIndices];
    ArrayList<DocWord> wordsInToken = new ArrayList<DocWord>();
    final int maxWidth = geom.getViewTextX();
    final int tabWidth = wc.getTabWidth(lastFontSz);
    final int spaceWidth = wc.width(" ", lastFontSz);
    // java actually disallows me from doing this correctly, since generics
    // don't exist at runtime
    @SuppressWarnings("unchecked")
    final ArrayList<DocWord> lines[] =
        new ArrayList[BreakLinesBy.kNumIndices];
    @SuppressWarnings("unchecked")
    final ArrayList<Boolean> firsts[] =
        new ArrayList[BreakLinesBy.kNumIndices];
    for (int i = 0; i < BreakLinesBy.kNumIndices; i++) {
      curIds[i] = -1;
      widths[i] = 0;
      lines[i] = new ArrayList<DocWord>();
      firsts[i] = new ArrayList<Boolean>();
    }

    lineBreaks.clear();
    firstLines.clear();
    DocWord lastWord = head;
    DocWord curWord = lastWord.next();
    StringBuilder token = new StringBuilder();
    token.append(lastWord.word());
    wordsInToken.add(lastWord);
    boolean tokenHasWord = !lastWord.isPunct();

    while (curWord != null) {
      // build up a token, which may be several words (ie. "Hello,)
      if (wordsInToken.size() == 0 ||
          (curWord != null &&
          curWord.idsMatch(lastWord) &&
          (!tokenHasWord || (curWord.isPunct() && !curWord.isOpenQuote())))) {
        wordsInToken.add(curWord);
        token.append(curWord.word());
        lastWord = curWord;
        curWord = curWord.next();
        continue;
      }

      final int tokenWidth = wc.width(token, lastFontSz);
      for (int i = 0; i < BreakLinesBy.kNumIndices; i++) {
        if (curIds[i] != lastWord.getId(i) ||
            1 + tokenWidth + widths[i] >= maxWidth) {
          // previous line has ended, and this token starts a new line
          if (curIds[i] != lastWord.getId(i)) {
            firsts[i].add(true);
            widths[i] = tabWidth + tokenWidth;
            curIds[i] = wordsInToken.get(0).getId(i);
          } else {
            firsts[i].add(false);
            widths[i] = tokenWidth;
          }
          lines[i].add(wordsInToken.get(0));
        } else {
          widths[i] += spaceWidth + tokenWidth;
        }

        for (DocWord w: wordsInToken) {
          w.setLineNum(i, lines[i].size() - 1);
        }
      }

      token.setLength(0);
      wordsInToken.clear();
    }
    for (BreakLinesBy k: BreakLinesBy.values()) {
      lineBreaks.put(k, lines[k.getIdx()]);
      firstLines.put(k, firsts[k.getIdx()]);
    }
  }

  public ScrollInfo getScrollInfo(BreakLinesBy renderType, double scrollFraction) {
    final int viewWidth = geom.getViewTextX();
    if (!isValidFor(renderType, viewWidth)) {
      calculateLineBreaks(renderType, viewWidth, wc);
    }
    List<DocWord> lines =
        Collections.unmodifiableList (lineBreaks.get(renderType));
    List<Boolean> firsts =
        Collections.unmodifiableList (firstLines.get(renderType));
    final int numLines = lines.size();
    final double fracLines = numLines * scrollFraction;
    final int lineNum = Math.min((int)fracLines, numLines - 1);
    final double lineFrac = fracLines - lineNum;
    return new ScrollInfo(lines, firsts, lineNum, lineFrac, renderType);
  }

}
