package org.bookmc.mixin;

import org.bookmc.mixin.annotation.AbstractAnnotationProcessor;
import org.bookmc.mixin.annotation.java.JavaAnnotationProcessor;
import org.bookmc.mixin.extension.MixinExtension;
import org.bookmc.mixin.utils.Constants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.annotation.Nonnull;

public class MixinAnnotationProcessorConfigurationPlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull Project project) {
        project.getRepositories().mavenLocal();
        project.getExtensions().add("mixin", new MixinExtension());

        if (!project.getPluginManager().hasPlugin("me.dreamhopping.pml.gradle")) {
            throw new IllegalStateException("Failed to find PufferfishGradle, has it actually been applied?");
        }

        project.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project project) {
        SourceSetContainer sourceSets = project.getExtensions()
            .getByType(JavaPluginExtension.class)
            .getSourceSets();

        for (SourceSet sourceSet : sourceSets) {
            if (sourceSet.getName().startsWith("mc")) {
                String configuration = sourceSet.getName() + "AnnotationProcessor";
                project.getDependencies().add(configuration, "org.bookmc:mixin-obfuscation-service:" + Constants.Dependencies.OBFUSCATION_SERVICE_VERSION);
            }

            JavaCompile compileTask = (JavaCompile) project.getTasks().getByPath(sourceSet.getCompileTaskName("java"));
            AbstractAnnotationProcessor<JavaCompile> annotationProcessor = new JavaAnnotationProcessor(compileTask);
            annotationProcessor.configure(project, sourceSet);
        }
    }
}
