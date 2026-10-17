/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

public interface ClassTransformerExtension {
    /// The name for this extension when added to [projects][org.gradle.api.Project].
    String NAME = "classTransformer";

    // region Bytecode Renaming
    default TaskProvider<TransformJar> register(AbstractArchiveTask input) {
        return this.register(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> register(AbstractArchiveTask input, Action<? super TransformJar> action) {
        return this.register("transform" + StringGroovyMethods.capitalize(input.getName()), input, action);
    }

    default TaskProvider<TransformJar> register(TaskProvider<? extends AbstractArchiveTask> input) {
        return this.register(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> register(TaskProvider<? extends AbstractArchiveTask> input, Action<? super TransformJar> action) {
        return this.register("transform" + StringGroovyMethods.capitalize(input.getName()), input, action);
    }

    default TaskProvider<TransformJar> register(String name, AbstractArchiveTask input) {
        return this.register(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> register(String name, AbstractArchiveTask input, Action<? super TransformJar> action) {
        return this.register(name, it -> {
            it.from(input);
            action.execute(it);
        });
    }

    default TaskProvider<TransformJar> register(String name, TaskProvider<? extends AbstractArchiveTask> input) {
        return this.register(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> register(String name, TaskProvider<? extends AbstractArchiveTask> input, Action<? super TransformJar> action) {
        return this.register(name, it -> {
            it.from(input);
            action.execute(it);
        });
    }

    default TaskProvider<TransformJar> register(String name) {
        return this.register(name, it -> {});
    }

    TaskProvider<TransformJar> register(String name, Action<? super TransformJar> action);
    // endregion
}
