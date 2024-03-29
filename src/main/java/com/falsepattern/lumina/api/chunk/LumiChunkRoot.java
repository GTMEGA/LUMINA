/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.api.chunk;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface LumiChunkRoot {
    int BLOCK_LIGHT_ARRAY_SIZE = 16 * 16 * 16;
    int SKY_LIGHT_ARRAY_SIZE = 16 * 16 * 16;

    @NotNull String lumi$chunkRootID();

    boolean lumi$isUpdating();

    void lumi$markDirty();

    boolean lumi$isDirty();

    void lumi$prepareSubChunk(int chunkPosY);

    boolean lumi$isSubChunkPrepared(int chunkPosY);

    int lumi$topPreparedSubChunkBasePosY();

    @NotNull Block lumi$getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ);
}
