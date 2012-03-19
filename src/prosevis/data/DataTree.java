package prosevis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import prosevis.processing.controller.IProgressNotifiable;

public class DataTree {
  // The first nodes at each level of the tree
  private final ArrayList<HierNode> firstElements;
  // Current node at each hierarchical level of the tree
  private final ArrayList<HierNode> currentElements;
  // simple flag for whether this tree has already been loaded or not
  private boolean loaded;
  // The file from whence this data was parsed
  private String sourceFile;
  private String shortName;
  // how many nodes we have at each level
  private final int[] nodeCount;

  // HERE BE DRAGONS, DEEP NLP STUFF
  private final int[] currIndices;
  private final int[] maxWords;
  private final int[] maxPhonemes;
  private HierNode head;
  private ImplicitWordNode currentWord;

  // This is related to rendering and basically corresponds to the maximum width of
  // words/phonemes/parts of speech at each hierarchical level
  private final double[] maxWordWidth;
  private final double[] maxPhonemeWidth;
  private final double[] maxPOSWidth;

  public DataTree() {
    firstElements = new ArrayList<HierNode>();
    currentElements = new ArrayList<HierNode>();
    currIndices = new int[ICon.MAX_DEPTH];
    maxWords = new int[ICon.MAX_DEPTH];
    maxPhonemes = new int[ICon.MAX_DEPTH];
    maxWordWidth = new double[ICon.MAX_DEPTH];
    maxPhonemeWidth = new double[ICon.MAX_DEPTH];
    maxPOSWidth = new double[ICon.MAX_DEPTH];
    nodeCount = new int[ICon.MAX_DEPTH];
    loaded = false;
  }

