public abstract class Tag {
  String fieldName;

  public abstract String toString();

  public boolean isValid() {
    return TagData.isValid(this);
  }
}
