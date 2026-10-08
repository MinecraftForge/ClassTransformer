/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import net.minecraftforge.gradleutils.shared.EnhancedPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;

import javax.inject.Inject;

abstract class ClassTransformerPlugin extends EnhancedPlugin<Project> {
    static final String NAME = "classTransfromer";
    static final String DISPLAY_NAME = "Class Transformer Gradle";

    @Inject
    public ClassTransformerPlugin() {
        super(NAME, DISPLAY_NAME, NAME + "Tools");
    }

    @Override
    public void setup(Project project) {
        project.getPluginManager().apply(BasePlugin.class);
        project.getExtensions().create(ClassTransformerExtension.class, ClassTransformerExtension.NAME, ClassTransformerExtensionImpl.class);
    }
}
