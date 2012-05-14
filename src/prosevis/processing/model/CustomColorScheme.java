package prosevis.processing.model;

import java.awt.Color;
import java.util.Map;

public class CustomColorScheme extends ColorScheme {
  private final String path;
 
  public CustomColorScheme(String schemeName, String schemeType, Map<String, Color> colors, String path) {
    super(schemeName, schemeType, colors);
    this.path = path;
  }

  public void saveToFile() {
    ColorSchemeUtil.saveToFile(this.path, this);
  }
}
