import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TagData {

  public static void main(String[] args) {
    TagSet ts = new TagSet();
    ts.add(new Tag("Oak"), new Tag("Log"));
    System.out.print("item: ");
    ts.print();
    System.out.print("with inheritance: ");
    ts.printAll();
  }

  private static Map<String, Node> map;
  private static Map<String, StringField> SFmap;
  private static Map<String, StringOption> SOmap;
  private static Map<String, BoolField> BFmap;
  private static Map<String, RealField> RFmap;
  private static Map<String, Field> Fmap;
  private static Map<String, FieldWithDefs> FWDmap;
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

    TagData.putStringField(null, "Material");
    TagData.putStringField("Material", "Color");
    TagData.putBoolField("Material", "Flammable");
    TagData.putStringOption("Material", "Wood");
    TagData.putStringOption("Wood", "Oak");
    TagData.putStringOption("Color", "Brown");
    TagData.putStringDefault("Wood", new Tag("Brown"));
    TagData.putStringDefault("Wood", new Tag("Flammable", true));
    TagData.putStringField(null, "Shape");
    TagData.putStringOption("Shape", "Log");
    TagData.putRealField("Shape", "Volume");
    TagData.putStringDefault("Log", new Tag("Volume", 1.4));

  }

  private static abstract class Node {
    String fieldName;
    String name;
    Node parent;
  }

  private static abstract class Field extends Node {
  }

  private static abstract class FieldWithDefs extends Field {
    abstract Tag getDefault(Tag value, String subfield);

    Set<Field> subfields;
  }

  private static class StringField extends FieldWithDefs {
    Set<StringOption> rootOptions;
    Map<String, Map<String, Tag>> defaults;

    StringField(String name) {
      this.name = name;
      this.fieldName = name;
      this.parent = null;
      this.subfields = new HashSet<Field>();
      this.rootOptions = new HashSet<StringOption>();
      this.defaults = new HashMap<String, Map<String, Tag>>();
    }

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
      return getDefault(map.get(value), subfield);
    }

    Tag getDefault(Node option, String subfield) {
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

    boolean setDefault(Node option, Tag val) {
      String subfield = val.fieldName();
      if (option == null || option.fieldName != this.name || subfield == null
          || Fmap.get(subfield) == null
          || !this.subfields.contains(Fmap.get(subfield))) {
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
    Set<StringOption> suboptions;

    StringOption(String name, String fieldName, Node parent) {
      this.name = name;
      this.fieldName = fieldName;
      this.suboptions = new HashSet<StringOption>();
      this.parent = parent;
    }
  }

  private static class BoolField extends FieldWithDefs {
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
        return false;
      }
      if (option) {
        this.truedefs.put(val.fieldName(), val);
      } else {
        this.falsedefs.put(val.fieldName(), val);
      }
      return true;
    }
  }

  private static class RealField extends Field {
    RealField(String name) {
      this.name = name;
      this.fieldName = name;
    }
  }

  private static boolean putField(String pardata, Field n) {
    if (pardata == null) {
      mapAdd(n);
      rootFields.add(n);
      return true;
    }
    FieldWithDefs p = FWDmap.get(pardata);
    if (p == null) {
      return false;
    }
    p.subfields.add(n);
    n.parent = p;
    mapAdd(n);
    return true;
  }

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

  static boolean putStringField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    return putField(pardata, new StringField(data));
  }

  static boolean putBoolField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    return putField(pardata, new BoolField(data));
  }

  static boolean putRealField(String pardata, String data) {
    if (data == null || map.get(data) != null) {
      return false;
    }
    return putField(pardata, new RealField(data));
  }

  static boolean putStringOption(String pardata, String data) {
    if (data == null || map.get(data) != null || pardata == null
        || map.get(pardata) == null) {
      return false;
    }
    StringField pF = SFmap.get(pardata);
    StringOption pO = SOmap.get(pardata);
    if (pF != null) {
      StringOption n = new StringOption(data, pF.fieldName, pF);
      pF.rootOptions.add(n);
      mapAdd(n);
      return true;
    } else if (pO != null) {
      StringOption n = new StringOption(data, pO.fieldName, pO);
      pO.suboptions.add(n);
      mapAdd(n);
      return true;
    }
    return false;
  }

  static boolean putStringDefault(String val, Tag t) {
    StringOption n = SOmap.get(val);
    if (n == null) {
      return false;
    }
    StringField p = SFmap.get(n.fieldName);
    if (p != null) {
      return p.setDefault(n, t);
    }
    return false;
  }

  static boolean putBoolDefault(String field, boolean val, Tag t) {
    BoolField n = BFmap.get(field);
    if (n == null) {
      return false;
    }
    n.setDefault(val, t);
    return true;
  }

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

  static String getParentFieldName(String data) {
    if (data == null || map.get(data) == null || map.get(data).parent == null) {
      return null;
    }
    return map.get(data).parent.fieldName;
  }

  static Set<String> getAllFields() {
    Set<String> res = new HashSet<String>();
    res.addAll(Fmap.keySet());
    return res;
  }

}
