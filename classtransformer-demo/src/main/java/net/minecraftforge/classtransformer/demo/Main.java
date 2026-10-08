/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.classtransformer.demo;

public class Main {
    static void main(String[] args) {
        System.out.println("Message: " + getMessage());
    }

    private static String getMessage() {
        return "ORIGINAL";
    }
}
