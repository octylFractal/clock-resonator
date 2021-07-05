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

import javafx.stage.Modality;
import javafx.stage.Stage;
import net.octyl.clockresonator.app.fx.def.MainScope;
import net.octyl.clockresonator.app.fx.def.TaskEntryEditorComponent;
import net.octyl.clockresonator.app.model.TaskEntry;

import javax.inject.Inject;
import javax.inject.Provider;

@MainScope
public class TaskEntryEditor {

    private final Stage stage;
    private final Provider<TaskEntryEditorComponent.Builder> taskEntryEditorComponent;

    @Inject
    public TaskEntryEditor(Stage stage, Provider<TaskEntryEditorComponent.Builder> taskEntryEditorComponent) {
        this.stage = stage;
        this.taskEntryEditorComponent = taskEntryEditorComponent;
    }

    public void open(TaskEntry initialTaskEntry) {
        var dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(this.stage);
        dialog.setTitle("Editing " + initialTaskEntry.name());
        var scene = taskEntryEditorComponent.get()
            .stage(dialog)
            .initialTaskEntry(initialTaskEntry)
            .build()
            .scene();
        dialog.setScene(scene);
        dialog.show();
    }

}
