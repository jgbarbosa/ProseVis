package prosevis.processing.controller;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import prosevis.data.Document;
import prosevis.data.TypeMap;
import prosevis.processing.model.color.ColorScheme;

public class FileLoader implements Runnable, IProgressNotifiable {
  private final static String LAST_PATH_SAVE = ".lastpath.txt";
  private final JFrame parentComponent;
  private final TypeMap typeMap;

  public FileLoader(JFrame parent, TypeMap typeMap) {
    parentComponent = parent;
    this.typeMap = typeMap;
  }

  private enum ForWhat {
    DataTree,
    ColorSchemeLoad,
    ColorSchemeSave,
    Other,
  }
  public static File loadColorSchemeFile() {
    return loadFile(ForWhat.ColorSchemeLoad);
  }
  private static File loadFile(ForWhat forwhat) {
    String [] lastPaths = new String[2];

    try {
      BufferedReader pathInput = new BufferedReader(new FileReader(new File(LAST_PATH_SAVE)));
      lastPaths[0] = pathInput.readLine();
      lastPaths[1] = pathInput.readLine();
      pathInput.close();
    } catch (IOException e) {
      System.err.println("Couldn't find "+ LAST_PATH_SAVE + ", or badly formatted.");
    }

    if (lastPaths[0] == null) {
      lastPaths[0] = "Data" + File.pathSeparator;
    }
    if (lastPaths[1] == null) {
      lastPaths[1] = "ColorSchemes" + File.pathSeparator;
    }

    int pathIdx = -1;
    switch (forwhat) {
    case DataTree:
      pathIdx = 0;
      break;
    case ColorSchemeLoad:
    case ColorSchemeSave:
      pathIdx = 1;
      break;
    default:
      return null;
    }

    JFileChooser fileChooser = new JFileChooser(lastPaths[pathIdx]);
    int retValue = -1;
    if (forwhat != ForWhat.ColorSchemeSave) {
      retValue = fileChooser.showDialog(null, "Open File");
    } else {
      retValue = fileChooser.showSaveDialog(null);
    }
    if (retValue != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    
    File file = fileChooser.getSelectedFile();
    lastPaths[pathIdx] = file.getParentFile().getAbsolutePath();

    /* Save the last selected directory */
    try {
      FileWriter writer = new FileWriter(LAST_PATH_SAVE);
      writer.write(lastPaths[0] + "\n");
      writer.write(lastPaths[1] + "\n");
      writer.close();
    } catch (IOException e) {
      System.err.println("Couldn't save last used path to file.");
    }
    return file;
  }

  private Document loadDataTree() {
    File file = loadFile(ForWhat.DataTree);
    Document doc = new Document();
    if (file != null && doc.load(file, this, typeMap)) {
      return doc;
    }
    return null;
  }
  
  public static File getColorSchemeSaveFile() {
    return loadFile(ForWhat.ColorSchemeSave);
  }

  @Override
  public void run() {
    parentComponent.dispatchEvent(new FileProgressEvent(Frame.getWindows()[0], loadDataTree(), this.typeMap));
  }

  @Override
  public void notifyProgess(double d) {
    parentComponent.dispatchEvent(new FileProgressEvent(Frame.getWindows()[0], d));
  }
  public static File loadXmlFile() {
    return loadFile(ForWhat.DataTree);
  }
}
