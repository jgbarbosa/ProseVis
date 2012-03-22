package prosevis.processing.model;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import prosevis.data.TypeMap;

public class ColorMap {

  // maps from a labelIdx to a mapping from typeIdxs for that label to colors for those types
  private final Map<Integer, Map<Integer, Color>> colorLookup = new HashMap<Integer, Map<Integer,Color>>();
  // a labelIdx maps to != null iff a custom color scheme is loaded
  private final Map<Integer, Map<Integer, Color>> customSchemes = new HashMap<Integer, Map<Integer, Color>>();
  private final TypeMap typeMap = new TypeMap();
  private ColorView cachedColorView;
  private boolean cacheValid = false;

  public TypeMap getTypeMapCopy() {
    return new TypeMap(typeMap);
  }

  public void mergeTypeMap(TypeMap typeMap) {
    boolean changed = this.typeMap.mergeTypeMap(typeMap);
    if (changed) {
      refreshColors();
    }
    cacheValid = cacheValid && !changed;
  }

  private void refreshColors() {
    for (int labelIdx: typeMap.getLabelIdxs()) {
      if (!customSchemes.containsKey(labelIdx)) {
        // if we don't even have colors for this scheme, we definitely don't
        // have a custom scheme for it
        customSchemes.put(labelIdx, null);
      }
    }
    for (int labelIdx: customSchemes.keySet()) {
      if (customSchemes.get(labelIdx) != null) {
        continue;
      }
      // no custom scheme here, make sure our colors are synced to the number of types
      if (!colorLookup.containsKey(labelIdx)) {
        colorLookup.put(labelIdx, new HashMap<Integer, Color>());
      }
      Map<Integer, Color> colors = colorLookup.get(labelIdx);
      Collection<Integer> types = typeMap.getTypeIdxsForLabel(labelIdx);
      if (types.size() != colors.size()) {
        colors.clear();
        for (Integer typeIdx: types) {
          colors.put(typeIdx, Color.getHSBColor((float)Math.random(), 0.7f, 0.9f));
        }
      }
    }
  }

  public ColorView getColorView() {
    if (!cacheValid) {
      Map<Integer, Map<Integer, Color>> newColorTable = new HashMap<Integer, Map<Integer,Color>>();
      for (Integer labelIdx: colorLookup.keySet()) {
        Map<Integer, Color> colors = new HashMap<Integer, Color>();
        Map<Integer, Color> oldColors = null;
        if (customSchemes.containsKey(labelIdx) && customSchemes.get(labelIdx) != null) {
          oldColors = customSchemes.get(labelIdx);
        } else {
          oldColors = colorLookup.get(labelIdx);
        }
        colors.putAll(oldColors);
        newColorTable.put(labelIdx, colors);
      }
      // deep copy here because hashmaps are definitely not threadsafe
      cachedColorView = new ColorView(getTypeMapCopy(), newColorTable);
      cacheValid = true;
    }
    return cachedColorView;
  }

  public Integer getLabelIdx(String label) {
    return typeMap.getLabelIdx(label);
  }

  public void dropColorsForLabel(String label, boolean replaceWithRandomColors) {
    int labelIdx = typeMap.getLabelIdx(label);
    customSchemes.put(labelIdx, null);

    if (replaceWithRandomColors) {
      refreshColors();
    }
    cacheValid = false;
  }

  public boolean addCustomColorScheme(String label, Map<String, Color> colors) {
    Integer labelIdx = typeMap.getLabelIdx(label);
    if (labelIdx == null) {
      return false;
    }
    Map<Integer, Color> types2colors = new HashMap<Integer, Color>();
    for (String typeLabel: colors.keySet()) {
      int typeIdx = -1;
      if (!typeLabel.equals(ColorScheme.kDefaultLabel)) {
        typeIdx = typeMap.getOrAddTypeIdx(labelIdx, typeLabel);
      }
      types2colors.put(typeIdx, colors.get(typeLabel));
    }
    customSchemes.put(labelIdx, types2colors);
    cacheValid = false;
    return true;
  }

  public int maybeGetTypeIdx(int labelIdx, String type) {
    return typeMap.maybeGetTypeIdx(labelIdx, type);
  }
}
