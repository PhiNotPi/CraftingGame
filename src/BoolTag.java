public class BoolTag extends Tag {
  Boolean val;

  public BoolTag(String fieldName, boolean val) {
    this.fieldName = fieldName;
    this.val = val;
  }

  public String toString() {
    return "[" + fieldName + ":" + val + "]";
  }
}
