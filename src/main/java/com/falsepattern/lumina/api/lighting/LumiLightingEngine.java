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

package com.falsepattern.lumina.api.lighting;

import com.falsepattern.lib.compat.BlockPos;

public interface LumiLightingEngine {
    int getCurrentLightValue(LightType lightType, BlockPos blockPos);

    int getCurrentLightValue(LightType lightType, int posX, int posY, int posZ);

    void scheduleLightUpdateForRange(LightType lightType, BlockPos startBlockPos, BlockPos endBlockPos);

    void scheduleLightUpdateForRange(LightType lightType,
                                     int startPosX,
                                     int startPosY,
                                     int startPosZ,
                                     int endPosX,
                                     int endPosY,
                                     int endPosZ);

    void scheduleLightUpdateForColumn(LightType lightType, int posX, int posZ);

    void scheduleLightUpdateForColumn(LightType lightType, int posX, int posZ, int startPosY, int endPosY);

    void scheduleLightUpdate(LightType lightType, BlockPos blockPos);

    void scheduleLightUpdate(LightType lightType, int posX, int posY, int posZ);

    void processLightUpdatesForType(LightType lightType);

    void processLightUpdatesForAllTypes();
}
