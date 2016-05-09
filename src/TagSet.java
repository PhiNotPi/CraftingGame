import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TagSet {

  private Map<String, Tag> map;
  private Set<Tag> tags;

  TagSet() {
    this.map = new HashMap<String, Tag>();
    this.tags = new HashSet<Tag>();
  }

  public boolean add(Tag t) {
    if (t == null) {
      return false;
    }
    String field = t.fieldName();
    Tag prev = map.get(field);
    if (prev != null) {
      tags.remove(prev);
    }
    map.put(field, t);
    tags.add(t);
    return true;
  }

  public boolean add(Tag... tags) {
    boolean res = true;
    for (Tag t : tags) {
      res &= add(t);
    }
    return res;
  }

  public void print() {
    for (Tag t : tags) {
      System.out.print(t);
    }
    System.out.println();
  }

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
