package prosevis.processing.model;

import java.util.ArrayList;

import prosevis.data.DataTree;
import prosevis.processing.view.ProseColorBy;

public interface ProseModelIF {

  void addData(DataTree newTree);

  ArrayList<String> getFileList();

  void removeAllData();

  DataTreeView[] getRenderingData();

  void updateZoom(int lastDy);

  void setBreakLevel(DataTreeView.RenderBy level);

  DataTreeView.RenderBy getBreakLevel();

  ProseColorBy getColorBy();

  void setColorBy(ProseColorBy value);
}