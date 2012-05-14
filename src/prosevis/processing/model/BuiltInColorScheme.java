package prosevis.processing.model;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import prosevis.data.TypeMap;

public class BuiltInColorScheme extends ColorScheme {
  private static Map<String, Color> buildEmptyMapping() {
    HashMap<String, Color> ret = new HashMap<String, Color>();
    ret.put(ColorSchemeUtil.kDefaultLabel, Color.white);
    return ret;
  }

  public BuiltInColorScheme(String schemeName, String schemeType) {
    super(schemeName, schemeType, buildEmptyMapping());
  }
  
  @Override
  public synchronized Map<String, Color> getMapping() {
    return super.getMapping();
  }

  @Override
  protected synchronized void setMapping(Map<String, Color> map) {
    super.setMapping(map);
  }
  
  public void refresh(TypeMap typeMap) {
    HashMap<String, Color> newColors = new HashMap<String, Color>();
    final Map<String, Color> oldColors = getMapping();
    final int labelIdx = typeMap.getLabelIdx(getLabel());
    Collection<Integer> typeIdxs = typeMap.getTypeIdxsForLabel(labelIdx);
    for (Integer typeIdx: typeIdxs) {
      String key = typeMap.getTypeForIdx(labelIdx, typeIdx);
      if (oldColors.containsKey(key)) {
        newColors.put(key, oldColors.get(key));
      } else {
        // otherwise just pick a random color
        newColors.put(key, Color.getHSBColor((float)Math.random(), 0.7f, 0.9f));
      }
    }
    setMapping(newColors);
  }
}
