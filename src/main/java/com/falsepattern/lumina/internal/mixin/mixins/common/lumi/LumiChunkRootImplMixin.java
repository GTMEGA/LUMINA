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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.falsepattern.lumina.internal.mixin.plugin.MixinPlugin.LUMI_ROOT_IMPL_MIXIN_PRIORITY;

@Unique
@Mixin(value = Chunk.class, priority = LUMI_ROOT_IMPL_MIXIN_PRIORITY)
public abstract class LumiChunkRootImplMixin implements LumiChunkRoot {
    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public World worldObj;
    @Shadow
    public boolean isModified;

    @Shadow
    public abstract int getTopFilledSegment();

    @Shadow
    public abstract Block getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract int getBlockMetadata(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract void setChunkModified();

    @Override
    public @NotNull String lumi$chunkRootID() {
        return "lumi_sub_chunk_root";
    }

    @Override
    public boolean lumi$isUpdating() {
        return worldObj.activeChunkSet.contains(thiz());
    }

    @Override
    public void lumi$markDirty() {
        setChunkModified();
    }

    @Override
    public boolean lumi$isDirty() {
        return isModified;
    }

    @Override
    public void lumi$prepareSubChunk(int chunkPosY) {
        chunkPosY &= 15;
        var subChunkBase = storageArrays[chunkPosY];

        if (subChunkBase == null) {
            val posY = chunkPosY << 4;
            subChunkBase = new ExtendedBlockStorage(posY, !worldObj.provider.hasNoSky);
            storageArrays[chunkPosY] = subChunkBase;
            LightingHooks.handleSubChunkInit(thiz(), subChunkBase);
        }

        lumi$markDirty();
    }

    @Override
    public boolean lumi$isSubChunkPrepared(int chunkPosY) {
        chunkPosY &= 15;
        val subChunk = storageArrays[chunkPosY];
        return subChunk != null;
    }

    @Override
    public int lumi$topPreparedSubChunkBasePosY() {
        return getTopFilledSegment();
    }

    @Override
    public @NotNull Block lumi$getBlock(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlock(subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
