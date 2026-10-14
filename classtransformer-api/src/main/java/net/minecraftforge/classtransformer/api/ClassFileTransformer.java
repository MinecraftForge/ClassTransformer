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

/// A [ClassFileTransformer] is responsible for possibly transforming the bytecode of classes at build-time.
///
/// ### Build-time transformation pipeline
/// Implementations of this interface that extend [JarTransformer] are [service loaded](ServiceLoader) before any
/// modules are passed to transformers. You can perform any necessary one-time setup in your record's static initialiser
/// or constructor.
///
/// When a module is loaded, each transformer is asked if they might be interested in transforming any classes inside it
/// by calling [#handlesModule(ModuleReference)], which is called once per pending module. If you already know the exact
/// module names you are interested in by this point, you can override [#handlesModule(ModuleDesc)] instead.
///
/// Once a module is loaded and a class from inside that module is being loaded, the transformers that declared they
/// handle the module are asked if they might be interested in transforming the class by calling
/// [#handlesClass(ModuleDesc, ClassDesc)].
///
/// If you declare that you handle the module and class, your [#transformClass(ModuleDesc, ClassDesc, ClassModel)]
/// method is then called with a lazily-parsed, immutable representation of the class file that is shared among all
/// transformers that handle this module and class. You can return your [ClassTransform] which is chained with other
/// transformers with [ClassTransform#andThen(ClassTransform)], or [ClassTransform#ACCEPT_ALL] to indicate that you are
/// not interested in transforming the class after further inspection.
///
/// Finally, the chained [ClassTransform]s are applied against the class bytes and the class is loaded.
public sealed interface ClassFileTransformer permits JarTransformer {
    /// Called once per module before they are loaded, each transformer is given the opportunity to inspect the module
    /// and determine if it is interested in transforming any classes inside it.
    ///
    /// @return Whether this transformer might be interested in transforming the given module.
    ///         Returning `false` will filter out your transformer for this module, which helps improve performance by
    ///         avoiding redundant work.
    ///
    /// @apiNote This method is guaranteed to be called no more than once per module
    /// @apiNote You should perform any necessary per-module setup in this method. Use your record's static initialiser
    ///          or constructor for one-time setup that is not specific to individual modules.
    ///
    /// @implNote The default implementation of this method delegates to [#handlesModule(ModuleDesc)] using the module's
    ///           name.
    default boolean handlesModule(ModuleReference moduleRef) {
        return handlesModule(ModuleDesc.of(moduleRef.descriptor().name()));
    }

    /// A convenience method for transformers that know the exact module names they are interested in. This method has
    /// the same invariants as [#handlesModule(ModuleReference)] and is called by the default implementation of that
    /// method. If you override [#handlesModule(ModuleReference)], you can safely override this method to always return
    /// `false` as it will never be called by the transformation pipeline.
    ///
    /// @param moduleDesc The name of the module being considered for transformation
    ///
    /// @return Whether this transformer might be interested in transforming the given module.
    ///         Returning `false` will filter out your transformer for this module, which helps improve performance by
    ///         avoiding redundant work.
    ///
    /// @see #handlesModule(ModuleReference)
    boolean handlesModule(ModuleDesc moduleDesc);

    /// Called once per class before they are loaded, each transformer is asked if they might be interested in
    /// transforming the class based on its [ClassDesc] and the [ModuleDesc] of the module it belongs to.
    ///
    /// @param moduleDesc The name of the module that contains the class being considered for transformation
    /// @param classDesc The symbolic descriptor of the class being considered for transformation
    ///
    /// @return Whether this transformer might be interested in transforming the given class based on its [ClassDesc].
    ///         Returning `false` will filter out your transformer for this class, which helps improve performance by
    ///         avoiding redundant work.
    ///
    /// @apiNote This method might be called multiple times for the same class name if the module is a multi-release jar
    ///          as each version of the class is considered separately.
    /// @apiNote The [ModuleDesc] is provided for convenience so that transformers can easily look up module-level
    ///          information without needing to first resolve where the class came from.
    ///
    ///          You do not need to check against the provided [ModuleDesc] if you already implemented
    ///          [#handlesModule(ModuleReference)] or [#handlesModule(ModuleDesc)] to filter out modules you are not
    ///          interested in, as we guarantee that this method will not be called for modules you don't handle.
    boolean handlesClass(ModuleDesc moduleDesc, ClassDesc classDesc);

    /// If [ClassFileTransformer#handlesModule(ModuleReference)] returned true for the given module, and
    /// [ClassFileTransformer#handlesClass] returned `true` for the given class, this method is called to transform it.
    ///
    /// @param moduleDesc The name of the module that contains the class being transformed
    /// @param classDesc The symbolic descriptor of the class being transformed
    /// @param classModel A lazily-parsed, immutable representation of the class file that is shared among all
    ///                   transformers that handle this module and class.
    ///
    /// @return A [ClassTransform] that describes how to transform the class, or [ClassTransform#ACCEPT_ALL] if this
    ///         transformer is not interested in transforming the class after inspecting its [ClassModel].
    ///
    /// @apiNote The returned [ClassTransform] is chained with other transformers using [ClassTransform#andThen].
    /// @apiNote The ClassFile API relies heavily on lazy parsing and deferred immutability to improve performance and
    ///          reduce memory usage. We recommend adopting an early-return strategy and only accessing what you need
    ///          to avoid unnecessary work.
    /// @apiNote Because [ClassTransform] objects are typically stateless and immutable, their instances can be shared
    ///          across multiple classes. You are encouraged to store stateless transforms in static final fields and
    ///          compose them where possible.
    ClassTransform transformClass(ModuleDesc moduleDesc, ClassDesc classDesc, ClassModel classModel);
}
