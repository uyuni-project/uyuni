package com.suse.manager.gatherer;

import com.suse.manager.model.gatherer.GathererModule;

import java.util.Map;
import java.util.Set;


public enum GathererFactory {

    /** Singleton instance. */
    INSTANCE;

    private final Map<String, GathererModule> gathererModules = new GathererCommand().listModules();

    public Set<String> listModules() {
        return gathererModules.keySet();
    }

    public GathererModule getDetails(String moduleName) {
        return gathererModules.get(moduleName);
    }
}
