package org.bookmc.mixin.annotation.java;

import org.bookmc.mixin.annotation.AbstractAnnotationProcessor;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;

public class JavaAnnotationProcessor extends AbstractAnnotationProcessor<JavaCompile> {
    private static final String COMPILER_ARGUMENT_FORMAT = "-A%s=%s";

    public JavaAnnotationProcessor(JavaCompile task) {
        super(task);
    }

    @Override
    public void addArgument(JavaCompile javaCompile, String key, String value) {
        javaCompile.getOptions().getCompilerArgs().add(String.format(COMPILER_ARGUMENT_FORMAT, key, value));
    }
}
