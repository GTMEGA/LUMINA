/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin implements IBlockAccess {
    /**
     * @author Ven
     * @reason Redirect into LUMINA
     */
    @Overwrite
    public boolean updateLightByType(EnumSkyBlock baseLightType, int posX, int posY, int posZ) {
        LightingHooks.scheduleLightingUpdate(thiz(), baseLightType, posX, posY, posZ);
        return true;
    }

    @Inject(method = "tick",
            at = @At("RETURN"),
            require = 1)
    private void processLightUpdates(CallbackInfo cir) {
        LightingHooks.processLightUpdates(thiz());
    }

    private World thiz() {
        return (World) (Object) this;
    }
}
