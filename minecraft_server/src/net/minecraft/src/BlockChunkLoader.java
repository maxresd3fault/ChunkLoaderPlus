package net.minecraft.src;

import java.util.Random;
import java.util.logging.Level;

public class BlockChunkLoader extends Block {
	public static final Material chunkLoaderMaterial = new Material(MapColor.field_28193_h).setImmovableMobility();
	
	public BlockChunkLoader(int id) {
		super(id, chunkLoaderMaterial);
		
		this.setLightValue(0.7F);
		this.setHardness(0.3F);
		this.setResistance(0.3F);
		this.setStepSound(Block.soundGlassFootstep);
	}
	
	public void onBlockAdded(World w, int x, int y, int z) {
		updateState(w, x, y, z, false);
	}
	
	public void updateState(World w, int x, int y, int z, boolean updateOverride) {
		boolean powered = w.isBlockGettingPowered(x, y, z);
		if (!updateOverride && w.getBlockMetadata(x, y, z) == 1 && powered) return;
		else if (!updateOverride && w.getBlockMetadata(x, y, z) == 0 && !powered) return;
		int meta = powered ? 1 : 0;
		
		if (w.getBlockMetadata(x, y, z) != meta) {
			w.setBlockMetadataWithNotify(x, y, z, meta);
			w.markBlockNeedsUpdate(x, y, z);
		}
		
		if (powered) {
			loadChunksAround(w, x, y, z);
		} else if (!powered) {
			unloadChunks(w, x, y, z);
		}
	}
	
	public void loadChunksAround(World w, int x, int y, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		Chunk chunk = w.getChunkFromChunkCoords(chunkX, chunkZ);
		
		int count = mod_ChunkLoaderPlus.chunkRefCounts.getOrDefault(chunk, 0) + 1;
		mod_ChunkLoaderPlus.saveChunkToNBT(chunk);
		mod_ChunkLoaderPlus.chunkRefCounts.put(chunk, count);
		mod_ChunkLoaderPlus.log(count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition, Level.INFO);
		
		for (Object obj : w.playerEntities) {
			EntityPlayerMP player = (EntityPlayerMP) obj;
			double dx = player.posX - (x + 0.5);
			double dy = player.posY - (y + 0.5);
			double dz = player.posZ - (z + 0.5);
			
			double distanceSq = dx * dx + dy * dy + dz * dz;
			
			if (distanceSq <= 64) {
				ModLoader.getMinecraftServerInstance().configManager.sendChatMessageToPlayer(player.username, "§a" + count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition);
			}
		}
	}
	
	public void unloadChunks(World w, int x, int y, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		Chunk chunk = w.getChunkFromChunkCoords(chunkX, chunkZ);
		
		boolean messageCase = false;
		int count = mod_ChunkLoaderPlus.chunkRefCounts.getOrDefault(chunk, 0) - 1;
		
		if (count <= 0) {
			mod_ChunkLoaderPlus.deleteChunkFromNBT(chunk);
			mod_ChunkLoaderPlus.chunkRefCounts.remove(chunk);
			mod_ChunkLoaderPlus.log("Unloading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition, Level.INFO);
		} else {
			mod_ChunkLoaderPlus.chunkRefCounts.put(chunk, count);
			mod_ChunkLoaderPlus.log(count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition, Level.INFO);
			messageCase = true;
		}
		
		for (Object obj : w.playerEntities) {
			EntityPlayerMP player = (EntityPlayerMP) obj;
			double dx = player.posX - (x + 0.5);
			double dy = player.posY - (y + 0.5);
			double dz = player.posZ - (z + 0.5);
			
			double distanceSq = dx * dx + dy * dy + dz * dz;
			
			if (distanceSq <= 64) {
				if (!messageCase) ModLoader.getMinecraftServerInstance().configManager.sendChatMessageToPlayer(player.username, "§4Unloading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition);
				else if (messageCase) ModLoader.getMinecraftServerInstance().configManager.sendChatMessageToPlayer(player.username, "§e" + count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition);
			}
		}
	}
	
	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int neighborBlockID) {
		updateState(w, x, y, z, false);
	}
	
	public void onBlockRemoval(World w, int x, int y, int z) {
		if (w.getBlockMetadata(x, y, z) == 1) {
			unloadChunks(w, x, y, z);
		}
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		if (world.worldProvider.worldType != 0) {
			return false;
		}
		return super.canPlaceBlockAt(world, x, y, z);
	}
	
	public int idDropped(int metadata, Random rand) {
		return mod_ChunkLoaderPlus.chunkLoader.blockID;
	}
}
