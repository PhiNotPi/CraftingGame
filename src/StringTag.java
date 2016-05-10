public class StringTag extends Tag {
  String name;

  public StringTag(String fieldName, String name) {
    this.fieldName = fieldName;
    this.name = name;
  }

  public String toString() {
    return "[" + fieldName + ":" + name + "]";
  }
}
