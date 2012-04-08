package prosevis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import prosevis.data.nodes.HierNode;
import prosevis.data.nodes.HierNodeWrapper;
import prosevis.data.nodes.ProseNode;
import prosevis.data.nodes.Syllable;
import prosevis.data.nodes.TreeSelector;
import prosevis.data.nodes.TreeSelector.WhichTree;
import prosevis.data.nodes.WordNode;
import prosevis.processing.controller.IProgressNotifiable;

public class DataTree {
  // head of the tree formed by parsing the xml file
  private HierNode xmlHead;
  // head of the tree formed by parsing the tab separated data file
  private HierNode head;

  // simple flag for whether this tree has already been loaded or not
  private boolean loaded;
  // The file from whence this data was parsed
  private String sourceFile;
  private String shortName;
  private final TreeSelector treeSelector = new TreeSelector();
  // used to help build up the tree at load time
  private WordNode currentWord;

  public DataTree() {
    loaded = false;
  }

  public boolean load(File file, IProgressNotifiable prog, TypeMap typeMap) {
    if (loaded) {
      throw new RuntimeException("You can't load this tree twice");
    }
    loaded = true;

    String xmlFileName = file.getName();
    xmlFileName = xmlFileName.substring(0, xmlFileName.lastIndexOf('.'));
    xmlFileName += ".xml";
    String xmlPath = file.getParent() + File.separator + xmlFileName;
    File xmlFile = new File(xmlPath);
    boolean haveXML = xmlFile.exists() && xmlFile.isFile();

    if (!parseTSV(file, prog, typeMap)) {
      return false;
    }

    if (haveXML) {
      parseXML(xmlFile, prog, typeMap);
    }

    return true;
  }

