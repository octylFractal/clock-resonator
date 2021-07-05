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

package net.octyl.clockresonator.app.fx;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.octyl.clockresonator.app.event.ClockTickEvent;
import net.octyl.clockresonator.app.model.OneTimeTaskEntry;
import net.octyl.clockresonator.app.model.RepeatingTaskEntry;
import net.octyl.clockresonator.app.model.TaskEntry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkState;

/**
 * Implements the view over a {@link TaskEntry}.
 */
public class TaskEntryView {

    @Singleton
    public static final class Factory {
        private final EventBus eventBus;

        @Inject
        public Factory(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        public TaskEntryView wrap(TaskEntry taskEntry) {
            var view = new TaskEntryView(taskEntry);
            view.updateProgress(Instant.now());
            eventBus.register(new Object() {
                @Subscribe
                public void onClockTick(ClockTickEvent event) {
                    checkState(Platform.isFxApplicationThread(), "Cross-thread eventing!");
                    view.updateProgress(event.now());
                }
            });
            return view;
        }
    }

    private final TaskEntry taskEntry;
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0.0);

    public TaskEntryView(TaskEntry taskEntry) {
        this.taskEntry = taskEntry;
    }

    public TaskEntry taskEntry() {
        return taskEntry;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    private void updateProgress(Instant now) {
        var start = taskEntry.lastOccurrence();
        var end = taskEntry.nextOccurrence();
        var fullDurationMillis = Duration.between(start, end).toMillis();
        var currentDurationMillis = Duration.between(start, now).toMillis();
        // Never reach 100, as the task is never "complete"
        double progress = Math.min(0.9999, currentDurationMillis / (double) fullDurationMillis);
        setProgress(progress);
    }

    public Node render(TaskEntry entry) {
        var nameLabel = new Label(entry.name());
        nameLabel.setStyle("""
            -fx-font-weight: bold;
            -fx-font-size: 18px;
            """);
        var detailsPanel = new VBox(
            8,
            nameLabel,
            new Label(lastOccurrenceText(entry)),
            new Label(nextOccurrenceText(entry))
        );
        detailsPanel.setAlignment(Pos.TOP_LEFT);

        var panel = new HBox(
            8,
            progressIndicator(entry.lastOccurrence(), entry.nextOccurrence()),
            detailsPanel
        );
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("""
            -fx-border-color: slategray;
            -fx-border-radius: 10px;
            """);
        return panel;
    }

    private String lastOccurrenceText(TaskEntry entry) {
        var lastText = formatInstant(entry.lastOccurrence());
        if (entry instanceof RepeatingTaskEntry) {
            return "Last occurrence: " + lastText;
        } else if (entry instanceof OneTimeTaskEntry) {
            return "Created at: " + lastText;
        }
        throw new IllegalStateException("Unknown task entry type " + entry.getClass());
    }

    private String nextOccurrenceText(TaskEntry entry) {
        var nextText = formatInstant(entry.nextOccurrence());
        if (entry instanceof RepeatingTaskEntry) {
            return "Next due at: " + nextText;
        } else if (entry instanceof OneTimeTaskEntry) {
            return "Due at: " + nextText;
        }
        throw new IllegalStateException("Unknown task entry type " + entry.getClass());
    }

    private String formatInstant(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private ProgressIndicator progressIndicator(Instant start, Instant end) {
        var fullDurationMillis = Duration.between(start, end).toMillis();
        var currentDurationMillis = Duration.between(start, Instant.now()).toMillis();
        var progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(currentDurationMillis / (double) fullDurationMillis);
        return progressIndicator;
    }
}
