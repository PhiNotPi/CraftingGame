public class Tag {

  // name, which is used mainly if this is a String tag
  // otherwise, it is set to the FieldName
  String name;
  // used only if a BoolField
  Boolean bool;
  // tags like [Density:1.4] have min and max set to the same value
  // this allows the use of RealFields in recipes
  Double min;
  Double max;

  public Tag(String name) {
    this.name = name;
  }

  public Tag(String name, boolean bool) {
    this.name = name;
    this.bool = bool;
  }

  public Tag(String name, double val) {
    this.name = name;
    this.min = val;
    this.max = val;
  }

  public Tag(String name, double min, double max) {
    this.name = name;
    this.min = min;
    this.max = max;
  }

  public String fieldName() {
    return TagData.getFieldName(name);
  }

  public String toString() {
    if (this.min != null) {
      if (max.equals(min)) {
        return "[" + name + ":" + min + "]";
      }
      return "[" + name + ":" + min + "," + max + "]";
    }
    if (this.bool != null) {
      return "[" + name + ":" + bool + "]";
    }
    return "[" + fieldName() + ":" + name + "]";
  }

}
