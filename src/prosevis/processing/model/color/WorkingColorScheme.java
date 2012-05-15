package prosevis.processing.model.color;

import java.awt.Color;
import java.util.Map;

import prosevis.data.TypeMap;



public class WorkingColorScheme extends ColorScheme {
  public WorkingColorScheme() {
    super(ColorSchemeUtil.kWorkingLabel, TypeMap.kNoLabelLabel, ColorScheme.buildEmptyMapping());
  }
  
  @Override
  public void setLabel(String label) {
    super.setLabel(label);
  }
  
  @Override
  public void setMapping(Map<String, Color> mapping) {
    mapping.put(ColorSchemeUtil.kDefaultLabel, Color.white);
    super.setMapping(mapping);
  }
}
