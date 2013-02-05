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
  /*
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
     */
    public final static Color[] goodColors = new Color[]{
        new Color(141, 211, 199),
        new Color(255, 255, 179),
        new Color(190, 186, 218),
        new Color(251, 128, 114),
        new Color(128, 177, 211),
        new Color(253, 180, 98),
        new Color(179, 222, 105),
        new Color(252, 205, 229),
        new Color(217, 217, 217),
        new Color(188, 128, 189),
        new Color(204, 235, 197),
        new Color(255, 237, 111),
        new Color(246, 150, 121), //< --
        new Color(244, 154, 193),

        new Color(161, 134, 190),

        new Color(131, 147, 202),

        new Color(122, 204, 200),
        
        
        
        new Color(130, 202, 156),

        new Color(196, 223, 155),
        new Color(125, 167, 217),
        new Color(255, 247, 153),
        new Color(253, 198, 137),
        new Color(135, 129, 189),
        new Color(249, 173, 129),
        new Color(245, 152, 157),
        new Color(189, 140, 191),
        new Color(109, 207, 246),
        new Color(163, 211, 156),
        new Color(242, 108, 79), //<--
        new Color(168, 100, 168),
        new Color(133, 96, 168),
        new Color(96, 92, 168),
        new Color(86, 116, 185),
        new Color(68, 140, 203),
        new Color(28, 187, 180),
        new Color(060, 184, 120),
        new Color(124, 197, 118),
        new Color(172, 211, 115),
        new Color(255, 245, 104),
        new Color(251, 175, 93),
        new Color(246, 142, 86),
        new Color(240, 110, 170),// <--
        new Color(0, 191, 243),
        new Color(242, 109, 125),
        new Color(242, 101, 34),
        new Color(247, 148, 29),
        new Color(255, 242, 000),
        new Color(141, 198, 63),
        new Color(57, 181, 74),
        new Color(0, 166, 81),
        new Color(0, 169, 157),
        new Color(0, 174, 239),
        new Color(0, 114, 88),
        new Color(0, 84, 166),
        new Color(49, 49, 146),
        new Color(46, 49, 146),
        new Color(236, 0, 140),
        new Color(237, 20, 91),
        new Color(158, 11, 15),
        new Color(160, 65, 13),
        new Color(136, 98, 10),
        new Color(171, 160, 0),
        new Color(89, 133, 39),
        new Color(25, 123, 48),
        new Color(0, 114, 54),
        new Color(0, 116, 107),
        new Color(0, 118, 163),
        new Color(0, 74, 128),
        new Color(0, 52, 113),
        new Color(27, 20, 100),
        new Color(68, 14, 98),
        new Color(99, 4, 96),
        new Color(158, 0, 93),
        new Color(158, 0, 57),
        new Color(121, 0, 0),
        new Color(123, 46, 0),
        new Color(125, 73, 0),
        new Color(130, 123, 0),
        new Color(64, 102, 24),
        new Color(0, 94, 32),
        new Color(0, 88, 38),
        new Color(0, 89, 82),
        new Color(0, 91, 127),
        new Color(0, 54, 99),
        new Color(0, 33, 87),
        new Color(13, 0, 76),
        new Color(50, 0, 75),
        new Color(75, 0, 73),
        new Color(123, 0, 70),
        new Color(122, 70, 38),
        new Color(199, 178, 156),
        new Color(153, 134, 117),
        new Color(115, 99, 87),
        new Color(83, 71, 65),
        new Color(55, 48, 45),
        new Color(198, 156, 110),
        new Color(166, 124, 82),
        new Color(140, 98, 57),
        new Color(117, 76, 36),
        new Color(96, 57, 19)
    };

    public static CustomColorScheme loadFromFile(File file) throws InstantiationException {
        try {
            Scanner scanner = new Scanner(file);
            String schemeName = file.getName().substring(0, file.getName().lastIndexOf('.'));
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
        if (rs.length() < 2) {
            rs = "0" + rs;
        }
        String gs = Integer.toHexString(color.getGreen());
        gs = gs.substring(0, Math.min(2, gs.length()));
        if (gs.length() < 2) {
            gs = "0" + gs;
        }
        String bs = Integer.toHexString(color.getBlue());
        bs = bs.substring(0, Math.min(2, bs.length()));
        if (bs.length() < 2) {
            bs = "0" + bs;
        }
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
            for (Entry<String, Color> kv : colorMap.entrySet()) {
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