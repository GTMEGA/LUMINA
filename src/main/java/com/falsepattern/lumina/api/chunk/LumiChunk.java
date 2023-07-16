/*
 * Copyright (c) 2023 FalsePattern, Ven
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

import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface LumiChunk {
    int SUB_CHUNK_ARRAY_SIZE = 16;
    int HEIGHT_MAP_ARRAY_SIZE = 16 * 16;
    int MAX_QUEUED_RANDOM_LIGHT_UPDATES = 16 * 16 * 16;

    String IS_LIGHT_INITIALIZED_NBT_TAG_NAME = "lighting_initialized";
    String SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME = "sky_light_height_map";

    @NotNull LumiChunkRoot lumi$root();

    @NotNull LumiWorld lumi$world();

    @NotNull String lumi$chunkID();

    void lumi$writeToNBT(@NotNull NBTTagCompound output);

    void lumi$readFromNBT(@NotNull NBTTagCompound input);

    @Nullable LumiSubChunk lumi$getSubChunkIfPrepared(int chunkPosY);

    @NotNull LumiSubChunk lumi$getSubChunk(int chunkPosY);

    int lumi$chunkPosX();

    int lumi$chunkPosZ();

    void lumi$queuedRandomLightUpdates(int remainingRandomLightUpdates);

    int lumi$queuedRandomLightUpdates();

    int lumi$getBrightnessAndLightValueMax(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBrightnessAndBlockLightValueMax(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getLightValueMax(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight);

    int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ);

    void lumi$minSkyLightHeight(int minSkyLightPosY);

    int lumi$minSkyLightHeight();

    void lumi$resetSkyLightHeightMap();

    void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean isHeightOutdated);

    boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ);

    void lumi$resetOutdatedHeightFlags();

    void lumi$isLightingInitialized(boolean lightingInitialized);

    boolean lumi$isLightingInitialized();
}
