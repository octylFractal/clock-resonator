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

package net.octyl.clockresonator.app.inject;

import com.google.common.eventbus.EventBus;
import dagger.Component;
import net.octyl.clockresonator.app.fx.JavaFxModule;
import net.octyl.clockresonator.app.fx.WindowRestorer;
import net.octyl.clockresonator.app.fx.def.MainSceneComponent;
import net.octyl.clockresonator.app.model.TaskEntryManager;
import net.octyl.clockresonator.app.util.ErrorReporter;

import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
        ApplicationInfoModule.class,
        EventBusModule.class,
        MainSceneComponent.Module.class,
        JavaFxModule.class,
        JsonModule.class
    }
)
public interface ApplicationComponent {
    MainSceneComponent.Builder mainScene();

    @ApplicationVersion
    String version();

    EventBus eventBus();

    ErrorReporter errorReporter();

    TaskEntryManager taskEntryManager();

    WindowRestorer windowRestorer();
}
