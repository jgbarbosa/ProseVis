package prosevis.processing.model;

import java.util.ArrayList;
import java.util.List;

import prosevis.data.BreakLinesBy;
import prosevis.data.DataTree;
import prosevis.data.TypeMap;

public interface ProseModelIF {
  TypeMap getTypeMapCopy();

  // modifiedTypes must be compatible with the last call to getTypeMapCopy()
  // ie you have to take that typeMap you got, modify it, then send it along
  // with the dataTree
  // implicitly, this means you can only load one file at a time, for now
  void addData(DataTree newTree, TypeMap modifiedTypes);

  ArrayList<String> getFileList();

  void removeAllData();

  DataTreeView[] getRenderingData();

  void updateZoom(int lastDy);

  void setBreakLevel(BreakLinesBy level);

  BreakLinesBy getBreakLevel();

  // returns a labelIdx
  int getColorBy();

  void setColorBy(String labelStr);

  ColorView getColorView();

  void setTextBy(String labelStr);

  void addColorScheme(ColorScheme colorScheme);

  void removeColorScheme(String label);

  ArrayList<String> getColorSchemeList();

  ColorScheme getColorScheme(String label);

  void searchForTerm(String searchTerm, String label, List<String> selectedFiles);

  void moveFilesToTop(List<String> selectedFiles);

  void removeData(List<String> selectedFiles);
}