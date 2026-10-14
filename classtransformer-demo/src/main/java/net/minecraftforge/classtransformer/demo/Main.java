/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.classtransformer.demo;

public final class Main {
    void main() {
        IO.println("Message: " + getMessage());
    }

    private static String getMessage() {
        return "ORIGINAL";
    }
}
