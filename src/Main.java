import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
    Ingredient ing = new Ingredient();
    ing.pro = TagData.randomTS();
    System.out.println(ing);
    TagSet ts = TagData.randomTS();
    while (!ing.matches(ts)) {
      System.out.println();
      ts.print();
      ts.printAll();
      System.out.println(ing.matches(ts));
      ts = TagData.randomTS();
    }
    System.out.println();
    ts.print();
    ts.printAll();
    System.out.println(ing.matches(ts));

    // Ingredient ing2 = new Ingredient();
    // ing2.pro.add(new CategTag("stone"));
    // TagSet ts2 = new TagSet();
    // ts2.add(new CategTag("granite"));
    // System.out.println(ing2.matches(ts2));

  }

}
