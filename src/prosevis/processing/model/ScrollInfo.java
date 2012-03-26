package prosevis.processing.model;

import prosevis.data.nodes.HierNode;

public class ScrollInfo {
  public final HierNode lineNode;
  // [0, 1.0] of a line height to adjust the window start by
  public final double lineFrac;

  public ScrollInfo(HierNode node, double frac) {
    lineNode = node;
    lineFrac = frac;
  }
}
