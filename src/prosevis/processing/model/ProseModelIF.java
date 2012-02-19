package prosevis.processing.model;

import java.util.ArrayList;

import prosevis.data.DataTree;

public interface ProseModelIF {

  void addData(DataTree newTree);

  ArrayList<String> getFileList();

  void removeAllData();
  
  DataTreeView[] getRenderingData();
}