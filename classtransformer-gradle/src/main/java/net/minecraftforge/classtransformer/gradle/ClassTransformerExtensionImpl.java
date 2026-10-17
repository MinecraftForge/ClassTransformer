/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskProvider;

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

    private <T extends Task> TaskProvider<T> addToAssemble(TaskProvider<T> dep) {
        var tasks = getProject().getTasks();
        // Make the assemble task build our file, like the normal java plugin does
        tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure(task -> task.dependsOn(dep));
        return dep;
    }

    @Override
    public TaskProvider<TransformJar> register(String name, Action<? super TransformJar> action) {
        var ret = getProject().getTasks().register(name, TransformJar.class);
        ret.configure(action);
        return addToAssemble(ret);
    }
}
