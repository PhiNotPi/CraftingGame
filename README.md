# CraftingGame

This is currently the skeleton of a resource-gathering/crafting game.  Right now, I am working on the tagging system, which will serve as the core of the game.  Eventually, almost every in-game entity will be labeled by a set of tags, which will determine most of the properties of that entity, and what that entity can do / be used for.

The main method is currently in TagData.java.  When run, the code in the static initializer describes the relationships between the possible tags.  Then, in the main method, the entire data structure is printed to the screen to provide a nice visualized.  Next, a couple items are created, and inheritance is used to fill in all the missing properties.
