package prosevis.processing.view;

public class GeometryModel {
  private int viewX;
  private final int viewY;
  private int sliderSz;
  private static final double kSliderFraction = 0.01;

  public GeometryModel(int tX, int tY) {
    viewY = tY;
    setX(tX);
  }

  public void setX(int x) {
    viewX = x;
    sliderSz = Math.max((int)(viewX * kSliderFraction), 10);
  }

  public int getViewX() {
    return viewX;
  }

  public int getSliderSize() {
    return sliderSz;
  }

  public int getViewTextX() {
    return viewX - sliderSz;
  }

  public int getViewY() {
    return viewY;
  }
}
