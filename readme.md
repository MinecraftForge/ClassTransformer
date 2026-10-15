# ClassTransformer
---
ClassTransformer is a general purpose Java ByteCode transforming tool. Designed to run on Java 25, using the official ClassFile api. It loads transformers that implement `net.minecraftforge.classtransformer.api.JarTransformer` from the environment using a ServiceLoader. 

### Usage
---
This is primarily designed to be invoked from gradle, as such, refere to the demo projects for working examples.
However, it can be invoked from the command line as well 
`java -p classstransformer.jar;custom-transformer-service.jar --input input.jar --output output.jar`

In order to properly compute frames, it is recommended that you pass in all libraries used by the input jar using the `--lib lib.jar` argument, which can be specified as many times as needed.

### Gradle
---
We provide a gradle plugin published on under the `net.minecraftforge.classstransformer` id.

You can see an example in the `classstransformer-demo` sub-folder.

```groovy
plugins {
  id 'net.minecraftforge.classtransformer' version '0.1.0'
}

classsTransformer.register(tasks.named('jar', Jar)) {
    archiveClassifier = 'transformed'
}
```