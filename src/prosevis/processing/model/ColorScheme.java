package prosevis.processing.model;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ColorScheme {
  public static ColorScheme loadFromFile(File file) throws InstantiationException {
    try {
      Scanner scanner = new Scanner(file);
      String schemeType = scanner.nextLine();
      String secondLine = scanner.nextLine();
      if (schemeType == null || secondLine == null) {
        throw new InstantiationException("Need at least two lines in every color scheme file, both scheme type, and default color");
      }
      Color defaultColor = parseColor(secondLine.trim());
      if (defaultColor == null) {
        throw new InstantiationException("Couldn't parse default color");
      }
      schemeType = schemeType.trim();
      Map<String, Color> mapping = new HashMap<String, Color>();
      mapping.put(kDefaultLabel, defaultColor);

      int lineNum = 1;
      while (scanner.hasNext()) {
        lineNum++;
        String line = scanner.nextLine();
        if (line == null) {
          throw new InstantiationException("Error parsing line " + lineNum);
        }
        line = line.trim();
        if (line.length() < 1 || line.startsWith("#")) {
          // skip empty lines
          continue;
        }
        if (line.indexOf('=') < 0) {
          throw new InstantiationException("Badly formatted line: " + lineNum);
        }

        String key = null;
        if (line.indexOf('"') >= 0) {
          int endingIdx = line.lastIndexOf('"');
          if (endingIdx <= 0) {
            throw new InstantiationException("Badly formatted label on line: " + lineNum + ", \"" + key + "\"");
          }
          key = line.substring(1, endingIdx);
        } else {
          key = line.substring(0, line.lastIndexOf('=')).trim();
        }
        String valueStr = line.substring(line.lastIndexOf('=') + 1).trim();

        Color value = parseColor(valueStr);
        if (value == null) {
          throw new InstantiationException("Badly formatted color value on line: " + lineNum);
        }
        if (mapping.containsKey(key)) {
          throw new InstantiationException("Duplicate key found on line: " + lineNum + ", \"" + key + "\"");
        }
        mapping.put(key, value);

      }

      return new ColorScheme(schemeType, mapping, file.getAbsolutePath());
    } catch (FileNotFoundException e) {
      throw new InstantiationException("Couldn't find file");
    }
  }

  private static Color parseColor(String valueStr) {
    if (valueStr.length() != 7 || valueStr.charAt(0) != '#') {
      return null;
    }

    try {
      int rComponent = Integer.parseInt(valueStr.substring(1, 3), 16);
      int gComponent = Integer.parseInt(valueStr.substring(3, 5), 16);
      int bComponent = Integer.parseInt(valueStr.substring(5, 7), 16);
      Color value = new Color(rComponent, gComponent, bComponent);
      return value;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static String kDefaultLabel = "~other";
  private final String path;
  private final Map<String, Color> colorMapping = new HashMap<String, Color>();
  private final String schemeType;

  private ColorScheme(String schemeType, Map<String, Color> colors, String path) {
    colorMapping.putAll(colors);
    this.path = path;
    this.schemeType = schemeType;
  }

  public void saveToFile() {
    saveToFile(this.path);
  }

  public boolean saveToFile(String path) {
    File file = new File(path);
    try {
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      writer.write(schemeType);
      writer.write(System.lineSeparator());
      writer.write(color2str(this.colorMapping.get(kDefaultLabel)));
      writer.write(System.lineSeparator());
      for (String key: this.colorMapping.keySet()) {
        if (key.equals(kDefaultLabel)) {
          continue;
        }
        writer.write("\"" + key + "\" = " + color2str(this.colorMapping.get(key)));
        writer.write(System.lineSeparator());
      }
      writer.close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private static String color2str(Color color) {
    String rs = Integer.toHexString(color.getRed());
    rs = rs.substring(0, Math.min(2, rs.length()));
    if (rs.length() < 2) { rs = "0" + rs; }
    String gs = Integer.toHexString(color.getGreen());
    gs = gs.substring(0, Math.min(2, gs.length()));
    if (gs.length() < 2) { gs = "0" + gs; }
    String bs = Integer.toHexString(color.getBlue());
    bs = bs.substring(0, Math.min(2, bs.length()));
    if (bs.length() < 2) { bs = "0" + bs; }
    return "#" + rs + gs + bs;
  }

  public String getLabel() {
    return schemeType;
  }

  public Map<String, Color> getMapping() {
    return colorMapping;
  }

}
