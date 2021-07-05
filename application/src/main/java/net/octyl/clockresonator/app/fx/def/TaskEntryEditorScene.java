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

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import dagger.Module;
import dagger.Provides;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.octyl.clockresonator.app.model.OneTimeTaskEntry;
import net.octyl.clockresonator.app.model.RepeatingTaskEntry;
import net.octyl.clockresonator.app.model.TaskEntry;
import net.octyl.clockresonator.app.model.TaskEntryManager;
import net.octyl.clockresonator.app.util.CronConstants;
import org.controlsfx.control.SearchableComboBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Module
public class TaskEntryEditorScene {
    @Qualifier
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    public @interface Def {
        String value() default "";
    }

    @Provides
    @TaskEntryEditorScope
    @Def("dirty")
    public static BooleanProperty dirtyProperty() {
        return new SimpleBooleanProperty(null, "dirty");
    }

    @Provides
    @TaskEntryEditorScope
    @Def
    public static Scene scene(
        @Def("mainPane") Parent mainPane,
        @Def("reset") Runnable resetRunnable
    ) {
        // Perform initial reset right now
        resetRunnable.run();
        return new Scene(mainPane, 600, 400);
    }

    @Provides
    @TaskEntryEditorScope
    @Def("mainPane")
    public static Parent mainPane(
        @Def TabPane tabPane,
        @Def("name") TextField nameField,
        @Def ButtonBar buttonBar
    ) {
        var grid = new GridPane();

        grid.setHgap(8);
        grid.setVgap(8);

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        GridPane.setHgrow(nameField, Priority.ALWAYS);

        grid.add(tabPane, 0, 1, 2, 1);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);

        var box = new VBox(buttonBar);
        box.setAlignment(Pos.BOTTOM_RIGHT);
        grid.add(box, 0, 2, 2, 1);
        GridPane.setVgrow(box, Priority.ALWAYS);
        GridPane.setValignment(box, VPos.BOTTOM);

        grid.setPadding(new Insets(8));

