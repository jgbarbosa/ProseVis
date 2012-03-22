package prosevis.processing.model;

import java.util.ArrayList;
import java.util.List;

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
  private int textByLabelIdx = TypeMap.kWordIdx;
  private final List<ColorScheme> colorSchemes = new ArrayList<ColorScheme>();

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

  @Override
  public synchronized void setTextBy(String label) {
    int labelIdx = colorDB.getLabelIdx(label);
    for (DataTreeView view : data) {
      view.setTextBy(labelIdx);
    }
    textByLabelIdx  = labelIdx;
  }

  @Override
  public synchronized void addColorScheme(ColorScheme colorScheme) {
    if (!colorDB.addCustomColorScheme(colorScheme.getLabel(), colorScheme.getMapping())) {
      return;
    }
    removeColorScheme(colorScheme.getLabel(), false);
    colorSchemes.add(colorScheme);
  }

  private void removeColorScheme(String label, boolean replaceWithRandomColors) {
    boolean foundScheme = false;
    for (int i = 0; i < colorSchemes.size(); i++) {
      if (colorSchemes.get(i).getLabel().equals(label)) {
        colorSchemes.remove(i);
        i--;
        foundScheme = true;
      }
    }
    if (!foundScheme) {
      // don't want to drop the color pallette if we have this error
      System.err.println("Tried to remove color scheme for label " + label + " but couldn't find it.");
      return;
    }
    colorDB.dropColorsForLabel(label, replaceWithRandomColors);
  }

  @Override
  public synchronized void removeColorScheme(String label) {
    removeColorScheme(label, true);
  }

  @Override
  public synchronized ArrayList<String> getColorSchemeList() {
    ArrayList<String> labels = new ArrayList<String>();

    for (ColorScheme colorScheme: colorSchemes) {
      labels.add(colorScheme.getLabel());
    }

    return labels;
  }

  @Override
  public synchronized ColorScheme getColorScheme(String label) {
    for (ColorScheme colorScheme: colorSchemes) {
      if (label.equals(colorScheme.getLabel())) {
        return colorScheme;
      }
    }
    return null;
  }

  @Override
  public synchronized void searchForTerm(String searchTerm, String label, List<String> selectedFiles) {
    Integer labelIdx = colorDB.getLabelIdx(label);
    if (labelIdx == null) {
      return;
    }
    int typeIdx = colorDB.maybeGetTypeIdx(labelIdx, searchTerm);
    if (typeIdx < 0) {
      // no such term exists in our data
      return;
    }
    // look up datatreeviews with matching paths, and dispatch the search to them
    for (String s: selectedFiles) {
      for (DataTreeView v: data) {
        if (v.getData().getPath().equals(s)) {
          v.searchForTerm(typeIdx, labelIdx);
          break;
        }
      }
    }
  }
}
