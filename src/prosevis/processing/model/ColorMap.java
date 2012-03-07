package prosevis.processing.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import prosevis.data.TypeMap;

public class ColorMap {

  // maps from a labelIdx to a mapping from typeIdxs for that label to colors for those types
  private final Map<Integer, Map<Integer, Color>> colorLookup = new HashMap<Integer, Map<Integer,Color>>();
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
    this.typeMap.mergeTypeMap(typeMap);
    cacheValid = false;
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
