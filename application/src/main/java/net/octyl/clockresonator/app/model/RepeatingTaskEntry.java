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

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

public record RepeatingTaskEntry(
    String id,
    String name,
    ZoneId timeZone,
    Cron cron,
    Optional<Instant> stopTime,
    Instant lastOccurrence,
    Instant executionBase
) implements TaskEntry {
    public RepeatingTaskEntry {
        if (executionBase == null) {
            executionBase = lastOccurrence;
        }
    }

    @Override
    public Instant nextOccurrence() {
        return ExecutionTime.forCron(cron).nextExecution(executionBase.atZone(timeZone))
            .orElseThrow(() -> new IllegalStateException("Unable to calculate next occurrence of task"))
            .toInstant();
    }

    @Override
    public Optional<TaskEntry> nextTaskEntry(Instant completionTime) {
        var nextOccurrence = nextOccurrence();
        var executionBase = completionTime.isBefore(nextOccurrence) ? nextOccurrence : completionTime;
        var next = new RepeatingTaskEntry(id, name, timeZone, cron, stopTime, completionTime, executionBase);
        // If it won't happen until after we want to stop, there's no next
        if (stopTime.isPresent() && (completionTime.isAfter(stopTime.get())
            || next.nextOccurrence().isAfter(stopTime.get()))) {
            return Optional.empty();
        }
        return Optional.of(next);
    }
}
