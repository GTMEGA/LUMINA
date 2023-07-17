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

package com.falsepattern.lumina;

import com.falsepattern.chunk.api.ChunkDataRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.Tags.*;
import static com.falsepattern.lumina.internal.data.ChunkNBTManager.chunkNBTManager;
import static com.falsepattern.lumina.internal.data.ChunkPacketManager.chunkPacketManager;
import static com.falsepattern.lumina.internal.data.SubChunkNBTManager.subChunkNBTManager;
import static com.falsepattern.lumina.internal.lighting.LightingEngineManager.lightingEngineManager;
import static com.falsepattern.lumina.internal.world.WorldManager.worldManager;

@Mod(modid = MOD_ID,
     version = VERSION,
     name = MOD_NAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.11,);required-after:chunkapi@[0.3,)")
@NoArgsConstructor
public final class LUMINA {
    private static final Logger LOG = LogManager.getLogger(MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        worldManager().registerWorldProviders();
        lightingEngineManager().registerLightingEngineProvider();

        chunkNBTManager().registerDataManager();
        subChunkNBTManager().registerDataManager();
        chunkPacketManager().registerDataManager();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        ChunkDataRegistry.disableDataManager("minecraft", "lighting");
        LOG.info("Disabled [minecraft:lighting] data manager");
        ChunkDataRegistry.disableDataManager("minecraft", "blocklight");
        LOG.info("Disabled [minecraft:blocklight] data manager");
        ChunkDataRegistry.disableDataManager("minecraft", "skylight");
        LOG.info("Disabled [minecraft:skylight] data manager");
    }
}