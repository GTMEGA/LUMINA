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

package com.falsepattern.lumina.internal.engine;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.world.WorldChunkSlice;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@UtilityClass
public final class LightingHooks {
    /**
     * 2 light types * 4 directions * 2 halves * (inwards + outwards)
     */
    public static final int FLAG_COUNT = 32;
    public static final String NEIGHBOR_LIGHT_CHECKS_KEY = "NeighborLightChecks";
    public static final int DEFAULT_PRECIPITATION_HEIGHT = -999;

    public static final EnumFacing[] HORIZONTAL_FACINGS = {EnumFacing.SOUTH,
                                                           EnumFacing.WEST,
                                                           EnumFacing.NORTH,
                                                           EnumFacing.EAST};

    public static void relightSkylightColumn(LumiWorld world,
                                             LumiChunk chunk,
                                             int subChunkPosX,
                                             int subChunkPosZ,
                                             int startPosY,
                                             int endPosY) {
        {
            val minPosY = Math.min(startPosY, endPosY);
            val maxPosY = Math.max(startPosY, endPosY) - 1;
            startPosY = minPosY;
            endPosY = maxPosY;
        }

        val basePosX = (chunk.lumi$chunkPosX() * 16) + subChunkPosX;
        val basePosZ = (chunk.lumi$chunkPosZ() * 16) + subChunkPosZ;

        val minChunkPosY = startPosY / 16;
        val maxChunkPosY = endPosY / 16;

        scheduleSkyLightUpdateForColumn(world, basePosX, basePosZ, startPosY, endPosY);

        val bottomSubChunk = chunk.lumi$subChunk(minChunkPosY);
        if (bottomSubChunk == null && startPosY > 0) {
            val lightingEngine = chunk.lumi$lightingEngine();
            val posY = startPosY - 1;
            lightingEngine.scheduleLightUpdate(EnumSkyBlock.Sky, basePosX, posY, basePosZ);
        }

        short flags = 0;
        for (var chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            if (subChunk != null)
                continue;

            val subChunkFlag = 1 << chunkPosY;
            flags |= subChunkFlag;
        }

        if (flags == 0)
            return;

        for (val direction : HORIZONTAL_FACINGS) {
            val chunkPosXOffset = direction.getFrontOffsetX();
            val chunkPosZOffset = direction.getFrontOffsetZ();
            val chunkPosX = chunk.lumi$chunkPosX() + chunkPosXOffset;
            val chunkPosZ = chunk.lumi$chunkPosZ() + chunkPosZOffset;

            // Checks whether the position is at the specified border (the 16 bit is set for both 15+1 and 0-1)
            val someInterestingExpression = ((subChunkPosX + chunkPosXOffset) | (subChunkPosZ + chunkPosZOffset)) & 16;
            val neighborChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (someInterestingExpression != 0 && neighborChunk == null) {
                val axisDirection = axisDirectionFromFacing(direction, subChunkPosX, subChunkPosZ);
                flagChunkBoundaryForUpdate(chunk,
                                           flags,
                                           EnumSkyBlock.Sky,
                                           direction,
                                           axisDirection,
                                           BoundaryFacing.OUT);
                continue;
            }

            for (var chunkPosY = maxChunkPosY; chunkPosY >= minChunkPosY; chunkPosY--) {
                val subChunkFlag = 1 << chunkPosY;
                val subChunkExists = (flags & subChunkFlag) != 0;
                if (!subChunkExists)
                    continue;

                val posX = basePosX + chunkPosXOffset;
                val posZ = basePosZ + chunkPosZOffset;
                val minPosY = chunkPosY * 16;
                val maxPosY = minPosY + 15;
                scheduleSkyLightUpdateForColumn(world, posX, posZ, minPosY, maxPosY);
            }
        }
    }

    public static void scheduleRelightChecksForArea(LumiWorld world,
                                                    EnumSkyBlock lightType,
                                                    int minPosX,
                                                    int minPosY,
                                                    int minPosZ,
                                                    int maxPosX,
                                                    int maxPosY,
                                                    int maxPosZ) {
        val lightingEngine = world.lumi$lightingEngine();
        for (var posY = minPosY; posY <= maxPosY; posY++)
            for (var posZ = minPosZ; posZ <= maxPosZ; posZ++)
                for (var posX = minPosX; posX <= maxPosX; posX++)
                    lightingEngine.scheduleLightUpdate(lightType, posX, posY, posZ);
    }

