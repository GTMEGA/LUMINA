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

package com.falsepattern.lumina.api.init;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.StableAPI.Internal;

import static com.falsepattern.lib.StableAPI.Expose;
import static com.falsepattern.lumina.api.LumiAPI.LUMI_MOD_NAME;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiExtendedBlockStorageInitHook {
    @Internal
    String LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_INFO = "Implemented by [" + LUMI_MOD_NAME + "] with the interface " +
                                                        "[com.falsepattern.lumina.api.init.LumiExtendedBlockStorageInitHook]";
    @Expose
    String LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_METHOD = "lumi$onExtendedBlockStorageInit()V";

    @Internal
    void lumi$doExtendedBlockStorageInit();

    @Internal
    void lumi$onExtendedBlockStorageInit();

    @Internal
    boolean lumi$initHookExecuted();
}
