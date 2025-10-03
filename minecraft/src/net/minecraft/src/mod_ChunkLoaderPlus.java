package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import forge.Configuration;
import forge.Property;
import net.minecraft.client.Minecraft;

public class mod_ChunkLoaderPlus extends BaseModMp {
	public static final Map<Chunk, Integer> chunkRefCounts = new HashMap<Chunk, Integer>();
	
	public static int clOff;
	public static int clOn;
	public static int partTex;
	public static Block chunkLoader;
	private Configuration config;
	public static World lastWorld = null;
	
	public String Name() {
		return "ChunkLoader+";
	}
	
	public String Version() {
		return "R10125";
	}
	
	public String Description() {
		return "Adds a 'Chunk Loader' block.";
	}
	
	public String Icon() {
		return "/maxres/ChunkLoaderPlus/ModMenu.png";
	}
	
	public mod_ChunkLoaderPlus() {
		clOff = ModLoader.addOverride("/terrain.png", "/maxres/ChunkLoaderPlus/BlockChunkLoaderIdle.png");
		clOn = ModLoader.addOverride("/terrain.png", "/maxres/ChunkLoaderPlus/BlockChunkLoaderActive.png");
		partTex = ModLoader.addOverride("/terrain.png", "/maxres/ChunkLoaderPlus/ParticleSparkle.png");
		
		File configDir = new File(Minecraft.getMinecraftDir(), "/config/");
		config = new Configuration(new File(configDir, "ChunkLoaderPlus.cfg"));
		
		try {
			config.load();
			Property chunkLoaderID = config.getOrCreateBlockIdProperty("chunkLoaderID", 166);
			
			if (Integer.parseInt(chunkLoaderID.value) != 0) {
				chunkLoader = (new BlockChunkLoader(Integer.parseInt(chunkLoaderID.value)).setBlockName("chunkLoader"));
				ModLoader.AddName(chunkLoader, "Chunk Loader");
				ModLoader.RegisterBlock(chunkLoader);
				ModLoader.AddRecipe(new ItemStack(chunkLoader, 1), new Object[] {
					"O#O", "#R#", "O#O", Character.valueOf('O'), Block.obsidian, Character.valueOf('#'), Item.diamond, Character.valueOf('R'), Item.redstone
				});
			} else {
				log("Invalid value in config: " + chunkLoaderID.value, 2);
			}
		} finally {
			config.save();
		}
		ModLoader.SetInGameHook(this, true, true);
	}
	
	@Override
	public boolean OnTickInGame(Minecraft client) {
		if (!client.theWorld.multiplayerWorld && (lastWorld != client.theWorld)) {
			chunkRefCounts.clear();
			loadAllChunksFromNBT();
			ChunkLoaderOverrides.injectUpdateBlocksAndPlayCaveSounds(client.theWorld);
		}
		lastWorld = client.theWorld;
		return true;
	}
	
	public static void saveChunkToNBT(Chunk chunk) {
		SaveHandler saveHandler = (SaveHandler) ModLoader.getMinecraftInstance().theWorld.saveHandler;
		File out = saveHandler.func_28113_a("ChunkLoaderPlus");
		NBTTagCompound nbt;
		
		try {
			if (out.exists()) {
				FileInputStream fis = new FileInputStream(out);
				nbt = CompressedStreamTools.func_1138_a(fis);
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
		SaveHandler saveHandler = (SaveHandler) ModLoader.getMinecraftInstance().theWorld.saveHandler;
		File out = saveHandler.func_28113_a("ChunkLoaderPlus");
		
		try {
			if (!out.exists()) return;
			
			FileInputStream fis = new FileInputStream(out);
			NBTTagCompound nbt = CompressedStreamTools.func_1138_a(fis);
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
		SaveHandler saveHandler = (SaveHandler) ModLoader.getMinecraftInstance().theWorld.saveHandler;
		File in = saveHandler.func_28113_a("ChunkLoaderPlus");
		
		if (!in.exists()) return;
		
		try {
			FileInputStream fis = new FileInputStream(in);
			NBTTagCompound nbt = CompressedStreamTools.func_1138_a(fis);
			fis.close();
			
			if (!nbt.hasKey("ChunkLoaderPositions")) return;
			
			NBTTagList list = nbt.getTagList("ChunkLoaderPositions");
			
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);
				int x = tag.getInteger("x");
				int z = tag.getInteger("z");
				Chunk chunk = ModLoader.getMinecraftInstance().theWorld.getChunkFromChunkCoords(x, z);
				chunk.isChunkLoaded = true;
				
				World world = ModLoader.getMinecraftInstance().theWorld;
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
	
	public static void log(String str, int messageCase) {
		if (messageCase == 0) {
			System.out.println("[ChunkLoader+]: " + str);
		} else if (messageCase == 1) {
			System.out.println("[ChunkLoader+][WARN]: " + str);
		} else if (messageCase == 2) {
			System.out.println("[ChunkLoader+][FATAL ERROR]: " + str);
		}
	}
}