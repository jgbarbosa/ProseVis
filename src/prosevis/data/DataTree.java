package prosevis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import prosevis.data.nodes.HierNode;
import prosevis.data.nodes.HierNodeWrapper;
import prosevis.data.nodes.ProseNode;
import prosevis.data.nodes.Syllable;
import prosevis.data.nodes.WordNode;
import prosevis.processing.controller.IProgressNotifiable;

public class DataTree {

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

  // head of the tree formed by parsing the xml file
  private HierNode xmlHead;
  // head of the tree formed by parsing the tab separated data file
  private HierNode head;

  // simple flag for whether this tree has already been loaded or not
  private boolean loaded;
  // The file from whence this data was parsed
  private String sourceFile;
  private String shortName;

  // HERE BE DRAGONS, DEEP NLP STUFF
  private WordNode currentWord;

  // This is related to rendering and basically corresponds to the maximum width of
  // words/phonemes/parts of speech at each hierarchical level

  public DataTree() {
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
      List<HierNode> lastElements = null;
      if (line != null) {
        // the init routine needs to peek at the tree because it needs to know the
        // initial values for the numbering (ie paragraph 1, sect 2, sentence 3 etc)
        // this routine does not actually do anything about the data on the line
        lastElements = initTree(line.split("\t"));
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
        processInputLine(columns, typeMap, lastElements);
      } while ((line = reader.readLine()) != null);

      reader.close();
    } catch (IOException e) {
      String message = "Error while reading file, aborting.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }

  private void addInternalNodes(List<HierNode> lastElements, long[] idTuple) {
    if (lastElements.size() != idTuple.length) {
      throw new RuntimeException("Can't add internal nodes with more id's than we have hierarchy levels.");
    }
    boolean parentsDiffered = false;
    for (int i = 0; i < lastElements.size(); i++) {
      if (lastElements.get(i).getId() != idTuple[i] || parentsDiffered) {
        // this level needs a new node, and all following levels do as well
        parentsDiffered = true;
        HierNode parent = (i == 0)?head:lastElements.get(i - 1);
        int ordinal = lastElements.get(i).getNodeNumber() + 1;
        HierNode newNode = new HierNode(parent, ordinal, idTuple[i]);
        parent.addChild(newNode);
        lastElements.get(i).setNext(newNode);
        lastElements.set(i, newNode);
      }
    }
  }

  private long[] parseIdTuple(String [] line) {
    long sectionId, paragraphId, sentenceId, phraseId;
    sectionId = paragraphId = sentenceId = phraseId = -1;
    try {
      sectionId = Long.parseLong(line[0]);
      paragraphId = Long.parseLong(line[1]);
      sentenceId = Long.parseLong(line[3]);
      phraseId = Long.parseLong(line[4]);
    } catch (NumberFormatException e) {
      System.err.println("Found badly formatted section number, but recovering.");
    }
    long [] idTuple = new long[] {sectionId, paragraphId, sentenceId, phraseId};
    return idTuple;
  }

  private List<HierNode> initTree(String[] line) {
    long[] idTuple = parseIdTuple(line);

    // no parent for head naturally
    head = new HierNode(null, 0, 0);
    HierNode sect = new HierNode(head, 0, idTuple[0]);
    HierNode para = new HierNode(sect, 0, idTuple[1]);
    HierNode sent = new HierNode(para, 0, idTuple[2]);
    HierNode phra = new HierNode(sent, 0, idTuple[3]);

    head.addChild(sect);
    sect.addChild(para);
    para.addChild(sent);
    sent.addChild(phra);

    List<HierNode> lastElements = new ArrayList<HierNode>();
    lastElements.add(sect);
    lastElements.add(para);
    lastElements.add(sent);
    lastElements.add(phra);
    return lastElements;
  }

  private void processInputLine(String[] line, TypeMap typeMap, List<HierNode> lastElements) {
    // Trim each field
    for (int i = 0; i < line.length; i++) {
      line[i] = line[i].trim();
    }

    long [] idTuple = parseIdTuple(line);
    addInternalNodes(lastElements, idTuple);

    // Process the line once structural changes are complete
    processSyllable(lastElements.get(lastElements.size() - 1), line, typeMap, lastElements);
  }

  private void processSyllable(ProseNode parent, String[] line, TypeMap typeMap, List<HierNode> lastElements) {
    // Clean-up for quotes around commas
    if (line[TypeMap.kWordIdx].equals("\",\""))
      line[TypeMap.kWordIdx] = ",";
    line[TypeMap.kWordIdx] = line[TypeMap.kWordIdx].toLowerCase();

    Syllable s = buildSyllable(line, typeMap);

    // TODO(wiley) Aha! This adds syllables to duplicate words following each other, like "that that"
    String word = (currentWord == null)?null:typeMap.getTypeForIdx(TypeMap.kWordIdx, currentWord.getTypeIdxForLabelIdx(TypeMap.kWordIdx));
    if (currentWord == null || !word.equals(line[TypeMap.kWordIdx])) {
      // this syllable starts a new word

      // Create new word
      WordNode newWord = buildWordNode(parent, line, s, typeMap);

      if (currentWord != null) {
        currentWord.setNext(newWord);
      }
      currentWord = newWord;

      lastElements.get(lastElements.size() - 1).addChild(currentWord);
    } else {
      // Else add phoneme to the current word
      currentWord.addSyllable(s);
    }
  }

  private WordNode buildWordNode(ProseNode parent, String[] line, Syllable s, TypeMap typeMap) {
    WordNode result = new WordNode(parent, line[TypeMap.kWordIdx], s);
    for (int idx = TypeMap.kWordIdx; idx < TypeMap.kMaxFields; idx++) {
      int typeIdx = typeMap.getOrAddTypeIdx(idx, line[idx].toLowerCase());
      result.addLabelTypePair(idx, typeIdx);
    }

    String moddedWord = line[TypeMap.kWordIdx];
    if (moddedWord.isEmpty()) {
      moddedWord = ",";
    }
    String soundCode = ParsingTools.soundex(moddedWord);
    int soundexTypeIdx = typeMap.getOrAddTypeIdx(TypeMap.kSoundexIdx, soundCode);
    result.addLabelTypePair(TypeMap.kSoundexIdx, soundexTypeIdx);

    return result;
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
    attr[0] = typeMap.getOrAddTypeIdx(TypeMap.kStressIdx, stressType);
    attr[1] = typeMap.getOrAddTypeIdx(TypeMap.kPhonemeIdx, line[TypeMap.kPhonemeIdx]);
    attr[2] = typeMap.getOrAddTypeIdx(TypeMap.kPhonemeC1Idx, sylComp[0]);
    attr[3] = typeMap.getOrAddTypeIdx(TypeMap.kPhonemeVIdx, sylComp[1]);
    attr[4] = typeMap.getOrAddTypeIdx(TypeMap.kPhonemeC2Idx, sylComp[2]);

    return new Syllable(attr);
  }

  public String getPath() {
    return this.sourceFile;
  }

  public String getName() {
    return this.shortName;
  }

  public int getNumNodes(BreakLinesBy level) {
    HierNode requestedHierachy = (level == BreakLinesBy.Line)?xmlHead:head;
    int curHeight = requestedHierachy.getHeight();
    int desiredHeight = level.getHeight();
    while (curHeight > desiredHeight) {
      requestedHierachy = (HierNode)requestedHierachy.getLastChild();
      curHeight--;
    }
    return requestedHierachy.getNodeNumber();
  }

  // Find the ordinal'th node on the level hierarchy level
  // for example, find the 263rd paragraph (of the entire document)
  public HierNode findNode(BreakLinesBy level, int ordinal) {
    HierNode requestedHierachy = (level == BreakLinesBy.Line)?xmlHead:head;
    int currHeight = requestedHierachy.getHeight();
    HierNodeWrapper wrapper = new HierNodeWrapper();
    int requestedHeight = level.getHeight();
    requestedHierachy.findNode(currHeight, requestedHeight, ordinal, wrapper);
    if (wrapper.resultType != HierNodeWrapper.ResultType.VALID) {
      throw new RuntimeException("Failed while looking up node");
    }
    return wrapper.result;
  }
}
