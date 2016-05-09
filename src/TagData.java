import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TagData {

  public static void main(String[] args) {
    TagData.printAll();
    System.out.println();
    TagSet ts = new TagSet();
    ts.add(new Tag("Oak"), new Tag("Log"));
    System.out.print("item: ");
    ts.print();
    System.out.print("with inheritance: ");
    ts.printAll();
    System.out.println();
    ts = new TagSet();
    // the [density:true] tag should be auto-rejected
    ts.add(new Tag("Mahogany"), new Tag("Log"), new Tag("Density", true));
    System.out.print("item: ");
    ts.print();
    System.out.print("with inheritance: ");
    ts.printAll();
  }

  // Each node has a unique name, and these maps
  // go from name->node. The different maps are
  // for storing only specific types of nodes, so
  // that less casting is needed.
  private static Map<String, Node> map;
  private static Map<String, StringField> SFmap;
  private static Map<String, StringOption> SOmap;
  private static Map<String, BoolField> BFmap;
  private static Map<String, RealField> RFmap;
  private static Map<String, Field> Fmap;
  private static Map<String, FieldWithDefs> FWDmap;
  // these are Fields without a parent field
  private static Set<Field> rootFields;

  static {
    map = new HashMap<String, Node>();
    SFmap = new HashMap<String, StringField>();
    SOmap = new HashMap<String, StringOption>();
    BFmap = new HashMap<String, BoolField>();
    RFmap = new HashMap<String, RealField>();
    Fmap = new HashMap<String, Field>();
    FWDmap = new HashMap<String, FieldWithDefs>();
    rootFields = new HashSet<Field>();

    // eventually, this will be replaced by a parser
    // and a config file to do everything
    TagData.putStringField(null, "Material");
    TagData.putStringOption("Material", "Wood");
    TagData.putStringOption("Wood", "Oak");
    TagData.putStringOption("Wood", "Mahogany");
    TagData.putStringField("Material", "Color");
    TagData.putStringOption("Color", "Brown");
    TagData.putStringOption("Brown", "Reddish-Brown");
    TagData.putBoolField("Material", "Flammable");
    TagData.putRealField("Material", "Density");
    TagData.putStringDefault("Wood", new Tag("Brown"));
    TagData.putStringDefault("Wood", new Tag("Flammable", true));
    TagData.putStringDefault("Wood", new Tag("Density", 2.7));
    TagData.putStringDefault("Mahogany", new Tag("Reddish-Brown"));
    TagData.putStringField(null, "Shape");
    TagData.putStringOption("Shape", "Log");
    TagData.putRealField("Shape", "Volume");
    TagData.putStringDefault("Log", new Tag("Volume", 1.4));

  }

  private static abstract class Node {
    // name is unique
    String name;
    // field name, which differs from this.name only in the case of
    // StringOptions
    String fieldName;
    // where inheritance comes from
    Node parent;
  }

  // everything that can act as a "key"
  private static abstract class Field extends Node {
  }

  // this class contains subfields, and has default
  // values of the subfields depending on its current value.
  // FWDs must be categorical, like either StringField or BoolField
  private static abstract class FieldWithDefs extends Field {
    // given a tag value, and a desired subfield, return the default value of
    // the subfield
    abstract Tag getDefault(Tag value, String subfield);

    // subfields are fields that depend on this field
    // for example, an SF named "color" could be a subfield of a SF named
    // "material"
    Set<Field> subfields;
    // the code to create defaults is more specialized to each FWB type.
  }

  private static class StringField extends FieldWithDefs {
    Set<StringOption> rootOptions;
    Set<String> allOptions;
    Map<String, Map<String, Tag>> defaults;

    StringField(String name) {
      this.name = name;
      this.fieldName = name;
      this.parent = null;
      this.subfields = new HashSet<Field>();
      this.rootOptions = new HashSet<StringOption>();
      this.allOptions = new HashSet<String>();
      this.defaults = new HashMap<String, Map<String, Tag>>();
    }

    // this is slit up into several helper methods,
    // depending on what information is provided
    @Override
    Tag getDefault(Tag value, String subfield) {
      if (value == null) {
        return null;
      }
      return getDefault(value.name, subfield);
    }

    Tag getDefault(String value, String subfield) {
      if (value == null) {
        return null;
      }
      return getDefault(SOmap.get(value), subfield);
    }

    // the option is a selected option, like "wood",
    // and subfield is the selected subfield, like "color."
    // Of course, "wood" must be an option for this particular field
    Tag getDefault(StringOption option, String subfield) {
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
      String subfield = val.fieldName();
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

  private static class StringOption extends Node {
    // options that inherit from it, so "oak" might be a suboption of "wood"
    Set<StringOption> suboptions;

    StringOption(String name, String fieldName, Node parent) {
      this.name = name;
      this.fieldName = fieldName;
      this.suboptions = new HashSet<StringOption>();
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
      if (value != null && value.fieldName() == this.name) {
        // input Tag must be a tag of this type
        // then, we return the value from the appropriate Map
        if (value.bool) {
          return truedefs.get(subfield);
        }
        return falsedefs.get(subfield);
      }
      return null;
    }

    boolean setDefault(boolean option, Tag val) {
      String subfield = val.fieldName();
      if (Fmap.get(subfield) == null
          || !this.subfields.contains(Fmap.get(subfield))) {
        // Tag ust be for an immediate subfield
        return false;
      }
      // add Tag to appropriate Map
      if (option) {
        this.truedefs.put(val.fieldName(), val);
      } else {
        this.falsedefs.put(val.fieldName(), val);
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
    if (n instanceof StringField) {
      SFmap.put(n.name, (StringField) n);
    }
    if (n instanceof StringOption) {
      SOmap.put(n.name, (StringOption) n);
    }
    if (n instanceof BoolField) {
      BFmap.put(n.name, (BoolField) n);
    }
    if (n instanceof RealField) {
      RFmap.put(n.name, (RealField) n);
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
  static boolean putStringField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    return putField(pardata, new StringField(data));
  }

  // helper
  static boolean putStringField(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putStringField(pardata, d);
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

  // creates a StringOption for a given parent, which can be either a
  // StringField or another StringOption. If a SF is supplied, then it is a
  // "root" option, but if a SO is supplied, then it inherits defaults from that
  // option.
  static boolean putStringOption(String pardata, String data) {
    if (data == null || map.get(data) != null || pardata == null
        || map.get(pardata) == null) {
      return false;
    }
    StringField pF = SFmap.get(pardata);
    StringOption pO = SOmap.get(pardata);
    if (pF != null) {
      // StringField supplied
      StringOption n = new StringOption(data, pF.fieldName, pF);
      pF.rootOptions.add(n);
      pF.allOptions.add(data);
      mapAdd(n);
      return true;
    } else if (pO != null) {
      // StringOption supplied
      StringOption n = new StringOption(data, pO.fieldName, pO);
      pO.suboptions.add(n);
      SFmap.get(pO.fieldName).allOptions.add(data);
      mapAdd(n);
      return true;
    }
    return false;
  }

  // helper
  static boolean putStringOption(String pardata, String... data) {
    boolean res = true;
    for (String d : data) {
      res &= putStringOption(pardata, d);
    }
    return res;
  }

  // For a given StringOption, adds the Tag t as a default value implied by the
  // presence of val. It is required that the Tag t be valid Tag for a direct
  // subfield of the StringField associated with StringOption val.
  static boolean putStringDefault(String val, Tag t) {
    StringOption n = SOmap.get(val);
    StringField p = SFmap.get(n.fieldName);
    if (n == null || p == null) {
      return false;
    }
    return p.setDefault(n, t);
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

  // Determines the value of a subfield as implied by the provided Tag
  // It's required that "subfield" be a direct subfield of the provided Tag
  // Recursive calling is handled by TagSet.java
  static Tag getDefault(Tag value, String subfield) {
    if (value == null || value.fieldName() == null) {
      return null;
    }
    FieldWithDefs n = FWDmap.get(value.fieldName());
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

  // used to determine what field a given StringOption/field should inherit from
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
  static boolean isValid(Tag t) {
    String field = t.fieldName();
    BoolField Bf = BFmap.get(field);
    StringField Sf = SFmap.get(field);
    RealField Rf = RFmap.get(field);
    if (Bf != null) {
      if (t.bool != null && t.min == null && t.name.equals(field)) {
        return true;
      }
    } else if (Rf != null) {
      if (t.bool == null && t.min != null && t.max != null && t.min <= t.max
          && t.name.equals(field)) {
        return true;
      }
    } else if (Sf != null) {
      if (t.bool == null && t.min == null) {
        return true;
      }
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
    if (curnode instanceof StringField) {
      StringField cur = (StringField) curnode;
      System.out.print(indent(depth) + "cat " + cur.name);
      if (cur.rootOptions.size() == 0 && cur.subfields.size() == 0) {
        System.out.println(";");
      } else {
        System.out.println(" {");
        for (StringOption o : cur.rootOptions) {
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
    } else if (curnode instanceof BoolField) {
      BoolField cur = (BoolField) curnode;
      System.out.print(indent(depth) + "bool " + cur.name);
      if (cur.subfields.size() == 0) {
        System.out.println(";");
      } else {
        System.out.println(" {");
        for (Field f : cur.subfields) {
          printAllR(f, depth + 1, null);
        }
        System.out.println(indent(depth) + "}");
      }
    } else if (curnode instanceof StringOption) {
      StringOption cur = (StringOption) curnode;
      System.out.print(indent(depth) + "opt " + cur.name);
      if (cur.suboptions.size() == 0
          && (defs == null || defs.keySet().size() == 0)) {
        System.out.println(";");
      } else {
        System.out.println(" {");
        if (defs != null) {
          for (String key : defs.keySet()) {
            Tag t = defs.get(key);
            if (SFmap.get(key) != null) {
              System.out.println(indent(depth + 1) + "~" + t.name + ";");
            } else if (BFmap.get(key) != null) {
              System.out.println(indent(depth + 1) + "~" + t.name + ": "
                  + t.bool + ";");
            } else if (RFmap.get(key) != null) {
              if (t.min.equals(t.max)) {
                System.out.println(indent(depth + 1) + "~" + t.name + ": "
                    + t.min + ";");
              } else {
                System.out.println(indent(depth + 1) + "~" + t.name + ": "
                    + t.min + "," + t.max + ";");
              }
            }
          }
        }
        for (StringOption o : cur.suboptions) {
          printAllR(o, depth + 1, SFmap.get(cur.fieldName).defaults.get(o.name));
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
