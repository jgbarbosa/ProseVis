package prosevis.processing.model.color;

import java.util.ArrayList;
import java.util.List;

import prosevis.data.TypeMap;

public class ColorSchemeDB {
  public static final String kNoneSchemeName = "None";
  public static final String kRandomWordsName = "Random Words";
  public static final String kRandomPOSName = "Random POS";
  public static final String kRandomSoundName = "Random Whole Sound";
  public static final String kRandomInitialSoundName = "Random Initial Sound";
  public static final String kRandomVowelSoundName = "Random Vowel Sound";
  public static final String kRandomFinalSoundName = "Random Final Sound";
  public static final String kRandomSoundexName = "Random Soundex";
  public static final String kRandomComparision = "Random Comparison";
  
  
  private boolean nameIsValid(String name) {
    if (name == null) {
      return false;
    }
    for (ColorScheme bs: builtInSchemes) {
      String rn = bs.getName();
      if (rn.equals(name)) {
        return false;
      }
    }
    if (ColorSchemeUtil.kWorkingLabel.equals(name)) {
      return false;
    }
    return true;
  }

  private final ArrayList<BuiltInColorScheme> builtInSchemes = new ArrayList<BuiltInColorScheme>();
  private final ArrayList<ColorScheme> customColorSchemes = new ArrayList<ColorScheme>();
  private final TypeMap typeMap = new TypeMap();
  private ColorScheme selectedScheme;
  private WorkingColorScheme workingColorScheme;
  
  public ColorSchemeDB() {
    BuiltInColorScheme noneScheme = new BuiltInColorScheme(kNoneSchemeName, TypeMap.kNoLabelLabel);
    BuiltInColorScheme wordScheme = new BuiltInColorScheme(kRandomWordsName, TypeMap.kWordLabel);
    BuiltInColorScheme posScheme = new BuiltInColorScheme(kRandomPOSName, TypeMap.kPosLabel);
    BuiltInColorScheme soundAllScheme = new BuiltInColorScheme(kRandomSoundName, TypeMap.kPhonemeAllLabel);
    BuiltInColorScheme soundInitialScheme = new BuiltInColorScheme(kRandomInitialSoundName, TypeMap.kPhonemeStartLabel);
    BuiltInColorScheme soundVowelScheme = new BuiltInColorScheme(kRandomVowelSoundName, TypeMap.kPhonemeVowelLabel);
    BuiltInColorScheme soundFinalScheme = new BuiltInColorScheme(kRandomFinalSoundName, TypeMap.kPhonemeFinalLabel);
    BuiltInColorScheme soundexScheme = new BuiltInColorScheme(kRandomSoundexName, TypeMap.kSoundexLabel);
    BuiltInColorScheme comparisonScheme = new BuiltInColorScheme(kRandomComparision, TypeMap.kColorByComparison);
    
    builtInSchemes.add(noneScheme);
    builtInSchemes.add(wordScheme);
    builtInSchemes.add(posScheme);
    builtInSchemes.add(soundAllScheme);
    builtInSchemes.add(soundInitialScheme);
    builtInSchemes.add(soundVowelScheme);
    builtInSchemes.add(soundFinalScheme);
    builtInSchemes.add(soundexScheme);
    builtInSchemes.add(comparisonScheme);
    selectedScheme = noneScheme;
  }
  
  public boolean addColorScheme(ColorScheme scheme) {
    if (!nameIsValid(scheme.getName())) {
      return false;
    }
    for (ColorScheme s: customColorSchemes) {
      if (s.getName().equals(scheme.getName())) {
        // can't add scheme with duplicate name
        return false;
      }
    }
    
    customColorSchemes.add(scheme);
    return true;
  }
  
  public void removeColorScheme(String name) {
    if (!nameIsValid(name)) {
      return;
    }
    int idx = 0;
    while (idx < customColorSchemes.size()) {
      ColorScheme s = customColorSchemes.get(idx);
      if (s.getName().equals(name)) {
        if (s == selectedScheme) {
          selectedScheme = customColorSchemes.get(0);
        }
        customColorSchemes.remove(idx);
      } else {
        idx++;
      }
    }
  }

  public ColorScheme selectColorScheme(String name) {
    
    if (workingColorScheme != null && workingColorScheme.getName().equals(name)) {
      selectedScheme = workingColorScheme;
    }
    for (ColorScheme scheme: customColorSchemes) {
      if (scheme.getName().equals(name)) {
        selectedScheme = scheme;
      }
    }
    for (ColorScheme scheme: builtInSchemes) {
      if (scheme.getName().equals(name)) {
        selectedScheme = scheme;
      }
    }
    
    return selectedScheme;
  }

  public TypeMap getTypeMapCopy() {
    return new TypeMap(typeMap);
  }

  public void mergeTypeMap(TypeMap typeMap) {
    boolean changed = this.typeMap.mergeTypeMap(typeMap);
    if (changed) {
      refreshRandomColors();
    }
  }

  public ColorScheme getSelectedColorScheme() {
    return selectedScheme;
  }
  
  public int maybeGetTypeIdx(int labelIdx, String type) {
    return typeMap.maybeGetTypeIdx(labelIdx, type);
  }

  public Integer getLabelIdx(String label) {
    return typeMap.getLabelIdx(label);
  }

  public String[] getComparisonHeaders() {
    return typeMap.getComparisonDataHeaders();
  }

  public boolean hasComparisonData() {
    return typeMap.hasComparisonDataHeaders();
  }

  public void clearComparisonData() {
    this.typeMap.clearComparisonData();
  }

  public ArrayList<String> getNamesOfCustomSchemes() {
    ArrayList<String> ret = new ArrayList<String>(customColorSchemes.size());
    for (ColorScheme s: customColorSchemes) {
      ret.add(s.getName());
    }
    return ret;
  }

  public ColorScheme getComparisonColors() {
    for (ColorScheme s: customColorSchemes) {
      if (s.getLabel().equals(TypeMap.kColorByComparison)) {
        return s;
      }
    }
    for (ColorScheme s: builtInSchemes) {
      if (s.getLabel().equals(TypeMap.kColorByComparison)) {
        return s;
      }
    }
    System.err.println("Bad things about to happen, couldn't find comparison colors");
    return null;
  }

  private void refreshRandomColors() {
    for (BuiltInColorScheme bs: builtInSchemes) {
      bs.refresh(typeMap);
    }
  }

  public List<String> getNamesOfBuiltInSchemes() {
    ArrayList<String> ret = new ArrayList<String>(builtInSchemes.size());
    for (ColorScheme s: builtInSchemes) {
      ret.add(s.getName());
    }
    if (workingColorScheme != null) {
      ret.add(workingColorScheme.getName());
    }
    return ret;
  }

  public void registerWorkingColorScheme(WorkingColorScheme colorScheme) {
    this.workingColorScheme = colorScheme;
  }
}
