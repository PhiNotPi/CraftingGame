public class CategTag extends Tag {
  String name;

  public CategTag(String name) {
    this.name = name;
    this.fieldName = TagData.getFieldName(name);
  }

  public String toString() {
    return "[" + fieldName + ":" + name + "]";
  }

  @Override
  public boolean isa(Tag tag) {
    if (tag == null || !(tag instanceof CategTag)) {
      return false;
    }
    CategTag other = (CategTag) tag;
    if (this.fieldName.equals(other.fieldName)
        && (this.name.equals(other.name) || TagData.getAllOptions(other.name)
            .contains(name))) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof CategTag)) {
      return false;
    }
    CategTag other = (CategTag) obj;
    if (this.fieldName.equals(other.fieldName) && this.name.equals(other.name)) {
      return true;
    }
    return false;
  }
}
