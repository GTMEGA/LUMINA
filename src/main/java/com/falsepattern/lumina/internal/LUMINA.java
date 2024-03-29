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

package com.falsepattern.lumina.internal;

import com.falsepattern.chunk.api.DataRegistry;
import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.internal.Share.LOG;
import static com.falsepattern.lumina.internal.Tags.*;
import static com.falsepattern.lumina.internal.lighting.LightingEngineManager.lightingEngineManager;
import static com.falsepattern.lumina.internal.storage.ChunkNBTManager.chunkNBTManager;
import static com.falsepattern.lumina.internal.storage.ChunkPacketManager.chunkPacketManager;
import static com.falsepattern.lumina.internal.storage.SubChunkNBTManager.subChunkNBTManager;
import static com.falsepattern.lumina.internal.world.WorldProviderManager.worldProviderManager;

@Mod(modid = MOD_ID,
     version = VERSION,
     name = MOD_NAME,
     acceptedMinecraftVersions = MINECRAFT_VERSION,
     dependencies = DEPENDENCIES,
     guiFactory = GUI_FACTORY_PATH)
@NoArgsConstructor
public final class LUMINA {
    public static Logger createLogger(String name) {
        return LogManager.getLogger(MOD_NAME + "|" + name);
    }

    private static boolean falseTweaks;
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        falseTweaks = Loader.isModLoaded("falsetweaks");
    }

    public static boolean lumi$isThreadedUpdates() {
        return falseTweaks && ThreadedChunkUpdates.isEnabled();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        worldProviderManager().registerWorldProviders();
        lightingEngineManager().registerLightingEngineProvider();

        chunkNBTManager().registerDataManager();
        subChunkNBTManager().registerDataManager();
        chunkPacketManager().registerDataManager();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        DataRegistry.disableDataManager("minecraft", "lighting");
        LOG.info("Disabled [minecraft:lighting] data manager");
        DataRegistry.disableDataManager("minecraft", "blocklight");
        LOG.info("Disabled [minecraft:blocklight] data manager");
        DataRegistry.disableDataManager("minecraft", "skylight");
        LOG.info("Disabled [minecraft:skylight] data manager");
    }
}
