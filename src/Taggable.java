import java.util.HashSet;
import java.util.Set;


public class Taggable {

  private Set<Tag> tags;
  public Taggable(){
    this.tags = new HashSet<Tag>();
  }
  
  @Override
  public String toString() {
    return "Taggable [tags=" + tags + "]";
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }

  public boolean add(Tag tnew){
    for(Tag t : tags){
      if(t.isa(tnew) && !tnew.isa(t)){
        return false; // tag already covered
      }
    }
    Set<Tag> after = new HashSet<Tag>(); // unsure if needed
    for(Tag t : tags){
      if(! tnew.isa(t) ){
        after.add(t);
      }
    }
    after.add(tnew);
    tags = after;
    return true;
  }
  public void remove(Tag targ){
    Set<Tag> after = new HashSet<Tag>();
    for(Tag t : tags){
      if(! t.isa(targ)){
        after.add(t);
      }
    }
    tags = after;
  }

}
