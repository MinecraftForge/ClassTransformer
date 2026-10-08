/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

module net.minecraftforge.classtransformer.demo.transformer {
    requires static org.jspecify;
    requires net.minecraftforge.classtransformer.api;

    provides net.minecraftforge.classtransformer.api.JarTransformer
        with net.minecraftforge.classtransformer.demo.transformer.TransformerService;
}