  private boolean parseXML(File xmlFile, IProgressNotifiable prog,
      TypeMap typeMap) {
    xmlHead = new HierNode(null, 0, 0);
    int numLinesSoFar = 0;
    try {
      Builder parser = new Builder();
      Document doc = parser.build(xmlFile);
      XmlSearcher searcher = new XmlSearcher(doc);
      // for each section in our document
      for (HierNode section = (HierNode) this.head.getFirstChild(); section != null; section = (HierNode) section
          .getNext()) {
        // mirror the sections in our xml tree
        HierNode sectionHierNode = new HierNode(xmlHead,
            section.getNodeNumber(), section.getId());
        xmlHead.addChild(sectionHierNode);
        // for each group underneath the sections
        for (HierNode group = (HierNode) section.getFirstChild();
            group != null && group.getParent() == section;
            group = (HierNode) group.getNext()) {
          // mirror the groups too
          HierNode groupHierNode = new HierNode(xmlHead.getLastChild(),
              group.getNodeNumber(), group.getId());
          ((HierNode) xmlHead.getLastChild()).addChild(groupHierNode);
          Element groupElement = searcher.findElement("" + group.getId());
          // we're looking for the last word in the group
          HierNode tmp = group;
          while (tmp.getFirstChild() != null
              && tmp.getFirstChild().getFirstChild() != null) {
            tmp = (HierNode) tmp.getLastChild();
          }
          WordNode lastWord = (WordNode) tmp.getLastChild();
          // we're looking for the first word in the group
          tmp = group;
          while (tmp.getFirstChild() != null
              && tmp.getFirstChild().getFirstChild() != null) {
            tmp = (HierNode) tmp.getFirstChild();
          }
          WordNode curWord = (WordNode) tmp.getFirstChild();
          WordNode prevWord = null;

          XmlTraverser lineItr = new XmlTraverser(groupElement);
          String line = lineItr.getNextLineOfText().trim()
              .toLowerCase().replaceAll("“", "\"").replaceAll("”", "\"");
          HierNode lineHierNode = new HierNode(groupHierNode, numLinesSoFar,
              numLinesSoFar);
          groupHierNode.addChild(lineHierNode);
          numLinesSoFar++;
          int lineIdx = 0;
          LinkedList<String> last10 = new LinkedList<String>();
          while (prevWord != lastWord) {
            if (curWord == null) {
              for (String s : last10) {
                System.out.println(s);
              }
            }
            String word = typeMap.getTypeForIdx(TypeMap.kWordIdx,
                curWord.getTypeIdxForLabelIdx(TypeMap.kWordIdx));
            last10.add(word);
            while (last10.size() > 10) {
              last10.pollFirst();
            }
            int idx = line.indexOf(word, lineIdx);
            boolean matchedWord = false;
            if (idx >= 0) {
              lineIdx = idx + word.length();
              matchedWord = true;
            } else if (line.length() > 1) {
              matchedWord = true;
              // there is still some stuff left on the end of this line
              // maybe this word matches this line but has been parsed incorrectly
              // lets try and match it ourselves
              for (int wordIdx = 0; wordIdx < word.length(); wordIdx++) {
                char curChar = word.charAt(wordIdx);
                if (!((curChar >= 'a' && curChar <= 'z') ||
                    (curChar >= 'A' && curChar <= 'Z') ||
                    (curChar >= '0' && curChar <= '9'))) {
                   continue;
                }
                idx = line.indexOf(word.charAt(wordIdx), lineIdx);
                if (idx >= 0) {
                  lineIdx = idx + 1;
                } else {
                  matchedWord = false;
                  break;
                }
              }
            }
            if (matchedWord) {
              curWord.addXmlLineParent(lineHierNode);
              lineHierNode.addChild(curWord, true);
            } else {
              System.out.print(line.substring(lineIdx));
              if (line.substring(lineIdx).length() > 0)
                System.out.println("Looking for: " + word);
              line = lineItr.getNextLineOfText();
              if (line == null) {
                System.err.println("Failed to find new line while looking for word: " + word);
                System.err.println("Last ten words: " + last10);
                return false;
              }
              line = line.trim().toLowerCase()
                  .replaceAll("“", "\"").replaceAll("”", "\"");
              lineHierNode = new HierNode(groupHierNode, numLinesSoFar,
                  numLinesSoFar);
              groupHierNode.addChild(lineHierNode);
              numLinesSoFar++;
              lineIdx = 0;
              continue;
            }

            prevWord = curWord;
            curWord = (WordNode) curWord.getNext();
          }
        }
      }

    } catch (ParsingException ex) {
      return false;
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private boolean parseTSV(File file, IProgressNotifiable prog, TypeMap typeMap) {
    sourceFile = file.getAbsolutePath();
    shortName = file.getName();

    long totalBytes = file.length();
    long bytesProcessed = 0L;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = reader.readLine();
      // this may be slightly off on Windows (\r\n)
      bytesProcessed += line.length() + 1;
      String[] columns = line.split("\t");

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
        // the init routine needs to peek at the tree because it needs to know
        // the
        // initial values for the numbering (ie paragraph 1, sect 2, sentence 3
        // etc)
        // this routine does not actually do anything about the data on the line
        lastElements = initTree(line.split("\t"));
      } else {
        return false;
      }

      do {
        // this may be slightly off on Windows (\r\n)
        bytesProcessed += line.length() + 1;
        if (prog != null) {
          prog.notifyProgess(bytesProcessed / (double) totalBytes);
        }
        columns = line.split("\t");
        processInputLine(columns, typeMap, lastElements);
      } while ((line = reader.readLine()) != null);

      reader.close();

      return true;
    } catch (IOException e) {
      String message = "Error while reading file, aborting.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void addInternalNodes(List<HierNode> lastElements, long[] idTuple) {
    if (lastElements.size() != idTuple.length) {
      throw new RuntimeException(
          "Can't add internal nodes with more id's than we have hierarchy levels.");
    }
    boolean parentsDiffered = false;
    for (int i = 0; i < lastElements.size(); i++) {
      if (lastElements.get(i).getId() != idTuple[i] || parentsDiffered) {
        // this level needs a new node, and all following levels do as well
        parentsDiffered = true;
        HierNode parent = (i == 0) ? head : lastElements.get(i - 1);
        int ordinal = lastElements.get(i).getNodeNumber() + 1;
        HierNode newNode = new HierNode(parent, ordinal, idTuple[i]);
        parent.addChild(newNode);
        lastElements.set(i, newNode);
      }
    }
  }

  private long[] parseIdTuple(String[] line) {
    long sectionId, paragraphId, sentenceId, phraseId;
    sectionId = paragraphId = sentenceId = phraseId = -1;
    try {
      sectionId = Long.parseLong(line[0]);
      paragraphId = Long.parseLong(line[1]);
      sentenceId = Long.parseLong(line[3]);
      phraseId = Long.parseLong(line[4]);
    } catch (NumberFormatException e) {
      System.err
          .println("Found badly formatted section number, but recovering.");
    }
    long[] idTuple = new long[] { sectionId, paragraphId, sentenceId, phraseId };
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

  private void processInputLine(String[] line, TypeMap typeMap,
      List<HierNode> lastElements) {
    // Trim each field
    for (int i = 0; i < line.length; i++) {
      line[i] = line[i].trim();
      if (line[i].startsWith("\"") && line[i].endsWith("\"") && line[i].length() > 2) {
        // sometimes for words with apostrophe's the word is surrounded in quotes
        line[i] = line[i].substring(1, line[i].length() - 1);
      }
    }

    if ("\"\"".equals(line[TypeMap.kWordIdx])) {
      // This is so painful I cannot begin to describe it
      line[TypeMap.kWordIdx] = "\"";
    }

    long[] idTuple = parseIdTuple(line);
    addInternalNodes(lastElements, idTuple);

    // Process the line once structural changes are complete
    processSyllable(lastElements.get(lastElements.size() - 1), line, typeMap,
        lastElements);
  }

  private void processSyllable(ProseNode parent, String[] line,
      TypeMap typeMap, List<HierNode> lastElements) {
    line[TypeMap.kWordIdx] = line[TypeMap.kWordIdx].toLowerCase();

    Syllable s = buildSyllable(line, typeMap);

    // TODO(wiley) Aha! This adds syllables to duplicate words following each
    // other, like "that that"
    String word = (currentWord == null) ? null : typeMap.getTypeForIdx(
        TypeMap.kWordIdx, currentWord.getTypeIdxForLabelIdx(TypeMap.kWordIdx));
    if (currentWord == null || !word.equals(line[TypeMap.kWordIdx])) {
      // this syllable starts a new word

      // Create new word
      WordNode newWord = buildWordNode(parent, line, s, typeMap);

      currentWord = newWord;

      lastElements.get(lastElements.size() - 1).addChild(currentWord);
    } else {
      // Else add phoneme to the current word
      currentWord.addSyllable(s);
    }
  }

  private WordNode buildWordNode(ProseNode parent, String[] line, Syllable s,
      TypeMap typeMap) {
    WordNode result = new WordNode(parent, this.treeSelector,
        line[TypeMap.kWordIdx], s);
    for (int idx = TypeMap.kWordIdx; idx < TypeMap.kMaxFields; idx++) {
      int typeIdx = typeMap.getOrAddTypeIdx(idx, line[idx].toLowerCase());
      result.addLabelTypePair(idx, typeIdx);
    }

    String moddedWord = line[TypeMap.kWordIdx];
    if (moddedWord.isEmpty()) {
      moddedWord = ",";
    }
    String soundCode = ParsingTools.soundex(moddedWord);
    int soundexTypeIdx = typeMap
        .getOrAddTypeIdx(TypeMap.kSoundexIdx, soundCode);
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
    String stressType = line[TypeMap.kStressIdx].toLowerCase().replaceAll("\"",
        "");
    if (stressType.isEmpty()) {
      stressType = "null";
    }
    attr[0] = typeMap.getOrAddTypeIdx(TypeMap.kStressIdx, stressType);
    attr[1] = typeMap.getOrAddTypeIdx(TypeMap.kPhonemeIdx,
        line[TypeMap.kPhonemeIdx]);
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
    HierNode requestedHierachy = (level == BreakLinesBy.Line) ? xmlHead : head;
    int curHeight = requestedHierachy.getHeight();
    int desiredHeight = level.getHeight();
    while (curHeight > desiredHeight) {
      requestedHierachy = (HierNode) requestedHierachy.getLastChild();
      curHeight--;
    }
    return requestedHierachy.getNodeNumber();
  }

  // Find the ordinal'th node on the level hierarchy level
  // for example, find the 263rd paragraph (of the entire document)
  public HierNode findNode(BreakLinesBy level, int ordinal) {
    HierNode requestedHierachy = (level == BreakLinesBy.Line) ? xmlHead : head;
    int currHeight = requestedHierachy.getHeight();
    HierNodeWrapper wrapper = new HierNodeWrapper();
    int requestedHeight = level.getHeight();
    requestedHierachy.findNode(currHeight, requestedHeight, ordinal, wrapper);
    if (wrapper.resultType != HierNodeWrapper.ResultType.VALID) {
      throw new RuntimeException("Failed while looking up node");
    }
    return wrapper.result;
  }

  public void setWhichTree(WhichTree v) {
    this.treeSelector.setWhichTree(v);
  }
}
