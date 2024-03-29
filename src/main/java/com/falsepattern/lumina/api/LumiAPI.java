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

package com.falsepattern.lumina.api;

import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldWrapper;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.falsepattern.lumina.internal.Tags.*;
import static com.falsepattern.lumina.internal.lighting.LightingEngineManager.lightingEngineManager;
import static com.falsepattern.lumina.internal.world.WorldProviderManager.worldProviderManager;

@SuppressWarnings("unused")
public final class LumiAPI {
    public static final String LUMI_MOD_ID = MOD_ID;
    public static final String LUMI_MOD_NAME = MOD_NAME;
    public static final String LUMI_VERSION = VERSION;

    private static final LumiWorldWrapper WORLD_WRAPPER = worldProviderManager();
    private static final LumiLightingEngineProvider LIGHTING_ENGINE_PROVIDER = lightingEngineManager();

    private LumiAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NotNull LumiWorld[] lumiWorldsFromBaseWorld(@NotNull World worldBase) {
        return WORLD_WRAPPER.lumiWorldsFromBaseWorld(worldBase);
    }

    public static @NotNull LumiLightingEngine provideLightingEngine(@NotNull LumiWorld world,
                                                                    @NotNull Profiler profiler) {
        return LIGHTING_ENGINE_PROVIDER.provideLightingEngine(world, profiler);
    }

    public static @NotNull LumiWorldWrapper worldWrapper() {
        return WORLD_WRAPPER;
    }

    public static @NotNull LumiLightingEngineProvider lightingEngineProvider() {
        return LIGHTING_ENGINE_PROVIDER;
    }
}
