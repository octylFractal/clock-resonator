/*
 * Copyright (c) Octavia Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.octyl.clockresonator.app.util;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableListBase;
import javafx.collections.ObservableMap;
import javafx.collections.WeakMapChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;

/**
 * An observable list of the values in a map, ordered by some {@link Comparator}.
 */
public class ObservableValueList<V> extends ObservableListBase<V> implements RandomAccess {
    // Must hold a strong reference to this here
    @SuppressWarnings("unused")
    private final MapChangeListener<Object, V> listener;
    // This is kinda expensive for midpoint add/remove, but a LinkedList would make searching expensive
    // Theoretically some sort of "sparse array list" would be nice here (skip list?)
    private final List<V> delegate = new ArrayList<>();

    public ObservableValueList(ObservableMap<?, V> owner, Comparator<V> comparator) {
        owner.addListener(new WeakMapChangeListener<>(listener = change -> {
            beginChange();
            try {
                if (change.wasRemoved()) {
                    var value = change.getValueRemoved();
                    int index = Collections.binarySearch(delegate, value, comparator);
                    if (index >= 0) {
                        delegate.remove(index);
                        nextRemove(index, value);
                    }
                }
                if (change.wasAdded()) {
                    var value = change.getValueAdded();
                    int index = Collections.binarySearch(delegate, value, comparator);
                    var insertionPoint = index >= 0 ? index : -(index + 1);
                    delegate.add(insertionPoint, value);
                    nextAdd(insertionPoint, insertionPoint + 1);
                }
            } finally {
                endChange();
            }
        }));
    }

    @Override
    public V get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }
}
