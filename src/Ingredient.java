import java.util.ArrayList;


public class Ingredient {

  public ArrayList<Tag> pro;
  public ArrayList<Tag> con;
  public Ingredient() {
    super();
    this.pro = new ArrayList<Tag>();
    this.con = new ArrayList<Tag>();
  }
  public boolean matches(Taggable tl){
    ArrayList<Tag> mpro = new ArrayList<Tag>();
    for(Tag t : tl.getTags()){
      ArrayList<Tag> tmpro = new ArrayList<Tag>();
      for(Tag pt : pro){
        if(t.isa(pt)){
          tmpro.add(pt);
          mpro.add(pt);
        }
      }
      for(Tag ct : con){
        if(t.isa(ct)){
          boolean ovrrde = false;
          for(Tag pt : tmpro){
            if(pt.isa(ct)){
              ovrrde = true; // if it matches a more specific tag, then it's okay
            }
          }
          if(!ovrrde){
            return false;
          }
        }
      }
    }
    if(mpro.containsAll(pro)){
      return true;
    }
    return false;
  }
  @Override
  public String toString() {
    return "Ingredient [pro=" + pro + ", con=" + con + "]";
  }

}
