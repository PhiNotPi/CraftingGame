import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TagData {
  // Each node has a unique name, and these maps
  // go from name->node. The different maps are
  // for storing only specific types of nodes, so
  // that less casting is needed.
  private static Map<String, Node> map = new HashMap<String, Node>();
  private static Map<String, CategField> CFmap = new HashMap<String, CategField>();
  private static Map<String, CategOption> COmap = new HashMap<String, CategOption>();
  private static Map<String, BoolField> BFmap = new HashMap<String, BoolField>();
  private static Map<String, RealField> RFmap = new HashMap<String, RealField>();
  private static Map<String, StringField> SFmap = new HashMap<String, StringField>();
  private static Map<String, Field> Fmap = new HashMap<String, Field>();
  private static Map<String, FieldWithDefs> FWDmap = new HashMap<String, FieldWithDefs>();
  // these are Fields without a parent field
  private static Set<Field> rootFields = new HashSet<Field>();

  private static abstract class Node {
    // name is unique
    String name;
    // field name, which differs from this.name only in the case of
    // CategOptions
    String fieldName;
    // where inheritance comes from
    Node parent;
  }

  // everything that can act as a "key"
  private static abstract class Field extends Node {
  }

  // this class contains subfields, and has default
  // values of the subfields depending on its current value.
  // FWDs must be categorical, like either CategField or BoolField
  private static abstract class FieldWithDefs extends Field {
    // given a tag value, and a desired subfield, return the default value of
    // the subfield
    abstract Tag getDefault(Tag value, String subfield);

    // subfields are fields that depend on this field
    // for example, an CF named "color" could be a subfield of a CF named
    // "material"
    Set<Field> subfields;
    // the code to create defaults is more specialized to each FWB type.
  }

  private static class CategField extends FieldWithDefs {
    Set<CategOption> rootOptions;
    Set<String> allOptions;
    Map<String, Map<String, Tag>> defaults;

    CategField(String name) {
      this.name = name;
      this.fieldName = name;
      this.parent = null;
      this.subfields = new HashSet<Field>();
      this.rootOptions = new HashSet<CategOption>();
      this.allOptions = new HashSet<String>();
      this.defaults = new HashMap<String, Map<String, Tag>>();
    }

    // this is slit up into several helper methods,
    // depending on what information is provided
    @Override
    Tag getDefault(Tag value, String subfield) {
      if (value == null || !(value instanceof CategTag)) {
        return null;
      }
      return getDefault(((CategTag) value).name, subfield);
    }

    Tag getDefault(String value, String subfield) {
      if (value == null) {
        return null;
      }
      return getDefault(COmap.get(value), subfield);
    }

    // the option is a selected option, like "wood",
    // and subfield is the selected subfield, like "color."
    // Of course, "wood" must be an option for this particular field
    Tag getDefault(CategOption option, String subfield) {
      if (option == null) {
        return null;
      }
      for (Node cur = option; cur != null && cur.fieldName == this.name; cur = cur.parent) {
        Map<String, Tag> defs = this.defaults.get(cur.name);
        if (defs != null && defs.get(subfield) != null) {
          return defs.get(subfield);
        }
      }
      return null;
    }

    // An example might be a Node named "wood" and a tag [color:brown]
    boolean setDefault(Node option, Tag val) {
      String subfield = val.fieldName;
      if (option == null || option.fieldName != this.name || subfield == null
          || Fmap.get(subfield) == null
          || !this.subfields.contains(Fmap.get(subfield))) {
        // it is necessary that the Node be an option for this field,
        // and that Tag be valid tag for an immediate subfield
        return false;
      }
      Map<String, Tag> defs = this.defaults.get(option.name);
      if (defs == null) {
        this.defaults.put(option.name, new HashMap<String, Tag>());
        defs = this.defaults.get(option.name);
      }
      defs.put(subfield, val);
      return true;
    }
  }

  private static class CategOption extends Node {
    // options that inherit from it, so "oak" might be a suboption of "wood"
    Set<CategOption> suboptions;

    CategOption(String name, String fieldName, Node parent) {
      this.name = name;
      this.fieldName = fieldName;
      this.suboptions = new HashSet<CategOption>();
      this.parent = parent;
    }
  }

  private static class BoolField extends FieldWithDefs {
    // We don't need a Map of Maps since there's only two options
    Map<String, Tag> truedefs;
    Map<String, Tag> falsedefs;

    BoolField(String name) {
      this.name = name;
      this.fieldName = name;
      this.subfields = new HashSet<Field>();
      this.truedefs = new HashMap<String, Tag>();
      this.falsedefs = new HashMap<String, Tag>();
    }

    @Override
    Tag getDefault(Tag value, String subfield) {
      if (value != null && value.fieldName == this.name
          && value instanceof BoolTag) {
        // input Tag must be a tag of this type
        // then, we return the value from the appropriate Map
        if (((BoolTag) value).val) {
          return truedefs.get(subfield);
        }
        return falsedefs.get(subfield);
      }
      return null;
    }

    boolean setDefault(boolean option, Tag val) {
      String subfield = val.fieldName;
      if (Fmap.get(subfield) == null
          || !this.subfields.contains(Fmap.get(subfield))) {
        // Tag ust be for an immediate subfield
        return false;
      }
      // add Tag to appropriate Map
      if (option) {
        this.truedefs.put(val.fieldName, val);
      } else {
        this.falsedefs.put(val.fieldName, val);
      }
      return true;
    }
  }

  private static class RealField extends Field {
    // can't hold subfields, and "infinite" options
    RealField(String name) {
      this.name = name;
      this.fieldName = name;
    }
  }

  private static class StringField extends Field {
    // also can't hold subfields, and also "infinite" options
    StringField(String name) {
      this.name = name;
      this.fieldName = name;
    }
  }

  // takes a Field and puts it into the "tree"
  private static boolean putField(String pardata, Field n) {
    if (pardata == null) {
      mapAdd(n); // register in static Maps
      rootFields.add(n);
      return true;
    }
    FieldWithDefs p = FWDmap.get(pardata);
    if (p == null) {
      // The parent must be a valid FieldWithDef
      return false;
    }
    // Add the field as subfield
    p.subfields.add(n);
    n.parent = p;
    mapAdd(n); // Register in static Maps
    return true;
  }

  // Registers the selected Node in all the relevant Maps
  // I've tried to contain all my casting to here
  private static void mapAdd(Node n) {
    map.put(n.name, n);
    if (n instanceof CategField) {
      CFmap.put(n.name, (CategField) n);
    }
    if (n instanceof CategOption) {
      COmap.put(n.name, (CategOption) n);
    }
    if (n instanceof BoolField) {
      BFmap.put(n.name, (BoolField) n);
    }
    if (n instanceof RealField) {
      RFmap.put(n.name, (RealField) n);
    }
    if (n instanceof StringField) {
      SFmap.put(n.name, (StringField) n);
    }
    if (n instanceof Field) {
      Fmap.put(n.name, (Field) n);
    }
    if (n instanceof FieldWithDefs) {
      FWDmap.put(n.name, (FieldWithDefs) n);
    }
  }

  // the put_???__Field methods just create a field of the given type/name for
  // the above putField method
  static boolean putCategField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    return putField(pardata, new CategField(data));
  }

  // helper
  static boolean putCategField(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putCategField(pardata, d);
    }
    return res;
  }

  static boolean putBoolField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    return putField(pardata, new BoolField(data));
  }

  // helper
  static boolean putBoolField(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putBoolField(pardata, d);
    }
    return res;
  }

  static boolean putRealField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    putField(pardata, new RealField(data));
    return true;
  }

  // helper
  static boolean putRealField(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putRealField(pardata, d);
    }
    return res;
  }

  static boolean putStringField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    putField(pardata, new StringField(data));
    return true;
  }

  // helper
  static boolean putStringField(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putStringField(pardata, d);
    }
    return res;
  }

  // creates a CategOption for a given parent, which can be either a
  // CategField or another CategOption. If a CF is supplied, then it is a
  // "root" option, but if a CO is supplied, then it inherits defaults from that
  // option.
  static boolean putCategOption(String pardata, String data) {
    if (data == null || map.get(data) != null || pardata == null
        || map.get(pardata) == null) {
      return false;
    }
    CategField pF = CFmap.get(pardata);
    CategOption pO = COmap.get(pardata);
    if (pF != null) {
      // CategField supplied
      CategOption n = new CategOption(data, pF.fieldName, pF);
      pF.rootOptions.add(n);
      pF.allOptions.add(data);
      mapAdd(n);
      return true;
    } else if (pO != null) {
      // CategOption supplied
      CategOption n = new CategOption(data, pO.fieldName, pO);
      pO.suboptions.add(n);
      CFmap.get(pO.fieldName).allOptions.add(data);
      mapAdd(n);
      return true;
    }
    return false;
  }

  // helper
  static boolean putCategOption(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putCategOption(pardata, d);
    }
    return res;
  }

  // For a given CategOption, adds the Tag t as a default value implied by the
  // presence of val. It is required that the Tag t be valid Tag for a direct
  // subfield of the CategField associated with CategOption val.
  static boolean putCategDefault(String val, Tag t) {
    CategOption n = COmap.get(val);
    CategField p = CFmap.get(n.fieldName);
    if (n == null || p == null) {
      return false;
    }
    return p.setDefault(n, t);
  }

  // helper
  static boolean putCategDefault(String pardata, Tag... tags) {
    boolean res = true;
    for (Tag t : tags) {
      res &= putCategDefault(pardata, t);
    }
    return res;
  }

  // Adds a Tag t to be a default tag implied by the chosen value of a boolean
  // field. Similarly, Tag t must be for an immediate subfield.
  static boolean putBoolDefault(String field, boolean val, Tag t) {
    BoolField n = BFmap.get(field);
    if (n == null) {
      return false;
    }
    n.setDefault(val, t);
    return true;
  }

  // helper
  static boolean putBoolDefault(String field, boolean val, Tag... tags) {
    boolean res = true;
    for (Tag t : tags) {
      res &= putBoolDefault(field, val, t);
    }
    return res;
  }

  // Determines the value of a subfield as implied by the provided Tag
  // It's required that "subfield" be a direct subfield of the provided Tag
  // Recursive calling is handled by TagSet.java
  static Tag getDefault(Tag value, String subfield) {
    if (value == null || value.fieldName == null) {
      return null;
    }
    FieldWithDefs n = FWDmap.get(value.fieldName);
    if (n == null) {
      return null;
    }
    return n.getDefault(value, subfield);
  }

  static String getFieldName(String data) {
    if (data == null || map.get(data) == null) {
      return null;
    }
    return map.get(data).fieldName;
  }

  // used to determine what field a given CategOption/field should inherit from
  static String getParentFieldName(String data) {
    if (data == null || map.get(data) == null || map.get(data).parent == null) {
      return null;
    }
    return map.get(data).parent.fieldName;
  }

  // returns all fields, used to list every possible property of an object
  static Set<String> getAllFields() {
    Set<String> res = new HashSet<String>();
    res.addAll(Fmap.keySet());
    return res;
  }

  // determines whether a given tag is valid
  static boolean isValid(Tag tag) {
    String field = tag.fieldName;
    BoolField Bf = BFmap.get(field);
    CategField Cf = CFmap.get(field);
    RealField Rf = RFmap.get(field);
    StringField Sf = SFmap.get(field);
    if (Bf != null) {
      return (tag instanceof BoolTag);
    } else if (Rf != null) {
      return (tag instanceof RealTag);
    } else if (Sf != null) {
      return (tag instanceof StringTag);
    } else if (Cf != null) {
      return (tag instanceof CategTag);
    }
    return false;
  }

  // displays EVERYTHING
  public static void printAll() {
    for (Field f : rootFields) {
      printAllR(f, 0, null);
    }
  }

  // recursive print helper
  public static void printAllR(Node curnode, int depth, Map<String, Tag> defs) {
    if (curnode instanceof CategField) {
      CategField cur = (CategField) curnode;
      System.out.print(indent(depth) + "cat " + cur.name);
      if (cur.rootOptions.size() == 0 && cur.subfields.size() == 0) {
        System.out.println(";");
      } else {
        System.out.println(" {");
        for (CategOption o : cur.rootOptions) {
          printAllR(o, depth + 1, cur.defaults.get(o.name));
        }
        for (Field f : cur.subfields) {
          printAllR(f, depth + 1, null);
        }
        System.out.println(indent(depth) + "}");
      }
    } else if (curnode instanceof RealField) {
      RealField cur = (RealField) curnode;
      System.out.println(indent(depth) + "real " + cur.name + ";");
    } else if (curnode instanceof StringField) {
      StringField cur = (StringField) curnode;
      System.out.println(indent(depth) + "str " + cur.name + ";");
    } else if (curnode instanceof BoolField) {
      BoolField cur = (BoolField) curnode;
      System.out.print(indent(depth) + "bool " + cur.name);
      if (cur.subfields.size() == 0) {
        System.out.println(";");
      } else {
        System.out.println(" {");
        if (cur.truedefs.size() > 0) {
          System.out.println(indent(depth + 1) + "true {");
          for (String key : cur.truedefs.keySet()) {
            Tag tag = cur.truedefs.get(key);
            System.out
                .println(indent(depth + 2) + TagDataParser.tagFormat(tag));
          }
          System.out.println(indent(depth + 1) + "}");
        }
        if (cur.falsedefs.size() > 0) {
          System.out.println(indent(depth + 1) + "false {");
          for (String key : cur.falsedefs.keySet()) {
            Tag tag = cur.falsedefs.get(key);
            System.out
                .println(indent(depth + 2) + TagDataParser.tagFormat(tag));
          }
          System.out.println(indent(depth + 1) + "}");
        }
        for (Field f : cur.subfields) {
          printAllR(f, depth + 1, null);
        }
        System.out.println(indent(depth) + "}");
      }
    } else if (curnode instanceof CategOption) {
      CategOption cur = (CategOption) curnode;
      System.out.print(indent(depth) + "opt " + cur.name);
      if (cur.suboptions.size() == 0
          && (defs == null || defs.keySet().size() == 0)) {
        System.out.println(";");
      } else {
        System.out.println(" {");
        if (defs != null) {
          for (String key : defs.keySet()) {
            Tag tag = defs.get(key);
            System.out
                .println(indent(depth + 1) + TagDataParser.tagFormat(tag));
          }
        }
        for (CategOption o : cur.suboptions) {
          printAllR(o, depth + 1, CFmap.get(cur.fieldName).defaults.get(o.name));
        }
        System.out.println(indent(depth) + "}");
      }
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

}
