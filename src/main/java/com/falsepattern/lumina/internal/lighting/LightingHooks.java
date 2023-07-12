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

package com.falsepattern.lumina.internal.lighting;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@UtilityClass
public final class LightingHooks {
    public static void generateHeightMap(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.generateHeightMap(chunk);
        }
    }

    public static void generateSkylightMap(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.generateSkylightMap(chunk);
        }
    }

    public static int getLightValue(Chunk baseChunk,
                                    EnumSkyBlock lightType,
                                    int subChunkPosX,
                                    int posY,
                                    int subChunkPosZ) {
        val baseWorld = baseChunk.worldObj;

        var lightValue = 0;
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);

            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightUpdates(lightType);

            val chunkLightValue = chunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
            lightValue = Math.max(lightValue, chunkLightValue);
        }
        return lightValue;
    }

    public static void initSkyLightForSubChunk(World baseWorld, Chunk baseChunk, ExtendedBlockStorage baseSubChunk) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = world.lumi$wrap(baseSubChunk);
            LightingHooksOld.initSkyLightForSubChunk(world, chunk, subChunk);
        }
    }

    public static void initSkyLightForSubChunk(World baseWorld, Chunk baseChunk, int chunkPosY) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            LightingHooksOld.initSkyLightForSubChunk(world, chunk, subChunk);
        }
    }

    public static void recheckLightingGaps(Chunk baseChunk, boolean onlyOne) {
        val baseWorld = baseChunk.worldObj;

        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.doRecheckGaps(chunk, onlyOne);
        }
    }

    public static void scheduleLightUpdates(World baseWorld,
                                            EnumSkyBlock lightType,
                                            int posX,
                                            int posY,
                                            int posZ) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.scheduleLightUpdate(lightType, posX, posY, posZ);
        }
    }

    public static void processLightUpdates(World baseWorld) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightUpdates();
        }
    }

    public static void relightBlockIfCanSeeSky(Chunk baseChunk, int subChunkPosX, int posY, int subChunkPosZ) {
        val baseWorld = baseChunk.worldObj;

        val index = subChunkPosX + (subChunkPosZ * 16);
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);

            val maxPosY = chunk.lumi$skyLightHeights()[index];
            if (posY >= maxPosY - 1)
                LightingHooksOld.relightBlock(chunk, subChunkPosX, posY + 1, subChunkPosZ);
        }
    }

    public static void scheduleRelightChecksForChunkBoundaries(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            LightingHooksOld.scheduleRelightChecksForChunkBoundaries(world, chunk);
        }
    }

    public static int getBrightnessAndLightValueMax(Chunk baseChunk,
                                                    EnumSkyBlock lightType,
                                                    int subChunkPosX,
                                                    int posY,
                                                    int subChunkPosZ) {
        val chunk = (LumiChunk) baseChunk;
        return chunk.lumi$getBrightnessAndLightValueMax(lightType, subChunkPosX, posY, subChunkPosZ);
    }

    public static boolean doesChunkHaveLighting(Chunk baseChunk) {
        val baseWorld = baseChunk.worldObj;

        var chunkHasLighting = true;
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val chunk = world.lumi$wrap(baseChunk);
            chunkHasLighting &= LightingHooksOld.checkChunkLighting(world, chunk);
        }
        return chunkHasLighting;
    }

    public static void randomLightUpdates(Chunk baseChunk) {
        if (baseChunk.queuedLightChecks >= (16 * 16 * 16))
            return;

        val baseWorld = baseChunk.worldObj;

        val chunkPosX = baseChunk.xPosition;
        val chunkPosZ = baseChunk.zPosition;
        val chunkPos = new ChunkCoordIntPair(chunkPosX, chunkPosZ);

        val isActiveChunk = baseWorld.activeChunkSet.contains(chunkPos);
        final int maxUpdateIterations;
        if (baseWorld.isRemote && isActiveChunk) {
            maxUpdateIterations = 256;
        } else if (baseWorld.isRemote) {
            maxUpdateIterations = 64;
        } else {
            maxUpdateIterations = 32;
        }

        val minPosX = chunkPosX * 16;
        val minPosZ = chunkPosZ * 16;

        val worldCount = LumiWorldManager.lumiWorldCount();

        var remainingIterations = maxUpdateIterations;
        while (remainingIterations > 0) {
            if (baseChunk.queuedLightChecks >= (16 * 16 * 16))
                return;
            remainingIterations--;

            val chunkPosY = baseChunk.queuedLightChecks % 16;
            val subChunkPosX = (baseChunk.queuedLightChecks / 16) % 16;
            val subChunkPosZ = baseChunk.queuedLightChecks / (16 * 16);
            baseChunk.queuedLightChecks++;

            val minPosY = chunkPosY * 16;

            val posX = minPosX + subChunkPosX;
            val posZ = minPosZ + subChunkPosZ;

            for (var i = 0; i < worldCount; i++) {
                val world = LumiWorldManager.getWorld(baseWorld, i);
                val chunk = world.lumi$wrap(baseChunk);
                if (!chunk.lumi$root().lumi$isSubChunkPrepared(chunkPosY))
                    continue;

                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    val posY = minPosY + subChunkPosY;

                    cornerCheck:
                    {
                        if (subChunkPosX != 0 && subChunkPosX != 15)
                            break cornerCheck;
                        if (subChunkPosY != 0 && subChunkPosY != 15)
                            break cornerCheck;
                        if (subChunkPosZ != 0 && subChunkPosZ != 15)
                            break cornerCheck;

                        // Perform a full lighting update
                        baseWorld.func_147451_t(posX, posY, posZ);
                        continue;
                    }

                    renderUpdateCheck:
                    {
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);
                        if (blockOpacity < 15)
                            break renderUpdateCheck;

                        val blockBrightness = chunk.lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
                        if (blockBrightness > 0)
                            break renderUpdateCheck;

                        val lightValue = chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
                        if (lightValue == 0)
                            continue;

                        chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, 0);
                        baseWorld.markBlockRangeForRenderUpdate(posX, posY, posZ, posX, posY, posZ);
                        break;
                    }

                    // Perform a full lighting update
                    baseWorld.func_147451_t(posX, posY, posZ);
                    break;
                }
            }
        }
    }
}