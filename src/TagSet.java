import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TagSet {

  // the map is FieldName to a Tag with that FieldName, used for easy lookup
  private Map<String, Tag> map;
  // set of explicitly assigned Tags
  private Set<Tag> tags;

  TagSet() {
    this.map = new HashMap<String, Tag>();
    this.tags = new HashSet<Tag>();
  }

  // Add a Tag and update the Map
  public boolean add(Tag t) {
    if (t == null || !t.isValid()) {
      return false;
    }
    String field = t.fieldName;
    Tag prev = map.get(field);
    if (prev != null) {
      tags.remove(prev);
    }
    map.put(field, t);
    tags.add(t);
    return true;
  }

  // for my convenience
  public boolean add(Tag... tags) {
    boolean res = true;
    for (Tag t : tags) {
      res &= add(t);
    }
    return res;
  }

  // print only the Tags explicitly assigned, nothing is implied
  public void print() {
    for (Tag t : tags) {
      System.out.print(t);
    }
    System.out.println();
  }

  // print the value of every possible property
  public void printAll() {
    for (String key : TagData.getAllFields()) {
      Tag t = getTag(key);
      if (t == null) {
        System.out.print("[" + key + ":null]");
      } else {
        System.out.print(t);
      }
    }
    System.out.println();
  }

  // determine the Tag associated with a given Field, using recursive calls to
  // tag defaults if needed
  public Tag getTag(String field) {
    if (field == null) {
      return null;
    }
    if (map.get(field) != null) {
      return map.get(field);
    }
    String par = TagData.getParentFieldName(field);
    return TagData.getDefault(getTag(par), field);
  }

}
