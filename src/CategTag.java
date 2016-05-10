public class CategTag extends Tag {
  String name;

  public CategTag(String name) {
    this.name = name;
    this.fieldName = TagData.getFieldName(name);
  }

  public String toString() {
    return "[" + fieldName + ":" + name + "]";
  }
}
