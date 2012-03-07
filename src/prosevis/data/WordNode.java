package prosevis.data;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;


public class WordNode extends AbstractWordNode {
	String theWord;

	int wrd;
	POSType pos;
	int accent;
	int tone;
	int soundex;

	ArrayList<Syllable> syllables;

	boolean isPunct;

	boolean displayBreak = false;

	public WordNode(String word, POSType pos, int wordId, int accentId, int toneId, int soundexId, int[] sAttributes, float[] prob) {
	    boolean notPunct = ParsingTools.notPunct(word);
		this.syllables = new ArrayList<Syllable>();
		this.theWord = word;

		if (notPunct) {
			this.wrd = wordId;
			this.pos = pos;
			this.accent = accentId;
			this.tone = toneId;
			this.soundex = soundexId;
			this.syllables.add(new Syllable(sAttributes, prob));
			this.isPunct = false;
		} else {
			this.accent = accentId;
			this.tone = -1;
			this.soundex = -1;
			this.pos = pos;

			this.syllables.add(new Syllable(sAttributes, prob));

			this.isPunct = true;
		}

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

	public void displayDavidData(int index) {
		syllables.get(index).displayDavidData();
	}

	// Get Max Probability among the active files
	public float getDavidDataRelProb(int index, boolean[] actFiles){
		return syllables.get(index).getRelProb(actFiles);
	}

	// Get Max Probability Index among the active files
	public int getDavidDataProbIdx(int index, boolean[] actFiles){
		return syllables.get(index).getMaxProbIdx(actFiles);
	}

	public boolean isPunct(){
		return isPunct;
	}

	public String getWord(){
		return theWord;
	}

	public int getSoundex(){
		return soundex;
	}

	public int getAccent(){
		return accent;
	}

	public POSType getPOS(){
		return pos;
	}

	public int getTone(){
		return tone;
	}

	public int getWrd(){
		return wrd;
	}

	public ArrayList<Syllable> getSyllables(){
		return syllables;
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
}
