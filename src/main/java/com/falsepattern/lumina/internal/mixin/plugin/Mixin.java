/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.always;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.avoid;
import static com.falsepattern.lumina.internal.mixin.plugin.TargetedMod.ARCHAICFIX;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // @formatter:off
    common_MixinLongHashMap(Side.COMMON, avoid(ARCHAICFIX), "MixinLongHashMap"),
    common_MixinAnvilChunkLoader(Side.COMMON, always(), "MixinAnvilChunkLoader"),
    common_MixinChunk(Side.COMMON, always(), "MixinChunk"),
    common_MixinChunkProviderServer(Side.COMMON, always(), "MixinChunkProviderServer"),
    common_MixinExtendedBlockStorage(Side.COMMON, always(), "MixinExtendedBlockStorage"),
    common_MixinSPacketChunkData(Side.COMMON, always(), "MixinSPacketChunkData"),
    common_MixinWorld(Side.COMMON, always(), "MixinWorld"),
    common_impl_MixinChunkILumiChunk(Side.COMMON, always(), "impl.MixinChunkILumiChunk"),
    common_impl_MixinExtendedBlockStorageILumiEBS(Side.COMMON, always(), "impl.MixinExtendedBlockStorageILumiEBS"),
    common_impl_MixinWorldILumiWorld(Side.COMMON, always(), "impl.MixinWorldILumiWorld"),
    client_MixinMinecraft(Side.CLIENT, always(), "MixinMinecraft"),
    client_MixinChunk(Side.CLIENT, always(), "MixinChunk"),
    client_MixinChunkCache(Side.CLIENT, always(), "MixinChunkCache"),
    client_MixinWorld(Side.CLIENT, always(), "MixinWorld"),
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

