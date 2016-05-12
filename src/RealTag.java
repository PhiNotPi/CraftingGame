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

  @Override
  public boolean isa(Tag tag) {
    if (tag == null || !(tag instanceof RealTag)) {
      return false;
    }
    RealTag other = (RealTag) tag;
    if (this.fieldName.equals(other.fieldName) && this.min >= other.min
        && this.max <= other.max) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RealTag)) {
      return false;
    }
    RealTag other = (RealTag) obj;
    if (this.fieldName.equals(other.fieldName) && this.min.equals(other.min)
        && this.max.equals(other.max)) {
      return true;
    }
    return false;
  }
}
