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

import dagger.Module;
import dagger.Provides;
import dev.dirs.ProjectDirectories;
import net.octyl.clockresonator.app.ClockResonator;

import javax.inject.Singleton;
import java.lang.module.ModuleDescriptor;

@Module
public class ApplicationInfoModule {
    @Provides
    @Singleton
    @ApplicationVersion
    public static String provideApplicationVersion() {
        return ClockResonator.class.getModule().getDescriptor().version()
            .map(ModuleDescriptor.Version::toString)
            .orElse("UNKNOWN");
    }

    @Provides
    @Singleton
    public static ProjectDirectories provideProjectDirectories() {
        return ProjectDirectories.from("net", "octyl", "Clock Resonator");
    }
}
