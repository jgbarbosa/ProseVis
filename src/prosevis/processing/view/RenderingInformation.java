package prosevis.processing.view;

import prosevis.data.TypeMap;
import prosevis.processing.model.ColorScheme;
import prosevis.processing.model.DataTreeView;

public class RenderingInformation {

  public final DataTreeView[] views;
  public final int sliderWidth;
  public final int viewWidth;
  public final int viewHeight;
  public final boolean[] enabled;
  public final TypeMap typeMap;

  public RenderingInformation(DataTreeView[] views, TypeMap typeMap, int sliderSize, int viewWidth, int viewHeight, boolean[] enabled) {
    this.views = views;
    this.typeMap = typeMap;
    this.sliderWidth = sliderSize;
    this.viewWidth = viewWidth;
    this.viewHeight = viewHeight;
    this.enabled = enabled;
  }
}
