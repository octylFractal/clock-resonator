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

import dagger.Module;
import dagger.Provides;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.octyl.clockresonator.app.fx.LabeledProgressBarTableCell;
import net.octyl.clockresonator.app.fx.TaskEntryEditor;
import net.octyl.clockresonator.app.fx.TaskEntryView;
import net.octyl.clockresonator.app.model.OneTimeTaskEntry;
import net.octyl.clockresonator.app.model.RepeatingTaskEntry;
import net.octyl.clockresonator.app.model.TaskEntryManager;
import net.octyl.clockresonator.app.util.FXCollections2;
import net.octyl.clockresonator.app.util.OS;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Module
public class MainScene {
    @Qualifier
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    public @interface Def {
        String value() default "";
    }

    @Provides
    @MainScope
    @Def
    public static Scene scene(@Def("mainPane") BorderPane mainPane) {
        return new Scene(mainPane, 900, 600);
    }

    @Provides
    @MainScope
    @Def("mainPane")
    public static BorderPane mainPane(
        @Def MenuBar menuBar,
        @Def ToolBar toolBar,
        @Def TableView<TaskEntryView> mainTable
    ) {
        var pane = new BorderPane();
        var barHolder = new VBox(
            4,
            menuBar,
            toolBar
        );
        pane.setTop(barHolder);
        pane.setCenter(mainTable);
        return pane;
    }

