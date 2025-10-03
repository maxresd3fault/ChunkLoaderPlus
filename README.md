# ChunkLoader+

**Overview**:
ChunkLoader+ is a mod made for Minecraft b1.7.3 with ModLoaderMP and Forge forked from the 'ChunkLoader' mod made by *TotallyNotArt1* ([available in Modification Station Discord](https://modification-station.net/)). It adds a singular 'Chunk Loader' block which when powered with redstone will keep the chunk it is placed in loaded.

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

**Installation**
The mod can either be installed as a standalone mod or as a jar mod. If you want mob spawners to work in loaded chunks when no players are in range, you must install `cy.class` (client) and `bx.class` (server) as jar mods.
