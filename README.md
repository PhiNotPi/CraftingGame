# CraftingGame

This is currently the skeleton of a resource-gathering/crafting game.  Right now, I am working on the tagging system, which will serve as the core of the game.  Eventually, almost every in-game entity will be labeled by a set of tags, which will determine most of the properties of that entity, and what that entity can do / be used for.

The main method is currently in Main.java.  When run, the code parses tags.dat and uses that information to build a data structure (in TagData.java) to describe how tags are related to each other. Then, the entire data structure is printed to the screen in the same format.  Next, a random Ingredient is created and displayed.  Finally, several random TagSets are generated, with one line showing the explicit properties, the next line including the implicit properties, and then the next line telling whether it matches the given ingredient.  The code terminates once a matching TagSet is generated.
