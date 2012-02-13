package prosevis.processing;

import java.util.ArrayList;

import prosevis.data.DataTree;

public class ApplicationModel {

  private ArrayList<DataTree> data = new ArrayList<DataTree>();
  
  public synchronized void addData(DataTree newTree) {
    String newFilePath = newTree.getPath();
    for (DataTree tree: data) {
      if (tree.getPath().equals(newFilePath)) {
        // disallow duplicates
        return;
      }
    }
    data.add(newTree);
  }
  
  public synchronized ArrayList<String> getFileList() {
    ArrayList<String> ret = new ArrayList<String>();
    for (DataTree tree: data) {
      ret.add(tree.getPath());
    }
    return ret;
  }
  
  public synchronized void removeAllData() {
    this.data.clear();
  }

}
