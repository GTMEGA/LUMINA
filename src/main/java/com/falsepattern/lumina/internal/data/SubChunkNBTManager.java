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

package com.falsepattern.lumina.internal.data;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SubChunkNBTManager implements ChunkDataManager.SectionNBTDataManager {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Sub Chunk NBT Manager");

    private static final String VERSION_NBT_TAG_NAME = Tags.MOD_ID + "_version";
    private static final String BLOCK_LIGHT_NBT_TAG_NAME = "block_light";
    private static final String SKY_LIGHT_NBT_TAG_NAME = "sky_light";

    private static final String VERSION_NBT_TAG_VALUE = Tags.VERSION;

    private static final SubChunkNBTManager INSTANCE = new SubChunkNBTManager();

    private boolean isRegistered = false;

    public static SubChunkNBTManager subChunkNBTManager() {
        return INSTANCE;
    }

    public void registerDataManager() {
        if (isRegistered)
            return;

        ChunkDataRegistry.registerDataManager(this);
        isRegistered = true;
        LOG.info("Registered data manager");
    }

    @Override
    public String domain() {
        return Tags.MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_sub_chunk";
    }

    @Override
    public void writeSectionToNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound output) {
        output.setString(VERSION_NBT_TAG_NAME, VERSION_NBT_TAG_VALUE);

        val lightValues = new NibbleArray(4096, 4);
        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val worldTagName = world.lumi$worldID();
            val worldTag = new NBTTagCompound();

            val subChunk = world.lumi$wrap(subChunkBase);
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();
            val hasSky = world.lumi$root().lumi$hasSky();

            {
                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
                            lightValues.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
                worldTag.setByteArray(BLOCK_LIGHT_NBT_TAG_NAME, lightValues.data);
            }

            if (hasSky) {
                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = subChunk.lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
                            lightValues.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
                worldTag.setByteArray(SKY_LIGHT_NBT_TAG_NAME, lightValues.data);
            }

            {
                val subChunkTagName = subChunk.lumi$subChunkID();
                val subChunkTag = new NBTTagCompound();
                subChunk.lumi$writeToNBT(subChunkTag);
                worldTag.setTag(subChunkTagName, subChunkTag);
            }

            {
                val lightingEngineTagName = lightingEngine.lightingEngineID();
                val lightingEngineTag = new NBTTagCompound();
                lightingEngine.lumi$writeToSubChunkNBT(chunk, subChunk, lightingEngineTag);
                worldTag.setTag(lightingEngineTagName, lightingEngineTag);
            }

            output.setTag(worldTagName, worldTag);
        }
    }

    @Override
    public void readSectionFromNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input) {
        val version = input.getString(VERSION_NBT_TAG_NAME);
        if (!VERSION_NBT_TAG_VALUE.equals(version))
            return;

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val subChunk = world.lumi$wrap(subChunkBase);
            val lightingEngine = world.lumi$lightingEngine();
            val hasSky = world.lumi$root().lumi$hasSky();

            val worldTagName = world.lumi$worldID();
            if (!input.hasKey(worldTagName, 10))
                continue;
            val worldTag = input.getCompoundTag(worldTagName);

            if (worldTag.hasKey(BLOCK_LIGHT_NBT_TAG_NAME, 7)) {
                val blockLightData = worldTag.getByteArray(BLOCK_LIGHT_NBT_TAG_NAME);
                val blockLightValues = new NibbleArray(blockLightData, 4);
                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = blockLightValues.get(subChunkPosX, subChunkPosY, subChunkPosZ);
                            subChunk.lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
            }

            if (hasSky && worldTag.hasKey(SKY_LIGHT_NBT_TAG_NAME, 7)) {
                val skyLightData = worldTag.getByteArray(SKY_LIGHT_NBT_TAG_NAME);
                val skyLightValues = new NibbleArray(skyLightData, 4);
                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = skyLightValues.get(subChunkPosX, subChunkPosY, subChunkPosZ);
                            subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
            }

            val subChunkTagName = subChunk.lumi$subChunkID();
            if (worldTag.hasKey(subChunkTagName, 10)) {
                val subChunkTag = worldTag.getCompoundTag(subChunkTagName);
                subChunk.lumi$readFromNBT(subChunkTag);
            }

            val lightingEngineTagName = lightingEngine.lightingEngineID();
            if (worldTag.hasKey(lightingEngineTagName, 10)) {
                val lightingEngineTag = worldTag.getCompoundTag(lightingEngineTagName);
                lightingEngine.lumi$readFromSubChunkNBT(chunk, subChunk, lightingEngineTag);
            }
        }
    }
}
