package prosevis.processing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import prosevis.data.BreakLinesBy;
import prosevis.data.Word;
import prosevis.processing.view.GeometryModel;
import prosevis.processing.view.WidthCalculator;

public class LineWrapper {
  private final Map<BreakLinesBy, List<Word>> lineBreaks =
      new EnumMap<BreakLinesBy, List<Word>>(BreakLinesBy.class);
  private int lastViewWidth;
  private int lastFontSz;
  private final Word head;
  private final GeometryModel geom;
  private final WidthCalculator wc;

  public LineWrapper(Word data, GeometryModel geoModel, WidthCalculator wc) {
    this.geom = geoModel;
    this.wc = wc;
    lastFontSz = -1;
    lastViewWidth = -1;
    head = data;
  }

  public void setFontSize(int sz) {
    if (sz != lastFontSz) {
      lineBreaks.clear();
      lastFontSz = sz;
    }
  }

  public int getNumLines(BreakLinesBy renderType) {
    final int viewWidth = geom.getViewTextX();
    if (!isValidFor(renderType, viewWidth)) {
      calculateLineBreaks(viewWidth, wc);
    }

    return lineBreaks.get(renderType).size();
  }

  private boolean isValidFor(BreakLinesBy renderType, int viewWidth) {
    if (lastViewWidth == viewWidth && lineBreaks.containsKey(renderType)) {
      return true;
    }
    return false;
  }

  private void calculateLineBreaks(int viewWidth, WidthCalculator wc) {
    final long[] curIds = new long[BreakLinesBy.kNumIndices];
    final int[] widths = new int[BreakLinesBy.kNumIndices];
    ArrayList<Word> wordsInToken = new ArrayList<Word>();
    final int maxWidth = viewWidth;
    final int spaceWidth = wc.width(" ", lastFontSz);
    // java actually disallows me from doing this correctly, since generics
    // don't exist at runtime and the compiler writers are pedantic about it
    @SuppressWarnings("unchecked")
    final ArrayList<Word> lines[] =
        new ArrayList[BreakLinesBy.kNumIndices];
    for (int i = 0; i < BreakLinesBy.kNumIndices; i++) {
      curIds[i] = -1;
      widths[i] = 0;
      lines[i] = new ArrayList<Word>();
    }

    lineBreaks.clear();

    String lastSpeaker = null;
    String lastScene = null;
    String lastStage = null;
    Word lastWord = head;
    Word curWord = lastWord.next();
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
            (1 + tokenWidth + widths[i] >= maxWidth - spaceWidth &&
            BreakLinesBy.usesLengthBasedLineBreaks(i))) {
          // previous line has ended, and this token starts a new line
          if (curIds[i] != lastWord.getId(i) &&
              BreakLinesBy.usesLengthBasedLineBreaks(i) && lines[i].size() > 0) {
            // add a blank line if we're looking at divisions that are widely
            // spaced enough to benefit from the additional whitespace
            lines[i].add(null);
          }
          if (i == BreakLinesBy.Line.getIdx()) {
            if (lastWord.getShakespeareAct() != null &&
                  !lastWord.getShakespeareAct().equals(lastScene)) {
              lines[i].add(new Word(lastWord.getShakespeareAct()));
              lines[i].get(lines[i].size() - 1).setLineNum(i, lines[i].size() - 1);
              lastScene = lastWord.getShakespeareAct();
            }
            if (lastWord.getShakespeareStage() != null &&
                !lastWord.getShakespeareStage().equals(lastStage)) {
              lines[i].add(new Word(lastWord.getShakespeareStage()));
              lines[i].get(lines[i].size() - 1).setLineNum(i, lines[i].size() - 1);
              lastStage = lastWord.getShakespeareStage();
            }
            if (lastWord.getShakespeareSpeaker() != null &&
                !lastWord.getShakespeareSpeaker().equals(lastSpeaker)) {
              lines[i].add(new Word(lastWord.getShakespeareSpeaker()));
              lines[i].get(lines[i].size() - 1).setLineNum(i, lines[i].size() - 1);
              lastSpeaker = lastWord.getShakespeareSpeaker();
            }
          }
          curIds[i] = wordsInToken.get(0).getId(i);
          widths[i] = tokenWidth;
          lines[i].add(wordsInToken.get(0));
        } else {
          widths[i] += spaceWidth + tokenWidth;
        }

        for (Word w: wordsInToken) {
          w.setLineNum(i, lines[i].size() - 1);
        }
      }

      token.setLength(0);
      wordsInToken.clear();
    }
    for (BreakLinesBy k: BreakLinesBy.values()) {
      lineBreaks.put(k, lines[k.getIdx()]);
    }
    lastViewWidth = viewWidth;
  }

  public ScrollInfo getScrollInfo(BreakLinesBy renderType, double scrollFraction) {
    final int viewWidth = geom.getViewTextX();
    if (!isValidFor(renderType, viewWidth)) {
      calculateLineBreaks(viewWidth, wc);
    }
    List<Word> lines =
        Collections.unmodifiableList (lineBreaks.get(renderType));
    final int numLines = lines.size();
    final double fracLines = numLines * scrollFraction;
    final int lineNum = Math.min((int)fracLines, numLines - 1);
    final double lineFrac = fracLines - lineNum;
    return new ScrollInfo(lines, lineNum, lineFrac, renderType);
  }

}
