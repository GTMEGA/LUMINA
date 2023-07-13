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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;
import static com.falsepattern.lumina.internal.lighting.LightingHooksOld.FLAG_COUNT;

@Unique
@Mixin(Chunk.class)
public abstract class LumiChunkImplMixin implements LumiChunk {
    @Final
    @Shadow
    public int xPosition;
    @Final
    @Shadow
    public int zPosition;

    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public boolean[] updateSkylightColumns;
    @Shadow
    public World worldObj;
    @Shadow
    public int[] heightMap;
    @Shadow
    public int heightMapMinimum;

    private LumiChunkRoot lumi$root;
    private LumiWorld lumi$world;
    private short[] lumi$neighborLightCheckFlags;
    private boolean lumi$lightingInitialized;

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void lumiChunkInit(CallbackInfo ci) {
        this.lumi$root = (LumiChunkRoot) this;
        this.lumi$world = (LumiWorld) worldObj;
        this.lumi$neighborLightCheckFlags = new short[FLAG_COUNT];
        this.lumi$lightingInitialized = false;
    }

    @Override
    public LumiChunkRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public LumiWorld lumi$world() {
        return lumi$world;
    }

    @Override
    public @Nullable LumiSubChunk lumi$subChunk(int chunkPosY) {
        val subChunk = storageArrays[chunkPosY];
        if (subChunk instanceof LumiSubChunk)
            return (LumiSubChunk) subChunk;
        return null;
    }

    @Override
    public int lumi$chunkPosX() {
        return xPosition;
    }

    @Override
    public int lumi$chunkPosZ() {
        return zPosition;
    }

    @Override
    public int lumi$getBrightnessAndLightValueMax(LightType lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBrightnessAndBlockLightValueMax(subChunkPosX, posY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightnessAndBlockLightValueMax(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockBrightness = lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockBrightness, blockLightValue);
    }

    @Override
    public int lumi$getLightValueMax(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        val skyLightValue = lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockLightValue, skyLightValue);
    }

    @Override
    public void lumi$setLightValue(LightType lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            case SKY_LIGHT_TYPE:
                lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(LightType lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        val chunkPosY = (posY & 255) / 16;
        val subChunkPosY = posY & 15;

        lumi$root.lumi$prepareSubChunk(chunkPosY);
        val subChunk = lumi$subChunk(chunkPosY);
        subChunk.lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        lumi$root.lumi$markDirty();
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val chunkPosY = (posY & 255) / 16;

        val subChunk = lumi$subChunk(chunkPosY);
        if (subChunk == null)
            return BLOCK_LIGHT_TYPE.defaultLightValue();

        val subChunkPosY = posY & 15;
        return subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        if (!lumi$world.lumi$root().lumi$hasSky())
            return;

        val chunkPosY = (posY & 255) / 16;
        val subChunkPosY = posY & 15;

        lumi$root.lumi$prepareSubChunk(chunkPosY);
        val subChunk = lumi$subChunk(chunkPosY);
        subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        lumi$root.lumi$markDirty();
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        if (!lumi$world.lumi$root().lumi$hasSky())
            return 0;

        val chunkPosY = (posY & 255) / 16;

        val subChunk = lumi$subChunk(chunkPosY);
        if (subChunk == null) {
            if (lumi$canBlockSeeSky(subChunkPosX, posY, subChunkPosZ))
                return SKY_LIGHT_TYPE.defaultLightValue();
            return 0;
        }

        val subChunkPosY = posY & 15;
        return subChunk.lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = lumi$root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = lumi$root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockBrightness(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = lumi$root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = lumi$root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockOpacity(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ) {
        val posX = (xPosition << 4) + subChunkPosX;
        val posZ = (zPosition << 4) + subChunkPosZ;
        return lumi$world.lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ) {
        val posX = (xPosition << 4) + subChunkPosX;
        val posZ = (zPosition << 4) + subChunkPosZ;
        return lumi$world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ) {
        val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
        val maxPosY = heightMap[index];
        return maxPosY <= posY;
    }

    @Override
    public void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight) {
        val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
        heightMap[index] = skyLightHeight;
    }

    @Override
    public int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ) {
        val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
        return heightMap[index];
    }

    @Override
    public void lumi$minSkyLightHeight(int minSkyLightHeight) {
        this.heightMapMinimum = minSkyLightHeight;
    }

    @Override
    public int lumi$minSkyLightHeight() {
        return heightMapMinimum;
    }

    @Override
    public void lumi$resetSkyLightHeightMap() {
        Arrays.fill(heightMap, Integer.MAX_VALUE);
        heightMapMinimum = Integer.MAX_VALUE;
    }

    @Override
    public void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean height) {
        val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
        updateSkylightColumns[index] = height;
    }

    @Override
    public boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ) {
        val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
        return updateSkylightColumns[index];
    }

    @Override
    public void lumi$resetOutdatedHeightFlags() {
        Arrays.fill(updateSkylightColumns, true);
    }

    @Override
    public short[] lumi$neighborLightCheckFlags() {
        return lumi$neighborLightCheckFlags;
    }

    @Override
    public void lumi$lightingInitialized(boolean lightingInitialized) {
        this.lumi$lightingInitialized = lightingInitialized;
    }

    @Override
    public boolean lumi$lightingInitialized() {
        return lumi$lightingInitialized;
    }
}
