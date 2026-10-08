/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.classtransformer.demo.transformer;

import net.minecraftforge.classtransformer.api.JarTransformer;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;

public class TransformerService implements JarTransformer {
    private static final ModuleDesc TARGET_MODULE = ModuleDesc.of("net.minecraftforge.classtransformer.demo");
    private static final ClassDesc TARGET_CLASS = ClassDesc.of("net.minecraftforge.classtransformer.demo.Main");

    @Override
    public boolean handlesModule(ModuleDesc moduleDesc) {
        return TARGET_MODULE.equals(moduleDesc);
    }

    @Override
    public boolean handlesClass(ModuleDesc moduleDesc, ClassDesc classDesc) {
        return handlesModule(moduleDesc) && TARGET_CLASS.equals(classDesc);
    }

    @Override
    public ClassTransform transform(ModuleDesc moduleDesc, ClassDesc classDesc, ClassModel classModel) {
        if (!handlesClass(moduleDesc, classDesc))
            throw new IllegalStateException("Should not be called for a module/class we filtered " + moduleDesc + " " + classDesc);

        CodeTransform replacement = (builder, element) -> {
            builder.ldc("TRANSFORMED")
                    .areturn();
        };

        return ClassTransform.transformingMethodBodies(
            method -> method.methodName().equalsString("getMessage"),
            replacement
        );
    }
}
