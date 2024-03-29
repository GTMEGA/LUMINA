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

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.internal.Tags.MOD_NAME;

@UtilityClass
public final class Share {
    public static final Logger LOG = LogManager.getLogger(MOD_NAME);
}
