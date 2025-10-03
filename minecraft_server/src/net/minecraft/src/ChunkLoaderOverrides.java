package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ChunkLoaderOverrides {
	@SuppressWarnings("unchecked")
	public static void hookChunkUnload(ChunkProviderServer cps) {
		try {
			Field unloadSetField = ChunkProviderServer.class.getDeclaredField("b"); // field_725_a
			unloadSetField.setAccessible(true);
			
			final Set<Integer> originalSet = (Set<Integer>) unloadSetField.get(cps);
			
			Set<Integer> proxySet = new HashSet<Integer>() {
				@Override
				public boolean add(Integer key) {
					int chunkX = getX(key);
					int chunkZ = getZ(key);
					
					for (Chunk c: mod_ChunkLoaderPlus.chunkRefCounts.keySet()) {
						if (c.xPosition == chunkX && c.zPosition == chunkZ) {
							mod_ChunkLoaderPlus.log("Minecraft tried to unload chunk at x: " + chunkX + ", z: " + chunkZ + ", overriding!", 0);
							return false;
						}
					}
					
					return super.add(key);
				}
			};
			
			proxySet.addAll(originalSet);
			unloadSetField.set(cps, proxySet);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void injectDoRandomUpdateTicks(WorldServer world) {
		try {
			Field activeChunkSetField = World.class.getDeclaredField("P"); // activeChunkSet
			activeChunkSetField.setAccessible(true);
			
			Set<ChunkCoordIntPair> proxySet = new HashSet<ChunkCoordIntPair>() {
				@Override
				public void clear() {
					super.clear();
					
					for (Chunk c : mod_ChunkLoaderPlus.chunkRefCounts.keySet()) {
						this.add(new ChunkCoordIntPair(c.xPosition, c.zPosition));
					}
				}
			};
			
			activeChunkSetField.set(world, proxySet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getX(int chunkKey) {
		return (chunkKey >> 16) & 0x7FFF | ((chunkKey & 0x80000000) != 0 ? -0x8000 : 0);
	}
	
	public static int getZ(int chunkKey) {
		return chunkKey & 0x7FFF | ((chunkKey & 0x8000) != 0 ? -0x8000 : 0);
	}
}