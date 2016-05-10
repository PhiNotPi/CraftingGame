public class RealTag extends Tag {
  Double min;
  Double max;

  public RealTag(String fieldName, double val) {
    this.fieldName = fieldName;
    this.min = val;
    this.max = val;
  }

  public RealTag(String fieldName, double min, double max) {
    this.fieldName = fieldName;
    this.min = min;
    this.max = max;
  }

  public String toString() {
    if (max.equals(min)) {
      return "[" + fieldName + ":" + min + "]";
    }
    return "[" + fieldName + ":" + min + "," + max + "]";
  }
}
