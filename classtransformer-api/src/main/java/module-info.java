/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

import org.jspecify.annotations.NullMarked;

@NullMarked
module net.minecraftforge.classtransformer.api {
    requires transitive static org.jspecify;
    exports net.minecraftforge.classtransformer.api;
}
