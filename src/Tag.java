
public class Tag {

  public static void main(String[] args) {
    Taggable t = new Taggable();
    t.add(new Tag("Oak"));
    t.add(new Tag("Metal"));
    t.add(new Tag("Wood"));
    System.out.println(t);
    Ingredient i = new Ingredient();
    i.pro.add(new Tag("Wood"));
    i.con.add(new Tag("Material"));
    System.out.println(i);
    System.out.println("  match? " + i.matches(t));
    i = new Ingredient();
    i.pro.add(new Tag("Metal"));
    i.pro.add(new Tag("Oak"));
    i.con.add(new Tag("Wood"));
    System.out.println(i);
    System.out.println("  match? " + i.matches(t));
  }
  
  String name;
  Double val;
  public Tag (String name){
    this.name = name;
    this.val = null;
  }
  public Tag (String name, double val){
    this.name = name;
    this.val = val;
  }
  
  
  @Override
  public String toString() {
    if(val == null){
      return "Tag [name=" + name + "]";
    }
    return "Tag [name=" + name + ", val=" + val + "]";
  }

  private static InheritanceTree<String> inher;
  static {
    inher = new InheritanceTree<String>();
    inher.put("Wood", "Material");
    inher.put("Metal", "Material");
    inher.put("Oak", "Wood");
    inher.print();
  }
  static boolean isa(String data, String pardata){
    return inher.isa(data, pardata);
  }
  boolean isa(String pardata){
    return isa(this.name, pardata);
  }
  boolean isa(Tag par){
    return isa(this.name, par.name);
  }

}
