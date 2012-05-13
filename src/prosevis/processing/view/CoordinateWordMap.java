package prosevis.processing.view;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import prosevis.data.Word;

public class CoordinateWordMap {
  private static class Zone {
    private int x;
    private int y;
    private int dx;
    private int dy;
    private Word w;

    public Zone(int x, int y, int dx, int dy, Word w) {
      this.x = x;
      this.y = y;
      this.dx = dx;
      this.dy = dy;
      this.w = w;
    }
    
    public void reset(int x, int y, int dx, int dy, Word w) {
      this.x = x;
      this.y = y;
      this.dx = dx;
      this.dy = dy;
      this.w = w;
    }
    
    public boolean contains(int px, int py) {
      if (px >= x && px <= x + dx && py >= y && py <= y + dy) {
        return true;
      }
      return false;
    }
    
    public Word getWord() {
      return w;
    }
  }

  private final ArrayList<Zone> zones = new ArrayList<Zone>();
  private int allocatedZones = 0;
  
  // keep these in memory and try to avoid a bunch of heap operations
  private Zone addZone(int x, int y, int dx, int dy, Word w) {
    if (allocatedZones >= zones.size()) {
      zones.add(new Zone(x, y, dx, dy, w));
    }
    Zone ret = zones.get(allocatedZones);
    ret.reset(x,  y, dx, dy, w);
    allocatedZones++;
    return ret;
  }

  public void clear() {
    allocatedZones = 0;  
  }
  
  public Word translate(Point p) {
    return translate(p.x, p.y);
  }
  
  public Word translate(int x, int y) {
    for (int i = 0; i < allocatedZones; i++) {
      if (zones.get(i).contains(x, y)) {
        return zones.get(i).getWord();
      }
    }
    return null;
  }
  
  public void put(int x, int y, int dx, int dy, Word w) {
    addZone(x, y, dx, dy, w);
  }
}
