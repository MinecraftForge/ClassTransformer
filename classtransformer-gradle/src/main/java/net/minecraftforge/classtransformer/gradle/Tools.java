/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer.gradle;

import net.minecraftforge.gradleutils.shared.Tool;

final class Tools {
    private Tools() { }

    static final Tool TOOL = Tool.ofForge(
    "tool",
    "net.minecraftforge:classtransformer:0.1.0",
    25,
    "net.minecraftforge.classtransformer.Main",
    "net.minecraftforge.classtransformer"
    );
}
