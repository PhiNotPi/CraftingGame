import java.util.HashSet;
import java.util.Set;

public class Ingredient {

  TagSet pro;
  TagSet con;

  public Ingredient() {
    this.pro = new TagSet();
    this.con = new TagSet();
  }

  public Ingredient(TagSet pro) {
    this.pro = pro;
    this.con = new TagSet();
  }

  public Ingredient(TagSet pro, TagSet con) {
    this.pro = pro;
    this.con = con;
  }

  public boolean matches(TagSet tl) {
    Set<Tag> mpro = new HashSet<Tag>();
    for (Tag t : tl.getTags()) {
      Set<Tag> tmpro = new HashSet<Tag>();
      for (Tag pt : pro.getTags()) {
        if (t.isa(pt)) {
          tmpro.add(pt);
          mpro.add(pt);
        }
      }
      for (Tag ct : con.getTags()) {
        if (t.isa(ct)) {
          boolean ovrrde = false;
          for (Tag pt : tmpro) {
            if (pt.isa(ct)) {
              // a more specific pro overrides a less specific con
              ovrrde = true;
            }
          }
          if (!ovrrde) {
            return false;
          }
        }
      }
    }
    if (mpro.containsAll(pro.getTags())) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Ingredient [pro=" + pro + ", con=" + con + "]";
  }

}
