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

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.dirs.ProjectDirectories;
import javafx.stage.Stage;
import net.octyl.clockresonator.app.util.ErrorReporter;
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

/**
 * Restores the state of a window after application relaunch.
 */
@Singleton
public class WindowRestorer {
    private static final Logger LOGGER = LogManager.getLogger();

    // https://github.com/google/dagger/issues/2106
    // Wrapping the records prevents dagger from observing them and exploding
    private static final class DaggerHack {
        private record State(
            double x,
            double y,
            double width,
            double height,
            boolean maximized
        ) {
            static State from(Stage stage) {
                return new State(
                    stage.getX(),
                    stage.getY(),
                    stage.getWidth(),
                    stage.getHeight(),
                    stage.isMaximized()
                );
            }
        }

        private record StateWithId(
            String identifier,
            State state
        ) {
        }
    }

    private final JsonMapper jsonMapper;
    private final Path windowDataFolder;
    private final Sinks.Many<DaggerHack.StateWithId> stateSaveSink;

    @Inject
    public WindowRestorer(JsonMapper jsonMapper, ProjectDirectories projectDirs, ErrorReporter errorReporter) {
        this.jsonMapper = jsonMapper;
        this.windowDataFolder = Path.of(projectDirs.dataLocalDir, "window-state");
        this.stateSaveSink = Sinks.many().unicast().onBackpressureBuffer();
        this.stateSaveSink.asFlux()
            .sampleTimeout(__ -> Mono.delay(Duration.ofSeconds(1L)))
            .doOnNext(state -> {
                try {
                    saveState(state.identifier, state.state);
                } catch (IOException e) {
                    errorReporter.reportError(Level.WARN, "Failed to save window state", e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    private Path statePathFor(String identifier) {
        return windowDataFolder.resolve(identifier + ".json");
    }

    /**
     * Attach the restorer to the given window. Note that this will <em>block</em> while loading the window state in
     * order to ensure better presentation to the user.
     *
     * @param identifier the unique identifier for this window, to know which state to load
     * @param window the window
     */
    public void attach(String identifier, Stage window) throws IOException {
        LOGGER.info(() -> "Attaching to window, id=" + identifier);
        if (Files.exists(statePathFor(identifier))) {
            initializeFromState(identifier, window);
        }
        window.xProperty().addListener(observable -> stateSaveSink.tryEmitNext(new DaggerHack.StateWithId(
            identifier, DaggerHack.State.from(window)
        )));
        window.yProperty().addListener(observable -> stateSaveSink.tryEmitNext(new DaggerHack.StateWithId(
            identifier, DaggerHack.State.from(window)
        )));
        window.widthProperty().addListener(observable -> stateSaveSink.tryEmitNext(new DaggerHack.StateWithId(
            identifier, DaggerHack.State.from(window)
        )));
        window.heightProperty().addListener(observable -> stateSaveSink.tryEmitNext(new DaggerHack.StateWithId(
            identifier, DaggerHack.State.from(window)
        )));
        window.maximizedProperty().addListener(observable -> stateSaveSink.tryEmitNext(new DaggerHack.StateWithId(
            identifier, DaggerHack.State.from(window)
        )));
    }

    private void initializeFromState(String identifier, Stage window) throws IOException {
        var state = jsonMapper.readValue(statePathFor(identifier).toFile(), DaggerHack.State.class);
        LOGGER.info(() -> "Initializing windows state, id=" + identifier + ", state=" + state);
        window.setX(state.x);
        window.setY(state.y);
        window.setHeight(state.height);
        window.setWidth(state.width);
        window.setMaximized(state.maximized);
    }

    private void saveState(String identifier, DaggerHack.State state) throws IOException {
        LOGGER.info(() -> "Saving state, id=" + identifier + ", state=" + state);
        var finalPath = statePathFor(identifier);
        Files.createDirectories(windowDataFolder);
        var tmp = Files.createTempFile(windowDataFolder, "tmp", finalPath.getFileName().toString());
        try {
            jsonMapper.writeValue(tmp.toFile(), state);
            Files.move(tmp, finalPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
