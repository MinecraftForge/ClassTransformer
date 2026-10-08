/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer;

import net.minecraftforge.classtransformer.api.JarTransformer;
import net.minecraftforge.renamer.api.Transformer;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

class JarTransformerWrapper implements Transformer {
    private final Consumer<String> log;
    private final List<JarTransformer> transformers;
    private @Nullable ModuleDesc moduleDesc;

    JarTransformerWrapper(Consumer<String> log, List<JarTransformer> transformers) {
        this.log = log;
        this.transformers = transformers;
    }

    @Override
    public void preprocess(Map<String, Entry> entries) {
        moduleDesc = findModuleDesc(entries);
        log.accept("ModuleDesc: " + moduleDesc);

        Function<String, byte[]> data = name -> {
            var entry = entries.get(name);
            return entry == null ? null : entry.getData();
        };

        for (var transformer : transformers)
            transformer.preprocess(entries.keySet(), data);
    }

    private ModuleDesc findModuleDesc(Map<String, Entry> entries) {
        var module = entries.get("module-info.class");
        if (module != null)
            return ModuleDesc.of(readModuleInfoName(module));

        // Technically JarFile supports the manifest being any case, but that's a pain and everything outputs it as upper case these days.
        var manifest = readManifest(entries.get(JarFile.MANIFEST_NAME));
        if (manifest == null) {
            // No manifest, so it will be based off the file name, which last I looked into it is implementation specific on the JVM.
            // As well as this API being designed for the module world, I'm just gunna fall back to naming the module 'unknown'
            return ModuleDesc.of("unknown");
        }

        var multiRelease = Boolean.parseBoolean(manifest.getMainAttributes().getValue("Multi-Release"));
        if (multiRelease) {
            // If this is multi-release, then the module-info *might* be in a versioned folder.
            // We have to do the slow lookup to find it
            for (var entry : entries.entrySet()) {
                if (entry.getKey().endsWith("module-info.class") && entry.getKey().startsWith("META-INF/versions/"))
                    return ModuleDesc.of(readModuleInfoName(entry.getValue()));
            }
        }

        var automatic = manifest.getMainAttributes().getValue("Automatic-Module-Name");
        if (automatic != null)
            return ModuleDesc.of(automatic);

        // if all else fails, we are unknown
        return ModuleDesc.of("unknown");
    }

    private static String readModuleInfoName(Transformer.Entry entry) {
        var classModel = ClassFile.of().parse(entry.getData());
        var modAttr = classModel.findAttribute(Attributes.module()).orElse(null);
        if (modAttr == null)
            throw new NoSuchElementException("Module info class " + entry.getName() + " does not have module attribute");
        return modAttr.moduleName().name().stringValue();
    }

    private static @Nullable Manifest readManifest(Transformer.@Nullable Entry entry) {
        if (entry == null)
            return null;

        var mf = new Manifest();
        try {
            mf.read(new ByteArrayInputStream(entry.getData()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read manifest entry", e);
        }
        return mf;
    }

    public ClassEntry process(ClassEntry entry) {
        var classDesc = ClassDesc.ofInternalName(entry.getClassName());
        var classTransform = ClassTransform.ACCEPT_ALL;
        ClassModel classModel = null;

        for (var transformer : transformers) {
            if (!transformer.handlesClass(moduleDesc, classDesc))
                continue;

            if (classModel == null)
                classModel = ClassFile.of().parse(entry.getData());

            var returnedTransform = transformer.transform(moduleDesc, classDesc, classModel);
            if (returnedTransform != ClassTransform.ACCEPT_ALL)
                classTransform = classTransform.andThen(returnedTransform);
        }

        if (classTransform == ClassTransform.ACCEPT_ALL)
            return entry;

        var transformed = ClassFile.of().transformClass(classModel, classTransform);
        if (entry.isMultiRelease())
            return ClassEntry.create(entry.getClassName(), entry.getTime(), transformed, entry.getVersion());
        return ClassEntry.create(entry.getName(), entry.getTime(), transformed);
    }

    @Override
    public @Nullable ManifestEntry process(ManifestEntry entry) {
        final var original = new JarTransformer.Entry(entry.getName(), entry.getTime(), entry.getData());
        final var transformed = process(original);
        if (transformed == original)
            return entry;
        if (transformed == null)
            return null;
        return ManifestEntry.create(transformed.time(), transformed.data());
    }

    @Override
    public @Nullable ResourceEntry process(ResourceEntry entry) {
        final var original = new JarTransformer.Entry(entry.getName(), entry.getTime(), entry.getData());
        final var transformed = process(original);
        if (transformed == original)
            return entry;
        if (transformed == null)
            return null;
        return ResourceEntry.create(transformed.name(), transformed.time(), transformed.data());
    }

    private JarTransformer.@Nullable Entry process(JarTransformer.Entry original) {
        var transformed = original;
        for (var transformer : transformers) {
            if (!transformer.handlesModule(moduleDesc))
                continue;
            transformed = transformer.handleResource(transformed);
            if (transformed == null)
                return null;
        }
        return transformed;
    }

    @Override
    public Collection<? extends Entry> getExtras() {
        var ret = new ArrayList<ResourceEntry>();
        for (var transformer : transformers) {
            if (!transformer.handlesModule(moduleDesc))
                continue;
            var extras = transformer.getExtras();
            for (var extra : extras)
               ret.add(ResourceEntry.create(extra.name(), extra.time(), extra.data()));
        }
        return ret;
    }
}