        return grid;
    }

    @Provides
    @TaskEntryEditorScope
    @Def
    public static TabPane tabPane(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("oneTime") Tab oneTimePanel,
        @Def("repeating") Tab repeatingPanel
    ) {
        var tabPane = new TabPane(
            oneTimePanel,
            repeatingPanel
        );
        tabPane.getSelectionModel().selectedItemProperty().addListener(observable ->
            dirtyProperty.set(true)
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("name")
    public static TextField nameField(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var field = new TextField();
        field.textProperty().addListener(observable -> dirtyProperty.set(true));
        field.setPromptText("e.g. Go shopping");
        return field;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("oneTime")
    public static Tab oneTimePanel(
        @Def("oneTime") DatePicker oneTimeNextOccurrence
    ) {
        var grid = new GridPane();

        grid.setVgap(8);
        grid.setHgap(8);

        grid.add(new Label("Next occurrence"), 0, 0);
        grid.add(oneTimeNextOccurrence, 1, 0);
        GridPane.setHgrow(oneTimeNextOccurrence, Priority.ALWAYS);

        grid.setPadding(new Insets(8));

        var tab = new Tab("One-Time", grid);
        tab.setGraphic(FontIcon.of(FontAwesomeSolid.CALENDAR_DAY, 16));
        return tab;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("oneTime")
    public static DatePicker oneTimeNextOccurrence(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var datePicker = new DatePicker();
        datePicker.valueProperty().addListener(observable -> dirtyProperty.set(true));
        datePicker.setMaxWidth(Double.POSITIVE_INFINITY);
        return datePicker;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("repeating")
    public static Tab repeatingPanel(
        @Def("repeating") ComboBox<ZoneId> repeatingTimeZone,
        @Def("repeating") TextField repeatingCronField,
        @Def("repeating") Label repeatingCronDescription,
        @Def("repeating") CheckBox repeatingStopTimeOn,
        @Def("repeating") DatePicker repeatingStopTime
    ) {
        var grid = new GridPane();

        grid.setVgap(8);
        grid.setHgap(8);

        int row = 0;

        var timeZone = new Label("Time Zone");
        timeZone.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(timeZone, 0, row);
        grid.add(repeatingTimeZone, 1, row);
        GridPane.setHgrow(repeatingTimeZone, Priority.ALWAYS);
        row++;

        var cronSchedule = new Label("Cron Schedule");
        cronSchedule.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(cronSchedule, 0, row);
        grid.add(repeatingCronField, 1, row);
        GridPane.setHgrow(repeatingCronField, Priority.ALWAYS);
        row++;

        var label = new Label("[Mi H DoM Mo DoW]");
        label.setTextFill(Color.GRAY);
        label.setAlignment(Pos.CENTER_RIGHT);
        grid.add(label, 0, row);
        grid.add(repeatingCronDescription, 1, row);
        row++;

        var stopAtWithCheck = new HBox(4, repeatingStopTimeOn, new Label("Stop At"));
        stopAtWithCheck.setAlignment(Pos.CENTER_LEFT);
        stopAtWithCheck.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(stopAtWithCheck, 0, row);
        grid.add(repeatingStopTime, 1, row);
        GridPane.setHgrow(repeatingStopTime, Priority.ALWAYS);
        row++;

        grid.setPadding(new Insets(8));

        var tab = new Tab("Repeating", grid);
        tab.setGraphic(FontIcon.of(FontAwesomeSolid.SYNC, 16));
        return tab;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("repeating")
    public static ComboBox<ZoneId> repeatingTimeZone(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var box = new SearchableComboBox<>(
            FXCollections.observableList(
                ZoneId.getAvailableZoneIds().stream()
                    .sorted()
                    .map(ZoneId::of)
                    .toList()
            )
        );
        box.valueProperty().addListener(observable -> dirtyProperty.set(true));
        box.setMaxWidth(Double.POSITIVE_INFINITY);
        return box;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("repeating")
    public static TextField repeatingCronField(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var field = new TextField();
        field.textProperty().addListener(observable -> dirtyProperty.set(true));
        field.setPromptText("e.g. 0 0 * * *");
        return field;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("repeating")
    public static Label repeatingCronDescription(
        @Def("repeating") TextField repeatingCronField
    ) {
        var label = new Label();
        label.setWrapText(true);
        label.textProperty().bind(
            Bindings.createStringBinding(() -> {
                Cron parsedCron;
                try {
                    parsedCron = CronConstants.PARSER.parse(repeatingCronField.getText());
                } catch (IllegalArgumentException e) {
                    return e.getMessage();
                }
                return "Occurs: " + CronConstants.DESCRIPTOR.describe(parsedCron);
            }, repeatingCronField.textProperty())
        );
        return label;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("repeating")
    public static CheckBox repeatingStopTimeOn(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var checkBox = new CheckBox();
        checkBox.selectedProperty().addListener(observable -> dirtyProperty.set(true));
        return checkBox;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("repeating")
    public static DatePicker repeatingStopTime(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("repeating") CheckBox repeatingStopTimeOn
    ) {
        var datePicker = new DatePicker();
        datePicker.valueProperty().addListener(observable -> dirtyProperty.set(true));
        datePicker.disableProperty().bind(repeatingStopTimeOn.selectedProperty().not());
        datePicker.setMaxWidth(Double.POSITIVE_INFINITY);
        return datePicker;
    }

    @Provides
    @TaskEntryEditorScope
    @Def
    public static ButtonBar buttonBar(
        @Def("done") Button done,
        @Def("cancel") Button cancel,
        @Def("reset") Button reset

    ) {
        // Create the ButtonBar instance
        ButtonBar buttonBar = new ButtonBar();

        ButtonBar.setButtonData(done, ButtonBar.ButtonData.OK_DONE);

        ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);

        ButtonBar.setButtonData(reset, ButtonBar.ButtonData.BACK_PREVIOUS);

        buttonBar.getButtons().addAll(done, cancel, reset);

        return buttonBar;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("done")
    public static Button done(
        @Def Stage stage,
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("done") Runnable doneFunction,
        @Def("name") TextField nameField,
        @Def("repeating") TextField repeatingCronField
    ) {
        // Responsible for creating the new entry and closing the dialog
        var btn = new Button("Done");
        btn.disableProperty().bind(
            // Disable if: not dirty
            dirtyProperty.not()
                // or no name
                .or(nameField.textProperty().isEmpty())
                // or cron invalid
                .or(Bindings.createBooleanBinding(() -> {
                    try {
                        CronConstants.PARSER.parse(repeatingCronField.getText());
                        return false;
                    } catch (IllegalArgumentException e) {
                        return true;
                    }
                }, repeatingCronField.textProperty()))
        );
        btn.setOnAction(event -> {
            doneFunction.run();
            stage.close();
        });
        return btn;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("cancel")
    public static Button cancel(@Def Stage stage) {
        // Responsible for closing the dialog
        var btn = new Button("Cancel");
        btn.setOnAction(event -> stage.close());
        return btn;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("reset")
    public static Button resetButton(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("reset") Runnable resetFunction
    ) {
        // Responsible for resetting back to the initial entry
        var btn = new Button("Reset");
        btn.disableProperty().bind(dirtyProperty.not());
        btn.setOnAction(event -> resetFunction.run());
        return btn;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("reset")
    public static Runnable resetFunction(
        TaskEntry initialTaskEntry,
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def TabPane tabPane,
        @Def("oneTime") Tab oneTimePane,
        @Def("repeating") Tab repeatingPane,
        @Def("name") TextField nameField,
        @Def("oneTime") DatePicker oneTimeNextOccurrence,
        @Def("repeating") ComboBox<ZoneId> repeatingTimeZone,
        @Def("repeating") TextField repeatingCronField,
        @Def("repeating") CheckBox repeatingStopTimeOn,
        @Def("repeating") DatePicker repeatingStopTime
    ) {
        return () -> {
            nameField.setText(initialTaskEntry.name());

            oneTimeNextOccurrence.setValue(
                (initialTaskEntry.nextOccurrence().equals(Instant.MAX)
                    ? Instant.now()
                    : initialTaskEntry.nextOccurrence()).atZone(ZoneId.systemDefault()).toLocalDate()
            );
            if (initialTaskEntry instanceof RepeatingTaskEntry repeatingTaskEntry) {
                tabPane.getSelectionModel().select(repeatingPane);
                repeatingTimeZone.setValue(repeatingTaskEntry.timeZone());
                repeatingCronField.setText(repeatingTaskEntry.cron().asString());
                repeatingStopTimeOn.setSelected(repeatingTaskEntry.stopTime().isPresent());
                repeatingTaskEntry.stopTime().ifPresent(stopTime ->
                    repeatingStopTime.setValue(stopTime.atZone(repeatingTaskEntry.timeZone()).toLocalDate())
                );
            } else {
                tabPane.getSelectionModel().select(oneTimePane);
                repeatingTimeZone.setValue(ZoneId.systemDefault());
            }
            dirtyProperty.set(false);
        };
    }

    @Provides
    @TaskEntryEditorScope
    @Def("done")
    public static Runnable doneFunction(
        TaskEntryManager taskEntryManager,
        TaskEntry initialTaskEntry,
        @Def TabPane tabPane,
        @Def("oneTime") Tab oneTimePane,
        @Def("repeating") Tab repeatingPane,
        @Def("name") TextField nameField,
        @Def("oneTime") DatePicker oneTimeNextOccurrence,
        @Def("repeating") ComboBox<ZoneId> repeatingTimeZone,
        @Def("repeating") TextField repeatingCronField,
        @Def("repeating") CheckBox repeatingStopTimeOn,
        @Def("repeating") DatePicker repeatingStopTime
    ) {
        return () -> {
            TaskEntry newTaskEntry;
            var selectedItem = tabPane.getSelectionModel().getSelectedItem();
            if (selectedItem == oneTimePane) {
                Instant lastOccurrence = initialTaskEntry.lastOccurrence().equals(Instant.MIN)
                    ? Instant.now()
                    : initialTaskEntry.lastOccurrence();
                newTaskEntry = new OneTimeTaskEntry(
                    initialTaskEntry.id(),
                    nameField.getText(),
                    lastOccurrence,
                    oneTimeNextOccurrence.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()
                );
            } else if (selectedItem == repeatingPane) {
                Cron cron = CronConstants.PARSER.parse(repeatingCronField.getText());
                var timeZone = repeatingTimeZone.getValue();
                var lastExecTime = ExecutionTime.forCron(cron).lastExecution(ZonedDateTime.now(timeZone))
                    .orElseThrow(() -> new IllegalStateException("Failed to find last occurrence"))
                    .toInstant();
                newTaskEntry = new RepeatingTaskEntry(
                    initialTaskEntry.id(),
                    nameField.getText(),
                    timeZone,
                    cron,
                    repeatingStopTimeOn.isSelected()
                        ? Optional.of(repeatingStopTime.getValue().atStartOfDay(timeZone).toInstant())
                        : Optional.empty(),
                    lastExecTime,
                    lastExecTime
                );
            } else {
                throw new IllegalStateException("Only two tabs should be present, got " + selectedItem);
            }
            taskEntryManager.put(newTaskEntry);
        };
    }
}
