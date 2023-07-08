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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.engine.LumiLightingEngine;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
@Accessors(fluent = true, chain = false)
public abstract class LumiWorldRootImplMixin implements IBlockAccess, LumiWorldRoot {
    @Final
    @Shadow
    public WorldProvider provider;
    @Final
    @Shadow
    public Profiler theProfiler;

    @Shadow
    protected IChunkProvider chunkProvider;
    @Shadow
    public boolean isRemote;

    @Shadow
    public abstract boolean doChunksNearChunkExist(int centerPosX, int centerPosY, int centerPosZ, int blockRange);

    @Shadow
    public abstract boolean checkChunksExist(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ);

    @Shadow
    public abstract void func_147479_m(int posX, int posY, int posZ);

    @Setter
    @Getter
    private LumiLightingEngine lightingEngine;

    @Override
    public World asVanillaWorld() {
        return thiz();
    }

    @Override
    public Profiler profiler() {
        return theProfiler;
    }

    @Override
    public boolean isClientSide() {
        return isRemote;
    }

    @Override
    public boolean hasSky() {
        return !provider.hasNoSky;
    }

    @Override
    public IChunkProvider chunkProvider() {
        return chunkProvider;
    }

    @Override
    public boolean doChunksExist(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ) {
        return checkChunksExist(minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ);
    }

    @Override
    public boolean doChunksExist(int centerPosX, int centerPosY, int centerPosZ, int blockRange) {
        return doChunksNearChunkExist(centerPosX, centerPosY, centerPosZ, blockRange);
    }

    @Override
    public void markBlockForRenderUpdate(int posX, int posY, int posZ) {
        func_147479_m(posX, posY, posZ);
    }

    private World thiz() {
        return (World) (Object) this;
    }
}