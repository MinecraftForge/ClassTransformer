/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer;

import net.minecraftforge.classtransformer.api.JarTransformer;
import net.minecraftforge.renamer.api.Transformer;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class JarTransformerWrapper implements Transformer {
    private final List<JarTransformer> transformers;
    private final ModuleDesc moduleDesc;
    private final ClassFile.ClassHierarchyResolverOption classResolver;

    JarTransformerWrapper(String moduleName, List<JarTransformer> transformers, ClassHierarchyResolver resolver) {
        this.transformers = transformers;
        this.moduleDesc = ModuleDesc.of(moduleName);
        this.classResolver = ClassFile.ClassHierarchyResolverOption.of(resolver);
    }

    @Override
    public void preprocess(Map<String, Entry> entries) {
        Function<String, byte[]> data = name -> {
            var entry = entries.get(name);
            return entry == null ? null : entry.getData();
        };

        for (var transformer : transformers)
            transformer.preprocess(entries.keySet(), data);
    }

    public ClassEntry process(ClassEntry entry) {
        var classDesc = ClassDesc.ofInternalName(entry.getClassName());
        var classTransform = ClassTransform.ACCEPT_ALL;
        ClassModel classModel = null;

        for (var transformer : transformers) {
            if (!transformer.handlesClass(moduleDesc, classDesc))
                continue;

            if (classModel == null)
                classModel = ClassFile.of(classResolver).parse(entry.getData());

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
