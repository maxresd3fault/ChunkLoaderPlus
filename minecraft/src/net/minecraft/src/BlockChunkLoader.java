package net.minecraft.src;

import java.util.Random;
import java.util.logging.Level;

public class BlockChunkLoader extends Block {
	public static final Material chunkLoaderMaterial = new Material(MapColor.ironColor).setImmovableMobility();
	
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
		
		if (!w.multiplayerWorld) {
			if (powered) {
				loadChunksAround(w, x, y, z);
			} else if (!powered) {
				unloadChunks(w, x, y, z);
			}
		}
	}
	
	public void loadChunksAround(World w, int x, int y, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		Chunk chunk = w.getChunkFromChunkCoords(chunkX, chunkZ);
		
		int count = mod_ChunkLoaderPlus.chunkRefCounts.getOrDefault(chunk, 0) + 1;
		mod_ChunkLoaderPlus.saveChunkToNBT(chunk);
		mod_ChunkLoaderPlus.chunkRefCounts.put(chunk, count);
		chunk.isChunkLoaded = true;
		mod_ChunkLoaderPlus.log(count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition, Level.INFO);
		ModLoader.getMinecraftInstance().thePlayer.addChatMessage("§a" + count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition);
	}
	
	public void unloadChunks(World w, int x, int y, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		Chunk chunk = w.getChunkFromChunkCoords(chunkX, chunkZ);
		
		boolean messageCase = false;
		int count = mod_ChunkLoaderPlus.chunkRefCounts.getOrDefault(chunk, 0) - 1;
		
		if (count <= 0) {
			chunk.isChunkLoaded = false;
			mod_ChunkLoaderPlus.deleteChunkFromNBT(chunk);
			mod_ChunkLoaderPlus.chunkRefCounts.remove(chunk);
			mod_ChunkLoaderPlus.log("Unloading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition, Level.INFO);
		} else {
			mod_ChunkLoaderPlus.chunkRefCounts.put(chunk, count);
			mod_ChunkLoaderPlus.log(count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition, Level.INFO);
			messageCase = true;
		}
		
		if (!messageCase) ModLoader.getMinecraftInstance().thePlayer.addChatMessage("§4Unloading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition);
		else if (messageCase) ModLoader.getMinecraftInstance().thePlayer.addChatMessage("§e" + count + " loader(s) are loading the chunk at x: " + chunk.xPosition + ", z: " + chunk.zPosition);
	}
	
	@Override
	public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
		int meta = world.getBlockMetadata(x, y, z);
		return (meta == 1) ? mod_ChunkLoaderPlus.clOn : mod_ChunkLoaderPlus.clOff;
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		return (meta == 1) ? mod_ChunkLoaderPlus.clOn : mod_ChunkLoaderPlus.clOff;
	}
	
	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int neighborBlockID) {
		updateState(w, x, y, z, false);
	}
	
	public void onBlockRemoval(World w, int x, int y, int z) {
		if (!w.multiplayerWorld && w.getBlockMetadata(x, y, z) == 1) {
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
	
	public void randomDisplayTick(World w, int x, int y, int z, Random rand) {
		int meta = w.getBlockMetadata(x, y, z);
		
		if (meta == 1) {
			for (int i = 0; i < 2; i++) {
				double particleX = (double) x + 0.5D;
				double particleY = (double) y + 0.5D;
				double particleZ = (double) z + 0.5D;
				int face = rand.nextInt(6);
				
				switch (face) {
					case 0:
						particleY = (double) y + 1.0D + 0.01D;
						break;
					case 1:
						particleY = (double) y - 0.01D;
						break;
					case 2:
						particleX = (double) x + 1.0D + 0.01D;
						break;
					case 3:
						particleX = (double) x - 0.01D;
						break;
					case 4:
						particleZ = (double) z + 1.0D + 0.01D;
						break;
					case 5:
						particleZ = (double) z - 0.01D;
				}
				
				if (face <= 1) {
					particleX += rand.nextDouble() - 0.5D;
					particleZ += rand.nextDouble() - 0.5D;
				} else if (face <= 3) {
					particleY += rand.nextDouble() - 0.5D;
					particleZ += rand.nextDouble() - 0.5D;
				} else {
					particleX += rand.nextDouble() - 0.5D;
					particleY += rand.nextDouble() - 0.5D;
				}
				
				double velocityX = (rand.nextDouble() - 1.0D) * 0.02D;
				double velocityY = (rand.nextDouble() - 1.0D) * 0.02D;
				double velocityZ = (rand.nextDouble() - 1.0D) * 0.02D;
				ParticleSparkle particle = new ParticleSparkle(w, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
				ModLoader.getMinecraftInstance().effectRenderer.addEffect(particle);
			}
		}
	}
}
