import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) throws FileNotFoundException {
    Scanner in = new Scanner(new File("tags.dat"));
    while(in.hasNextLine()){
      TagDataParser.tokenize(in.nextLine());
    }
    in.close();
    TagDataParser.parse();
    TagDataParser.build();
    TagData.printAll();
  }

}
