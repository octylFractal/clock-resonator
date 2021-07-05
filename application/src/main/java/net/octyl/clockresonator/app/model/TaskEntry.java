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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a task, repeating or not.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OneTimeTaskEntry.class, name = "oneTime"),
    @JsonSubTypes.Type(value = RepeatingTaskEntry.class, name = "repeating")
})
// TODO sealed types in 17
public interface TaskEntry {
    /**
     * {@return the ID of the task}
     */
    String id();

    /**
     * {@return the name of the task}
     */
    String name();

    /**
     * When did this task last occur?
     *
     * <p>
     * For non-repeating tasks, this is the creation time. Note that for repeating tasks, this should not be when it
     * last SHOULD HAVE occurred, but when it was actually completed.
     * </p>
     *
     * @return the last occurrence
     */
    Instant lastOccurrence();

    /**
     * When does this task next occur?
     *
     * <p>
     * For non-repeating tasks, this is the due date.
     * </p>
     *
     * @return the next occurrence
     */
    Instant nextOccurrence();

    /**
     * Produce the next entry, if there is one.
     *
     * @param completionTime The time that the current task was completed at
     * @return the next entry
     */
    Optional<TaskEntry> nextTaskEntry(Instant completionTime);
}
