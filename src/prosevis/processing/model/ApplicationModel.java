package prosevis.processing.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import prosevis.data.BreakLinesBy;
import prosevis.data.Document;
import prosevis.data.TypeMap;
import prosevis.processing.controller.ComparisonState;
import prosevis.processing.model.color.ColorScheme;
import prosevis.processing.model.color.ColorSchemeDB;
import prosevis.processing.model.color.CustomColorScheme;
import prosevis.processing.model.color.WorkingColorScheme;
import prosevis.processing.view.GeometryModel;
import prosevis.processing.view.RenderingInformation;
import prosevis.processing.view.WidthCalculator;

public class ApplicationModel {
  public static final int kMaxFontSz = 28;
  public static final int kMinFontSz = 3;
  private static final int kZoomSensitivity = 5;
  private static final int kZoomMin = kMinFontSz * kZoomSensitivity;
  private static final int kZoomMax = kMaxFontSz * kZoomSensitivity;

  private final int xResolution;
  private final int yResolution;
  private final ArrayList<DataTreeView> data = new ArrayList<DataTreeView>();
  private BreakLinesBy lineBreaks = BreakLinesBy.Phrase;
  // zoom level is like the font size, except in units scaled by kZoomSensitivity
  // this lets us add the raw number of pixels a user has right-click-dragged to
  // zoomLevel and later convert that to a desired font size
  private int zoomLevel = 14 * kZoomSensitivity;
  private final ColorSchemeDB colorDB = new ColorSchemeDB();
  private final GeometryModel geoModel;
  private ComparisonState[] comparisonState = null;
  private int smoothingWindow = 1;

  public ApplicationModel(int xres, int yres) {
    xResolution = xres;
    yResolution = yres;
    geoModel = new GeometryModel(xResolution, yResolution);
  }

  // modifiedTypes must be compatible with the last call to getTypeMapCopy()
  // ie you have to take that typeMap you got, modify it, then send it along
  // with the dataTree
  // implicitly, this means you can only load one file at a time, for now
  public synchronized void addData(Document newTree, TypeMap correspondingTypeMap) {
    WidthCalculator wc = WidthCalculator.getWidthCalculator();
    if (wc == null) {
      return;
    }
    String newFilePath = newTree.getPath();
    for (DataTreeView tree: data) {
      if (tree.getData().getPath().equals(newFilePath)) {
        // disallow duplicates
        return;
      }
    }
    colorDB.mergeTypeMap(correspondingTypeMap);
    DataTreeView view = new DataTreeView(newTree, zoomLevel / kZoomSensitivity, geoModel, wc);
    view.setRenderingBy(lineBreaks);
    view.setColorBy(colorDB.getSelectedColorScheme());
    view.setSmoothingWindow(smoothingWindow);
    data.add(view);
    geoModel.setX(xResolution / data.size());

    if (colorDB.hasComparisonData() && comparisonState == null) {
      String [] headers = colorDB.getComparisonHeaders();
      comparisonState = new ComparisonState[headers.length];
      for (int i = 0; i < headers.length; i++) {
        comparisonState[i] = new ComparisonState(headers[i], Color.WHITE);
      }
      refreshComparisonColors();
    }
  }

  public synchronized ArrayList<String> getFileList() {
    ArrayList<String> ret = new ArrayList<String>();
    for (DataTreeView tree: data) {
      ret.add(tree.getData().getPath());
    }
    return ret;
  }

  public synchronized void removeAllData() {
    this.data.clear();
    this.colorDB.clearComparisonData();
    this.comparisonState = null;
  }

  public synchronized void removeData(List<String> selectedFiles) {
    for (String path: selectedFiles) {
      // find the DataTree corresponding to this path
      int idx = 0;
      for (idx = 0; idx < data.size(); idx++) {
        if (data.get(idx).getData().getPath().equals(path)) {
          data.remove(idx);
          break;
        }
      }
    }
    if (data.isEmpty()) {
      this.colorDB.clearComparisonData();
      this.comparisonState = null;
    } else {
      geoModel.setX(xResolution / data.size());
    }
  }


  public synchronized RenderingInformation getRenderingData() {
    boolean [] enabled = null;
    if (comparisonState != null) {
      enabled = new boolean[comparisonState.length];
      for (int i = 0; i < enabled.length; i++) {
        enabled[i] = comparisonState[i].getEnabled();
      }
    }
    return new RenderingInformation(
        data.toArray(new DataTreeView[0]),
        colorDB.getTypeMapCopy(),
        geoModel.getSliderSize(),
        geoModel.getViewX(),
        geoModel.getViewY(),
        enabled);
  }

