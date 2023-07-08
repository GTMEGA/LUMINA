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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.util.LongHashMap;

@Mixin(LongHashMap.class)
public abstract class LongHashMapMixin {
    private static final int HASH_PRIME = 92821;

    /**
     * @author FalsePattern
     * @reason Small perf snippet, ported from ArchaicFix, LGPLv3.
     */
    @Overwrite
    private static int getHashedKey(long key) {
        val a = (int) key;
        val b = (int) (key >>> 32);
        return (a + b) * HASH_PRIME;
    }
}