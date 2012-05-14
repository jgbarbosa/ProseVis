package prosevis.processing.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorScheme {
  private String schemeName;
  private final String schemeType;
  private Map<String, Color> colorMapping = new HashMap<String, Color>();

  public ColorScheme(String schemeName, String schemeType, Map<String, Color> colors) {
    this.schemeName = schemeName;
    this.schemeType = schemeType;
    this.colorMapping.putAll(colors);
  }
  public String getLabel() {
    return schemeType;
  }

  public Map<String, Color> getMapping() {
    return colorMapping;
  }

  public String getName() {
    return schemeName;
  }

  // Subclasses are responsible for synchronizing accesses to the colormap
  // keep in mind that ColorSchemes are accessed from the Processing sketch
  // thread, but also from the Swing thread as new files are being added
  // (and built in color schemes get updated)
  protected void setMapping(Map<String, Color> map) {
    this.colorMapping = map;
  }
}
