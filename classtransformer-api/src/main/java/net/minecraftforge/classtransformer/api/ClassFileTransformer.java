/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.classtransformer.api;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.lang.module.ModuleReference;

public interface ClassFileTransformer {
    default boolean handlesModule(ModuleReference moduleRef) {
        return handlesModule(ModuleDesc.of(moduleRef.descriptor().name()));
    }

    /// @return Whether this transformer might be interested in transforming the given module.
    ///         This is mainly useful for targeting specific modules, as well as for skipping modules that
    ///         are definitely not of interest.
    boolean handlesModule(ModuleDesc moduleDesc);

    /// @return Whether this transformer might be interested in transforming the given class based on its [ClassDesc].
    ///         This is mainly useful for targeting specific classes or packages, as well as for skipping packages that
    ///         are definitely not of interest.
    boolean handlesClass(ModuleDesc moduleDesc, ClassDesc classDesc);

    /// If [ClassFileTransformer#handlesClass] returned `true` for the given class, this method is called to transform it.
    /// The returned [ClassTransform] is chained with other transformers' transforms using [ClassTransform#andThen].
    ///
    /// If this transformer is not interested in transforming the class, it should return [ClassTransform#ACCEPT_ALL].
    ClassTransform transform(ModuleDesc moduleDesc, ClassDesc classDesc, ClassModel classModel);
}
