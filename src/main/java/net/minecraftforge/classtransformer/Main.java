/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.classtransformer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.minecraftforge.classtransformer.api.JarTransformer;
import net.minecraftforge.renamer.api.Renamer;
import org.jspecify.annotations.Nullable;

public class Main {
    static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<File> inputO  = parser.accepts("input",  "Input jar file").withRequiredArg().ofType(File.class).required();
        OptionSpec<File> outputO = parser.accepts("output", "Output jar file, if unspecified, overwrites input").withRequiredArg().ofType(File.class);
        OptionSpec<File> logO    = parser.accepts("log",    "File to log data to, optional, defaults to System.out").withRequiredArg().ofType(File.class);
        OptionSpec<Integer> threadsO = parser.accepts("threads", "Number of threads to use, defaults to processor count.").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
        OptionSpec<Void> storeO = parser.accepts("store", "Disables compression, this is designed to produce stable output archives no matter what zlib implementation the user has installed");
        OptionSpec<Void> helpO = parser.accepts("help", "Prints help and exits").forHelp();

        OptionSet options;
        try {
            options = parser.parse(expandArgs(args));
        } catch (OptionException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.err.println();
            parser.printHelpOn(System.err);
            System.exit(1);
            return;
        }

        Consumer<String> log = ln -> {
            if (!ln.isEmpty()) {
                System.out.println(ln);
            }
        };
        if (options.has(logO)) {
            PrintStream out = System.out;
            PrintStream file = new PrintStream(new FileOutputStream(options.valueOf(logO)));
            log = ln -> {
                if (!ln.isEmpty()) {
                    out.println(ln);
                    file.println(ln);
                }
            };
        }

        log.accept("Class Transformer v" + getVersion());

        if (options.has(helpO)) {
            parser.printHelpOn(System.out);
            return;
        }

        Renamer.Builder builder = Renamer.builder();
        //builder.withJvmClasspath();
        builder.logger(log);

        log.accept("log: " + (options.has(logO) ? options.valueOf(logO).getAbsolutePath() : "null"));

        File inputF = options.valueOf(inputO);
        log.accept("input: " + inputF.getAbsolutePath());

        File outputF = options.has(outputO) ? options.valueOf(outputO) : inputF;
        log.accept("output: " + outputF.getAbsolutePath());

        log.accept("threads: " + options.valueOf(threadsO));
        builder.threads(options.valueOf(threadsO));

        if (options.has(storeO)) {
            log.accept("Store: True");
            builder.store();
        } else {
            log.accept("Store: False");
        }

        // Find the module so we can filter out transformers that dont care about it
        var modules = ModuleFinder.of(inputF.toPath()).findAll();
        if (modules.size() != 1)
            throw new IllegalStateException("Failed to find expected module from input file, found " + modules.size() + " but expected exactly 1");
        var module = modules.iterator().next();

        log.accept("Target Module: " + module.descriptor().toNameAndVersion());

        var transformers = new ArrayList<JarTransformer>();
        var services = ServiceLoader.load(JarTransformer.class);
        for (var itr = services.iterator(); itr.hasNext(); ) {
            try {
                var service = itr.next();
                if (service.handlesModule(module)) {
                    transformers.add(service);
                    log.accept("Transformer: " + service.getClass().getModule().getName() + ':' + service.getClass().getName());
                } else {
                    log.accept("Skipped Transformer: " + service.getClass().getModule().getName() + ':' + service.getClass().getName());
                }
            } catch (Throwable t) {
                throw new IllegalStateException("Could not load JarTransformer service", t);
            }
        }
        if (!transformers.isEmpty()) {
            builder.add(new JarTransformerWrapper(module.descriptor().name(), transformers));
            try (Renamer renamer = builder.build()) {
                renamer.run(inputF, outputF);
            }
        } else {
            // No transformers, just copy the file
            log.accept("No transformers, copying file to output");
            Files.copy(inputF.toPath(), outputF.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static @Nullable String[] expandArgs(String[] args) throws IOException {
        List<String> params = new ArrayList<>(args.length);

        for (int x = 0; x < args.length; x++) {
            if (args[x].startsWith("--cfg")) {
                String path = null;
                if (args[x].startsWith("--cfg="))
                    path = args[x].substring(6);
                else if (args.length > x + 1)
                    path = args[++x];
                else
                    throw new IllegalArgumentException("Must specify a file when using --cfg argument.");

                Path file = Paths.get(path);
                if (!Files.exists(file))
                    throw new IllegalArgumentException("error: missing config '" + path + "'");

                try (Stream<String> stream = Files.lines(file)) {
                    stream.forEach(params::add);
                } catch (IOException e) {
                    throw new RuntimeException("error: Failed to read config file '" + path + "'", e);
                }
            } else {
                params.add(args[x]);
            }
        }
        return params.toArray(new String[0]);
    }

    private static String getVersion() {
        var desc = Main.class.getModule().getDescriptor();
        if (desc == null) // We are in the unnamed module for some reason
            return "UNKNOWN";
        return desc.version().map(ModuleDescriptor.Version::toString).orElse("UNKNOWN");
    }
}
