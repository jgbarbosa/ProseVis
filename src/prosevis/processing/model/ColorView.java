package prosevis.processing.model;

import java.awt.Color;
import java.util.Map;

import prosevis.data.TypeMap;

public class ColorView {

  private final TypeMap typeMap;
  private final Map<Integer, Map<Integer, Color>> colorLookup;

  public ColorView(TypeMap typeMap, Map<Integer, Map<Integer, Color>> colorTable) {
    this.typeMap = typeMap;
    this.colorLookup = colorTable;
  }

  public String getType(int labelIdx, int typeIdx) {
    return typeMap.getTypeForIdx(labelIdx, typeIdx);
  }

  public Color getColor(int labelIdx, int typeIdx) {
    return colorLookup.get(labelIdx).get(typeIdx);
  }

}
