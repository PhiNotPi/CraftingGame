import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Recipe {

  ArrayList<Ingredient> ingredients;

  public Recipe() {
    this.ingredients = new ArrayList<Ingredient>();
  }

  @Override
  public String toString() {
    return "Recipe [ingredients=" + ingredients + "]";
  }

  // recursive matching
  static public HashMap<TagSet, Ingredient> match(
      ArrayList<Ingredient> ingsOrig, Set<TagSet> recs) {
    if (ingsOrig.size() == 0 || recs.size() == 0) {
      return new HashMap<TagSet, Ingredient>();
    }
    // results from children
    Set<HashMap<TagSet, Ingredient>> options = new HashSet<HashMap<TagSet, Ingredient>>();
    ArrayList<Ingredient> ings = new ArrayList<Ingredient>();
    ings.addAll(ingsOrig);
    Ingredient ing = ings.remove(0);
    for (TagSet rec : recs) {
      if (ing.matches(rec)) {
        HashSet<TagSet> remaining = new HashSet<TagSet>();
        remaining.addAll(recs);
        remaining.remove(rec);
        HashMap<TagSet, Ingredient> option = match(ings, remaining);
        option.put(rec, ing);
        if (option.size() == ingsOrig.size()) {
          return option;
        }
        options.add(option);
      }
    }
    HashMap<TagSet, Ingredient> max = new HashMap<TagSet, Ingredient>();
    for (HashMap<TagSet, Ingredient> option : options) {
      if (option.size() > max.size()) {
        max = option;
      }
    }
    return max;
  }

}
