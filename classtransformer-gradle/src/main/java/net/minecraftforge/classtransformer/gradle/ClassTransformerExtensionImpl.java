/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyFactory;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;

abstract class ClassTransformerExtensionImpl implements ClassTransformerExtension, HasPublicType {
    protected abstract @Inject Project getProject();
    protected abstract @Inject ObjectFactory getObjects();

    @Inject
    public ClassTransformerExtensionImpl() { }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(ClassTransformerExtension.class);
    }

    private static final String ASSEMBLE = "assemble";
    private <T extends Task> TaskProvider<T> addToAssemble(TaskProvider<T> dep) {
        var tasks = getProject().getTasks();
        // Make the assemble task build our file, like the normal java plugin does
        if (tasks.getNames().contains(ASSEMBLE))
            tasks.named(ASSEMBLE).configure(task -> task.dependsOn(dep));
        return dep;
    }

    @Override
    public TaskProvider<TransformJar> classes(String name, Action<? super TransformJar> action) {
        var ret = getProject().getTasks().register(name, TransformJar.class);
        ret.configure(action);
        return addToAssemble(ret);
    }
}
