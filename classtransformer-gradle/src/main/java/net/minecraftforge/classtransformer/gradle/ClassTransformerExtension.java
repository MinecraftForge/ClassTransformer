/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

public interface ClassTransformerExtension {
    /// The name for this extension when added to [projects][org.gradle.api.Project].
    String NAME = "classTransformer";

    // region Bytecode Renaming
    default TaskProvider<TransformJar> classes(AbstractArchiveTask input) {
        return this.classes(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> classes(AbstractArchiveTask input, Action<? super TransformJar> action) {
        return this.classes("transform" + StringGroovyMethods.capitalize(input.getName()), input, action);
    }

    default TaskProvider<TransformJar> classes(TaskProvider<? extends AbstractArchiveTask> input) {
        return this.classes(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> classes(TaskProvider<? extends AbstractArchiveTask> input, Action<? super TransformJar> action) {
        return this.classes("transform" + StringGroovyMethods.capitalize(input.getName()), input, action);
    }

    default TaskProvider<TransformJar> classes(String name, AbstractArchiveTask input) {
        return this.classes(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> classes(String name, AbstractArchiveTask input, Action<? super TransformJar> action) {
        return this.classes(name, it -> {
            it.from(input);
            action.execute(it);
        });
    }

    default TaskProvider<TransformJar> classes(String name, TaskProvider<? extends AbstractArchiveTask> input) {
        return this.classes(input, it -> it.from(input));
    }

    default TaskProvider<TransformJar> classes(String name, TaskProvider<? extends AbstractArchiveTask> input, Action<? super TransformJar> action) {
        return this.classes(name, it -> {
            it.from(input);
            action.execute(it);
        });
    }

    default TaskProvider<TransformJar> classes(String name) {
        return this.classes(name, it -> {});
    }

    TaskProvider<TransformJar> classes(String name, Action<? super TransformJar> action);
    // endregion
}
