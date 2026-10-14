import org.jspecify.annotations.NullMarked;

/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
@NullMarked
module net.minecraftforge.classtransformer.demo.transformer {
    requires org.jspecify;
    requires net.minecraftforge.classtransformer.api;

    provides net.minecraftforge.classtransformer.api.JarTransformer
        with net.minecraftforge.classtransformer.demo.transformer.TransformerService;
}