  public boolean load(File file, IProgressNotifiable prog, TypeMap typeMap) {
    if (loaded) {
      throw new RuntimeException("You can't load this tree twice");
    }
    loaded = true;

    sourceFile = file.getAbsolutePath();
    shortName = file.getName();

    long totalBytes = file.length();
    long bytesProcessed = 0L;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = reader.readLine();
      // this may be slightly off on Windows (\r\n)
      bytesProcessed += line.length() + 1;
      String[] columns  = line.split("\t");

      // create a fresh color map, we'll merge it with existing maps later
      for (int i = TypeMap.kWordIdx; i < columns.length; i++) {
        if ("part_of_speech".equals(columns[i])) {
          // hack, I think part_of_speech is rather long
          columns[i] = "pos";
        }
        typeMap.addLabel(columns[i], i);
      }

      // Alright, we're done validating the file, lets try and get some data
      line = reader.readLine();
      if (line != null) {
        // the init routine needs to peek at the tree because it needs to know the
        // initial values for the numbering (ie paragraph 1, sect 2, sentence 3 etc)
        // this routine does not actually do anything about the data on the line
        initTree(line);
      } else {
        return false;
      }
      do {
        // this may be slightly off on Windows (\r\n)
        bytesProcessed += line.length() + 1;
        if (prog != null) {
          prog.notifyProgess(bytesProcessed / (double)totalBytes);
        }
        columns = line.split("\t");
        if (columns.length < ICon.TOTAL_COL)
          continue;
        processInputLine(columns, typeMap);
      } while ((line = reader.readLine()) != null);

      finalizeMaximums();
      reader.close();
    } catch (IOException e) {
      String message = "Error while reading file, aborting.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }
  private void finalizeMaximums() {
    for (int i = 0; i < ICon.MAX_DEPTH; i++) {
      if (currentElements.get(i).getWordCount() > maxWords[i]) {
        maxWords[i] = currentElements.get(i).getWordCount();
      }
      if (currentElements.get(i).getPhonemeCount() > maxPhonemes[i]) {
        maxPhonemes[i] = currentElements.get(i).getPhonemeCount();
      }
      if (currentElements.get(i).getTextWidth() > maxWordWidth[i]) {
        maxWordWidth[i] = currentElements.get(i).getTextWidth();
      }
      if (currentElements.get(i).getPhonemeWidth() > maxPhonemeWidth[i]) {
        maxPhonemeWidth[i] = currentElements.get(i).getPhonemeWidth();
      }
      if (currentElements.get(i).getPOSWidth() > maxPOSWidth[i]) {
        maxPOSWidth[i] = currentElements.get(i).getPOSWidth();
      }
    }
  }

  private void initTree(String line) {
    String[] columns = line.split("\t");
    for (int i = 0; i < ICon.MAX_DEPTH; i++) {
      currIndices[i] = 1;
      if (!columns[i].equals("NULL") && !columns[i].equals("null")) {
        try {
          currIndices[i] = Integer.parseInt(columns[i]);
        } catch (NumberFormatException e) {
          System.err.println("Found badly formatted section number, but recovering.");
        }
      }
      maxWords[i] = 0;
      maxPhonemes[i] = 0;
      maxWordWidth[i] = 0.0;
      maxPhonemeWidth[i] = 0.0;
      maxPOSWidth[i] = 0.0;
      nodeCount[i] = 1;
    }

    HierNode phra = new HierNode(true, 1);

    HierNode sent = new HierNode(false, 1);
    sent.addChild(phra);

    HierNode para = new HierNode(false, 1);
    para.addChild(sent);

    HierNode sect = new HierNode(false, 1);
    sect.addChild(para);

    HierNode chap = new HierNode(false, 1);
    chap.addChild(sect);

    head = new HierNode(false, 1);
    head.addChild(chap);

    firstElements.add(chap);
    firstElements.add(sect);
    firstElements.add(para);
    firstElements.add(sent);
    firstElements.add(phra);

    currentElements.add(chap);
    currentElements.add(sect);
    currentElements.add(para);
    currentElements.add(sent);
    currentElements.add(phra);
  }

  private void processInputLine(String[] line, TypeMap typeMap) {
    // Trim each field
    for (int i = 0; i < line.length; i++) {
      line[i] = line[i].trim();
    }

    // Loop through each depth column
    for (int i = 0; i < ICon.MAX_DEPTH; i++) {
      int currIndex = 1;
      if (!line[i].equals("NULL") && !line[i].equals("null")) {
        try {
          currIndex = Integer.parseInt(line[i]);
        } catch (NumberFormatException e) {
          System.err.println("Found badly formatted section number, but recovering.");
        }
      }

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
    processSyllable(line, typeMap);
  }

  private void processSyllable(String[] line, TypeMap typeMap) {
    // Clean-up for quotes around commas
    if (line[TypeMap.kWordIdx].equals("\",\""))
      line[TypeMap.kWordIdx] = ",";

    Syllable s = buildSyllable(line, typeMap);

    // TODO(wiley) Aha! This adds syllables to duplicate words following each other, like "that that"
    // Does this syllable start a new word?
    String word = (currentWord == null)?null:typeMap.getTypeForIdx(TypeMap.kWordIdx, currentWord.getTypeIdxForLabelIdx(TypeMap.kWordIdx));
    if (currentWord == null || !word.equals(line[TypeMap.kWordIdx])) {

      // Create new word
      ImplicitWordNode newWord = buildWordNode(line, s, typeMap);

      // Determine word, pos, and phoneme length
      double wordWidth = getTextWidth(line[TypeMap.kWordIdx]);
      double phonemeWidth = getTextWidth(line[TypeMap.kPhonemeIdx]);
      double posWidth = getTextWidth(line[TypeMap.kPOSLabelIdx]);

      if (currentWord != null) {
        currentWord.setNext(newWord);
      }
      currentWord = newWord;

      currentElements.get(ICon.MAX_DEPTH - 1).addChild(currentWord);

      // Increment word, length and phoneme count at each level
      for (int i = 0; i < currentElements.size(); i++) {
        currentElements.get(i).incPhonemeCount();
        currentElements.get(i).incWordCount();
        currentElements.get(i).incTextWidth(wordWidth);
        currentElements.get(i).incPhonemeWidth(phonemeWidth);
        currentElements.get(i).incPOSWidth(posWidth);
      }

      // Else add phoneme to the current word
    } else {

      // Add syllable to the current word
      currentWord.addSyllable(s);

      // Determine word and phoneme length
      double phonemeWidth = getTextWidth(line[TypeMap.kPhonemeIdx]);

      // Increment phoneme count at each level
      for (int i = 0; i < currentElements.size(); i++) {
        currentElements.get(i).incPhonemeCount();
        currentElements.get(i).incPhonemeWidth(phonemeWidth);
      }
    }
  }

  private ImplicitWordNode buildWordNode(String[] line, Syllable s, TypeMap typeMap) {
    ImplicitWordNode result = new ImplicitWordNode(line[TypeMap.kWordIdx], s);
    for (int idx = TypeMap.kWordIdx; idx < line.length; idx++) {
      int typeIdx = typeMap.getTypeIdx(idx, line[idx].toLowerCase());
      result.addLabelTypePair(idx, typeIdx);
    }

    String moddedWord = line[TypeMap.kWordIdx];
    if (moddedWord.isEmpty()) {
      moddedWord = ",";
    }
    String soundCode = ParsingTools.soundex(moddedWord);
    int soundexTypeIdx = typeMap.getTypeIdx(TypeMap.kSoundexIdx, soundCode);
    result.addLabelTypePair(TypeMap.kSoundexIdx, soundexTypeIdx);

    return result;
  }

  private double getTextWidth(String str) {
    // stub for now, we'll revisit this
    return 12 * (str.length() + 1) / 72.0;
  }
  public HierNode newNode(int depth, boolean direct) {
    HierNode node;
    nodeCount[depth]++;


    if (depth < ICon.MAX_DEPTH - 1) {
      node = new HierNode(false, nodeCount[depth]);
      node.addChild(newNode(depth + 1, false));
    } else {
      node = new HierNode(true, nodeCount[depth]);
    }

    if (direct) {
      if (depth == 0) {
        head.addChild(node);
      } else {
        currentElements.get(depth - 1).addChild(node);
      }
    }

    currentElements.get(depth).setNext(node);
    currentElements.get(depth).getLastChild().addBreak();

    // Update maximums for this depth
    if (currentElements.get(depth).getWordCount() > maxWords[depth]) {
      maxWords[depth] = currentElements.get(depth).getWordCount();
    }

    if (currentElements.get(depth).getPhonemeCount() > maxPhonemes[depth]) {
      maxPhonemes[depth] = currentElements.get(depth).getPhonemeCount();
    }

    if (currentElements.get(depth).getTextWidth() > maxWordWidth[depth]) {
      maxWordWidth[depth] = currentElements.get(depth).getTextWidth();
    }

    if (currentElements.get(depth).getPhonemeWidth() > maxPhonemeWidth[depth]) {
      maxPhonemeWidth[depth] = currentElements.get(depth).getPhonemeWidth();
    }

    if (currentElements.get(depth).getPOSWidth() > maxPOSWidth[depth]) {
      maxPOSWidth[depth] = currentElements.get(depth).getPOSWidth();
    }

    // Set the new node as the current node at this depth
    currentElements.set(depth, node);
    return node;
  }

  // Update lists that are specific to the syllable
  public Syllable buildSyllable(String[] line, TypeMap typeMap) {
    // Parse phoneme into three components
    String[] sylComp = ParsingTools.parsePhoneme(line[TypeMap.kPhonemeIdx]);

    int[] attr = new int[5];


    // this one is a little weird, because the field itself is actually an
    // integer, or null, but we want to interpret it as a string
    String stressType = line[TypeMap.kStressIdx].toLowerCase().replaceAll("\"", "");
    if (stressType.isEmpty()) {
      stressType = "null";
    }
    attr[0] = typeMap.getTypeIdx(TypeMap.kStressIdx, stressType);
    attr[1] = typeMap.getTypeIdx(TypeMap.kPhonemeIdx, line[TypeMap.kPhonemeIdx]);
    attr[2] = typeMap.getTypeIdx(TypeMap.kPhonemeC1Idx, sylComp[0]);
    attr[3] = typeMap.getTypeIdx(TypeMap.kPhonemeVIdx, sylComp[1]);
    attr[4] = typeMap.getTypeIdx(TypeMap.kPhonemeC2Idx, sylComp[2]);

    return new Syllable(attr);
  }

  public String getPath() {
    return this.sourceFile;
  }

  public String getName() {
    return this.shortName;
  }

  public int getNumNodes(int hierarchyLevel) {
    assert(hierarchyLevel <= ICon.CHAPTER_IND);
    assert(hierarchyLevel >= ICon.WORD_IND);
    return nodeCount[hierarchyLevel];
  }

  // Find the ordinal'th node on the level hierarchy level
  // for example, fine the 263rd paragraph (of the entire document)
  public HierNode findNode(int level, int ordinal) {
    HierNodeWrapper wrapper  = new HierNodeWrapper();
    head.findNode(-1, level, ordinal, wrapper);
    if (wrapper.resultType != HierNodeWrapper.ResultType.VALID) {
      throw new RuntimeException("Failed while looking up node");
    }
    return wrapper.result;
  }
}
