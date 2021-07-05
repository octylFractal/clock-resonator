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

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class LabeledProgressBarTableCell extends TableCell<TaskEntryView, Number> {
    {
        var bar = new ProgressBar();
        bar.progressProperty().bind(itemProperty());
        bar.visibleProperty().bind(emptyProperty().not());
        bar.setMaxWidth(Double.POSITIVE_INFINITY);

        var label = new Label("100.00%");
        itemProperty().addListener(obs -> {
            if (getItem() != null) {
                double v = Math.min(100, 100 * getItem().doubleValue());
                label.setText("%6.2f%%".formatted(v));
            }
        });
        label.visibleProperty().bind(emptyProperty().not());
        label.setMinWidth(USE_PREF_SIZE);
        label.setAlignment(Pos.BASELINE_RIGHT);
        label.setStyle("-fx-font-family: monospace");

        var box = new HBox(2, bar, label);
        HBox.setHgrow(bar, Priority.ALWAYS);
        setGraphic(box);
        setEditable(false);
    }
}
