package prosevis.processing.model;

import java.util.ArrayList;

import prosevis.data.DataTree;

public class ApplicationModel implements ProseModelIF {

  private static final int ZOOM_SENSITIVITY = 3;
  private static final int ZOOM_MIN = 7 * ZOOM_SENSITIVITY;
  private static final int ZOOM_MAX = 28 * ZOOM_SENSITIVITY;
  private ArrayList<DataTreeView> data = new ArrayList<DataTreeView>();
  private int zoomLevel = 5;
  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#addData(prosevis.data.DataTree)
   */
  @Override
  public synchronized void addData(DataTree newTree) {
    String newFilePath = newTree.getPath();
    for (DataTreeView tree: data) {
      if (tree.getData().getPath().equals(newFilePath)) {
        // disallow duplicates
        return;
      }
    }
    data.add(new DataTreeView(newTree));
  }

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#getFileList()
   */
  @Override
  public synchronized ArrayList<String> getFileList() {
    ArrayList<String> ret = new ArrayList<String>();
    for (DataTreeView tree: data) {
      ret.add(tree.getData().getPath());
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#removeAllData()
   */
  @Override
  public synchronized void removeAllData() {
    this.data.clear();
  }

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#getRenderingData()
   */
  @Override
  public synchronized DataTreeView[] getRenderingData() {
    return data.toArray(new DataTreeView[0]);
  }

  @Override
  public synchronized void updateZoom(int lastDy) {
    if (lastDy == 0) {
      return;
    }
    zoomLevel = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoomLevel + lastDy));
    int newSize = zoomLevel / ZOOM_SENSITIVITY;
    for (DataTreeView view : data) {
      view.setSize(newSize);
    }
  }
}
