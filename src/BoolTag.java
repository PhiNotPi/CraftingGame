public class BoolTag extends Tag {
  Boolean val;

  public BoolTag(String fieldName, boolean val) {
    this.fieldName = fieldName;
    this.val = val;
  }

  public String toString() {
    return "[" + fieldName + ":" + val + "]";
  }

  @Override
  public boolean isa(Tag tag) {
    return this.equals(tag);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof BoolTag)) {
      return false;
    }
    BoolTag other = (BoolTag) obj;
    if (this.fieldName.equals(other.fieldName) && this.val == other.val) {
      return true;
    }
    return false;
  }
}
