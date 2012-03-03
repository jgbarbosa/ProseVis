package prosevis.data;

public enum POSType {
  NONE,
  PERIOD,
  COMMA,
  COLON,
  OPEN_QUOTES,
  CLOSE_QUOTES,
  OTHER,
  CC,
  CD,
  DT,
  EX,
  FW,
  IN,
  JJ,
  JJR,
  JJS,
  MD,
  NN,
  NNP,
  NNPS,
  NNS,
  PDT,
  PRP,
  PRP$,
  RB,
  RBR,
  RBS,
  RP,
  TO,
  UH,
  VB,
  VBD,
  VBG,
  VBN,
  VBP,
  VBZ,
  WDT,
  WP,
  WP$,
  WRB,
  _LRB_, //-LRB-
  _RRB_; //-RRB-

  public static POSType fromString(String rawStr) {
    rawStr = rawStr.toUpperCase();
    if (".".equals(rawStr)) {
      return PERIOD;
    } else if (",".equals(rawStr)) {
      return COMMA;
    } else if (":".equals(rawStr)) {
      return COLON;
    } else if ("``".equals(rawStr)) {
      return OPEN_QUOTES;
    } else if ("''".equals(rawStr)) {
      return CLOSE_QUOTES;
    } else if ("-LRB-".equals(rawStr)) {
      return _LRB_;
    } else if ("-RRB-".equals(rawStr)) {
      return _RRB_;
    } else {
      try {
        return POSType.valueOf(rawStr);
      } catch (Exception e) {
        System.err.println("Found unknown POS type: " + rawStr);
        return OTHER;
      }
    }
  }
};
