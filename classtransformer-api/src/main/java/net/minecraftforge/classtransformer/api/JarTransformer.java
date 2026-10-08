/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.classtransformer.api;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/// A service found by the static transformer processor
/// This is intended to allow the transformers to have full information about the jar.
/// As well as giving a way to deal with non-class files, and adding extra files.
public interface JarTransformer extends ClassFileTransformer {
    /// Will be called before any entries are processed.
    /// This is meant to allow you pre-load any global data that is needed for your specific handle methods
    ///
    /// @param existing The list of existing entries in the jar
    /// @param dataProvider Will return the raw byte[] for the requested entry
    default void preprocess(Set<String> existing, Function<String, byte@Nullable[]> dataProvider) {
    }

    /// Process a non-class resource entry in the jar
    ///
    /// @param entry The entry information
    /// @return The new entry data to be used, if null is returned this will effectively remove the file from the jar
    default @Nullable Entry handleResource(Entry entry) {
        return entry;
    }

    /// Return a collection of any new entries to add to the final jar
    /// Currently, these entries are not submitted to other transformers
    default Collection<Entry> getExtras() {
        return List.of();
    }

    record Entry(String name, long time, byte[] data){}
}
