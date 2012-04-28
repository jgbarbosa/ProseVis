package prosevis.processing.model;

import java.awt.Color;
import java.util.Map;

import prosevis.data.TypeMap;

public class ColorView {

  private final TypeMap typeMap;
  private final Map<Integer, Map<Integer, Color>> colorLookup;
  private boolean isFirstRender = true;

  public ColorView(TypeMap typeMap, Map<Integer, Map<Integer, Color>> colorTable) {
    this.typeMap = typeMap;
    this.colorLookup = colorTable;
  }

  public String getType(int labelIdx, int typeIdx) {
    return typeMap.getTypeForIdx(labelIdx, typeIdx);
  }

  public Color getColor(int labelIdx, int typeIdx) {
    Map<Integer, Color> map = colorLookup.get(labelIdx);
    if (map.containsKey(typeIdx)) {
      return map.get(typeIdx);
    }
    return map.get(TypeMap.kNoTypeIdx);
  }

  public boolean firstRenderSinceUpdate() {
    boolean tmp = isFirstRender;
    isFirstRender  = false;
    return tmp;
  }
}
