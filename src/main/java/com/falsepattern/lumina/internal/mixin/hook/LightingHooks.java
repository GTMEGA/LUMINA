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

package com.falsepattern.lumina.internal.mixin.hook;

import com.falsepattern.lumina.api.LumiChunkAPI;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.internal.config.LumiConfig;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Arrays;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static cpw.mods.fml.relauncher.Side.CLIENT;

@SuppressWarnings("ForLoopReplaceableByForEach")
@UtilityClass
public final class LightingHooks {
    private static final int DEFAULT_PRECIPITATION_HEIGHT = -999;

    public static int getCurrentLightValue(Chunk chunkBase,
                                           EnumSkyBlock baseLightType,
                                           int subChunkPosX,
                                           int posY,
                                           int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (int i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val lightValue = lightingEngine.getCurrentLightValue(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static int getCurrentLightValueUncached(Chunk chunkBase,
                                                   EnumSkyBlock baseLightType,
                                                   int subChunkPosX,
                                                   int posY,
                                                   int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (int i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val lightValue = lightingEngine.getCurrentLightValueUncached(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static int getMaxBrightness(Chunk chunkBase,
                                       EnumSkyBlock baseLightType,
                                       int subChunkPosX,
                                       int posY,
                                       int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val lightType = LightType.of(baseLightType);
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        var maxLightValue = 0;
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightValue = world.lumi$getBrightness(lightType, posX, posY, posZ);
            maxLightValue = Math.max(maxLightValue, lightValue);
        }
        return maxLightValue;
    }

    public static boolean isChunkFullyLit(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        var chunkHasLighting = true;
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            chunkHasLighting &= lightingEngine.isChunkFullyLit(chunk);
        }
        return chunkHasLighting;
    }

    public static void handleChunkInit(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        resetPrecipitationHeightMap(chunkBase);
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (int i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val chunk = world.lumi$wrap(chunkBase);
            LumiChunkAPI.scheduleChunkLightingEngineInit(chunk);
        }
    }

    @SideOnly(CLIENT)
    public static void handleClientChunkInit(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        resetPrecipitationHeightMap(chunkBase);
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            lightingEngine.handleClientChunkInit(chunk);
        }
    }

    public static void handleSubChunkInit(Chunk chunkBase, ExtendedBlockStorage subChunkBase) {
        val worldBase = chunkBase.worldObj;

        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            val subChunk = world.lumi$wrap(subChunkBase);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleSubChunkInit(Chunk chunkBase, int chunkPosY) {
        val worldBase = chunkBase.worldObj;

        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            val subChunk = chunk.lumi$getSubChunk(chunkPosY);
            lightingEngine.handleSubChunkInit(chunk, subChunk);
        }
    }

    public static void handleChunkLoad(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(chunkBase);
            lightingEngine.handleChunkLoad(chunk);
        }
    }

    public static void doRandomChunkLightingUpdates(Chunk chunkBase) {
        if (!LumiConfig.DO_RANDOM_LIGHT_UPDATES)
            return;

        if (!chunkBase.worldObj.isRemote && chunkBase.inhabitedTime < 10 * 20)
            return;

        val worldBase = chunkBase.worldObj;
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.doRandomChunkLightingUpdates(chunk);
        }
    }

    public static void resetQueuedRandomLightUpdates(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;

        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val chunk = world.lumi$wrap(chunkBase);
            chunk.lumi$resetQueuedRandomLightUpdates();
        }
    }

    public static void updateLightingForBlock(Chunk chunkBase,
                                              int subChunkPosX,
                                              int posY,
                                              int subChunkPosZ) {
        val worldBase = chunkBase.worldObj;
        val posX = (chunkBase.xPosition << 4) + subChunkPosX;
        val posZ = (chunkBase.zPosition << 4) + subChunkPosZ;

        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.updateLightingForBlock(posX, posY, posZ);
        }
    }

    public static void scheduleLightingUpdate(World worldBase,
                                              EnumSkyBlock baseLightType,
                                              int posX,
                                              int posY,
                                              int posZ) {
        val lightType = LightType.of(baseLightType);
        scheduleLightingUpdate(worldBase, lightType, posX, posY, posZ);
    }

    public static void scheduleLightingUpdate(World worldBase,
                                              LightType lightType,
                                              int posX,
                                              int posY,
                                              int posZ) {
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.scheduleLightingUpdate(lightType, posX, posY, posZ);
        }
    }

    public static void processLightingUpdatesForAllTypes(Chunk chunkBase) {
        val worldBase = chunkBase.worldObj;
        processLightingUpdatesForAllTypes(worldBase);
    }

    public static void processLightingUpdatesForAllTypes(World worldBase) {
        val lumiWorldsFromBaseWorld = lumiWorldsFromBaseWorld(worldBase);
        for (var i = 0; i < lumiWorldsFromBaseWorld.length; i++) {
            val world = lumiWorldsFromBaseWorld[i];
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightingUpdatesForAllTypes();
        }
    }

    private static void resetPrecipitationHeightMap(Chunk chunkBase) {
        Arrays.fill(chunkBase.precipitationHeightMap, DEFAULT_PRECIPITATION_HEIGHT);
    }
}
