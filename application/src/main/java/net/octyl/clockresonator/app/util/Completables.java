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

package net.octyl.clockresonator.app.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Completables {
    // A small hack to get the executor used by CF
    private static final Executor DEFAULT_EXECUTOR = new CompletableFuture<>().defaultExecutor();

    public static <T> CompletableFuture<T> callAsync(Callable<T> callable) {
        return callAsync(DEFAULT_EXECUTOR, callable);
    }

    public static <T> CompletableFuture<T> callAsync(Executor executor, Callable<T> callable) {
        var ftr = new CompletableFuture<T>();
        executor.execute(() -> {
            if (!ftr.isDone()) {
                try {
                    ftr.complete(callable.call());
                } catch (Throwable t) {
                    ftr.completeExceptionally(t);
                }
            }
        });
        return ftr;
    }

    private Completables() {
    }
}
