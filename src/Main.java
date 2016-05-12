import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {

  public static void main(String[] args) throws FileNotFoundException {
    Scanner in = new Scanner(new File("tags.dat"));
    while (in.hasNextLine()) {
      TagDataParser.tokenize(in.nextLine());
    }
    in.close();
    TagDataParser.parse();
    TagDataParser.build();
    // TagDataParser.print();
    // System.out.println("Tag data:");
    // TagData.setImplicit("stone", new CategTag("sandstone"));
    // TagData.setImplicit("flammable", new BoolTag("flammable",false));
    // TagData.setImplicit("name", new StringTag("name","whatever"));
    TagData.printAll();
    // System.out.println(new CategTag("granite").isa(new CategTag("stone")));
    Recipe rec = new Recipe();
    for (int i = 0; i < 3; i++) {
      Ingredient ing = new Ingredient();
      ing.pro = TagData.randomTS();
      rec.ingredients.add(ing);
    }
    // ing = new Ingredient();
    // ing.pro = TagData.randomTS();
    // rec.ingredients.add(ing);
    // ing = new Ingredient();
    // ing.pro = TagData.randomTS();
    // rec.ingredients.add(ing);

    System.out.println(rec);
    TagSet ts = TagData.randomTS();
    Set<TagSet> accum = new HashSet<TagSet>();
    // accum.add(ing.pro);
    while (Recipe.match(rec.ingredients, accum).size() != rec.ingredients
        .size()) {
      // System.out.println(Recipe.match(rec.ingredients, accum));
      ts = TagData.randomTS();
      accum.add(ts);
      // System.out.println();
      // ts.print();
    }
    System.out.println();
    Map<TagSet, Ingredient> res = Recipe.match(rec.ingredients, accum);
    for (TagSet react : res.keySet()) {
      System.out.println(react);
      System.out.println("  " + res.get(react));
    }

    // Ingredient ing2 = new Ingredient();
    // ing2.pro.add(new CategTag("stone"));
    // TagSet ts2 = new TagSet();
    // ts2.add(new CategTag("granite"));
    // System.out.println(ing2.matches(ts2));

  }

}
