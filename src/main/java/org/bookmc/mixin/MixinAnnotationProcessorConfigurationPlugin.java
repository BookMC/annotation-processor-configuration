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
        project.getExtensions().add(Constants.Extension.NAME, new MixinExtension());

        if (!project.getPluginManager().hasPlugin(Constants.Plugin.PUFFERFISH_GRADLE_ID)) {
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
                // Add our obfuscation service and Mixin itself onto the classpath of the annotation processor
                // It has to be per-sourceset because different sourcesets will obviously have a difference classpath
                // so this resolves the mixin annotation processor only running on the main source set when in reality
                // we don't even want it ran on that since there (should) be no Mixin code there...
                String configuration = sourceSet.getName() + "AnnotationProcessor";
                project.getDependencies().add(configuration, "org.bookmc:mixin-obfuscation-service:" + Constants.Dependencies.OBFUSCATION_SERVICE_VERSION);

                // In the future this should be further abstracted and would have support for Kotlin etc.
                // but for now it's just plain old Java...
                JavaCompile compileTask = (JavaCompile) project.getTasks().getByPath(sourceSet.getCompileTaskName("java"));
                AbstractAnnotationProcessor<JavaCompile> annotationProcessor = new JavaAnnotationProcessor(compileTask);
                annotationProcessor.configure(project, sourceSet);
            }
        }
    }
}
