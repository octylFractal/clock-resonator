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

package net.octyl.clockresonator.app.fx.def;

import dagger.BindsInstance;
import dagger.Subcomponent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.octyl.clockresonator.app.model.TaskEntry;

@TaskEntryEditorScope
@Subcomponent(
    modules = {
        TaskEntryEditorScene.class
    }
)
public interface TaskEntryEditorComponent {

    @dagger.Module(subcomponents = TaskEntryEditorComponent.class)
    interface Module {
    }

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        Builder stage(@TaskEntryEditorScene.Def Stage stage);

        @BindsInstance
        Builder initialTaskEntry(TaskEntry taskEntry);

        TaskEntryEditorComponent build();
    }

    @TaskEntryEditorScene.Def
    Scene scene();
}
