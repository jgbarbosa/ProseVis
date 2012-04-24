package prosevis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nu.xom.Builder;
import nu.xom.ParsingException;
import prosevis.processing.controller.FileLoader;
import prosevis.processing.controller.IProgressNotifiable;

public class Document {
  private Word head;

  // simple flag for whether this tree has already been loaded or not
  private boolean loaded = false;
  // The file from whence this data was parsed
  private String sourceFile;
  private String shortName;

  private boolean xmlLoaded;
  public boolean load(File file, IProgressNotifiable prog, TypeMap typeMap) {
    if (loaded) {
      throw new RuntimeException("You can't load this tree twice");
    }
    loaded = true;

    if (!parseTSV(file, prog, typeMap)) {
      return false;
    }


    String xmlFileName = file.getName();
    xmlFileName = xmlFileName.substring(0, xmlFileName.lastIndexOf('.'));
    xmlFileName += ".xml";
    String xmlPath = file.getParent() + File.separator + xmlFileName;
    File xmlFile = new File(xmlPath);

    xmlLoaded = false;
    do {
      boolean haveXML = xmlFile != null && xmlFile.exists() && xmlFile.isFile();
      if (!haveXML) {
        int code = JOptionPane.showConfirmDialog(null,
            "Couldn't find an obvious XML file for " + shortName + ". Would you like to attach XML?");
        if (code == JOptionPane.CANCEL_OPTION) {
          // this cancels the entire document load
          return false;
        } else if (code == JOptionPane.YES_OPTION) {
          xmlFile = FileLoader.loadXmlFile();
          continue;
        } else {
          // this just cancels the xml load
          break;
        }
      }
      xmlLoaded = parseXML(xmlFile, prog, typeMap);
      xmlFile = null;
    } while (!xmlLoaded);

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

      Word lastWord = null;
      while ((line = reader.readLine()) != null) {
        // this may be slightly off on Windows (\r\n)
        bytesProcessed += line.length() + 1;
        if (prog != null) {
          prog.notifyProgess(bytesProcessed / (double) totalBytes);
        }
        columns = line.split("\t");
        lastWord = processInputLine(columns, typeMap, lastWord);
        if (head == null) {
          head = lastWord;
        }
      }

      reader.close();

      return true;
    } catch (IOException e) {
      String message = "Error while reading file, aborting.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }


  private boolean parseXML(File xmlFile, IProgressNotifiable prog,
      TypeMap typeMap) {
    int numLinesSoFar = 0;

    try {
      Builder parser = new Builder();
      nu.xom.Document doc = parser.build(xmlFile);
      XmlSearcher searcher = new XmlSearcher(doc);
      XmlSearcher sectionSearcher = new XmlSearcher(doc, XmlSearcher.kSEASRNamespace, "sid");
      XmlTraverser lineItr = new XmlTraverser(searcher.findElement(
          "" + head.getId(BreakLinesBy.LineGroup.getIdx())));
      String lastAct = sectionSearcher.findTextInChildLike(
          "" + head.getId(BreakLinesBy.Section.getIdx()), "head");
      String lastSpeaker = searcher.findTextInChildLike(
          "" + head.getId(BreakLinesBy.LineGroup.getIdx()), "speaker");
      String lastStage = sectionSearcher.findTextInChildLike(
          "" + head.getId(BreakLinesBy.Section.getIdx()), "stage");
      StringBuilder line = new StringBuilder(lineItr.getNextCleanLineOfText());
      int lineIdx = 0;

      for (Word w = head; w != null; ) {
        String word = typeMap.getTypeForIdx(TypeMap.kWordIdx,
            w.getTypeIdxForLabelIdx(TypeMap.kWordIdx));
        int idx = line.indexOf(word, lineIdx);
        boolean matchedWord = false;
        boolean matchedSomePart = false;
        if (idx >= 0) {
          lineIdx = idx + word.length();
          matchedWord = true;
        } else if (line.length() - lineIdx > 1) {
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
            idx = line.indexOf("" + word.charAt(wordIdx), lineIdx);
            if (idx >= 0) {
              lineIdx = idx + 1;
              matchedSomePart = true;
            } else {
              matchedWord = false;
              break;
            }
          }
        }
        if (matchedWord || (line.length() - lineIdx >= word.length() && matchedSomePart)) {
          if (!matchedWord && (line.length() - lineIdx >= word.length() && matchedSomePart)) {
            System.err.println("Warning: While matching XML to text, " +
                "failed to match fragment: '" + line.substring(lineIdx) + "'" +
                " to word: '" + word + "'");
          }
          w.setShakespeareInfo(lastAct, lastStage, lastSpeaker);
          w.setProseLine(numLinesSoFar);
          w = w.next();
        } else {
          line.delete(0, lineIdx);
          String nextLine = lineItr.getNextCleanLineOfText();
          if (nextLine == null) {
            lineItr = new XmlTraverser(searcher.findElement(
                "" + w.getId(BreakLinesBy.LineGroup.getIdx())));
            lastAct = sectionSearcher.findTextInChildLike(
                "" + w.getId(BreakLinesBy.Section.getIdx()), "head");
            lastStage = sectionSearcher.findTextInChildLike(
                "" + w.getId(BreakLinesBy.Section.getIdx()), "stage");
            lastSpeaker = searcher.findTextInChildLike(
                "" + w.getId(BreakLinesBy.LineGroup.getIdx()), "speaker");
            nextLine = lineItr.getNextCleanLineOfText();
          }
          line.append(nextLine);
          lineIdx = 0;
          numLinesSoFar++;

        }
      }

    } catch (ParsingException ex) {
      return false;
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private long[] parseIdTuple(String[] line) {
    long[] idTuple = new long[BreakLinesBy.kNumIndices];
    for (int i = 0; i < idTuple.length; i++) {
      idTuple[i] = -1;
    }
    try {
      idTuple[BreakLinesBy.Section.getIdx()] = Long.parseLong(line[0]);
      idTuple[BreakLinesBy.Paragraph.getIdx()] = Long.parseLong(line[1]);
      idTuple[BreakLinesBy.Sentence.getIdx()] = Long.parseLong(line[3]);
      idTuple[BreakLinesBy.Phrase.getIdx()] = Long.parseLong(line[4]);
    } catch (NumberFormatException e) {
      System.err
          .println("Found badly formatted section number, but recovering.");
    }
    return idTuple;
  }

  private Word processInputLine(String[] line, TypeMap typeMap, Word lastWord) {
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

    Syllable s = buildSyllable(line, typeMap);

    // TODO(wiley) Aha! This adds syllables to duplicate words following each
    // other, like "that that"
    if (lastWord == null || !lastWord.word().equals(line[TypeMap.kWordIdx])) {
      // Create new word
      Word newWord = buildWordNode(line, s, typeMap);

      if (lastWord != null) {
        lastWord.setNext(newWord);
      }
      lastWord = newWord;
    } else {
      // Else add phoneme to the current word
      lastWord.addSyllable(s);
    }

    return lastWord;
  }

  private Word buildWordNode(String[] line, Syllable s, TypeMap typeMap) {
    long[] idTuple = parseIdTuple(line);
    boolean isOpeningQuote = line[TypeMap.kWordIdx].equals("\"") &&
        line[TypeMap.kPOSLabelIdx].equals("``");

    Word result = new Word(
        line[TypeMap.kWordIdx], s, idTuple, isOpeningQuote);

    for (int idx = TypeMap.kWordIdx ; idx < TypeMap.kMaxFields; idx++) {
      int typeIdx = typeMap.getOrAddTypeIdx(idx, line[idx]);
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

  public Word getFirstWord() {
    return head;
  }

  public boolean hasXml() {
    return xmlLoaded;
  }
}
