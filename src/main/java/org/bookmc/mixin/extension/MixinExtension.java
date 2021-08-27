package org.bookmc.mixin.extension;

import org.gradle.api.tasks.SourceSet;

import java.util.HashMap;
import java.util.Map;

public class MixinExtension {
    private final Map<String, String> refMapNames = new HashMap<>();

    public void add(SourceSet sourceSet, String name) {
        refMapNames.put(sourceSet.getName(), name);
    }

    public void add(String sourceSet, String refMap) {
        refMapNames.put(sourceSet, refMap);
    }

    public Map<String, String> getRefMapNames() {
        return refMapNames;
    }
}
