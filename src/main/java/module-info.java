/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
import org.jspecify.annotations.NullMarked;

/// This module is **explicitly** designed to not be consumed as a library.
/// Users should be using the api module instead.
@NullMarked
module net.minecraftforge.classtransformer {
    requires joptsimple;
    requires static org.jspecify;
    requires net.minecraftforge.renamer;
    // These are needed for renamer, but it's an automatic module, so we have to explicitly declare them
        requires org.objectweb.asm;
        requires org.objectweb.asm.commons;
        requires org.objectweb.asm.tree;

    requires net.minecraftforge.classtransformer.api;
    uses net.minecraftforge.classtransformer.api.JarTransformer;
}
