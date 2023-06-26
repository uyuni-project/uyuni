package com.suse.oval.vulnerablepkgextractor;

import java.util.Optional;

public class VulnerablePackage {
    private String name;
    private String fixVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getFixVersion() {
        return Optional.ofNullable(fixVersion);
    }

    public void setFixVersion(String fixedVersion) {
        this.fixVersion = fixedVersion;
    }
}
