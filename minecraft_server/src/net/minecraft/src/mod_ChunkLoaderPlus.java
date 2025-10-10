package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import forge.Configuration;
import forge.Property;
import net.minecraft.server.MinecraftServer;

public class mod_ChunkLoaderPlus extends BaseModMp {
	public static final Map<Chunk, Integer> chunkRefCounts = new HashMap<Chunk, Integer>();
	
	public static Block chunkLoader;
	
	public static boolean isLoaded = false;
	protected static boolean TileEntityMobSpawnerClassInstalled = true;
	
	private Configuration config;
	
	public String Version() {
		return "R101025";
	}
	
	public mod_ChunkLoaderPlus() {
		File configDir = new File("config");
		config = new Configuration(new File(configDir, "ChunkLoaderPlus.cfg"));
		
		try {
			config.load();
			Property chunkLoaderID = config.getOrCreateBlockIdProperty("chunkLoaderID", 166);
			Property disableChunkLoaderCrafting = config.getOrCreateBooleanProperty("disableChunkLoaderCrafting", 0, false);
			
			chunkLoader = (new BlockChunkLoader(Integer.parseInt(chunkLoaderID.value))).setBlockName("chunkLoader");
			ModLoader.RegisterBlock(chunkLoader);
			
			if (!Boolean.parseBoolean(disableChunkLoaderCrafting.value)) {
				ModLoader.AddRecipe(new ItemStack(chunkLoader, 1), new Object[] {
					"O#O", "#R#", "O#O", Character.valueOf('O'), Block.obsidian, Character.valueOf('#'), Item.diamond, Character.valueOf('R'), Item.redstone
				});
			} else {
				log("Disabling crafting of Chunk Loaders!", Level.INFO);
			}
		} finally {
			config.save();
		}
		ModLoader.SetInGameHook(this, true, true);
	}
	
    @Override
    public void ModsLoaded() {
    	try {
			Class.forName("bx");
			
			if (!TileEntityMobSpawnerClassInstalled) {
				log("You have not injected bx.class, mob spawners will not work in loaded chunks with no players.", Level.WARNING);
			}
		} catch (Exception e) {
		}
        try {
            Class.forName("mod_MaxresBase");
        } catch (Exception e) {
        	log("MaxresBase not installed! This mod will not function.", Level.SEVERE);
        }
    }
    
	@Override
	public void OnTickInGame(MinecraftServer server) {
		if(!isLoaded) {
			WorldServer worldServer = server.worldMngr[0];
			worldServer.chunkProviderServer.chunkLoadOverride = true;
			loadAllChunksFromNBT();
			ChunkProviderServer cps = worldServer.chunkProviderServer;
			ChunkLoaderOverrides.hookChunkUnload(cps);
			worldServer.chunkProviderServer.chunkLoadOverride = false;
			isLoaded = true;
		}
	}
	
	public static void saveChunkToNBT(Chunk chunk) {
		WorldServer world = ModLoader.getMinecraftServerInstance().worldMngr[0];
		ISaveHandler saveHandler = world.getWorldFile();
		File out = saveHandler.func_28111_b("ChunkLoaderPlus");
		NBTTagCompound nbt;
		
		try {
			if (out.exists()) {
				FileInputStream fis = new FileInputStream(out);
				nbt = CompressedStreamTools.func_770_a(fis);
				fis.close();
			} else {
				nbt = new NBTTagCompound();
			}
			
			NBTTagList list;
			
			if (nbt.hasKey("ChunkLoaderPositions")) {
				list = nbt.getTagList("ChunkLoaderPositions");
			} else {
				list = new NBTTagList();
			}
			
			boolean exists = false;
			
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound existingTag = (NBTTagCompound) list.tagAt(i);

				if (existingTag.getInteger("x") == chunk.xPosition &&
					existingTag.getInteger("z") == chunk.zPosition) {
					exists = true;
					break;
				}
			}
			
			if (!exists) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("x", chunk.xPosition);
				tag.setInteger("z", chunk.zPosition);
				list.setTag(tag);
				nbt.setTag("ChunkLoaderPositions", list);

				FileOutputStream fos = new FileOutputStream(out);
				CompressedStreamTools.writeGzippedCompoundToOutputStream(nbt, fos);
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteChunkFromNBT(Chunk chunk) {
		WorldServer world = ModLoader.getMinecraftServerInstance().worldMngr[0];
		ISaveHandler saveHandler = world.getWorldFile();
		File out = saveHandler.func_28111_b("ChunkLoaderPlus");
		
		try {
			if (!out.exists()) return;
			
			FileInputStream fis = new FileInputStream(out);
			NBTTagCompound nbt = CompressedStreamTools.func_770_a(fis);
			fis.close();
			
			if (!nbt.hasKey("ChunkLoaderPositions")) return;
			
			NBTTagList list = nbt.getTagList("ChunkLoaderPositions");
			NBTTagList newList = new NBTTagList();
			
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound existingTag = (NBTTagCompound) list.tagAt(i);
				
				if (existingTag.getInteger("x") != chunk.xPosition ||
					existingTag.getInteger("z") != chunk.zPosition) {
					newList.setTag(existingTag);
				}
			}
			
			if (newList.tagCount() == 0) {
				if (out.exists()) {
					out.delete();
				}
			} else {
				nbt.setTag("ChunkLoaderPositions", newList);
				FileOutputStream fos = new FileOutputStream(out);
				CompressedStreamTools.writeGzippedCompoundToOutputStream(nbt, fos);
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadAllChunksFromNBT() {
		WorldServer world = ModLoader.getMinecraftServerInstance().worldMngr[0];
		ISaveHandler saveHandler = world.getWorldFile();
		File in = saveHandler.func_28111_b("ChunkLoaderPlus");
		
		if (!in.exists()) return;
		
		try {
			FileInputStream fis = new FileInputStream(in);
			NBTTagCompound nbt = CompressedStreamTools.func_770_a(fis);
			fis.close();
			
			if (!nbt.hasKey("ChunkLoaderPositions")) return;
			
			NBTTagList list = nbt.getTagList("ChunkLoaderPositions");
			
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);
				int x = tag.getInteger("x");
				int z = tag.getInteger("z");
				ChunkProviderServer cps = (ChunkProviderServer) world.getChunkProvider();
				cps.loadChunk(x, z);
				
				Chunk chunk = world.getChunkFromChunkCoords(x, z);
				int startX = chunk.xPosition << 4;
				int startZ = chunk.zPosition << 4;

				for (int bx = 0; bx < 16; bx++) {
					for (int by = 0; by < 127; by++) {
						for (int bz = 0; bz < 16; bz++) {
							int worldX = startX + bx;
							int worldZ = startZ + bz;
							
							int id = world.getBlockId(worldX, by, worldZ);
							if (id == mod_ChunkLoaderPlus.chunkLoader.blockID) {
								Block block = Block.blocksList[id];
								if (block instanceof BlockChunkLoader) {
									((BlockChunkLoader) block).updateState(world, worldX, by, worldZ, true);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void log(String str, Level level) {
		ModLoader.getLogger().log(level, "[ChunkLoader+]: " + str);
	}
}
