# ChunkLoader+

**Overview**:

ChunkLoader+ is a mod made for Minecraft b1.7.3 with ModLoaderMP and Forge forked from the 'ChunkLoader' mod made by *TotallyNotArt1* (available in the [Modification Station Discord](https://modification-station.net/) or on [Modrinth](https://modrinth.com/mod/chunk-loader-mod-b1.7.3)). It adds a singular 'Chunk Loader' block which when powered with redstone will keep the chunk it is placed in loaded.

**Features**:

* Chunk remains loaded in game memory no matter how far away player travels
* Block updates continue (growing crops, snow, grass growth, etc)
* Chunk loaders restart on world load (both singleplayer and server) no matter where the player is
* Chat updates to inform the player of what the loader is doing (on multiplayer chat updates will only show for players in the same chunk as the loader)
* Only one block ID used, which can be changed
* On multiplayer crafting of the loader can be disabled, making the block unobtainable for non OP players

**Requirements**:

* [ModLoaderMP](https://mcarchive.net/mods/modloadermp)
* [Forge](https://mcarchive.net/mods/minecraftforge?gvsn=)
* [MaxresBase](https://github.com/maxresd3fault/MaxresBase)

**Installation**:

The mod can either be installed as a standalone mod or as a jar mod. It also requires my helper mod, MaxresBase, which provides important overrides via reflector allow this mod to function. If you want mob spawners to work in loaded chunks when no players are in range, you must install `cy.class` (client) and `bx.class` (server) as jar mods.

**Limitations**:

* In singleplayer, when the player goes to the Nether any chunk loader in the Overworld will stop working (but it will restart upon going back to the Overworld). This is due to a limitation of how singleplayer Minecraft works, only one world, or dimension, can be loaded at any time unlike in multiplayer.
* Chunk loaders will not work in the Nether and are not placable there. Much restructuring would need to be done to accommodate loaders in other dimensions. I forked this mod for my own needs and don't need this feature, if you want it, make a pull request.
