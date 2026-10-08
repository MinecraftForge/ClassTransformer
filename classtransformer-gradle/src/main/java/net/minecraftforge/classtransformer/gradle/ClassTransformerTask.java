/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import net.minecraftforge.gradleutils.shared.EnhancedPlugin;
import net.minecraftforge.gradleutils.shared.EnhancedTask;
import org.gradle.api.Project;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
interface ClassTransformerTask extends EnhancedTask<ClassTransformerProblems> {
    @Override
    default Class<? extends EnhancedPlugin<? super Project>> pluginType() {
        return ClassTransformerPlugin.class;
    }

    @Override
    default Class<ClassTransformerProblems> problemsType() {
        return ClassTransformerProblems.class;
    }
}
