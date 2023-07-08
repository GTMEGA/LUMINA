/*
 * Copyright (C) 2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.api.chunk;

import net.minecraft.world.chunk.NibbleArray;
import org.jetbrains.annotations.Nullable;

public interface LumiSubChunk {
    LumiSubChunkRoot rootSubChunk();

    void setBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    void setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Deprecated
    NibbleArray blockLightValues();

    @Deprecated
    @Nullable NibbleArray skyLightValues();
}