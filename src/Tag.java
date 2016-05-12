public abstract class Tag {
  String fieldName;

  public abstract String toString();

  public abstract boolean isa(Tag tag);

  public abstract boolean equals(Object obj);

  public boolean isValid() {
    return TagData.isValid(this);
  }
}
