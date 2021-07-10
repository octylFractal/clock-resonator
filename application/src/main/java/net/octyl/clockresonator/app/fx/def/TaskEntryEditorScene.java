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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import net.octyl.clockresonator.app.model.CronTaskEntry;
import net.octyl.clockresonator.app.model.IntervalTaskEntry;
import net.octyl.clockresonator.app.model.OneTimeTaskEntry;
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
import java.time.LocalDate;
import java.time.Period;
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
    @Def("stopTimeOn")
    public static BooleanProperty stopTimeOnProperty() {
        return new SimpleBooleanProperty(null, "stopTimeOn");
    }

    @Provides
    @TaskEntryEditorScope
    @Def("stopTime")
    public static ObjectProperty<LocalDate> stopTimeProperty() {
        return new SimpleObjectProperty<>(null, "stopTime");
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
        @Def("oneTime") Tab oneTimePane,
        @Def("cron") Tab cronPane,
        @Def("interval") Tab intervalPane
    ) {
        var tabPane = new TabPane(
            oneTimePane,
            cronPane,
            intervalPane
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
    public static Tab oneTimePane(
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
    @Def("cron")
    public static Tab cronPane(
        @Def("cron") ComboBox<ZoneId> cronTimeZone,
        @Def("cron") TextField cronCronField,
        @Def("cron") Label cronCronDescription,
        @Def("cron") CheckBox cronStopTimeOn,
        @Def("cron") DatePicker cronStopTime
    ) {
        var grid = new GridPane();

        grid.setVgap(8);
        grid.setHgap(8);

        int row = 0;

        var timeZone = new Label("Time Zone");
        timeZone.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(timeZone, 0, row);
        grid.add(cronTimeZone, 1, row);
        GridPane.setHgrow(cronTimeZone, Priority.ALWAYS);
        row++;

        var cronSchedule = new Label("Cron Schedule");
        cronSchedule.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(cronSchedule, 0, row);
        grid.add(cronCronField, 1, row);
        GridPane.setHgrow(cronCronField, Priority.ALWAYS);
        row++;

        var label = new Label("[Mi H DoM Mo DoW]");
        label.setTextFill(Color.GRAY);
        label.setAlignment(Pos.CENTER_RIGHT);
        grid.add(label, 0, row);
        grid.add(cronCronDescription, 1, row);
        row++;

        var stopAtWithCheck = new HBox(4, cronStopTimeOn, new Label("Stop At"));
        stopAtWithCheck.setAlignment(Pos.CENTER_LEFT);
        stopAtWithCheck.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(stopAtWithCheck, 0, row);
        grid.add(cronStopTime, 1, row);
        GridPane.setHgrow(cronStopTime, Priority.ALWAYS);
        row++;

        grid.setPadding(new Insets(8));

        var tab = new Tab("Cron", grid);
        tab.setGraphic(FontIcon.of(FontAwesomeSolid.CALENDAR_ALT, 16));
        return tab;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("cron")
    public static ComboBox<ZoneId> cronTimeZone(
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
    @Def("cron")
    public static TextField cronCronField(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var field = new TextField();
        field.textProperty().addListener(observable -> dirtyProperty.set(true));
        field.setPromptText("e.g. 0 0 * * *");
        return field;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("cron")
    public static Label cronCronDescription(
        @Def("cron") TextField cronCronField
    ) {
        var label = new Label();
        label.setWrapText(true);
        label.textProperty().bind(
            Bindings.createStringBinding(() -> {
                Cron parsedCron;
                try {
                    parsedCron = CronConstants.PARSER.parse(cronCronField.getText());
                } catch (IllegalArgumentException e) {
                    return e.getMessage();
                }
                return "Occurs: " + CronConstants.DESCRIPTOR.describe(parsedCron);
            }, cronCronField.textProperty())
        );
        return label;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("cron")
    public static CheckBox cronStopTimeOn(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("stopTimeOn") BooleanProperty stopTimeOnProperty
    ) {
        var checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(stopTimeOnProperty);
        checkBox.selectedProperty().addListener(observable -> dirtyProperty.set(true));
        return checkBox;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("cron")
    public static DatePicker cronStopTime(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("stopTimeOn") BooleanProperty stopTimeOnProperty,
        @Def("stopTime") ObjectProperty<LocalDate> stopTimeProperty
    ) {
        var datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(stopTimeProperty);
        datePicker.valueProperty().addListener(observable -> dirtyProperty.set(true));
        datePicker.disableProperty().bind(stopTimeOnProperty.not());
        datePicker.setMaxWidth(Double.POSITIVE_INFINITY);
        return datePicker;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("interval")
    public static Tab intervalPane(
        @Def("intervalYears") TextField intervalYears,
        @Def("intervalMonths") TextField intervalMonths,
        @Def("intervalDays") TextField intervalDays,
        @Def("interval") CheckBox intervalStopTimeOn,
        @Def("interval") DatePicker intervalStopTime
    ) {
        var grid = new GridPane();

        grid.setVgap(8);
        grid.setHgap(8);

        int row = 0;

        var intervalDuration = new HBox(4, intervalYears, intervalMonths, intervalDays);
        var cronSchedule = new Label("Interval [Y/M/D]");
        cronSchedule.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(cronSchedule, 0, row);
        grid.add(intervalDuration, 1, row);
        GridPane.setHgrow(intervalDuration, Priority.ALWAYS);
        row++;

        var stopAtWithCheck = new HBox(4, intervalStopTimeOn, new Label("Stop At"));
        stopAtWithCheck.setAlignment(Pos.CENTER_LEFT);
        stopAtWithCheck.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.add(stopAtWithCheck, 0, row);
        grid.add(intervalStopTime, 1, row);
        GridPane.setHgrow(intervalStopTime, Priority.ALWAYS);
        row++;

        grid.setPadding(new Insets(8));

        var tab = new Tab("Interval", grid);
        tab.setGraphic(FontIcon.of(FontAwesomeSolid.SYNC, 16));
        return tab;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("intervalYears")
    public static TextField intervalYears(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var field = new TextField();
        field.textProperty().addListener(observable -> dirtyProperty.set(true));
        field.setTextFormatter(new TextFormatter<>(new NumberStringConverter(), null, change -> {
            // Reject non-numerical additions
            if (change.isAdded() && !change.getText().matches("[0-9]*")) {
                return null;
            }
            return change;
        }));
        field.setPromptText("Years");
        return field;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("intervalMonths")
    public static TextField intervalMonths(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var field = new TextField();
        field.textProperty().addListener(observable -> dirtyProperty.set(true));
        field.setTextFormatter(new TextFormatter<>(new NumberStringConverter(), null, change -> {
            // Reject non-numerical additions
            if (change.isAdded() && !change.getText().matches("[0-9]*")) {
                return null;
            }
            return change;
        }));
        field.setPromptText("Months");
        return field;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("intervalDays")
    public static TextField intervalDays(
        @Def("dirty") BooleanProperty dirtyProperty
    ) {
        var field = new TextField();
        field.textProperty().addListener(observable -> dirtyProperty.set(true));
        field.setTextFormatter(new TextFormatter<>(new NumberStringConverter(), null, change -> {
            // Reject non-numerical additions
            if (change.isAdded() && !change.getText().matches("[0-9]*")) {
                return null;
            }
            return change;
        }));
        field.setPromptText("Days");
        return field;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("interval")
    public static CheckBox intervalStopTimeOn(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("stopTimeOn") BooleanProperty stopTimeOnProperty
    ) {
        var checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(stopTimeOnProperty);
        checkBox.selectedProperty().addListener(observable -> dirtyProperty.set(true));
        return checkBox;
    }

    @Provides
    @TaskEntryEditorScope
    @Def("interval")
    public static DatePicker intervalStopTime(
        @Def("dirty") BooleanProperty dirtyProperty,
        @Def("stopTimeOn") BooleanProperty stopTimeOnProperty,
        @Def("stopTime") ObjectProperty<LocalDate> stopTimeProperty
    ) {
        var datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(stopTimeProperty);
        datePicker.valueProperty().addListener(observable -> dirtyProperty.set(true));
        datePicker.disableProperty().bind(stopTimeOnProperty.not());
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
        @Def TabPane tabPane,
        @Def("cron") Tab cronPane,
        @Def("interval") Tab intervalPane,
        @Def("name") TextField nameField,
        @Def("cron") TextField cronCronField,
        @Def("intervalYears") TextField intervalYears,
        @Def("intervalMonths") TextField intervalMonths,
        @Def("intervalDays") TextField intervalDays
    ) {
        // Responsible for creating the new entry and closing the dialog
        var btn = new Button("Done");
        var isCronPane = tabPane.getSelectionModel().selectedItemProperty().isEqualTo(cronPane);
        var isIntervalPane = tabPane.getSelectionModel().selectedItemProperty().isEqualTo(intervalPane);
        var cronInvalid = Bindings.createBooleanBinding(() -> {
            try {
                CronConstants.PARSER.parse(cronCronField.getText());
                return false;
            } catch (IllegalArgumentException e) {
                return true;
            }
        }, cronCronField.textProperty());
        var intervalInvalid = intervalYears.textProperty().isEmpty()
            .or(intervalMonths.textProperty().isEmpty())
            .or(intervalDays.textProperty().isEmpty());
        btn.disableProperty().bind(
            dirtyProperty.not()
                .or(nameField.textProperty().isEmpty())
                .or(isCronPane.and(cronInvalid))
                .or(isIntervalPane.and(intervalInvalid))
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
        @Def("cron") Tab cronPane,
        @Def("interval") Tab intervalPane,
        @Def("name") TextField nameField,
        @Def("oneTime") DatePicker oneTimeNextOccurrence,
        @Def("cron") ComboBox<ZoneId> cronTimeZone,
        @Def("cron") TextField cronCronField,
        @Def("intervalYears") TextField intervalYears,
        @Def("intervalMonths") TextField intervalMonths,
        @Def("intervalDays") TextField intervalDays,
        @Def("stopTimeOn") BooleanProperty stopTimeOnProperty,
        @Def("stopTime") ObjectProperty<LocalDate> stopTimeProperty
    ) {
        return () -> {
            nameField.setText(initialTaskEntry.name());

            oneTimeNextOccurrence.setValue(
                (initialTaskEntry.nextOccurrence().equals(Instant.MAX)
                    ? Instant.now()
                    : initialTaskEntry.nextOccurrence()).atZone(ZoneId.systemDefault()).toLocalDate()
            );

            if (initialTaskEntry instanceof OneTimeTaskEntry) {
                tabPane.getSelectionModel().select(oneTimePane);
                cronTimeZone.setValue(ZoneId.systemDefault());
            } else if (initialTaskEntry instanceof CronTaskEntry cronTaskEntry) {
                tabPane.getSelectionModel().select(cronPane);
                cronTimeZone.setValue(cronTaskEntry.timeZone());
                cronCronField.setText(cronTaskEntry.cron().asString());
                stopTimeOnProperty.set(cronTaskEntry.stopTime().isPresent());
                cronTaskEntry.stopTime().ifPresent(stopTime ->
                    stopTimeProperty.setValue(stopTime.atZone(cronTaskEntry.timeZone()).toLocalDate())
                );
            } else if (initialTaskEntry instanceof IntervalTaskEntry intervalTaskEntry) {
                tabPane.getSelectionModel().select(intervalPane);
                intervalYears.setText(String.valueOf(intervalTaskEntry.interval().getYears()));
                intervalMonths.setText(String.valueOf(intervalTaskEntry.interval().getMonths()));
                intervalDays.setText(String.valueOf(intervalTaskEntry.interval().getDays()));
                stopTimeOnProperty.set(intervalTaskEntry.stopTime().isPresent());
                intervalTaskEntry.stopTime().ifPresent(stopTime ->
                    stopTimeProperty.setValue(stopTime.atZone(ZoneId.systemDefault()).toLocalDate())
                );
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
        @Def("cron") Tab cronPane,
        @Def("interval") Tab intervalPane,
        @Def("name") TextField nameField,
        @Def("oneTime") DatePicker oneTimeNextOccurrence,
        @Def("cron") ComboBox<ZoneId> cronTimeZone,
        @Def("cron") TextField cronCronField,
        @Def("intervalYears") TextField intervalYears,
        @Def("intervalMonths") TextField intervalMonths,
        @Def("intervalDays") TextField intervalDays,
        @Def("stopTimeOn") BooleanProperty stopTimeOnProperty,
        @Def("stopTime") ObjectProperty<LocalDate> stopTimeProperty
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
            } else if (selectedItem == cronPane) {
                Cron cron = CronConstants.PARSER.parse(cronCronField.getText());
                var timeZone = cronTimeZone.getValue();
                var lastExecTime = ExecutionTime.forCron(cron).lastExecution(ZonedDateTime.now(timeZone))
                    .orElseThrow(() -> new IllegalStateException("Failed to find last occurrence"))
                    .toInstant();
                newTaskEntry = new CronTaskEntry(
                    initialTaskEntry.id(),
                    nameField.getText(),
                    timeZone,
                    cron,
                    stopTimeOnProperty.get()
                        ? Optional.of(stopTimeProperty.get().atStartOfDay(timeZone).toInstant())
                        : Optional.empty(),
                    lastExecTime,
                    lastExecTime
                );
            } else if (selectedItem == intervalPane) {
                var timeZone = cronTimeZone.getValue();
                var now = Instant.now();
                newTaskEntry = new IntervalTaskEntry(
                    initialTaskEntry.id(),
                    nameField.getText(),
                    Period.of(
                        Integer.parseInt(intervalYears.getText()),
                        Integer.parseInt(intervalMonths.getText()),
                        Integer.parseInt(intervalDays.getText())
                    ),
                    stopTimeOnProperty.get()
                        ? Optional.of(stopTimeProperty.get().atStartOfDay(timeZone).toInstant())
                        : Optional.empty(),
                    now
                );
            } else {
                throw new IllegalStateException("Only three tabs should be present, got " + selectedItem);
            }
            taskEntryManager.put(newTaskEntry);
        };
    }
}
