/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.classtransformer.api;

import org.jspecify.annotations.Nullable;

import java.lang.classfile.attribute.ModulePackagesAttribute;
import java.lang.constant.ModuleDesc;
import java.util.Collection;
import java.util.List;

/// A service found by the static transformer processor
/// This is intended to allow the transformers to have full information about the jar.
/// As well as giving a way to deal with non-class files, and adding extra files.
public non-sealed interface JarTransformer extends ClassFileTransformer {
    /// Process a non-class resource entry in the jar
    ///
    /// @param moduleDesc The name of the module this resource is in
    /// @param entry The entry information
    /// @return The new entry data to be used, if null is returned this will effectively remove the file from the jar
    default @Nullable Entry transformResource(ModuleDesc moduleDesc, Entry entry) {
        return entry;
    }

    /// Return a collection of any new entries to add to the final jar.
    /// Currently, these entries are not submitted to other transformers.
    /// Remember to also transform the module-info.class' [ModulePackagesAttribute] if you are adding new class entries
    /// to the jar that are in new packages. Beware that duplicate entries in the jar across all transformers will cause
    /// an exception to be thrown.
    default Collection<Entry> getExtras() {
        return List.of();
    }

    record Entry(String name, long time, byte[] data){}
}
