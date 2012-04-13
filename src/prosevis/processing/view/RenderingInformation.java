package prosevis.processing.view;

import prosevis.processing.model.ColorView;
import prosevis.processing.model.DataTreeView;

public class RenderingInformation {

  public final DataTreeView[] views;
  public final ColorView colorView;
  public final int sliderWidth;
  public final int viewWidth;
  public final int viewHeight;

  public RenderingInformation(DataTreeView[] views, ColorView colorView,
      int sliderSize, int viewWidth, int viewHeight) {
    this.views = views;
    this.colorView = colorView;
    this.sliderWidth = sliderSize;
    this.viewWidth = viewWidth;
    this.viewHeight = viewHeight;
  }
}
