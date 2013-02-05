package prosevis.processing.model.color;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorScheme {
    
  protected static Map<String, Color> buildEmptyMapping() {
    HashMap<String, Color> ret = new HashMap<String, Color>();
    ret.put(ColorSchemeUtil.kDefaultLabel, Color.white);
    return ret;
  }

  private String schemeName;
  private String schemeType;
  private Map<String, Color> colorMapping = new HashMap<String, Color>();

  public ColorScheme(String schemeName, String schemeType, Map<String, Color> colors) {
    this.schemeName = schemeName;
    this.schemeType = schemeType;
    this.colorMapping.putAll(colors);
  }
  public synchronized String getLabel() {
    return schemeType;
  }

  public synchronized Map<String, Color> getMapping() {
    return colorMapping;
  }

  public synchronized String getName() {
    return schemeName;
  }

  // keep in mind that ColorSchemes are accessed from the Processing sketch
  // thread, but also from the Swing thread as new files are being added
  // (and built in color schemes get updated)
  protected synchronized void setMapping(Map<String, Color> map) {
    this.colorMapping = map;
  }
  
  // keep in mind that ColorSchemes are accessed from the Processing sketch
  // thread, but also from the Swing thread as new files are being added
  // (and built in color schemes get updated)
  protected synchronized void setLabel(String label) {
    this.schemeType = label;
  }
}
