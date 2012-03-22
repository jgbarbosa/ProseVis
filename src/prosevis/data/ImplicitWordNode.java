package prosevis.data;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImplicitWordNode extends AbstractWordNode {

  private final Map<Integer, Integer> labels2types = new HashMap<Integer, Integer>();
  private boolean displayBreak = false;
  private final ArrayList<Syllable> syllables = new ArrayList<Syllable>();
  private final boolean isPunctuation;
  private boolean isSearchResult = false;
  public ImplicitWordNode(ProseNode parent, String word, Syllable s) {

    super(parent);
    syllables.add(s);
    isPunctuation = !ParsingTools.notPunct(word);
  }

  public void addLabelTypePair(int idx, int typeIdx) {
    this.labels2types.put(idx, typeIdx);
  }

  public int getTypeIdxForLabelIdx(int labelIdx) {
    return labels2types.get(labelIdx);
  }

  public int getTypeIdxForLabelIdx(int labelIdx, int syllableIdx) {
    for (int labelType : TypeMap.kSyllableTypes) {
      if (labelType == labelIdx) {
        return syllables.get(syllableIdx).getTypeIdxForLabelIdx(labelIdx);
      }
    }
    return getTypeIdxForLabelIdx(labelIdx);
  }

  /* Display breaks are used by the iterator to tag line breaks */
  @Override
  public boolean getDisplayBreak(){
      return displayBreak;
  }

  @Override
  public void setDisplayBreak(boolean displayBreak){
      this.displayBreak = displayBreak;
  }

  public void addSyllable(Syllable s){
    syllables.add(s);
  }

  public int getSyllableCount(){
      return syllables.size();
  }

  public double getPhonemeWidth(FontRenderContext frc, Font font){
      String text = "";

      for(int i = 0; i < syllables.size(); i++){
          text += syllables.get(i).getPhoneme() + " ";
      }

      TextLayout layoutData = new TextLayout(text, font, frc);

      return layoutData.getAdvance();
  }

  public boolean isPunct() {
    return isPunctuation;
  }

  @Override
  public ProseNode getFirstChild() {
    return null;
  }

  public void setIsSearchResult(boolean b) {
    this.isSearchResult  = b;
  }

}
