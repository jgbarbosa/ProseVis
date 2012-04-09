package prosevis.processing.controller;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import prosevis.data.DataTree;
import prosevis.data.TypeMap;

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
    ColorScheme,
    Other,
  }
  public static File loadColorSchemeFile() {
    return loadFile(ForWhat.ColorScheme);
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
    case ColorScheme:
      pathIdx = 1;
      break;
    default:
      return null;
    }

    JFileChooser fileChooser = new JFileChooser(lastPaths[pathIdx]);
    int returnVal = fileChooser.showDialog(null, "Open File");

    if (returnVal == JFileChooser.APPROVE_OPTION) {
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
    return null;
  }

  private DataTree loadDataTree() {
    File file = loadFile(ForWhat.DataTree);
    DataTree tree = new DataTree();
    if (file != null && tree.load(file, this, typeMap)) {
      return tree;
    }
    return null;
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
