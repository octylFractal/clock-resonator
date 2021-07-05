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

import dagger.Module;
import dagger.Provides;
import javafx.application.Platform;

import javax.inject.Singleton;
import java.util.concurrent.Executor;

@Module
public class JavaFxModule {
    @Provides
    @Singleton
    @JavaFx
    public static Executor provideJavaFxExecutor() {
        return Platform::runLater;
    }
}
