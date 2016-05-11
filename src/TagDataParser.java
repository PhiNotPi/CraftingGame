import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TagDataParser {

  static ArrayList<String> tokens = new ArrayList<String>();
  // characters that are their own token
  static final String singletons = ",;{}";
  // separators, repetition is ignored
  static final String seps = " :";
  // keywords that indicate a node
  static final ArrayList<String> types = new ArrayList<String>();
  static {
    types.add("str");
    types.add("real");
    types.add("bool");
    types.add("cat");
    types.add("opt");
  }

  // splits text into tokens, appended to list
  public static void tokenize(String input) {
    char[] inchars = input.toCharArray();
    // accumulator of characters for the current token
    String curtoken = "";
    // whether currently in quotes
    boolean quote = false;
    for (int i = 0; i < inchars.length; i++) {
      String curchar = Character.toString(inchars[i]);
      if (quote) {
        if (curchar.equals("\"")) {
          // exiting quotes
          tokens.add(curtoken);
          curtoken = "";
          quote = false;
        } else {
          curtoken += curchar;
        }
      } else if (curchar.equals("#")) {
        // assumes tokenize is called seperately on each line
        break;
      } else if (curchar.equals("\"")) {
        // entering quotes
        quote = true;
      } else if (singletons.contains(curchar)) {
        // character is its own token
        if (curtoken.length() > 0) {
          tokens.add(curtoken);
          curtoken = "";
        }
        tokens.add(curchar);
      } else if (seps.contains(curchar) || curchar.equals("\n")) {
        // separators
        if (curtoken.length() > 0) {
          tokens.add(curtoken);
          curtoken = "";
        }
      } else {
        // accumulate
        curtoken += curchar;
      }
    }
    if (curtoken.length() > 0) {
      // end of string reached, whatever's left is a token
      tokens.add(curtoken);
      curtoken = "";
    }
  }

  // map of name to node
  private static Map<String, Node> map = new HashMap<String, Node>();

  private static class Node {
    String type;
    String name;
    Node parent;
    Set<Node> children;
    Set<ArrayList<String>> defs;

    Node(String type, String name, Node parent) {
      this.type = type;
      this.name = name;
      this.parent = parent;
      this.children = new HashSet<Node>();
      this.defs = new HashSet<ArrayList<String>>();
    }
  }

  private static final Node root = new Node(null, null, null);

  public static void parse() {
    parse(root);
  }

  // "cur" is the node we are currently inside of.
  private static void parse(Node cur) {
    while (tokens.size() > 0) {
      // childType normally contains a type, but sometimes other delimiters
      String childType = tokens.remove(0).toLowerCase();
      if (childType.equals("}")) {
        // exiting a level
        return;
      } else if (types.contains(childType)) {
        String open;
        // do-while loop so commas can delimit names of the same type
        do {
          String childName = tokens.remove(0).toLowerCase();
          Node child = new Node(childType, childName, cur);
          map.put(childName, child);
          cur.children.add(child);
          if (tokens.size() == 0) {
            return;
          }
          open = tokens.get(0);
          if (open.equals("{") || open.equals(";") || open.equals(",")) {
            tokens.remove(0);
            if (open.equals("{")) {
              parse(child); // entering child
              if (tokens.size() > 0 && tokens.get(0).equals(",")) {
                open = tokens.remove(0);
              }
            }
          }
          // if "open" is some other word, then a delimiter isn't needed
        } while (open.equals(","));
      } else if (childType.equals("true") || childType.equals("false")) {
        // special nodes with no specific name
        Node child = new Node(childType, null, cur);
        cur.children.add(child);
        String open = tokens.remove(0);
        if (open.equals("{")) {
          parse(child);
        }
      } else {
        // a default tag value
        ArrayList<String> defTag = new ArrayList<String>();
        defTag.add(childType);
        while (!tokens.get(0).equals(";") && !tokens.get(0).equals("}")) {
          defTag.add(tokens.remove(0));
        }
        if (tokens.get(0).equals(";")) {
          tokens.remove(0);
        }
        cur.defs.add(defTag);
      }
    }
  }

  public static void print() {
    print(root, 0);
  }

  public static void print(Node cur, int depth) {
    System.out.println(indent(depth) + cur.type + " " + cur.name);
    for (ArrayList<String> tdat : cur.defs) {
      System.out.println(indent(depth + 1) + buildTag(tdat));
    }
    for (Node child : cur.children) {
      print(child, depth + 1);
    }
  }

  // for indentation during printing
  public static String indent(int depth) {
    String res = "";
    for (int i = 0; i < depth; i++) {
      res += "  ";
    }
    return res;
  }

  public static void build() {
    for (Node n : root.children) {
      build(n);
    }
    for (Node n : root.children) {
      genDefs(n);
    }
  }

  public static void build(Node cur) {
    if (cur.type.equals("cat")) {
      TagData.putCategField(cur.parent.name, cur.name);
    } else if (cur.type.equals("real")) {
      TagData.putRealField(cur.parent.name, cur.name);
    } else if (cur.type.equals("str")) {
      TagData.putStringField(cur.parent.name, cur.name);
    } else if (cur.type.equals("bool")) {
      TagData.putBoolField(cur.parent.name, cur.name);
    } else if (cur.type.equals("opt")) {
      TagData.putCategOption(cur.parent.name, cur.name);
    }
    for (Node n : cur.children) {
      build(n);
    }
  }

  // puts default tag values
  public static void genDefs(Node cur) {
    if (cur.type.equals("opt")) {
      for (ArrayList<String> def : cur.defs) {
        Tag defTag = buildTag(def);
        if (defTag != null) {
          TagData.putCategDefault(cur.name, defTag);
        }
      }
    } else if (cur.type.equals("true")) {
      for (ArrayList<String> def : cur.defs) {
        Tag defTag = buildTag(def);
        if (defTag != null) {
          TagData.putBoolDefault(cur.parent.name, true, defTag);
        }
      }
    } else if (cur.type.equals("false")) {
      for (ArrayList<String> def : cur.defs) {
        Tag defTag = buildTag(def);
        if (defTag != null) {
          TagData.putBoolDefault(cur.parent.name, false, defTag);
        }
      }
    }
    for (Node n : cur.children) {
      genDefs(n);
    }
  }

  public static Tag buildTag(ArrayList<String> tdata) {
    if (tdata == null || tdata.size() == 0) {
      return null;
    }
    String title = tdata.get(0).toLowerCase();
    Node node = map.get(title);
    if (node == null) {
      return null;
    }
    String fieldType = node.type;
    if (fieldType.equals("opt")) {
      if (tdata.size() == 1) {
        return new CategTag(title);
      }
    } else if (fieldType.equals("real")) {
      try {
        Double data = Double.parseDouble(tdata.get(1));
        if (tdata.size() == 2) {
          return new RealTag(title, data);
        } else if (tdata.size() == 3) {
          Double data2 = Double.parseDouble(tdata.get(2));
          return new RealTag(title, data, data2);
        }
      } catch (Exception e) {
      }
    } else if (tdata.size() == 2) {
      String data = tdata.get(1);
      if (fieldType.equals("str")) {
        return new StringTag(title, data);
      } else if (fieldType.equals("bool")) {
        if (data.toLowerCase().equals("true")) {
          return new BoolTag(title, true);
        } else if (data.toLowerCase().equals("false")) {
          return new BoolTag(title, false);
        }
      } else if (fieldType.equals("cat")) {
        Tag res = new CategTag(data);
        if (res.fieldName.equals(title)) {
          return res;
        }
      }
    }
    return null;
  }

  public static String tagFormat(Tag tag) {
    if (tag instanceof CategTag) {
      return ((CategTag) tag).name + ";";
    } else if (tag instanceof BoolTag) {
      return tag.fieldName + " " + ((BoolTag) tag).val + ";";
    } else if (tag instanceof StringTag) {
      return tag.fieldName + " \"" + ((StringTag) tag).name + "\";";
    } else if (tag instanceof RealTag) {
      RealTag t = (RealTag) tag;
      if (t.min.equals(t.max)) {
        return tag.fieldName + " " + t.min + ";";
      } else {
        return tag.fieldName + " " + t.min + " " + t.max + ";";
      }
    }
    return "error";
  }

}
