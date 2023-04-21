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

package com.falsepattern.lumina.api;

import net.minecraft.block.Block;

public interface ILumiEBS {
    /**
     * The custom skylight data
     */
    int lumiGetSkylight(int x, int y, int z);
    void lumiSetSkylight(int x, int y, int z, int defaultLightValue);

    /**
     * The custom blocklight data
     */
    int lumiGetBlocklight(int x, int y, int z);
    void lumiSetBlocklight(int x, int y, int z, int defaultLightValue);


    //Proxy this to carrier
    ILumiEBSRoot root();
}
