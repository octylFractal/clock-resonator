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

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.dirs.ProjectDirectories;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import net.octyl.clockresonator.app.fx.JavaFx;
import net.octyl.clockresonator.app.util.Completables;
import net.octyl.clockresonator.app.util.ErrorReporter;
import net.octyl.clockresonator.app.util.ObservableValueList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Manages saving and loading task entries.
 */
@Singleton
public class TaskEntryManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Executor javaFxExecutor;
    private final JsonMapper jsonMapper;
    private final Path taskFile;
    private final ErrorReporter errorReporter;
    private final Map<String, TaskEntry> entriesBackingMap = new ConcurrentHashMap<>();
    private final ObservableMap<String, TaskEntry> entries = FXCollections.observableMap(entriesBackingMap);
    private final ObservableValueList<TaskEntry> entriesValueList = new ObservableValueList<>(
        entries, Comparator.comparing(TaskEntry::id)
    );
    private final Sinks.Many<List<TaskEntry>> saveSink;

    @Inject
    public TaskEntryManager(@JavaFx Executor javaFxExecutor,
                            JsonMapper jsonMapper,
                            ProjectDirectories dirs,
                            ErrorReporter errorReporter) {
        this.javaFxExecutor = javaFxExecutor;
        this.jsonMapper = jsonMapper;
        this.taskFile = Path.of(dirs.dataDir, "tasks.json");
        this.errorReporter = errorReporter;
        this.saveSink = Sinks.many().unicast().onBackpressureBuffer();

        this.saveSink.asFlux()
            .sampleTimeout(__ -> Mono.delay(Duration.ofSeconds(1L)))
            .doOnNext(entryList -> {
                try {
                    saveEntries(entryList);
                } catch (IOException e) {
                    errorReporter.reportError(Level.WARN, "Failed to save task entries", e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        this.entries.addListener((InvalidationListener) obs ->
            saveSink.tryEmitNext(entriesBackingMap.values().stream().toList())
        );
    }

    public ObservableList<TaskEntry> getEntries() {
        return entriesValueList;
    }

    public void put(TaskEntry entry) {
        entries.put(entry.id(), entry);
    }

    public void delete(String id) {
        entries.remove(id);
    }

    public void initialize() {
        Completables.callAsync(this::loadEntries)
            .thenAcceptAsync(entriesList -> {
                for (TaskEntry entry : entriesList) {
                    entries.put(entry.id(), entry);
                }
            }, javaFxExecutor)
            .whenComplete(errorReporter.bind(Level.ERROR, "Failed to load task entries"));
    }

    private List<TaskEntry> loadEntries() throws IOException {
        LOGGER.info(() -> "Loading task entries");
        if (!Files.exists(taskFile)) {
            return List.of();
        }
        return this.jsonMapper.readValue(
            taskFile.toFile(),
            new TypeReference<>() {
            }
        );
    }

    private void saveEntries(Collection<? extends TaskEntry> taskEntries) throws IOException {
        LOGGER.info(() -> "Saving task entries, count=" + taskEntries.size());
        Files.createDirectories(taskFile.getParent());
        var tmp = Files.createTempFile(taskFile.getParent(), "tmp", taskFile.getFileName().toString());
        try {
            // we need to specialize the writer with compile-time generic info
            this.jsonMapper.writerFor(new TypeReference<Collection<? extends TaskEntry>>() {
            }).writeValue(tmp.toFile(), taskEntries);
            Files.move(
                tmp, taskFile,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
            );
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
