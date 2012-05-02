package prosevis.data;

public abstract class ParsingTools {
  public static String[] vowelSounds = { "I", "E", "{", "A", "V", "U", "i",
    "e", "u", "AI", "o", "O", "aI", "OI", "aU", "r=", "@", "@U", "EI" };


  // Given the phoneme field in a line, give back the individual phoneme parts
  public static String[] parsePhoneme(String phoneme) {
    String[] elements = phoneme.split(" ");
    boolean vowel = false;
    int vowelIndex = -1;
    String[] breakDown = { "", "", "" };

    for (int i = 0; i < elements.length; i++) {
      for (int j = 0; j < vowelSounds.length; j++) {
        if (elements[i].equals(vowelSounds[j])) {
          vowel = true;
          breakDown[1] = elements[i];
          vowelIndex = i;
          break;
        }
      }

      if (vowel) {
        break;
      }
    }

    if (vowelIndex != 0) {
      breakDown[0] = elements[0];
    }

    if (vowelIndex != elements.length - 1 && elements.length > 1) {
      breakDown[2] = elements[elements.length - 1];
    }

    if (phoneme.equals("s trike")) {
      for (int i = 0; i < 3; i++)
        System.out.println("breakdwn " + breakDown[i]);
    }
    return breakDown;
  }

  public static boolean notPunct(String test) {
    String word = test.trim().toLowerCase();

    if (word.length() < 4) {
      if (word.endsWith(".") || word.endsWith(",") || word.endsWith("\"")
          || word.endsWith("!") || word.endsWith(";") || word.endsWith(":")
          || word.endsWith("?") || word.endsWith("\'") || word.endsWith(")")
          || word.endsWith("-") || word.endsWith("(")) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  public static String soundex(String s) {
    char[] x = s.toUpperCase().toCharArray();
    char firstLetter = x[0];

    // convert letters to numeric code
    for (int i = 0; i < x.length; i++) {
      switch (x[i]) {
      case 'B':
      case 'F':
      case 'P':
      case 'V': {
        x[i] = '1';
        break;
      }

      case 'C':
      case 'G':
      case 'J':
      case 'K':
      case 'Q':
      case 'S':
      case 'X':
      case 'Z': {
        x[i] = '2';
        break;
      }

      case 'D':
      case 'T': {
        x[i] = '3';
        break;
      }

      case 'L': {
        x[i] = '4';
        break;
      }

      case 'M':
      case 'N': {
        x[i] = '5';
        break;
      }

      case 'R': {
        x[i] = '6';
        break;
      }

      default: {
        x[i] = '0';
        break;
      }
      }
    }

    // remove duplicates
    String output = "" + firstLetter;
    for (int i = 1; i < x.length; i++)
      if (x[i] != x[i - 1] && x[i] != '0')
        output += x[i];

    // pad with 0's or truncate
    output = output + "0000";
    return output.substring(0, 4);
  }


}