    public static void scheduleSkyLightUpdateForColumn(LumiWorld world,
                                                       int posX,
                                                       int posZ,
                                                       int minPosY,
                                                       int maxPosY) {
        val lightingEngine = world.lumi$lightingEngine();
        for (var posY = minPosY; posY <= maxPosY; posY++)
            lightingEngine.scheduleLightUpdate(EnumSkyBlock.Sky, posX, posY, posZ);
    }

    public static void relightBlock(LumiChunk chunk, int subChunkPosX, int posY, int subChunkPosZ) {
        var maxPosY = lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ) & 255;
        var minPosY = Math.max(posY, maxPosY);

        while (minPosY > 0 && chunk.lumi$getBlockOpacity(subChunkPosX, minPosY - 1, subChunkPosZ) == 0)
            --minPosY;
        if (minPosY == maxPosY)
            return;

        lumiSetHeightValue(chunk, subChunkPosX, subChunkPosZ, minPosY);

        if (chunk.lumi$world().lumi$root().lumi$hasSky())
            relightSkylightColumn(chunk.lumi$world(), chunk, subChunkPosX, subChunkPosZ, maxPosY, minPosY);

        maxPosY = lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ);
        if (maxPosY < chunk.lumi$minSkyLightHeight())
            chunk.lumi$minSkyLightHeight(maxPosY);
    }

    public static void doRecheckGaps(LumiChunk chunk, boolean onlyOne) {
        val world = chunk.lumi$world();
        val worldRoot = world.lumi$root();
        val profiler = worldRoot.lumi$profiler();

        profiler.startSection("recheckGaps");
        profilerSection:
        {
            val chunkPosX = chunk.lumi$chunkPosX();
            val chunkPosZ = chunk.lumi$chunkPosZ();

            val centerPosX = (chunkPosX * 16) + 8;
            val centerPosY = 0;
            val centerPosZ = (chunkPosZ * 16) + 8;
            val blockRange = 16;

            val slice = new WorldChunkSlice(world, chunkPosX, chunkPosZ);
            if (!worldRoot.lumi$doChunksExist(centerPosX, centerPosY, centerPosZ, blockRange))
                break profilerSection;

            for (int subChunkPosZ = 0; subChunkPosZ < 16; ++subChunkPosZ) {
                for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
                    if (!recheckGapsForColumn(chunk, slice, subChunkPosX, subChunkPosZ))
                        continue;
                    if (onlyOne)
                        break profilerSection;
                }
            }

            chunk.lumi$root().lumi$shouldRecheckLightingGaps(false);
        }
        profiler.endSection();
    }

    public static boolean recheckGapsForColumn(LumiChunk chunk,
                                               WorldChunkSlice slice,
                                               int subChunkPosX,
                                               int subChunkPosZ) {
        val index = subChunkPosX + (subChunkPosZ * 16);
        if (!chunk.lumi$outdatedSkyLightColumns()[index])
            return false;

        val posX = (chunk.lumi$chunkPosX() * 16) + subChunkPosX;
        val posZ = (chunk.lumi$chunkPosZ() * 16) + subChunkPosZ;
        val minPosY = recheckGapsGetLowestHeight(slice, posX, posZ);
        val maxPosY = lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ);

        recheckGapsSkylightNeighborHeight(chunk, slice, posX, posZ, maxPosY, minPosY);
        chunk.lumi$outdatedSkyLightColumns()[index] = false;
        return true;
    }

    public static int recheckGapsGetLowestHeight(WorldChunkSlice slice, int posX, int posZ) {
        var minPosY = Integer.MAX_VALUE;
        for (val facing : HORIZONTAL_FACINGS) {
            val neighbourPosX = posX + facing.getFrontOffsetX();
            val neighbourPosZ = posZ + facing.getFrontOffsetZ();
            val chunk = slice.getChunkFromWorldCoords(neighbourPosX, neighbourPosZ);

            minPosY = Math.min(minPosY, chunk.lumi$minSkyLightHeight());
        }
        return minPosY;
    }

    public static void recheckGapsSkylightNeighborHeight(LumiChunk chunk,
                                                         WorldChunkSlice slice,
                                                         int posX,
                                                         int posZ,
                                                         int height,
                                                         int max) {
        checkSkylightNeighborHeight(chunk, slice, posX, posZ, max);
        for (val facing : HORIZONTAL_FACINGS) {
            val neighbourPosX = posX + facing.getFrontOffsetX();
            val neighbourPosZ = posZ + facing.getFrontOffsetZ();
            checkSkylightNeighborHeight(chunk, slice, neighbourPosX, neighbourPosZ, height);
        }
    }

    public static void checkSkylightNeighborHeight(LumiChunk chunk,
                                                   WorldChunkSlice slice,
                                                   int posX,
                                                   int posZ,
                                                   int maxValue) {
        val subChunkPosX = posX & 15;
        val subChunkPosZ = posZ & 15;

        val neighbourChunk = slice.getChunkFromWorldCoords(posX, posZ);
        val neighbourMaxY = lumiGetHeightValue(neighbourChunk, subChunkPosX, subChunkPosZ);
        if (neighbourMaxY > maxValue) {
            val maxPosY = neighbourMaxY + 1;
            updateSkylightNeighborHeight(chunk, slice, posX, posZ, maxValue, maxPosY);
        } else if (neighbourMaxY < maxValue) {
            val maxPosY = maxValue + 1;
            updateSkylightNeighborHeight(chunk, slice, posX, posZ, neighbourMaxY, maxPosY);
        }
    }

    public static void updateSkylightNeighborHeight(LumiChunk chunk,
                                                    WorldChunkSlice slice,
                                                    int posX,
                                                    int posZ,
                                                    int minPosY,
                                                    int maxPosY) {
        if (maxPosY <= minPosY)
            return;
        if (!slice.isLoaded(posX, posZ, 16))
            return;

        for (var posY = minPosY; posY < maxPosY; posY++)
            chunk.lumi$lightingEngine().scheduleLightUpdate(EnumSkyBlock.Sky, posX, posY, posZ);
        chunk.lumi$root().lumi$markDirty();
    }

    public static boolean lumiCanBlockSeeTheSky(LumiChunk iLumiChunk, int subChunkPosX, int posY, int subChunkPosZ) {
        val index = subChunkPosX + (subChunkPosZ * 16);
        return posY >= iLumiChunk.lumi$skyLightHeights()[index];
    }

    public static void lumiSetSkylightUpdatedPublic(LumiChunk iLumiChunk) {
        Arrays.fill(iLumiChunk.lumi$outdatedSkyLightColumns(), true);
    }

    public static void lumiSetHeightValue(LumiChunk iLumiChunk, int subChunkPosX, int subChunkPosZ, int height) {
        val index = subChunkPosX + (subChunkPosZ * 16);
        iLumiChunk.lumi$skyLightHeights()[index] = height;
    }

    public static int lumiGetHeightValue(LumiChunk iLumiChunk, int subChunkPosX, int subChunkPosZ) {
        val index = subChunkPosX + (subChunkPosZ * 16);
        return iLumiChunk.lumi$skyLightHeights()[index];
    }

    /**
     * Generates the initial skylight map for the chunk upon generation or load.
     */
    public static void generateSkylightMap(LumiChunk chunk) {
        val basePosX = chunk.lumi$chunkPosX() * 16;
        val basePosZ = chunk.lumi$chunkPosZ() * 16;

        val rootChunk = chunk.lumi$root();
        val baseChunk = rootChunk.lumi$base();

        val maxChunkPosY = rootChunk.lumi$topPreparedSubChunkPosY();
        val hasSky = chunk.lumi$world().lumi$root().lumi$hasSky();

        chunk.lumi$minSkyLightHeight(Integer.MAX_VALUE);
        int heightMapMinimum = Integer.MAX_VALUE;
        val heightMap = chunk.lumi$skyLightHeights();
        for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
            int subChunkPosZ = 0;
            while (subChunkPosZ < 16) {
                val index = subChunkPosX + (subChunkPosZ * 16);

                baseChunk.precipitationHeightMap[index] = DEFAULT_PRECIPITATION_HEIGHT;
                int posY = maxChunkPosY + 16 - 1;

                while (true) {
                    if (posY > 0) {
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY - 1, subChunkPosZ);
                        if (blockOpacity == 0) {
                            --posY;
                            continue;
                        }

                        heightMap[index] = posY;
                        if (posY < heightMapMinimum)
                            heightMapMinimum = posY;
                    }

                    if (hasSky) {
                        var lightLevel = 15;
                        posY = (maxChunkPosY + 16) - 1;

                        do {
                            var blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);
                            if (blockOpacity == 0 && lightLevel != 15)
                                blockOpacity = 1;

                            lightLevel -= blockOpacity;
                            if (lightLevel > 0) {
                                val chunkPosY = posY / 16;
                                val subChunkPosY = posY & 15;

                                val subChunk = chunk.lumi$subChunk(chunkPosY);
                                if (subChunk != null) {
                                    val posX = basePosX + subChunkPosX;
                                    val posZ = basePosZ + subChunkPosZ;

                                    subChunk.lumi$setSkyLightValue(subChunkPosX,
                                                                   subChunkPosY,
                                                                   subChunkPosZ,
                                                                   lightLevel);
                                    chunk.lumi$world().lumi$root().lumi$markBlockForRenderUpdate(posX, posY, posZ);
                                }
                            }

                            --posY;
                        }
                        while (posY > 0 && lightLevel > 0);
                    }

                    subChunkPosZ++;
                    break;
                }
            }
        }

        chunk.lumi$root().lumi$markDirty();
        chunk.lumi$minSkyLightHeight(heightMapMinimum);
    }

    /**
     * Generates the height map for a chunk from scratch
     *
     * @param chunk
     */
    @SideOnly(Side.CLIENT)
    public static void generateHeightMap(LumiChunk chunk) {
        val chunkPosY = chunk.lumi$root().lumi$topPreparedSubChunkPosY();
        val heightMap = chunk.lumi$skyLightHeights();

        var heightMapMinimum = Integer.MAX_VALUE;
        for (int subChunkPosX = 0; subChunkPosX < 16; ++subChunkPosX) {
            int subChunkPosZ = 0;

            while (subChunkPosZ < 16) {
                val baseChunk = chunk.lumi$root().lumi$base();
                val index = subChunkPosX + (subChunkPosZ * 16);
                baseChunk.precipitationHeightMap[index] = DEFAULT_PRECIPITATION_HEIGHT;

                var posY = chunkPosY + 16 - 1;
                while (true) {
                    if (posY > 0) {
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY - 1, subChunkPosZ);
                        if (blockOpacity == 0) {
                            --posY;
                            continue;
                        }

                        heightMap[index] = posY;
                        if (posY < heightMapMinimum)
                            heightMapMinimum = posY;
                    }

                    ++subChunkPosZ;
                    break;
                }
            }
        }

        chunk.lumi$minSkyLightHeight(heightMapMinimum);
        chunk.lumi$root().lumi$markDirty();
    }

    public static void flagSecBoundaryForUpdate(LumiChunk chunk,
                                                BlockPos blockPos,
                                                EnumSkyBlock lightType,
                                                EnumFacing facing,
                                                BoundaryFacing boundaryFacing) {
        val posX = blockPos.getX();
        val posZ = blockPos.getZ();
        val chunkPosY = blockPos.getY() / 16;

        val axisDir = axisDirectionFromFacing(facing, posX, posZ);
        val sectionMask = (short) (1 << chunkPosY);
        flagChunkBoundaryForUpdate(chunk, sectionMask, lightType, facing, axisDir, boundaryFacing);
    }

    public static void flagChunkBoundaryForUpdate(LumiChunk chunk,
                                                  short subChunkMask,
                                                  EnumSkyBlock lightType,
                                                  EnumFacing facing,
                                                  AxisDirection axisDirection,
                                                  BoundaryFacing boundaryFacing) {
        val flagIndex = getFlagIndex(lightType, facing, axisDirection, boundaryFacing);
        chunk.lumi$neighborLightCheckFlags()[flagIndex] |= subChunkMask;
        chunk.lumi$root().lumi$markDirty();
    }

    public static int getFlagIndex(EnumSkyBlock lightType,
                                   EnumFacing facing,
                                   AxisDirection axisDirection,
                                   BoundaryFacing boundaryFacing) {
        val facingOffsetX = facing.getFrontOffsetX();
        val facingOffsetZ = facing.getFrontOffsetZ();
        return getFlagIndex(lightType, facingOffsetX, facingOffsetZ, axisDirection, boundaryFacing);
    }

    public static int getFlagIndex(EnumSkyBlock lightType,
                                   int facingOffsetX,
                                   int facingOffsetZ,
                                   AxisDirection axisDirection,
                                   BoundaryFacing boundaryFacing) {
        final int lightTypeBits;
        switch (lightType) {
            default:
            case Sky:
                lightTypeBits = 0x10;
                break;
            case Block:
                lightTypeBits = 0x00;
                break;
        }

        val facingOffsetXBits = (facingOffsetX + 1) << 2;
        val facingOffsetZBits = (facingOffsetZ + 1) << 1;
        val axisDirectionOffsetBits = axisDirection.sign() + 1;
        val boundaryFacingBits = boundaryFacing.ordinal();

        return lightTypeBits |
               facingOffsetXBits |
               facingOffsetZBits |
               axisDirectionOffsetBits |
               boundaryFacingBits;
    }

    public static AxisDirection axisDirectionFromFacing(EnumFacing facing, int facingOffsetX, int facingOffsetZ) {
        val subChunkPosX = facingOffsetX & 15;
        val subChunkPosZ = facingOffsetZ & 15;

        if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
            if (subChunkPosZ < 8)
                return AxisDirection.NEGATIVE;
        } else {
            if (subChunkPosX < 8)
                return AxisDirection.NEGATIVE;
        }
        return AxisDirection.POSITIVE;
    }

    public static void scheduleRelightChecksForChunkBoundaries(LumiWorld world, LumiChunk chunk) {
        for (val direction : HORIZONTAL_FACINGS) {
            val offsetX = direction.getFrontOffsetX();
            val offsetZ = direction.getFrontOffsetZ();

            val chunkPosX = chunk.lumi$chunkPosX() + offsetX;
            val chunkPosZ = chunk.lumi$chunkPosZ() + offsetZ;

            val nChunk = getLoadedChunk(chunk.lumi$world(), chunkPosX, chunkPosZ);

            if (nChunk == null)
                continue;

            for (val lightType : EnumSkyBlock.values()) {
                for (val axisDirection : AxisDirection.values()) {
                    //Merge flags upon loading of a chunk. This ensures that all flags are always already on the IN boundary below
                    mergeFlags(lightType, chunk, nChunk, direction, axisDirection);
                    mergeFlags(lightType, nChunk, chunk, oppositeFacing(direction), axisDirection);

                    //Check everything that might have been canceled due to this chunk not being loaded.
                    //Also, pass in chunks if already known
                    //The boundary to the neighbor chunk (both ways)
                    scheduleRelightChecksForBoundary(world, chunk, nChunk, null, lightType, offsetX, offsetZ, axisDirection);
                    scheduleRelightChecksForBoundary(world, nChunk, chunk, null, lightType, -offsetX, -offsetZ, axisDirection);
                    //The boundary to the diagonal neighbor (since the checks in that chunk were aborted if this chunk wasn't loaded, see scheduleRelightChecksForBoundary)
                    scheduleRelightChecksForBoundary(world,
                                                     nChunk,
                                                     null,
                                                     chunk,
                                                     lightType,
                                                     (offsetZ != 0 ? axisDirection.sign() : 0),
                                                     (offsetX != 0 ? axisDirection.sign() : 0),
                                                     axisDirectionFromFacing(direction).opposite());
                }
            }
        }
    }

    public static void mergeFlags(EnumSkyBlock lightType,
                                  LumiChunk inChunk,
                                  LumiChunk outChunk,
                                  EnumFacing facing,
                                  AxisDirection axisDirection) {
        if (outChunk.lumi$neighborLightCheckFlags() == null)
            return;

        val inIndex = getFlagIndex(lightType, facing, axisDirection, BoundaryFacing.IN);
        val outIndex = getFlagIndex(lightType, oppositeFacing(facing), axisDirection, BoundaryFacing.OUT);

        inChunk.lumi$neighborLightCheckFlags()[inIndex] |= outChunk.lumi$neighborLightCheckFlags()[outIndex];
        // no need to call Chunk.setModified() since checks are not deleted from outChunk
    }

    public static void scheduleRelightChecksForBoundary(LumiWorld world,
                                                        LumiChunk chunk,
                                                        LumiChunk nChunk,
                                                        LumiChunk sChunk,
                                                        EnumSkyBlock lightType,
                                                        int xOffset,
                                                        int zOffset,
                                                        AxisDirection axisDirection) {
        // OUT checks from neighbor are already merged
        val inFlagIndex = getFlagIndex(lightType, xOffset, zOffset, axisDirection, BoundaryFacing.IN);
        val flags = chunk.lumi$neighborLightCheckFlags()[inFlagIndex];

        if (flags == 0)
            return;

        if (nChunk == null) {
            val chunkPosX = chunk.lumi$chunkPosX() + xOffset;
            val chunkPosZ = chunk.lumi$chunkPosZ() + zOffset;
            nChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (nChunk == null)
                return;
        }

        if (sChunk == null) {
            val chunkPosX = chunk.lumi$chunkPosX() + (zOffset != 0 ? axisDirection.sign() : 0);
            val chunkPosZ = chunk.lumi$chunkPosZ() + (xOffset != 0 ? axisDirection.sign() : 0);

            sChunk = getLoadedChunk(world, chunkPosX, chunkPosZ);
            if (sChunk == null)
                return;
        }

        val outFlagIndex = getFlagIndex(lightType, -xOffset, -zOffset, axisDirection, BoundaryFacing.OUT);
        chunk.lumi$neighborLightCheckFlags()[inFlagIndex] = 0;
        nChunk.lumi$neighborLightCheckFlags()[outFlagIndex] = 0; //Clear only now that it's clear that the checks are processed

        chunk.lumi$root().lumi$markDirty();
        nChunk.lumi$root().lumi$markDirty();

        // Get the area to check
        // Start in the corner...
        int minPosX = chunk.lumi$chunkPosX() << 4;
        int minPosZ = chunk.lumi$chunkPosZ() << 4;

        // move to other side of chunk if the direction is positive
        if ((xOffset | zOffset) > 0) {
            minPosX += xOffset * 15;
            minPosZ += zOffset * 15;
        }

        // shift to other half if necessary (shift perpendicular to dir)
        if (axisDirection == AxisDirection.POSITIVE) {
            minPosX += (zOffset & 1) * 8; //x & 1 is same as abs(x) for x=-1,0,1
            minPosZ += (xOffset & 1) * 8;
        }

        // get maximal values (shift perpendicular to dir)
        val maxPosX = (7 * (zOffset & 1)) + minPosX;
        val maxPosZ = (7 * (xOffset & 1)) + minPosZ;

        for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
            if ((flags & (1 << chunkPosY)) != 0) {
                val minPosY = chunkPosY * 16;
                val maxPosY = minPosY + 15;
                scheduleRelightChecksForArea(world, lightType, minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ);
            }
        }
    }

    public static void writeNeighborLightChecksToNBT(LumiChunk chunk, NBTTagCompound output) {
        val neighborLightCheckFlags = chunk.lumi$neighborLightCheckFlags();
        var empty = true;
        val flagList = new NBTTagList();
        for (val flag : neighborLightCheckFlags) {
            val flagTag = new NBTTagShort(flag);
            flagList.appendTag(new NBTTagShort(flag));
            if (flag != 0)
                empty = false;
        }
        if (!empty)
            output.setTag(NEIGHBOR_LIGHT_CHECKS_KEY, flagList);
    }

    public static void readNeighborLightChecksFromNBT(LumiChunk chunk, NBTTagCompound input) {
        if (!input.hasKey(NEIGHBOR_LIGHT_CHECKS_KEY, 9))
            return;

        val list = input.getTagList(NEIGHBOR_LIGHT_CHECKS_KEY, 2);
        if (list.tagCount() != FLAG_COUNT) {
            Share.LOG.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})",
                           NEIGHBOR_LIGHT_CHECKS_KEY,
                           chunk.lumi$chunkPosX(),
                           chunk.lumi$chunkPosZ());
            return;
        }

        val neighborLightCheckFlags = chunk.lumi$neighborLightCheckFlags();
        for (var flagIndex = 0; flagIndex < FLAG_COUNT; ++flagIndex) {
            val flagTag = (NBTTagShort) list.tagList.get(flagIndex);
            val flag = flagTag.func_150289_e();
            neighborLightCheckFlags[flagIndex] = flag;
        }
    }

    public static void initChunkLighting(LumiWorld world, LumiChunk chunk) {
        val basePosX = chunk.lumi$chunkPosX() * 16;
        val basePosZ = chunk.lumi$chunkPosZ() * 16;

        val minPosX = basePosX - 16;
        val minPosY = 0;
        val minPosZ = basePosZ - 16;

        val maxPosX = basePosX + 31;
        val maxPosY = 255;
        val maxPosZ = basePosZ + 31;

        if (!world.lumi$root().lumi$doChunksExist(minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ))
            return;

        for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            if (subChunk == null)
                continue;

            val basePosY = chunkPosY * 16;
            for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                    for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                        val brightness = chunk.lumi$getBlockBrightness(subChunkPosX, subChunkPosY, subChunkPosZ);
                        if (brightness > 0) {
                            val posX = basePosX + subChunkPosX;
                            val posY = basePosY + subChunkPosY;
                            val posZ = basePosZ + subChunkPosZ;
                            world.lumi$lightingEngine().scheduleLightUpdate(EnumSkyBlock.Block, posX, posY, posZ);
                        }
                    }
                }
            }
        }

        if (world.lumi$root().lumi$hasSky()) {
            lumiSetSkylightUpdatedPublic(chunk);
            doRecheckGaps(chunk, false);
        }

        chunk.lumi$hasLightInitialized(true);
    }

    public static boolean checkChunkLighting(LumiWorld world, LumiChunk chunk) {
        if (!chunk.lumi$hasLightInitialized())
            initChunkLighting(world, chunk);

        for (int zOffset = -1; zOffset <= 1; ++zOffset) {
            for (int xOffset = -1; xOffset <= 1; ++xOffset) {
                if (xOffset == 0 && zOffset == 0)
                    continue;

                val chunkPosX = chunk.lumi$chunkPosX() + xOffset;
                val chunkPosZ = chunk.lumi$chunkPosZ() + zOffset;
                val chunkNeighbour = getLoadedChunk(world, chunkPosX, chunkPosZ);

                if (chunkNeighbour == null || !chunkNeighbour.lumi$hasLightInitialized())
                    return false;
            }
        }

        return true;
    }

    public static void initSkyLightForSubChunk(LumiWorld world, LumiChunk chunk, LumiSubChunk subChunk) {
        if (!world.lumi$root().lumi$hasSky())
            return;

        val maxPosY = subChunk.lumi$root().lumi$posY();
        val lightValue = EnumSkyBlock.Sky.defaultLightValue;
        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++)
            for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++)
                if (lumiGetHeightValue(chunk, subChunkPosX, subChunkPosZ) <= maxPosY)
                    for (var posY = 0; posY < 16; posY++)
                        subChunk.lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
    }

    public static @Nullable LumiChunk getLoadedChunk(LumiWorld world, int chunkPosX, int chunkPosZ) {
        val provider = world.lumi$root().lumi$chunkProvider();
        if (!provider.chunkExists(chunkPosX, chunkPosZ))
            return null;

        val baseChunk = provider.provideChunk(chunkPosX, chunkPosZ);
        return world.lumi$wrap(baseChunk);
    }

    public static AxisDirection axisDirectionFromFacing(EnumFacing facing) {
        switch (facing) {
            default:
            case UP:
            case SOUTH:
            case EAST:
                return AxisDirection.POSITIVE;
            case DOWN:
            case NORTH:
            case WEST:
                return AxisDirection.NEGATIVE;
        }
    }

    public static EnumFacing oppositeFacing(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.NORTH;
            case EAST:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.EAST;
            case DOWN:
                return EnumFacing.UP;
            default:
            case UP:
                return EnumFacing.DOWN;
        }
    }

    public enum BoundaryFacing {
        IN, OUT;

        public BoundaryFacing opposite() {
            return opposite(this);
        }

        private static BoundaryFacing opposite(BoundaryFacing boundaryFacing) {
            switch (boundaryFacing) {
                case IN:
                    return OUT;
                default:
                case OUT:
                    return IN;
            }
        }
    }
}
