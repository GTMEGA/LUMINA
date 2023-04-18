package com.falsepattern.lumina.internal.world;

import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.internal.world.lighting.LightingEngineHelpers;

public class WorldChunkSlice {
    private static final int DIAMETER = 5;

    private final ILumiChunk[] chunks;

    private final int x, z;

    public WorldChunkSlice(ILumiWorld world, int x, int z) {
        this.chunks = new ILumiChunk[DIAMETER * DIAMETER];

        int radius = DIAMETER / 2;

        for (int xDiff = -radius; xDiff <= radius; xDiff++) {
            for (int zDiff = -radius; zDiff <= radius; zDiff++) {
                ILumiChunk chunk = LightingEngineHelpers.getLoadedChunk(world, x + xDiff, z + zDiff);
                this.chunks[((xDiff + radius) * DIAMETER) + (zDiff + radius)] = chunk;
            }
        }

        this.x = x - radius;
        this.z = z - radius;
    }

    public ILumiChunk getChunk(int x, int z) {
        return this.chunks[(x * DIAMETER) + z];
    }

    public ILumiChunk getChunkFromWorldCoords(int x, int z) {
        return this.getChunk((x >> 4) - this.x, (z >> 4) - this.z);
    }

    public boolean isLoaded(int x, int z, int radius) {
        return this.isLoaded(x - radius, z - radius, x + radius, z + radius);
    }

    public boolean isLoaded(int xStart, int zStart, int xEnd, int zEnd) {
        xStart = (xStart >> 4) - this.x;
        zStart = (zStart >> 4) - this.z;
        xEnd = (xEnd >> 4) - this.x;
        zEnd = (zEnd >> 4) - this.z;

        for (int i = xStart; i <= xEnd; ++i) {
            for (int j = zStart; j <= zEnd; ++j) {
                if (this.getChunk(i, j) == null) {
                    return false;
                }
            }
        }

        return true;
    }
}
