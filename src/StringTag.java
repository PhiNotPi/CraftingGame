public class StringTag extends Tag {
  String name;

  public StringTag(String fieldName, String name) {
    this.fieldName = fieldName;
    this.name = name;
  }

  public String toString() {
    return "[" + fieldName + ":" + name + "]";
  }

  @Override
  public boolean isa(Tag tag) {
    return this.equals(tag);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StringTag)) {
      return false;
    }
    StringTag other = (StringTag) obj;
    if (this.fieldName.equals(other.fieldName) && this.name == other.name) {
      return true;
    }
    return false;
  }
}
