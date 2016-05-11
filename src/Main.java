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
    System.out.println("Tag data:");
    TagData.printAll();

    for (int i = 0; i < 10; i++) {
      System.out.println();
      TagSet ts = TagData.randomTS();
      ts.print();
      ts.printAll();
    }
  }

}
