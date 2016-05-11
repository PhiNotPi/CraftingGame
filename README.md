# CraftingGame

This is currently the skeleton of a resource-gathering/crafting game.  Right now, I am working on the tagging system, which will serve as the core of the game.  Eventually, almost every in-game entity will be labeled by a set of tags, which will determine most of the properties of that entity, and what that entity can do / be used for.

The main method is currently in Main.java.  When run, the code parses tags.dat and uses that information to describe how tags are related to each other. Then, the entire data structure is printed to the screen in the same format.  Next, several random TagSets are generated, with one line showing the explicit properties and the next line including the implicit properties.
