package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ChunkLoaderOverrides {
	public static void injectUpdateBlocksAndPlayCaveSounds(World world) {
		try {
			Field positionsToUpdateSetField = World.class.getDeclaredField("P"); // positionsToUpdate
			positionsToUpdateSetField.setAccessible(true);
			
			Set<ChunkCoordIntPair> proxySet = new HashSet<ChunkCoordIntPair>() {
				@Override
				public void clear() {
					super.clear();
					
					for (Chunk c : mod_ChunkLoaderPlus.chunkRefCounts.keySet()) {
						this.add(new ChunkCoordIntPair(c.xPosition, c.zPosition));
					}
				}
			};
			
			positionsToUpdateSetField.set(world, proxySet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}