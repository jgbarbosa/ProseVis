package prosevis.processing.view;

import java.awt.Color;
import java.util.HashMap;

import prosevis.data.POSType;

public class ColorSet {

  private final HashMap<POSType, Color> posColors = new HashMap<POSType, Color>();

  public ColorSet() {
    this(ColorSet.buildDefaultPOSColors());
  }

  public ColorSet(HashMap<POSType, Color> posColors) {
    // copy the mapping, but not the colors, and enum values are singletons anyway
    this.posColors.putAll(posColors);
  }

  public Color getColorForPOS(POSType pos) {
    return posColors.get(pos);
  }

  private static HashMap<POSType, Color> buildDefaultPOSColors() {
    HashMap<POSType, Color> ret = new HashMap<POSType, Color>();
    // make the two ending values not the same
    final int steps = POSType.values().length + 1;
    int i = 0;
    for (POSType pos: POSType.values()) {
      ret.put(pos, Color.getHSBColor(360.0f * (i / (float)steps), 0.9f, 0.9f));
      i++;
    }
    return ret;
  }


}
