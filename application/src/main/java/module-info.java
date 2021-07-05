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

module net.octyl.clockresonator.app {
    exports net.octyl.clockresonator.app;
    requires static java.inject;
    requires com.cronutils;
    requires com.google.common;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires dagger;
    requires dev.dirs;
    requires java.base;
    requires javafx.controls;
    requires org.apache.logging.log4j;
    requires org.checkerframework.checker.qual;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    // Required for event bus to work
    opens net.octyl.clockresonator.app.fx to com.google.common, com.fasterxml.jackson.databind;
    // Required for serialization to work
    opens net.octyl.clockresonator.app.model to com.fasterxml.jackson.databind;
}
