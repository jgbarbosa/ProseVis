package prosevis.data;

// for searching for nodes via binary search
public class HierNodeWrapper {
  public HierNodeWrapper() {
    this.result = null;
    this.resultType = ResultType.TOO_SMALL;
  }
  public enum ResultType {
    TOO_SMALL,
    TOO_BIG,
    VALID
  }
  
  public ResultType resultType;
  public HierNode result;
}
