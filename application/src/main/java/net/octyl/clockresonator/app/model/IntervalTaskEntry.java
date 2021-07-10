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

package net.octyl.clockresonator.app.model;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.util.Optional;

public record IntervalTaskEntry(
    String id,
    String name,
    Period interval,
    Optional<Instant> stopTime,
    Instant lastOccurrence
) implements TaskEntry {
    @Override
    public Instant nextOccurrence() {
        return lastOccurrence.atZone(ZoneId.systemDefault()).plus(interval).toInstant();
    }

    @Override
    public Optional<TaskEntry> nextTaskEntry(Instant completionTime) {
        var next = new IntervalTaskEntry(
            id, name, interval, stopTime, completionTime
        );
        // If it won't happen until after we want to stop, there's no next
        if (stopTime.isPresent() && (completionTime.isAfter(stopTime.get())
            || next.nextOccurrence().isAfter(stopTime.get()))) {
            return Optional.empty();
        }
        return Optional.of(next);
    }
}