  public synchronized void updateZoom(int lastDy) {
    if (lastDy == 0) {
      return;
    }
    zoomLevel = Math.max(kZoomMin, Math.min(kZoomMax, zoomLevel + lastDy));
    int newSize = zoomLevel / kZoomSensitivity;
    for (DataTreeView view : data) {
      view.setSize(newSize);
    }
  }

  public synchronized void setBreakLevel(BreakLinesBy level) {
    for (DataTreeView view : data) {
      view.setRenderingBy(level);
    }
    lineBreaks = level;
  }

  public synchronized BreakLinesBy getBreakLevel() {
    return lineBreaks;
  }

  public synchronized String getColorBy() {
    return colorDB.getSelectedColorScheme().getName();
  }

  public synchronized void setColorBy(String label) {
    ColorScheme selectedScheme = colorDB.selectColorScheme(label);
    for (DataTreeView view : data) {
      view.setColorBy(selectedScheme);
    }
  }

  public synchronized TypeMap getTypeMapCopy() {
    return colorDB.getTypeMapCopy();
  }

  public synchronized void setTextBy(String label) {
    int labelIdx = colorDB.getLabelIdx(label);
    for (DataTreeView view : data) {
      view.setTextBy(labelIdx);
    }
  }

  public synchronized void addColorScheme(CustomColorScheme colorScheme) {
    if (!colorDB.addColorScheme(colorScheme)) {
      return;
    }
    refreshComparisonColors();
  }

  public synchronized void removeColorScheme(String name) {
    colorDB.removeColorScheme(name);
    refreshComparisonColors();
  }

  public synchronized ArrayList<String> getCustomColorSchemeList() {
    return colorDB.getNamesOfCustomSchemes();
  }

  public synchronized void searchForTerm(String searchTerm, String label, List<String> selectedFiles) {
    Integer labelIdx = colorDB.getLabelIdx(label);
    if (labelIdx == null) {
      System.err.println("Searching for non-existing label, aborting.");
      return;
    }
    int typeIdx = colorDB.maybeGetTypeIdx(labelIdx, searchTerm);

    // look up datatreeviews with matching paths, and dispatch the search to them
    List<DataTreeView> selectedData = new ArrayList<DataTreeView>();
    if (selectedFiles.size() < 1) {
      selectedData = data;
    } else {
      for (String s: selectedFiles) {
        for (DataTreeView v: data) {
          if (v.getData().getPath().equals(s)) {
            selectedData.add(v);
          }
        }
      }
    }
    for (DataTreeView v: selectedData) {
      v.searchForTerm(typeIdx, labelIdx);
    }

    setColorBy(TypeMap.kNoLabelLabel);
  }

  public synchronized void moveFilesToTop(List<String> selectedFiles) {
    ArrayList<DataTreeView> movedFiles = new ArrayList<DataTreeView>();
    for (String path: selectedFiles) {
      // find the DataTree corresponding to this path
      int idx = 0;
      for (idx = 0; idx < data.size(); idx++) {
        if (data.get(idx).getData().getPath().equals(path)) {
          break;
        }
      }
      if (idx >= data.size()) {
        continue;
      }
     movedFiles.add(data.remove(idx));
    }
    data.addAll(0, movedFiles);
  }

  public synchronized int getScreenY() {
    return this.yResolution;
  }

  public synchronized int getScreenX() {
    return this.xResolution;
  }

  public synchronized ComparisonState[] getComparisonState() {
    return comparisonState;
  }

  public synchronized boolean hasComparisonData() {
    return comparisonState != null;
  }

  public synchronized void setComparisonEnabled(boolean selected, String text) {
    if (this.comparisonState == null) {
      return;
    }
    for (ComparisonState c: comparisonState) {
      if (c.getName().equals(text)) {
        c.setEnabled(selected);
        return;
      }
    }
  }

  private void refreshComparisonColors() {
    if (comparisonState == null) {
      return;
    }
    ColorScheme comparisonColors = colorDB.getComparisonColors();
    Map<String, Color> colorMap = comparisonColors.getMapping();

    for (int i = 0; i < comparisonState.length; i++) {
      ComparisonState s = comparisonState[i];
      s.setColor(colorMap.get(s.getName()));
    }
  }

  public synchronized boolean isLassoMode() {
    return this.xResolution > 3000;
  }

  public synchronized int getSmoothingWindow() {
    return smoothingWindow;
  }

  public synchronized int setSmoothingWindow(int window) {
    if (window >= 1 && smoothingWindow != window) {
      smoothingWindow = window;
      for (DataTreeView d: data) {
        d.setSmoothingWindow(smoothingWindow);
      }
    }
    return smoothingWindow;
  }

  public synchronized List<String> getBuiltInColorSchemeList() {
    return colorDB.getNamesOfBuiltInSchemes();
  }

  public synchronized void registerWorkingColorScheme(WorkingColorScheme colorScheme) {
    colorDB.registerWorkingColorScheme(colorScheme);
  }
}
