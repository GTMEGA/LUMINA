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
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ChunkNBTManager implements ChunkDataManager.ChunkNBTDataManager {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Chunk NBT Manager");

    private static final ChunkNBTManager INSTANCE = new ChunkNBTManager();

    private static final String VERSION_NBT_TAG_NAME = Tags.MOD_ID + "_version";

    private static final String VERSION_NBT_TAG_VALUE = Tags.VERSION;

    private boolean isRegistered = false;

    public static ChunkNBTManager chunkNBTManager() {
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
        return "lumi_chunk";
    }

    @Override
    public void writeChunkToNBT(Chunk chunkBase, NBTTagCompound output) {
        output.setString(VERSION_NBT_TAG_NAME, VERSION_NBT_TAG_VALUE);
        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            val worldTag = new NBTTagCompound();
            writeChunkData(chunk, worldTag);
            writeLightingEngineData(chunk, lightingEngine, worldTag);
            output.setTag(worldTagName, worldTag);
        }
    }

    private static void writeLightingEngineData(LumiChunk chunk,
                                                LumiLightingEngine lightingEngine,
                                                NBTTagCompound output) {
        val lightingEngineTagName = lightingEngine.lightingEngineID();
        val lightingEngineTag = new NBTTagCompound();
        lightingEngine.lumi$writeChunkToNBT(chunk, lightingEngineTag);
        output.setTag(lightingEngineTagName, lightingEngineTag);
    }

    private static void writeChunkData(LumiChunk chunk, NBTTagCompound output) {
        val chunkTagName = chunk.lumi$chunkID();
        val chunkTag = new NBTTagCompound();
        chunk.lumi$writeToNBT(chunkTag);
        output.setTag(chunkTagName, chunkTag);
    }

    @Override
    public void readChunkFromNBT(Chunk chunkBase, NBTTagCompound input) {
        val version = input.getString(VERSION_NBT_TAG_NAME);
        if (!VERSION_NBT_TAG_VALUE.equals(version))
            return;

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            if (!input.hasKey(worldTagName, 10))
                continue;
            val worldTag = input.getCompoundTag(worldTagName);
            readChunkData(chunk, worldTag);
            readLightingEngineData(chunk, lightingEngine, worldTag);
        }
    }

    private static void readLightingEngineData(LumiChunk chunk,
                                               LumiLightingEngine lightingEngine,
                                               NBTTagCompound input) {
        val lightingEngineTagName = lightingEngine.lightingEngineID();
        if (input.hasKey(lightingEngineTagName, 10)) {
            val lightingEngineTag = input.getCompoundTag(lightingEngineTagName);
            lightingEngine.lumi$readChunkFromNBT(chunk, lightingEngineTag);
        }
    }

    private static void readChunkData(LumiChunk chunk, NBTTagCompound input) {
        val chunkTagName = chunk.lumi$chunkID();
        if (input.hasKey(chunkTagName, 10)) {
            val chunkTag = input.getCompoundTag(chunkTagName);
            chunk.lumi$readFromNBT(chunkTag);
        }
    }
}
