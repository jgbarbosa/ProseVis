package prosevis.processing.model.color;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public abstract class ColorSchemeUtil {
  public static final String kWorkingLabel = "Working color scheme";
  public static String kDefaultLabel = "~other";
  // colors from colorbrewer2.org
  public final static Color[] goodColors = new Color[] {
        new Color(228, 26, 28),
        new Color(55, 126, 184),
        new Color(77, 175, 74),
        new Color(152, 78, 163),
        new Color(255, 127, 0),
        new Color(255, 255, 51),
        new Color(166, 86, 40),
        new Color(247, 129, 191),
    };

  public static CustomColorScheme loadFromFile(File file) throws InstantiationException {
    try {
      Scanner scanner = new Scanner(file);
      String schemeName =  file.getName().substring(0, file.getName().lastIndexOf('.'));
      String schemeType = scanner.nextLine();
      String secondLine = scanner.nextLine();
      if (schemeType == null || secondLine == null) {
        System.err.println("Need at least three lines in every color scheme file: scheme name, scheme type, and default color");
        return null;
      }
      Color defaultColor = parseColor(secondLine.trim());
      if (defaultColor == null) {
        System.err.println("Couldn't parse default color");
        return null;
      }
      schemeType = schemeType.trim();
      Map<String, Color> mapping = new HashMap<String, Color>();
      mapping.put(kDefaultLabel, defaultColor);

      int lineNum = 1;
      while (scanner.hasNext()) {
        lineNum++;
        String line = scanner.nextLine();
        if (line == null) {
          System.err.println("Error parsing line " + lineNum);
          return null;
        }
        line = line.trim();
        if (line.length() < 1 || line.startsWith("#")) {
          // skip empty lines
          continue;
        }
        if (line.indexOf('=') < 0) {
          System.err.println("Badly formatted line: " + lineNum);
          return null;
        }

        String key = null;
        if (line.indexOf('"') >= 0) {
          int endingIdx = line.lastIndexOf('"');
          if (endingIdx <= 0) {
            System.err.println("Badly formatted label on line: " + lineNum + ", \"" + key + "\"");
            return null;
          }
          key = line.substring(1, endingIdx);
        } else {
          key = line.substring(0, line.lastIndexOf('=')).trim();
        }
        String valueStr = line.substring(line.lastIndexOf('=') + 1).trim();

        Color value = parseColor(valueStr);
        if (value == null) {
          System.err.println("Badly formatted color value on line: " + lineNum);
          return null;
        }
        if (mapping.containsKey(key)) {
          System.err.println("Duplicate key found on line: " + lineNum + ", \"" + key + "\"");
          return null;
        }
        mapping.put(key, value);

      }

      return new CustomColorScheme(schemeName, schemeType, mapping, file.getAbsolutePath());
    } catch (FileNotFoundException e) {
      System.err.println("Couldn't find file");
      return null;
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

  public static boolean saveToFile(String path, CustomColorScheme scheme) {
    String schemeType = scheme.getLabel();
    File file = new File(path);
    Map<String, Color> colorMap = scheme.getMapping();
    final String lineSep = System.getProperty("line.separator");
    try {
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      writer.write(schemeType);
      writer.write(lineSep);
      writer.write(color2str(colorMap.get(kDefaultLabel)));
      writer.write(lineSep);
      for (Entry<String, Color> kv: colorMap.entrySet()) {
        if (kv.getKey().equals(kDefaultLabel)) {
          continue;
        }
        writer.write("\"" + kv.getKey() + "\" = " + color2str(kv.getValue()));
        writer.write(lineSep);
      }
      writer.close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }


}