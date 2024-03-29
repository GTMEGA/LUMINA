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

package com.falsepattern.lumina.api.lighting;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static cpw.mods.fml.relauncher.Side.CLIENT;

@SuppressWarnings("unused")
public interface LumiLightingEngine {
    @NotNull String lightingEngineID();

    void writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output);

    void readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input);

    void cloneChunk(@NotNull LumiChunk from, @NotNull LumiChunk to);

    void writeSubChunkToNBT(@NotNull LumiChunk chunk,
                            @NotNull LumiSubChunk subChunk,
                            @NotNull NBTTagCompound output);

    void readSubChunkFromNBT(@NotNull LumiChunk chunk,
                             @NotNull LumiSubChunk subChunk,
                             @NotNull NBTTagCompound input);

    void cloneSubChunk(@NotNull LumiChunk fromChunk,
                       @NotNull LumiSubChunk from,
                       @NotNull LumiSubChunk to);

    void writeChunkToPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer output);

    void readChunkFromPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer input);

    void writeSubChunkToPacket(@NotNull LumiChunk chunk,
                               @NotNull LumiSubChunk subChunk,
                               @NotNull ByteBuffer input);

    void readSubChunkFromPacket(@NotNull LumiChunk chunk,
                                @NotNull LumiSubChunk subChunk,
                                @NotNull ByteBuffer output);

    int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ);

    int getCurrentLightValueChunk(@NotNull Chunk chunk, @NotNull LightType lightType, int chunkPosX, int posY, int chunkPosZ);

    boolean isChunkFullyLit(@NotNull LumiChunk chunk);

    void handleChunkInit(@NotNull LumiChunk chunk);

    @SideOnly(CLIENT)
    void handleClientChunkInit(@NotNull LumiChunk chunk);

    void handleSubChunkInit(@NotNull LumiChunk chunk, @NotNull LumiSubChunk subChunk);

    void handleChunkLoad(@NotNull LumiChunk chunk);

    void doRandomChunkLightingUpdates(@NotNull LumiChunk chunk);

    void updateLightingForBlock(@NotNull BlockPos blockPos);

    void updateLightingForBlock(int posX, int posY, int posZ);

    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        @NotNull BlockPos startBlockPos,
                                        @NotNull BlockPos endBlockPos);

    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        int startPosX,
                                        int startPosY,
                                        int startPosZ,
                                        int endPosX,
                                        int endPosY,
                                        int endPosZ);

    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ);

    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int startPosY, int endPosY);

    void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ);

    void processLightingUpdatesForType(@NotNull LightType lightType);

    void processLightingUpdatesForAllTypes();
}
