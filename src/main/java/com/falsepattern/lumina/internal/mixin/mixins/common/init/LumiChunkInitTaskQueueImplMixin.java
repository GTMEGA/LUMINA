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

package com.falsepattern.lumina.internal.mixin.mixins.common.init;

import com.falsepattern.lumina.api.init.LumiChunkInitTaskQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Unique
@Mixin(Chunk.class)
public abstract class LumiChunkInitTaskQueueImplMixin implements LumiChunkInitTaskQueue {
    private ObjectList<Runnable> lumina$taskQueue;

    @Inject(method = "onChunkLoad",
            at = @At(value = "INVOKE",
                     target = "Lcpw/mods/fml/common/eventhandler/EventBus;post(Lcpw/mods/fml/common/eventhandler/Event;)Z",
                     remap = false),
            require = 1)
    private void onLoad(CallbackInfo ci) {
        lumi$executeInitTasks();
    }

    @Override
    public void lumi$addInitTask(Runnable task) {
        if (lumina$taskQueue == null)
            lumina$taskQueue = new ObjectArrayList<>();
        lumina$taskQueue.add(task);
    }

    @Override
    public void lumi$executeInitTasks() {
        if (lumina$taskQueue == null)
            return;
        lumina$taskQueue.forEach(Runnable::run);
        lumina$taskQueue.clear();
    }
}
