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

public class FileLoader implements Runnable, IProgressNotifiable {  
  private final static String LAST_PATH_SAVE = ".lastpath.txt";
  private JFrame parentComponent;
  
  public FileLoader(JFrame parent) {
    parentComponent = parent;
 }
  
  private DataTree loadFile() {
    String lastPath = null;
    try {
      BufferedReader pathInput = new BufferedReader(new FileReader(new File(LAST_PATH_SAVE)));
      lastPath = pathInput.readLine();
      pathInput.close();
    } catch (IOException e) {
      System.err.println("Couldn't find "+ LAST_PATH_SAVE);
    }
    
    if (null == lastPath || lastPath.length() < 1) {
      lastPath = "Data" + File.pathSeparator;
    }

    JFileChooser fileChooser = new JFileChooser(lastPath);
    int returnVal = fileChooser.showDialog(null, "Open File");

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      lastPath = file.getParentFile().getAbsolutePath();

      /* Save the last selected directory */
      try {
        FileWriter writer = new FileWriter(LAST_PATH_SAVE);
        writer.write(lastPath + "\n");
        writer.close();
      } catch (IOException e) {
        System.err.println("Couldn't save last used path to file.");
      }
      
      DataTree tree = new DataTree();
      if (tree.load(file, this)) {
        return tree;
      }
    }    
    return null;
  }

  @Override
  public void run() {
    parentComponent.dispatchEvent(new FileProgressEvent(Frame.getWindows()[0], loadFile()));
  }

  @Override
  public void notifyProgess(double d) {
    parentComponent.dispatchEvent(new FileProgressEvent(Frame.getWindows()[0], d)); 
  }
}