    @Provides
    @MainScope
    @Def
    public static MenuBar menuBar(@Def("file") Menu fileMenu) {
        var menuBar = new MenuBar(
            fileMenu
        );
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    @Provides
    @MainScope
    @Def
    public static ToolBar toolBar(
        @Def("create") Button createButton,
        @Def("edit") Button editButton,
        @Def("complete") Button completeButton,
        @Def("delete") Button deleteButton
    ) {
        return new ToolBar(
            createButton,
            editButton,
            completeButton,
            deleteButton
        );
    }

    @Provides
    @MainScope
    @Def("file")
    public static Menu fileMenu() {
        var exit = new MenuItem("E_xit", FontIcon.of(FontAwesomeSolid.SIGN_OUT_ALT, 16));
        exit.setOnAction(__ -> Platform.exit());
        exit.setAccelerator(KeyCombination.valueOf(OS.detected() == OS.MAC_OS ? "Meta+Q" : "Alt+F4"));

        return new Menu(
            "_File",
            null,
            exit
        );
    }

    @Provides
    @MainScope
    @Def("create")
    public static Button createButton(
        TaskEntryEditor editor
    ) {
        var button = new Button("Create", FontIcon.of(FontAwesomeSolid.CALENDAR_PLUS, 16));
        button.setOnAction(event -> {
            var candidateEntry = new OneTimeTaskEntry(
                UUID.randomUUID().toString(),
                "",
                Instant.MIN,
                Instant.MAX
            );
            editor.open(candidateEntry);
        });
        return button;
    }

    @Provides
    @MainScope
    @Def("edit")
    public static Button editButton(
        @Def TableView<TaskEntryView> mainTable,
        TaskEntryEditor editor
    ) {
        var button = new Button("Edit", FontIcon.of(FontAwesomeSolid.EDIT, 16));
        button.disableProperty().bind(
            Bindings.size(mainTable.getSelectionModel().getSelectedCells()).isNotEqualTo(1)
        );
        button.setOnAction(event -> {
            if (button.isDisable()) {
                return;
            }
            var entry = mainTable.getSelectionModel().getSelectedItem().taskEntry();
            editor.open(entry);
        });
        return button;
    }

    @Provides
    @MainScope
    @Def("complete")
    public static Button completeButton(
        @Def TableView<TaskEntryView> mainTable,
        TaskEntryManager manager
    ) {
        var button = new Button("Complete", FontIcon.of(FontAwesomeSolid.CHECK, 16));
        button.disableProperty().bind(
            Bindings.size(mainTable.getSelectionModel().getSelectedCells()).lessThan(1)
        );
        button.setOnAction(event -> {
            if (button.isDisable()) {
                return;
            }
            var items = List.copyOf(mainTable.getSelectionModel().getSelectedItems());
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (items.size() == 1) {
                alert.setContentText("Are you sure you want to complete '" + items.get(0).taskEntry().name() + "'?");
            } else {
                alert.setContentText("Are you sure you want to complete " + items.size() + " items?");
            }
            alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    var complete = Instant.now();
                    for (TaskEntryView item : items) {
                        item.taskEntry().nextTaskEntry(complete).ifPresent(manager::put);
                    }
                });
        });
        return button;
    }

    @Provides
    @MainScope
    @Def("delete")
    public static Button deleteButton(
        @Def TableView<TaskEntryView> mainTable,
        TaskEntryManager manager
    ) {
        var button = new Button("Delete", FontIcon.of(FontAwesomeSolid.CALENDAR_MINUS, 16));
        button.disableProperty().bind(
            Bindings.size(mainTable.getSelectionModel().getSelectedCells()).lessThan(1)
        );
        button.setOnAction(event -> {
            if (button.isDisable()) {
                return;
            }
            var items = List.copyOf(mainTable.getSelectionModel().getSelectedItems());
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (items.size() == 1) {
                alert.setContentText("Are you sure you want to delete '" + items.get(0).taskEntry().name() + "'?");
            } else {
                alert.setContentText("Are you sure you want to delete " + items.size() + " items?");
            }
            alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    for (TaskEntryView item : items) {
                        manager.delete(item.taskEntry().id());
                    }
                });
        });
        return button;
    }

    @Provides
    @MainScope
    @Def
    public static TableView<TaskEntryView> mainTable(
        TaskEntryManager taskEntryManager,
        TaskEntryView.Factory taskEntryViewFactory
    ) {
        var entryViews = FXCollections2.map(taskEntryManager.getEntries(), taskEntryViewFactory::wrap);
        var sortedEntries = entryViews.sorted(null);
        var view = new TableView<>(sortedEntries);
        sortedEntries.comparatorProperty().bind(view.comparatorProperty());

        view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        var typeColumn = new TableColumn<TaskEntryView, String>("Type");
        typeColumn.setCellValueFactory(cdf -> {
            String type;
            if (cdf.getValue().taskEntry() instanceof OneTimeTaskEntry) {
                type = "One-Time";
            } else if (cdf.getValue().taskEntry() instanceof RepeatingTaskEntry) {
                type = "Repeating";
            } else {
                type = "Unknown";
            }

            return Bindings.createStringBinding(() -> type);
        });
        typeColumn.setPrefWidth(75);

        var progressColumn = new TableColumn<TaskEntryView, Number>("Progress");
        progressColumn.setCellValueFactory(cdf -> cdf.getValue().progressProperty());
        progressColumn.setCellFactory(col -> new LabeledProgressBarTableCell());
        progressColumn.setPrefWidth(125);

        var nameColumn = new TableColumn<TaskEntryView, String>("Name");
        nameColumn.setCellValueFactory(cdf -> {
            var name = cdf.getValue().taskEntry().name();
            return Bindings.createStringBinding(() -> name);
        });
        nameColumn.setCellFactory(col -> {
            var cell = new TextFieldTableCell<TaskEntryView, String>();
            cell.setStyle("-fx-font-weight: bold");
            return cell;
        });
        nameColumn.setPrefWidth(200);

        var lastOccurrenceColumn = new TableColumn<TaskEntryView, String>("Last Occurrence");
        lastOccurrenceColumn.setCellValueFactory(cdf -> {
            var lastOccurrence = cdf.getValue().taskEntry().lastOccurrence().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
            return Bindings.createStringBinding(() -> lastOccurrence);
        });
        lastOccurrenceColumn.setPrefWidth(250);

        var nextOccurrenceColumn = new TableColumn<TaskEntryView, String>("Next Occurrence");
        nextOccurrenceColumn.setCellValueFactory(cdf -> {
            var nextOccurrence = cdf.getValue().taskEntry().nextOccurrence().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
            return Bindings.createStringBinding(() -> nextOccurrence);
        });
        nextOccurrenceColumn.setPrefWidth(250);

        view.getColumns().addAll(List.of(
            typeColumn,
            progressColumn,
            nameColumn,
            lastOccurrenceColumn,
            nextOccurrenceColumn
        ));

        // Default is to sort by DESCENDING progress
        view.getSortOrder().add(progressColumn);
        progressColumn.setSortType(TableColumn.SortType.DESCENDING);

        // We need to re-sort when a property changes
        var sortInvalidation = (InvalidationListener) obs -> view.sort();
        view.getItems().addListener((ListChangeListener<TaskEntryView>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (var removed : c.getRemoved()) {
                        for (var column : view.getColumns()) {
                            column.getCellObservableValue(removed).removeListener(sortInvalidation);
                        }
                    }
                }
                if (c.wasAdded()) {
                    for (var added : c.getAddedSubList()) {
                        for (var column : view.getColumns()) {
                            column.getCellObservableValue(added).addListener(sortInvalidation);
                        }
                    }
                }
            }
        });

        return view;
    }
}
