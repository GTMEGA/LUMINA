/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.collection;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class PosLongQueue {
    private final TLongSet valueSet = new PosLongHashSet();
    private final TLongList valueList = new TLongArrayList();

    public void add(long posLong) {
        if (valueSet.add(posLong))
            valueList.add(posLong);
    }

    public TLongIterator iterator() {
        return valueList.iterator();
    }

    public boolean isEmpty() {
        return valueList.isEmpty();
    }

    public void clear() {
        valueSet.clear();
        valueList.clear();
    }
}
