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

package net.octyl.clockresonator.app.jackson;

import com.cronutils.model.Cron;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.octyl.clockresonator.app.inject.ApplicationInfoModule;

public class CronModule extends SimpleModule {
    private static final Version VERSION = VersionUtil.parseVersion(
        ApplicationInfoModule.provideApplicationVersion(),
        "net.octyl.clockresonator",
        "clockresonator-jackson-cron"
    );

    public CronModule() {
        super(VERSION);
        addDeserializer(Cron.class, new CronDeserializer());
        addSerializer(new CronSerializer());
    }
}
