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

package net.octyl.clockresonator.app;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.octyl.clockresonator.app.event.ClockTickEvent;
import net.octyl.clockresonator.app.inject.ApplicationComponent;
import net.octyl.clockresonator.app.inject.DaggerApplicationComponent;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClockResonator extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    private final ApplicationComponent component = DaggerApplicationComponent.create();

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) ->
            component.errorReporter().reportError(
                Level.ERROR, "Error occurred on FX Thread", e
            )
        );
        component.taskEntryManager().initialize();
        try {
            component.windowRestorer().attach("primary", stage);
        } catch (IOException e) {
            component.errorReporter().reportError(
                Level.WARN, "Failed to restore window state", e
            );
        }
        startClockTicker();

        stage.setTitle("Clock Resonator (" + component.version() + ")");
        stage.setScene(
            component.mainScene()
                .stage(stage)
                .build()
                .scene()
        );
        stage.show();
    }

    private void startClockTicker() {
        var executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setNameFormat("clock-resonator-tick-%d")
            .setDaemon(true)
            .build());
        executor.scheduleAtFixedRate(
            () -> Platform.runLater(() -> component.eventBus().post(new ClockTickEvent(Instant.now()))),
            0,
            100,
            TimeUnit.MILLISECONDS
        );
    }
}
