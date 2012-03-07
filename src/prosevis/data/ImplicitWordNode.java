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

  public ImplicitWordNode(int[] sAttributes, float[] prob) {
    syllables.add(new Syllable(sAttributes, prob));
  }

  public void addLabelTypePair(int idx, int typeIdx) {
    this.labels2types.put(idx, typeIdx);
  }

  public int getTypeIdxForLabelIdx(int labelIdx) {
    return labels2types.get(labelIdx);
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
  public void addSyllable(int[] syllableAttr, float[] prob){
    syllables.add(new Syllable(syllableAttr, prob));
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
}
