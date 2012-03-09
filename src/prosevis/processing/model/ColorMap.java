package prosevis.processing.model;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import prosevis.data.TypeMap;

public class ColorMap {

  // maps from a labelIdx to a mapping from typeIdxs for that label to colors for those types
  private final Map<Integer, Map<Integer, Color>> colorLookup = new HashMap<Integer, Map<Integer,Color>>();
  // a labelIdx maps to true iff a custom color scheme is loaded
  private final Map<Integer, Boolean> customSchemes = new HashMap<Integer, Boolean>();
  private final TypeMap typeMap = new TypeMap();
  private ColorView cachedColorView;
  private boolean cacheValid = false;

  public Color getColor(int labelIdx, int typeIdx) {
    Map<Integer, Color> typeIdx2color = colorLookup.get(labelIdx);
    if (typeIdx2color.containsKey(typeIdx)) {
      return typeIdx2color.get(typeIdx);
    }
    // return the default color, usually white
    return typeIdx2color.get(-1);
  }

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
        customSchemes.put(labelIdx, false);
      }
    }
    for (int labelIdx: customSchemes.keySet()) {
      if (customSchemes.get(labelIdx)) {
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
      for (Entry<Integer, Map<Integer, Color>> entry: colorLookup.entrySet()) {
        Map<Integer, Color> colors = new HashMap<Integer, Color>();
        colors.putAll(entry.getValue());
        newColorTable.put(entry.getKey(), colors);
      }
      // deep copy here because hashmaps are definitely not threadsafe
      cachedColorView = new ColorView(getTypeMapCopy(), newColorTable);
      cacheValid = true;
    }
    return cachedColorView;
  }

  public int getLabelIdx(String label) {
    return typeMap.getLabelIdx(label);
  }
}
