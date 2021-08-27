package org.bookmc.mixin.annotation;

import com.google.common.io.Files;
import me.dreamhopping.pml.gradle.tasks.map.generate.GenerateMappingsTask;
import org.bookmc.mixin.extension.MixinExtension;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.IOException;

/**
 * The abstracted AnnotationProcessorAPI allows us to provide compiler arguments to the language
 * which will be used by the annotation processor to process the data.
 *
 * @param <LanguageTask> The abstracted language's compile task as a generic to be used
 */
public abstract class AbstractAnnotationProcessor<LanguageTask extends Task> {
    private final LanguageTask task;

    public AbstractAnnotationProcessor(LanguageTask task) {
        this.task = task;
    }

    /**
     * Since different languages have different ways to pass compiler arguments for the annotation processors
     * we must abstract it. This allows for support for other languages but we will only officially support Java for now...
     * Feel free to fork this or make a pull request/issue and it may be considered.
     *
     * @param languageTask The abstracted language's compiler task
     * @param key          The key of the argument
     * @param value        The value of the argument
     */
    public abstract void addArgument(LanguageTask languageTask, String key, String value);

    public void configure(Project project, SourceSet sourceSet) {
        String taskName = "generateMixinMappings" + sourceSet.getName().substring(2);
        GenerateMappingsTask mappingsTask = (GenerateMappingsTask) project.getTasks().getByName(taskName);
        task.dependsOn(taskName);

        File refMapFile = project.file(task.getTemporaryDir() + "/" + task.getName() + "-refmap.json");
        File generatedFile = project.file(task.getTemporaryDir() + "/" + task.getName() + "-mappings.json");

        try {
            applyCompilerArguments(mappingsTask, refMapFile, generatedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        task.doFirst(task -> {
            refMapFile.delete();
            generatedFile.delete();
        });

        String refmapName = getRefmapName(sourceSet, project);

        File taskSpecificRefMap = new File(refMapFile.getParentFile(), refmapName);

        task.doLast(action -> {
            taskSpecificRefMap.delete();

            if (refMapFile.exists()) {
                taskSpecificRefMap.getParentFile().mkdirs();
                try {
                    Files.copy(refMapFile, taskSpecificRefMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ((AbstractArchiveTask) project.getTasks().getByName(sourceSet.getJarTaskName())).from(taskSpecificRefMap);
    }

    private void applyCompilerArguments(GenerateMappingsTask mappingsTask, File refmapFile, File outputFile) throws IOException {
        if (mappingsTask.getOutputFile() == null) {
            throw new IllegalStateException("Failed to find mappingsJsonInput output file");
        }

        addArgument(task, "inMapFile", mappingsTask.getOutputFile().getCanonicalPath());
        addArgument(task, "outMapFile", outputFile.getCanonicalPath());
        addArgument(task, "outRefMapFile", refmapFile.getCanonicalPath());
        addArgument(task, "defaultObfuscationEnv", "pufferfishgradle");
    }

    private String getRefmapName(SourceSet sourceSet, Project project) {
        return project.getExtensions()
            .getByType(MixinExtension.class)
            .getRefMapNames()
            .getOrDefault(sourceSet.getName(), "mixin.refmap.json");
    }
}
