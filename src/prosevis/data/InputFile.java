package prosevis.data;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class InputFile {
  private File file;
  public HierNode head;

  public ArrayList<HierNode> firstElements; // First node at each level
  public ArrayList<HierNode> currElements; // Current node at each level as file
                                           // is read
  public WordNode currWord = null; // Current word as file is read
  public int[] currIndices; // Current index at each level as file is read
  public int[] nodeCount; // Number of nodes at each level
  public int[] maxWords; // Node with the max number of words at each level
  public int[] maxPhonemes; // Node with the max number of phonemes at each
                            // level
  public double[] maxWordWidth; // Node with the max text width at each level
  public double[] maxPhonemeWidth; // Node with the max phoneme width at each
                                   // level
  public double[] maxPOSWidth; // Node with the max phoneme width at each level

  public ArrayList<String> wordCode;
  public ArrayList<String> posCode;
  public ArrayList<String> accentCode;
  public ArrayList<String> toneCode;
  public ArrayList<String> soundexCode;

  public ArrayList<String> phonemeCode;
  public ArrayList<String> phoC1Code;
  public ArrayList<String> phoVCode;
  public ArrayList<String> phoC2Code;

  public int lineCount = 0;

  public Graphics2D g;
  public Font gFont;

  public boolean hasComparisonData = false;

  public InputFile(File nFile, Graphics2D nG) {
    wordCode = new ArrayList<String>();
    posCode = new ArrayList<String>();
    accentCode = new ArrayList<String>();
    toneCode = new ArrayList<String>();
    soundexCode = new ArrayList<String>();

    phonemeCode = new ArrayList<String>();
    phoC1Code = new ArrayList<String>();
    phoVCode = new ArrayList<String>();
    phoC2Code = new ArrayList<String>();

    initAndRead(nFile, nG);
  }

  public InputFile(InputFile template, File nFile, Graphics2D nG) {
    wordCode = template.wordCode;
    posCode = template.posCode;
    accentCode = template.accentCode;
    toneCode = template.toneCode;
    soundexCode = template.soundexCode;

    phonemeCode = template.phonemeCode;
    phoC1Code = template.phoC1Code;
    phoVCode = template.phoVCode;
    phoC2Code = template.phoC2Code;

    initAndRead(nFile, nG);
  }

  public void initAndRead(File nFile, Graphics2D nG) {
    currElements = new ArrayList<HierNode>();
    firstElements = new ArrayList<HierNode>();

    currIndices = new int[ICon.MAX_DEPTH];
    maxWords = new int[ICon.MAX_DEPTH];
    maxPhonemes = new int[ICon.MAX_DEPTH];
    maxWordWidth = new double[ICon.MAX_DEPTH];
    maxPhonemeWidth = new double[ICon.MAX_DEPTH];
    maxPOSWidth = new double[ICon.MAX_DEPTH];
    nodeCount = new int[ICon.MAX_DEPTH];

    file = nFile;
    g = nG;
    gFont = new Font("Helvetica", Font.PLAIN, 12);

    BufferedReader inputStream = null;

    try {
      // Read first line of file and check for obvious errors
      inputStream = new BufferedReader(new FileReader(file));
      String currLine = inputStream.readLine();
      String[] columns = currLine.split("\t");

      if (columns.length < ICon.TOTAL_COL) {
        String message = "Incompatible Input file format, aborting Open";
        JOptionPane.showMessageDialog(new JFrame(), message, "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Check if file has Comparison Data
      if (columns.length == ICon.MAX_COLS) {
        hasComparisonData = true;
      }

      /*
       * This is needed to initialize the hier nodes to start indexing from
       * whatever the first indices are in the input file. For instance, the
       * para numbering could start from 16 instead of 1. So instead of
       * initializing currIndices[PARAGRAPH_IND] = 1, init to 16 here.
       */

      currLine = inputStream.readLine();

      // Initialize tree nodes at each level and prepare for input
      if (currLine != null)
        initializeTree(currLine);

      // Read each line of data
      do {
        columns = currLine.split("\t");
        if (columns.length < ICon.TOTAL_COL)
          continue;
        processInputLine(columns);
        lineCount++;
      } while ((currLine = inputStream.readLine()) != null);

      finalizeMaximums();

    } catch (IOException e) {
      System.err.println("IOException: " + e.getMessage());
      String message = "Error when processing the input file";
      JOptionPane.showMessageDialog(new JFrame(), message, "Error",
          JOptionPane.ERROR_MESSAGE);
    } catch (NumberFormatException e) {
      String errMsg = "Found badly formatted number when parsing input file.";
      System.err.println(errMsg);
      System.err.println(e);
      JOptionPane.showMessageDialog(new JFrame(), errMsg, "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public String getName() {
    return file.getName();
  }

  public void finalizeMaximums() {

    for (int i = 0; i < ICon.MAX_DEPTH; i++) {
      if (currElements.get(i).getWordCount() > maxWords[i]) {
        maxWords[i] = currElements.get(i).getWordCount();
      }

      if (currElements.get(i).getPhonemeCount() > maxPhonemes[i]) {
        maxPhonemes[i] = currElements.get(i).getPhonemeCount();
      }

      if (currElements.get(i).getTextWidth() > maxWordWidth[i]) {
        maxWordWidth[i] = currElements.get(i).getTextWidth();
      }

      if (currElements.get(i).getPhonemeWidth() > maxPhonemeWidth[i]) {
        maxPhonemeWidth[i] = currElements.get(i).getPhonemeWidth();
      }

      if (currElements.get(i).getPOSWidth() > maxPOSWidth[i]) {
        maxPOSWidth[i] = currElements.get(i).getPOSWidth();
      }
    }
  }

  public void processInputLine(String[] line) {
    // Trim each field
    for (int i = 0; i < line.length; i++) {
      line[i] = line[i].trim();

    }

    // Loop through each depth column
    for (int i = 0; i < ICon.MAX_DEPTH; i++) {
      int currIndex;
      if (line[i].equals("NULL") || line[i].equals("null"))
        currIndex = 1;
      else
        currIndex = Integer.parseInt(line[i]);

      // Check for a change at each depth
      if (currIndex != currIndices[i]) {

        // Make structural changes at the depth that changed
        newNode(i, true);

        // Update depth information to detect future changes
        for (int j = i; j < ICon.MAX_DEPTH; j++) {
          if (line[j].equals("NULL"))
            currIndices[j] = 1;
          else
            currIndices[j] = Integer.parseInt(line[j]);
        }

        break;
      }
    }

    // Process the line once structural changes are complete
    processSyllable(line);

  }

  public void processSyllable(String[] line) {
    // Clean-up for quotes around commas
    if (line[ICon.WORD_IND].equals("\",\""))
      line[ICon.WORD_IND] = ",";

    // Parse phoneme into three components
    String[] sylComp = ParsingTools.parsePhoneme(line[ICon.PHONEME_IND]);

    int[] sAttributes = updateSylAttr(line, sylComp);

    float[] prob = null;

    if (hasComparisonData == true)
      prob = ParsingTools.getProb(line);

    // Does this syllable start a new word?
    if (currWord == null || !currWord.getWord().equals(line[ICon.WORD_IND])) {

      int[] wAttributes = updateWordAttr(line);

      // Create new word
      WordNode newWord = new WordNode(line[ICon.WORD_IND], wAttributes,
          sAttributes, prob, ParsingTools.notPunct(line[ICon.WORD_IND]));

      // Determine word, pos, and phoneme length
      double wordWidth = getTextWidth(line[ICon.WORD_IND]);
      double phonemeWidth = getTextWidth(line[ICon.PHONEME_IND]);
      double posWidth = getTextWidth(line[ICon.POS_IND]);

      if (currWord != null) {
        currWord.setNext(newWord);
      }
      currWord = newWord;

      currElements.get(ICon.MAX_DEPTH - 1).addChild(currWord);

      // Increment word, length and phoneme count at each level
      for (int i = 0; i < currElements.size(); i++) {
        currElements.get(i).incPhonemeCount();
        currElements.get(i).incWordCount();
        currElements.get(i).incTextWidth(wordWidth);
        currElements.get(i).incPhonemeWidth(phonemeWidth);
        currElements.get(i).incPOSWidth(posWidth);
      }

      // Else add phoneme to the current word
    } else {

      // Add syllable to the current word
      currWord.addSyllable(sAttributes, prob);

      // Determine word and phoneme length
      double phonemeWidth = getTextWidth(line[ICon.PHONEME_IND]);

      // Increment phoneme count at each level
      for (int i = 0; i < currElements.size(); i++) {
        currElements.get(i).incPhonemeCount();
        currElements.get(i).incPhonemeWidth(phonemeWidth);
      }
    }
  }

  // Update lists that are specific to the syllable
  public int[] updateSylAttr(String[] line, String[] sylComp) {
    int[] attr = new int[5];

    if (line[ICon.STRESS_IND].equals("NULL")
        || line[ICon.STRESS_IND].equals("\"NULL\"")) {
      attr[0] = -1;
    } else {
      attr[0] = Integer.parseInt(line[ICon.STRESS_IND]);
    }

    attr[1] = phonemeCode.indexOf(line[ICon.PHONEME_IND]);
    if (attr[1] == -1) {
      phonemeCode.add(line[ICon.PHONEME_IND]);
      attr[1] = phonemeCode.size() - 1;
    }

    attr[2] = phoC1Code.indexOf(sylComp[0]);
    if (attr[2] == -1) {
      phoC1Code.add(sylComp[0]);
      attr[2] = phoC1Code.size() - 1;
    }

    attr[3] = phoVCode.indexOf(sylComp[1]);
    if (attr[3] == -1) {
      phoVCode.add(sylComp[1]);
      attr[3] = phoVCode.size() - 1;
    }

    attr[4] = phoC2Code.indexOf(sylComp[2]);
    if (attr[4] == -1) {
      phoC2Code.add(sylComp[2]);
      attr[4] = phoC2Code.size() - 1;
    }

    return attr;
  }

  public boolean hasComparisonData() {
    return this.hasComparisonData;
  }

  // Update lists that are specific to the word
  public int[] updateWordAttr(String[] line) {
    int[] attr = new int[5];

    attr[0] = wordCode.indexOf(line[ICon.WORD_IND].toLowerCase());
    if (attr[0] == -1) {
      wordCode.add(line[ICon.WORD_IND].toLowerCase());
      attr[0] = wordCode.size() - 1;
    }

    attr[1] = posCode.indexOf(line[ICon.POS_IND]);
    if (attr[1] == -1) {
      posCode.add(line[ICon.POS_IND]);
      attr[1] = posCode.size() - 1;
    }

    attr[2] = accentCode.indexOf(line[ICon.ACCENT_IND]);
    if (attr[2] == -1) {
      accentCode.add(line[ICon.ACCENT_IND]);
      attr[2] = accentCode.size() - 1;
    }

    attr[3] = toneCode.indexOf(line[ICon.TONE_IND]);
    if (attr[3] == -1) {
      toneCode.add(line[ICon.TONE_IND]);
      attr[3] = toneCode.size() - 1;
    }

    if (line[ICon.WORD_IND].equals(""))
      line[ICon.WORD_IND] = ",";
    String soundCode = ParsingTools.soundex(line[ICon.WORD_IND]);
    attr[4] = soundexCode.indexOf(soundCode);
    if (attr[4] == -1) {
      soundexCode.add(soundCode);
      attr[4] = toneCode.size() - 1;
    }

    return attr;
  }

  public double getTextWidth(String input) {
    String text = input + " ";
    TextLayout layoutData = new TextLayout(text, gFont,
        g.getFontRenderContext());
    return layoutData.getAdvance();
  }

  /* Reconstruct phonemes as a single, space-delimited string */
  public String reconstructPhoneme(WordNode word) {
    ArrayList<Syllable> syllables = word.getSyllables();
    String text = "";

    for (int i = 0; i < syllables.size(); i++) {
      int index = syllables.get(i).getPhoneme();

      text += phonemeCode.get(index) + " ";
    }

    return text;
  }

  /* Reconstruct phonemes as a single, dash-delimited string */
  public String reconstructPhonemeT(WordNode word) {
    ArrayList<Syllable> syllables = word.getSyllables();
    String text = "";

    for (int i = 0; i < syllables.size(); i++) {
      int index = syllables.get(i).getPhoneme();

      if (i == syllables.size() - 1) {
        text += "(" + phonemeCode.get(index) + ")";
      } else {
        text += "(" + phonemeCode.get(index) + ")  ";
      }
    }

    return text;
  }

  // Create the initial nodes at each level of the tree
  public void initializeTree(String currLine) {
    String[] columns = currLine.split("\t");
    for (int i = 0; i < ICon.MAX_DEPTH; i++) {
      if (columns[i].equals("NULL") || columns[i].equals("null"))
        currIndices[i] = 1;
      else
        currIndices[i] = Integer.parseInt(columns[i]);
      maxWords[i] = 0;
      maxPhonemes[i] = 0;
      maxWordWidth[i] = 0.0;
      maxPhonemeWidth[i] = 0.0;
      maxPOSWidth[i] = 0.0;
      nodeCount[i] = 1;
    }

    HierNode phra = new HierNode(true);

    HierNode sent = new HierNode(false);
    sent.addChild(phra);

    HierNode para = new HierNode(false);
    para.addChild(sent);

    HierNode sect = new HierNode(false);
    sect.addChild(para);

    HierNode chap = new HierNode(false);
    chap.addChild(sect);

    head = new HierNode(false);
    head.addChild(chap);

    firstElements.add(chap);
    firstElements.add(sect);
    firstElements.add(para);
    firstElements.add(sent);
    firstElements.add(phra);

    currElements.add(chap);
    currElements.add(sect);
    currElements.add(para);
    currElements.add(sent);
    currElements.add(phra);
  }

  public HierNode newNode(int depth, boolean direct) {
    HierNode node;

    if (depth < ICon.MAX_DEPTH - 1) {
      node = new HierNode(false);
      node.addChild(newNode(depth + 1, false));
    } else {
      node = new HierNode(true);
    }

    if (direct) {
      if (depth == 0) {
        head.addChild(node);
      } else {
        currElements.get(depth - 1).addChild(node);
      }
    }

    currElements.get(depth).setNext(node);
    currElements.get(depth).getLastChild().addBreak();

    // Update maximums for this depth
    if (currElements.get(depth).getWordCount() > maxWords[depth]) {
      maxWords[depth] = currElements.get(depth).getWordCount();
    }

    if (currElements.get(depth).getPhonemeCount() > maxPhonemes[depth]) {
      maxPhonemes[depth] = currElements.get(depth).getPhonemeCount();
    }

    if (currElements.get(depth).getTextWidth() > maxWordWidth[depth]) {
      maxWordWidth[depth] = currElements.get(depth).getTextWidth();
    }

    if (currElements.get(depth).getPhonemeWidth() > maxPhonemeWidth[depth]) {
      maxPhonemeWidth[depth] = currElements.get(depth).getPhonemeWidth();
    }

    if (currElements.get(depth).getPOSWidth() > maxPOSWidth[depth]) {
      maxPOSWidth[depth] = currElements.get(depth).getPOSWidth();
    }

    nodeCount[depth]++;

    // Set the new node as the current node at this depth
    currElements.set(depth, node);
    return node;
  }

  public int getNodeCount(int depth) {
    return nodeCount[depth];
  }

  public double getMaxWordWidth(int depth) {
    return maxWordWidth[depth];
  }

  public double getMaxPhonemeWidth(int depth) {
    return maxPhonemeWidth[depth];
  }

  public double getMaxPOSWidth(int depth) {
    return maxPOSWidth[depth];
  }

  public int getMaxPhonemes(int depth) {
    return maxPhonemes[depth];
  }

  public String getPOSText(int index) {
    return posCode.get(index);
  }

  public String getAccentText(int index) {
    return accentCode.get(index);
  }

}
