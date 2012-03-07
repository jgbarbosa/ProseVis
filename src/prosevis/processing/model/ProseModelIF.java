package prosevis.processing.model;

import java.util.ArrayList;

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

  void setBreakLevel(DataTreeView.RenderBy level);

  DataTreeView.RenderBy getBreakLevel();

  // returns a labelIdx
  int getColorBy();

  void setColorBy(String labelStr);

  ColorView getColorView();
}