/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer;

import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.constant.ClassDesc;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ModulePathClassResolver implements Closeable {
    private final Map<String, ModuleReference> packages = new HashMap<>();
    private final Map<ModuleReference, ModuleReader> readers = new ConcurrentHashMap<>();

    void add(Path path) {
        var modules = ModuleFinder.of(path).findAll();
        for (var module : modules) {
            for (var pkg : module.descriptor().packages()) {
                packages.putIfAbsent(pkg, module);
            }
        }
    }

    ClassHierarchyResolver createResolver() {
        return ClassHierarchyResolver.ofResourceParsing(this::findClass)
            .orElse(ClassHierarchyResolver.defaultResolver()) // Return any normal java class
            .cached(ConcurrentHashMap::new);
    }

    private @Nullable InputStream findClass(ClassDesc classDesc) {
        var module = packages.get(classDesc.packageName());
        if (module == null)
            return null;

        var reader = readers.computeIfAbsent(module, ModulePathClassResolver::open);
        try {
            var desc = classDesc.descriptorString();
            var classResourceName = desc.substring(1, desc.length() - 1) + ".class";
            return reader.open(classResourceName).orElse(null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ModuleReader open(ModuleReference module) {
        try {
            return module.open();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        var exceptions =  new ArrayList<IOException>();
        for (var reader : readers.values()) {
            try {
                reader.close();
            } catch (IOException e) {
                exceptions.add(e);
            }
        }
        if (exceptions.isEmpty())
            return;

        if (exceptions.size() == 1)
            throw exceptions.getFirst();

        var error = new IOException("Failed to close modules");
        for (var e : exceptions) {
            error.addSuppressed(e);
        }
        throw error;
    }
}
