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

package com.falsepattern.lumina.internal.world;

import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldProvider;
import com.falsepattern.lumina.api.world.LumiWorldProviderRegistry;
import com.falsepattern.lumina.api.world.LumiWorldWrapper;
import com.falsepattern.lumina.internal.LumiDefaultValues;
import com.falsepattern.lumina.internal.collection.WeakIdentityHashMap;
import com.falsepattern.lumina.internal.event.EventPoster;
import com.falsepattern.lumina.internal.mixin.interfaces.LumiWorldRootCache;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import net.minecraft.world.World;

import lombok.var;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static lombok.AccessLevel.PRIVATE;

@Accessors(fluent = true, chain = false)
@NoArgsConstructor(access = PRIVATE)
public final class WorldProviderManager implements LumiWorldProviderRegistry, LumiWorldWrapper {
    private static final Logger LOG = createLogger("World Provider Manager");

    private static final WorldProviderManager INSTANCE = new WorldProviderManager();

    private final List<LumiWorldProvider> worldProviders = new ArrayList<>();

    private boolean isRegistered = false;
    private boolean isHijacked = false;
    private @Nullable String hijackingMod = null;

    public static WorldProviderManager worldProviderManager() {
        return INSTANCE;
    }

    public void registerWorldProviders() {
        if (isRegistered)
            return;

        EventPoster.postLumiWorldProviderRegistrationEvent(this);
        if (isHijacked && worldProviders.isEmpty()) {
            LOG.error("Default world providers have been hijacked by [{}], " +
                      "but it did not provide it's own replacements. " +
                      "The hijacked state has been reset, but things may not work correctly. God speed.",
                      hijackingMod);
            isHijacked = false;
        }

        if (!isHijacked)
            LumiDefaultValues.registerDefaultWorldProvider(this);

        isRegistered = true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void hijackDefaultWorldProviders(@NotNull String modName) {
        if (isRegistered) {
            LOG.error("Cannot hijack default world providers post registration", new IllegalStateException());
            return;
        }

        if (isHijacked) {
            LOG.warn("Default world providers has already been hijacked by: [{}]," +
                     " but {} has tried to hijack it again. Things will probably work fine.", hijackingMod, modName);
            return;
        }

        if (modName != null) {
            hijackingMod = modName;
            LOG.info("Default world providers have been hijacked by: [{}]", modName);
        } else {
            hijackingMod = "UNKNOWN MOD";
            LOG.error("A mod attempted to hijack the default world providers, " +
                      "but didn't provider did not name itself. " +
                      "The hijack *was* performed, and things should be fine. But please report this.",
                      new IllegalArgumentException());
        }

        isHijacked = true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void registerWorldProvider(@NotNull LumiWorldProvider worldProvider) {
        if (isRegistered) {
            LOG.error(new IllegalStateException("Cannot registration world providers post registration"));
            return;
        }

        if (worldProvider == null) {
            LOG.error(new IllegalArgumentException("World provider can't be null"));
            return;
        }

        val worldProviderID = worldProvider.worldProviderID();
        if (worldProviderID == null) {
            LOG.error(new IllegalArgumentException("World provider id can't be null"));
            return;
        }

        if (worldProviderID.isEmpty()) {
            LOG.error(new IllegalArgumentException("World provider id can't be empty"));
            return;
        }

        if (worldProviders.contains(worldProvider)) {
            LOG.error(new IllegalArgumentException(
                    String.format("World provider [%s] already registered", worldProviderID)));
            return;
        }

        LOG.info("Registered world provider: [{}]", worldProviderID);
        worldProviders.add(worldProvider);
    }

    private static final LumiWorld[] NULL_ARR = new LumiWorld[0];

    @Override
    public @NotNull @Unmodifiable LumiWorld[] lumiWorldsFromBaseWorld(@Nullable World worldBase) {
        if (!isRegistered) {
            LOG.error(new IllegalStateException("No world providers exist during registration, " +
                                                "an empty iterable will be returned."));
            return NULL_ARR;
        }
        if (worldBase == null)
            return NULL_ARR;
        var worlds = ((LumiWorldRootCache)worldBase).lumi$getLumiWorlds();
        if (worlds == null) {
            ((LumiWorldRootCache)worldBase).lumi$setLumiWorlds(worlds = collectProvidedWorlds(worldBase));
        }
        return worlds;
    }

    public @Nullable LumiWorldProvider getWorldProviderByInternalID(int internalWorldProviderID) {
        if (internalWorldProviderID >= 0 && internalWorldProviderID < worldProviders.size())
            return worldProviders.get(internalWorldProviderID);
        return null;
    }

    public int worldProviderCount() {
        return worldProviders.size();
    }

    private LumiWorld[] collectProvidedWorlds(World worldBase) {
        val worldList = new ArrayList<LumiWorld>();
        for (int i = 0, worldProvidersSize = worldProviders.size(); i < worldProvidersSize; i++) {
            LumiWorldProvider worldProvider = worldProviders.get(i);
            LumiWorld lumiWorld = worldProvider.provideWorld(worldBase);
            if (lumiWorld != null) {
                worldList.add(lumiWorld);
            }
        }
        return worldList.toArray(new LumiWorld[0]);
    }
}
