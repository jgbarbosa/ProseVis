package prosevis.processing.model;

import java.util.ArrayList;

import prosevis.data.DataTree;
import prosevis.data.TypeMap;
import prosevis.processing.model.DataTreeView.RenderBy;

public class ApplicationModel implements ProseModelIF {

  private static final int ZOOM_SENSITIVITY = 5;
  private static final int ZOOM_MIN = 7 * ZOOM_SENSITIVITY;
  private static final int ZOOM_MAX = 28 * ZOOM_SENSITIVITY;
  private final ArrayList<DataTreeView> data = new ArrayList<DataTreeView>();
  private DataTreeView.RenderBy lineBreaks = DataTreeView.RenderBy.PHRASE;
  private int zoomLevel = 14 * ZOOM_SENSITIVITY;
  private int colorByLabelIdx = TypeMap.kNoLabelIdx;
  private final ColorMap colorDB = new ColorMap();

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#addData(prosevis.data.DataTree)
   */
  @Override
  public synchronized void addData(DataTree newTree, TypeMap correspondingTypeMap) {
    String newFilePath = newTree.getPath();
    for (DataTreeView tree: data) {
      if (tree.getData().getPath().equals(newFilePath)) {
        // disallow duplicates
        return;
      }
    }
    colorDB.mergeTypeMap(correspondingTypeMap);
    DataTreeView view = new DataTreeView(newTree, zoomLevel / ZOOM_SENSITIVITY);
    view.setRenderingBy(lineBreaks);
    view.setColorBy(colorByLabelIdx);
    data.add(view);
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

  @Override
  public synchronized void setBreakLevel(RenderBy level) {
    for (DataTreeView view : data) {
      view.setRenderingBy(level);
    }
    lineBreaks = level;
  }

  @Override
  public synchronized RenderBy getBreakLevel() {
    return lineBreaks;
  }

  @Override
  public synchronized int getColorBy() {
    return colorByLabelIdx;
  }

  @Override
  public synchronized void setColorBy(String label) {
    int labelIdx = colorDB.getLabelIdx(label);
    for (DataTreeView view : data) {
      view.setColorBy(labelIdx);
    }
    colorByLabelIdx = labelIdx;
  }

  @Override
  public synchronized TypeMap getTypeMapCopy() {
    return colorDB.getTypeMapCopy();
  }

  @Override
  public synchronized ColorView getColorView() {
    return colorDB.getColorView();
  }
}
